package com.duetify.app.games.engine

/**
 * Pure game logic for "This or That" — no Android, no coroutines, so it is trivially testable.
 *
 * Each round shows one [Prompt] with two sides. Both partners privately pick a side; once both have
 * answered a round it is "revealed" as a match or a miss. The running compatibility score is just
 * the share of revealed rounds that matched.
 */

data class Prompt(
    val id: Int,
    val left: String,
    val right: String,
)

/** One partner's answer to a prompt. [pickedLeft] false means they picked the right-hand option. */
data class Answer(val promptId: Int, val pickedLeft: Boolean)

data class RoundResult(
    val prompt: Prompt,
    val selfPickedLeft: Boolean,
    val partnerPickedLeft: Boolean,
) {
    val matched: Boolean get() = selfPickedLeft == partnerPickedLeft
}

data class ThisOrThatState(
    val deck: List<Prompt>,
    val roundIndex: Int = 0,
    val selfAnswers: Map<Int, Boolean> = emptyMap(),
    val partnerAnswers: Map<Int, Boolean> = emptyMap(),
) {
    val currentPrompt: Prompt? get() = deck.getOrNull(roundIndex)
    val totalRounds: Int get() = deck.size
    val finished: Boolean get() = roundIndex >= deck.size

    val selfAnsweredCurrent: Boolean
        get() = currentPrompt?.let { selfAnswers.containsKey(it.id) } ?: false
    val partnerAnsweredCurrent: Boolean
        get() = currentPrompt?.let { partnerAnswers.containsKey(it.id) } ?: false

    /** Rounds where both have answered, in deck order. */
    val revealed: List<RoundResult>
        get() = deck.mapNotNull { prompt ->
            val self = selfAnswers[prompt.id]
            val partner = partnerAnswers[prompt.id]
            if (self != null && partner != null) {
                RoundResult(prompt, self, partner)
            } else {
                null
            }
        }

    val matches: Int get() = revealed.count { it.matched }

    /** 0..100. Undefined (0) until at least one round is revealed. */
    val compatibilityPercent: Int
        get() = revealed.size.takeIf { it > 0 }?.let { (matches * 100) / it } ?: 0
}

object ThisOrThatEngine {

    /** The default deck. Couple-flavoured, deliberately light. */
    val defaultDeck: List<Prompt> = listOf(
        Prompt(1, "Beach holiday", "Mountain escape"),
        Prompt(2, "Morning person", "Night owl"),
        Prompt(3, "Movie night in", "Night out dancing"),
        Prompt(4, "Coffee", "Tea"),
        Prompt(5, "Sweet", "Savoury"),
        Prompt(6, "Text back instantly", "Call me instead"),
        Prompt(7, "Plan every detail", "Go with the flow"),
        Prompt(8, "Cats", "Dogs"),
        Prompt(9, "Cook together", "Order in"),
        Prompt(10, "Save the surprise", "Tell me now"),
        Prompt(11, "City lights", "Countryside quiet"),
        Prompt(12, "Throwback playlist", "Brand-new releases"),
    )

    fun newGame(deck: List<Prompt> = defaultDeck): ThisOrThatState = ThisOrThatState(deck = deck)

    /**
     * Fold one answer into the state. Order-independent and idempotent per (side, prompt), so it is
     * safe to replay an event stream from the start — which is exactly how a mid-game joiner and
     * the answering player both arrive at the same state.
     */
    fun apply(state: ThisOrThatState, isSelf: Boolean, promptId: Int, pickedLeft: Boolean): ThisOrThatState {
        val updated = if (isSelf) {
            state.copy(selfAnswers = state.selfAnswers + (promptId to pickedLeft))
        } else {
            state.copy(partnerAnswers = state.partnerAnswers + (promptId to pickedLeft))
        }
        return advanceIfComplete(updated)
    }

    /** Advance to the next unrevealed round once both partners have answered the current one. */
    private fun advanceIfComplete(state: ThisOrThatState): ThisOrThatState {
        var next = state
        while (true) {
            val prompt = next.currentPrompt ?: break
            val bothAnswered =
                next.selfAnswers.containsKey(prompt.id) && next.partnerAnswers.containsKey(prompt.id)
            if (bothAnswered) next = next.copy(roundIndex = next.roundIndex + 1) else break
        }
        return next
    }
}
