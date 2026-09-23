package com.duetify.app.games.transport

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * In-process [GameTransport]. Rooms live in a shared registry, so two sessions created in the same
 * app process (the host plus a bot, or a pass-the-phone partner) genuinely talk to each other.
 *
 * This is the transport that lets the whole games layer be built, run and demoed with no backend.
 * The Firebase transport added in Phase 2 implements the same [GameTransport] contract for real
 * two-device play.
 */
@Singleton
class LocalGameTransport @Inject constructor(
    private val registry: LocalRoomRegistry,
) : GameTransport {

    override val connectionMode = ConnectionMode.LOCAL

    override suspend fun host(self: Participant): RoomSession {
        val room = registry.open()
        room.addParticipant(self.copy(isHost = true))
        return LocalRoomSession(room, self.id)
    }

    override suspend fun join(code: String, self: Participant): RoomSession? {
        val room = registry.find(code.uppercase()) ?: return null
        room.addParticipant(self.copy(isHost = false))
        return LocalRoomSession(room, self.id)
    }
}

/** Process-wide store of open local rooms. */
@Singleton
class LocalRoomRegistry @Inject constructor() {
    private val rooms = ConcurrentHashMap<String, LocalRoom>()

    fun open(): LocalRoom {
        var code = randomCode()
        while (rooms.containsKey(code)) code = randomCode()
        return LocalRoom(code).also { rooms[code] = it }
    }

    fun find(code: String): LocalRoom? = rooms[code]

    fun forget(code: String) {
        rooms.remove(code)
    }

    private fun randomCode(): String =
        (1..CODE_LENGTH).map { CODE_ALPHABET[Random.nextInt(CODE_ALPHABET.length)] }.joinToString("")

    private companion object {
        const val CODE_LENGTH = 4
        // No 0/O or 1/I: they are the codes people mistype when reading one aloud.
        const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}

/** Shared state for one local room: its roster and its append-only event log. */
class LocalRoom(val code: String) {
    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    // Generous replay so a late joiner (or a bot spun up after the first move) sees prior events.
    private val _events = MutableSharedFlow<GameEvent>(replay = 128, extraBufferCapacity = 64)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    fun addParticipant(participant: Participant) {
        _participants.update { current ->
            if (current.any { it.id == participant.id }) current else current + participant
        }
    }

    fun removeParticipant(id: String) {
        _participants.update { current -> current.filterNot { it.id == id } }
    }

    suspend fun emit(event: GameEvent) {
        _events.emit(event)
    }
}

private class LocalRoomSession(
    private val room: LocalRoom,
    override val selfId: String,
) : RoomSession {
    override val code: String = room.code
    override val connectionMode = ConnectionMode.LOCAL
    override val participants = room.participants
    override val events = room.events

    override suspend fun send(kind: String, data: Map<String, String>) {
        room.emit(GameEvent(senderId = selfId, kind = kind, data = data))
    }

    override suspend fun close() {
        room.removeParticipant(selfId)
    }
}
