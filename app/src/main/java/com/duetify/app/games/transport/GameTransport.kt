package com.duetify.app.games.transport

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Real-time plumbing for two-player "duet" games, kept deliberately game-agnostic.
 *
 * Everything is event-sourced: a room is an append-only stream of [GameEvent]s plus a roster of
 * [Participant]s. Each client folds the same event stream into its own copy of the game state, so
 * the transport never needs to understand any particular game — it only has to deliver events in
 * order and let late joiners catch up (hence the replay on [RoomSession.events]).
 *
 * Two implementations plug in behind this interface:
 *  - [LocalGameTransport] — in-process rooms for solo / vs-bot / same-device play. No backend.
 *  - a Firebase-backed transport (Phase 2) — the identical contract over Firestore, so real
 *    long-distance play is a DI swap, not a rewrite of any game.
 */
interface GameTransport {
    val connectionMode: ConnectionMode

    /** Open a fresh room and join it as the host. Returns the live session (its [RoomSession.code]
     *  is what the partner types in to join). */
    suspend fun host(self: Participant): RoomSession

    /** Join an existing room by its short code. Returns null if no such room exists. */
    suspend fun join(code: String, self: Participant): RoomSession?
}

/** A live connection to one room, from one participant's point of view. */
interface RoomSession {
    val code: String
    val connectionMode: ConnectionMode
    val selfId: String

    /** Current roster. Emits again whenever someone joins or leaves. */
    val participants: StateFlow<List<Participant>>

    /** Ordered game events. Replays history so a participant who joins mid-game catches up. */
    val events: SharedFlow<GameEvent>

    suspend fun send(kind: String, data: Map<String, String> = emptyMap())

    suspend fun close()
}

enum class ConnectionMode {
    /** Same process: solo, versus a bot, or pass-the-phone. Works with no network. */
    LOCAL,

    /** Two separate devices over a backend (Phase 2, Firebase). */
    ONLINE,
}

data class Participant(
    val id: String,
    val name: String,
    val isHost: Boolean,
    val isBot: Boolean = false,
)

/**
 * One thing that happened in a room. [kind] is a short game-defined tag (e.g. "choice"); [data]
 * carries its payload as plain strings so any transport can serialize it without reflection.
 */
data class GameEvent(
    val senderId: String,
    val kind: String,
    val data: Map<String, String> = emptyMap(),
    val ts: Long = System.currentTimeMillis(),
)
