package com.duetify.app.games.ui.thisorthat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duetify.app.games.engine.ThisOrThatEngine
import com.duetify.app.games.engine.ThisOrThatState
import com.duetify.app.games.transport.ConnectionMode
import com.duetify.app.games.transport.GameEvent
import com.duetify.app.games.transport.GameTransport
import com.duetify.app.games.transport.Participant
import com.duetify.app.games.transport.RoomSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/** What the "This or That" screen is currently showing. */
sealed interface DuetPhase {
    data object Lobby : DuetPhase
    data class Connecting(val message: String) : DuetPhase
    data class WaitingForPartner(val code: String) : DuetPhase
    data class Playing(
        val game: ThisOrThatState,
        val partnerName: String,
        val online: Boolean,
    ) : DuetPhase
    data class Finished(val game: ThisOrThatState, val partnerName: String) : DuetPhase
    data class Failed(val message: String) : DuetPhase
}

private const val KIND_CHOICE = "choice"
private const val KIND_RESTART = "restart"
private const val DATA_PROMPT = "prompt"
private const val DATA_SIDE = "side"
private const val SIDE_LEFT = "L"
private const val SIDE_RIGHT = "R"

@HiltViewModel
class ThisOrThatViewModel @Inject constructor(
    private val transport: GameTransport,
) : ViewModel() {

    private val selfId = UUID.randomUUID().toString()

    private val _phase = MutableStateFlow<DuetPhase>(DuetPhase.Lobby)
    val phase = _phase.asStateFlow()

    private var game: ThisOrThatState = ThisOrThatEngine.newGame()
    private var session: RoomSession? = null
    private var botSession: RoomSession? = null
    private var botMode = false
    private var partnerName = "Partner"
    private var eventJob: Job? = null
    private var rosterJob: Job? = null

    /** Instant, single-device play against a light-hearted bot. Always works, no backend. */
    fun playWithBot() {
        reset()
        botMode = true
        partnerName = BOT_NAME
        viewModelScope.launch {
            _phase.value = DuetPhase.Connecting("Warming up…")
            val host = transport.host(self(name = "You"))
            session = host
            botSession = transport.join(host.code, botParticipant())
            observe(host)
            startPlaying(online = false)
        }
    }

    /** Open a room and wait for a partner to join with the shown code. */
    fun createRoom() {
        reset()
        botMode = false
        viewModelScope.launch {
            _phase.value = DuetPhase.Connecting("Opening a room…")
            val host = runCatching { transport.host(self(name = "You")) }.getOrElse {
                _phase.value = DuetPhase.Failed(it.message ?: "Couldn't open a room")
                return@launch
            }
            session = host
            observe(host)
            _phase.value = DuetPhase.WaitingForPartner(host.code)
        }
    }

    /** Join a partner's room by code. */
    fun joinRoom(code: String) {
        reset()
        botMode = false
        viewModelScope.launch {
            _phase.value = DuetPhase.Connecting("Joining ${code.uppercase()}…")
            val joined = runCatching { transport.join(code.trim(), self(name = "You")) }.getOrNull()
            if (joined == null) {
                _phase.value = DuetPhase.Failed("No room with code ${code.uppercase()}")
                return@launch
            }
            session = joined
            observe(joined)
            startPlaying(online = joined.connectionMode == ConnectionMode.ONLINE)
        }
    }

    fun answer(pickedLeft: Boolean) {
        val active = session ?: return
        val prompt = game.currentPrompt ?: return
        if (game.selfAnsweredCurrent) return
        viewModelScope.launch {
            active.send(
                KIND_CHOICE,
                mapOf(
                    DATA_PROMPT to prompt.id.toString(),
                    DATA_SIDE to if (pickedLeft) SIDE_LEFT else SIDE_RIGHT,
                ),
            )
            if (botMode) scheduleBotAnswer(prompt.id)
        }
    }

    fun playAgain() {
        // Restart is a shared event so both sides reset together.
        val active = session ?: return
        viewModelScope.launch { active.send(KIND_RESTART) }
    }

    fun leave() {
        reset()
        _phase.value = DuetPhase.Lobby
    }

    private fun observe(active: RoomSession) {
        eventJob?.cancel()
        eventJob = viewModelScope.launch {
            active.events.collect { applyEvent(it) }
        }
        rosterJob?.cancel()
        rosterJob = viewModelScope.launch {
            active.participants.collect { roster ->
                val partner = roster.firstOrNull { it.id != selfId }
                if (partner != null) {
                    partnerName = partner.name
                    // A partner arriving while we're waiting starts the game.
                    if (_phase.value is DuetPhase.WaitingForPartner) {
                        startPlaying(online = active.connectionMode == ConnectionMode.ONLINE)
                    }
                }
            }
        }
    }

    private fun applyEvent(event: GameEvent) {
        when (event.kind) {
            KIND_CHOICE -> {
                val promptId = event.data[DATA_PROMPT]?.toIntOrNull() ?: return
                val pickedLeft = event.data[DATA_SIDE] != SIDE_RIGHT
                val isSelf = event.senderId == selfId
                game = ThisOrThatEngine.apply(game, isSelf, promptId, pickedLeft)
                emitGamePhase()
            }
            KIND_RESTART -> {
                game = ThisOrThatEngine.newGame(game.deck)
                emitGamePhase()
            }
        }
    }

    private fun startPlaying(online: Boolean) {
        game = ThisOrThatEngine.newGame()
        _phase.value = DuetPhase.Playing(game, partnerName, online)
    }

    private fun emitGamePhase() {
        _phase.value = if (game.finished) {
            DuetPhase.Finished(game, partnerName)
        } else {
            val online = (_phase.value as? DuetPhase.Playing)?.online ?: false
            DuetPhase.Playing(game, partnerName, online)
        }
    }

    private fun scheduleBotAnswer(promptId: Int) {
        val bot = botSession ?: return
        viewModelScope.launch {
            delay(BOT_THINK_MILLIS)
            bot.send(
                KIND_CHOICE,
                mapOf(
                    DATA_PROMPT to promptId.toString(),
                    DATA_SIDE to if (Random.nextBoolean()) SIDE_LEFT else SIDE_RIGHT,
                ),
            )
        }
    }

    private fun reset() {
        eventJob?.cancel(); eventJob = null
        rosterJob?.cancel(); rosterJob = null
        val toClose = listOfNotNull(session, botSession)
        session = null
        botSession = null
        botMode = false
        game = ThisOrThatEngine.newGame()
        viewModelScope.launch { toClose.forEach { runCatching { it.close() } } }
    }

    private fun self(name: String) = Participant(id = selfId, name = name, isHost = true)

    private fun botParticipant() =
        Participant(id = UUID.randomUUID().toString(), name = BOT_NAME, isHost = false, isBot = true)

    override fun onCleared() {
        reset()
        super.onCleared()
    }

    private companion object {
        const val BOT_NAME = "Robin"
        const val BOT_THINK_MILLIS = 550L
    }
}
