package com.duetify.app.games.transport

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Firestore-backed [GameTransport] — the real long-distance path. Same event-sourced contract as
 * [LocalGameTransport], so every game works across two phones with zero game-code changes.
 *
 * A room is a document under `rooms/{code}` with two subcollections: `participants` (the roster) and
 * `events` (the ordered game log). Firestore's own snapshot listeners give live sync + catch-up.
 *
 * Firebase handles (Firestore, Auth) are created lazily so this class is safe to construct even when
 * the app has no google-services.json — the lazies are only touched after [TransportProvider] has
 * confirmed Firebase is configured.
 */
@Singleton
class FirebaseGameTransport @Inject constructor() : GameTransport {

    override val connectionMode = ConnectionMode.ONLINE

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override suspend fun host(self: Participant): RoomSession {
        ensureSignedIn()
        val rooms = firestore.collection(ROOMS)
        var code = randomCode()
        var attempts = 0
        while (rooms.document(code).get().await().exists() && attempts < 5) {
            code = randomCode()
            attempts++
        }
        val roomDoc = rooms.document(code)
        roomDoc.set(
            mapOf("hostId" to self.id, "game" to "this_or_that", "createdAt" to System.currentTimeMillis()),
        ).await()
        val host = self.copy(isHost = true)
        roomDoc.collection(PARTICIPANTS).document(host.id).set(host.toMap()).await()
        return FirebaseRoomSession(roomDoc, host.id)
    }

    override suspend fun join(code: String, self: Participant): RoomSession? {
        ensureSignedIn()
        val roomDoc = firestore.collection(ROOMS).document(code.trim().uppercase())
        if (!roomDoc.get().await().exists()) return null
        val guest = self.copy(isHost = false)
        roomDoc.collection(PARTICIPANTS).document(guest.id).set(guest.toMap()).await()
        return FirebaseRoomSession(roomDoc, guest.id)
    }

    /** Anonymous auth gives every device a stable uid so Firestore rules can require sign-in. */
    private suspend fun ensureSignedIn() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

    private fun randomCode(): String =
        (1..CODE_LENGTH).map { CODE_ALPHABET[Random.nextInt(CODE_ALPHABET.length)] }.joinToString("")

    private companion object {
        const val ROOMS = "rooms"
        const val PARTICIPANTS = "participants"
        const val CODE_LENGTH = 4
        const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}

private class FirebaseRoomSession(
    private val roomDoc: DocumentReference,
    override val selfId: String,
) : RoomSession {

    override val code: String = roomDoc.id
    override val connectionMode = ConnectionMode.ONLINE

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    override val participants = _participants.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(replay = 128, extraBufferCapacity = 64)
    override val events = _events.asSharedFlow()

    private val registrations = mutableListOf<ListenerRegistration>()

    init {
        registrations += roomDoc.collection(PARTICIPANTS).addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _participants.value = snapshot.documents.mapNotNull { it.toParticipant() }
            }
        }
        registrations += roomDoc.collection(EVENTS).orderBy(FIELD_TS).addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener
            for (change in snapshot.documentChanges) {
                // Only ADDED matters: events are append-only. Initial load delivers history as ADDED,
                // which is exactly the catch-up a mid-game joiner needs.
                if (change.type == DocumentChange.Type.ADDED) {
                    change.document.toGameEvent()?.let { _events.tryEmit(it) }
                }
            }
        }
    }

    override suspend fun send(kind: String, data: Map<String, String>) {
        roomDoc.collection(EVENTS).add(
            mapOf(
                "senderId" to selfId,
                "kind" to kind,
                "data" to data,
                FIELD_TS to System.currentTimeMillis(),
            ),
        ).await()
    }

    override suspend fun close() {
        registrations.forEach { it.remove() }
        registrations.clear()
        runCatching { roomDoc.collection(PARTICIPANTS).document(selfId).delete().await() }
    }

    private companion object {
        const val PARTICIPANTS = "participants"
        const val EVENTS = "events"
        const val FIELD_TS = "ts"
    }
}

private fun Participant.toMap(): Map<String, Any> =
    mapOf("id" to id, "name" to name, "isHost" to isHost, "isBot" to isBot)

private fun DocumentSnapshot.toParticipant(): Participant? {
    val id = getString("id") ?: return null
    return Participant(
        id = id,
        name = getString("name") ?: "Partner",
        isHost = getBoolean("isHost") ?: false,
        isBot = getBoolean("isBot") ?: false,
    )
}

private fun DocumentSnapshot.toGameEvent(): GameEvent? {
    val senderId = getString("senderId") ?: return null
    val kind = getString("kind") ?: return null
    val data = (get("data") as? Map<*, *>)
        ?.entries
        ?.associate { it.key.toString() to it.value.toString() }
        ?: emptyMap()
    return GameEvent(senderId = senderId, kind = kind, data = data, ts = getLong("ts") ?: 0L)
}
