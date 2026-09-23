package com.duetify.app.games.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The compatibility score and round advancement are the game's whole point, so they are pinned here.
 */
class ThisOrThatEngineTest {

    private val deck = listOf(
        Prompt(1, "A", "B"),
        Prompt(2, "C", "D"),
        Prompt(3, "E", "F"),
    )

    private fun game() = ThisOrThatEngine.newGame(deck)

    @Test
    fun `a round only advances once both have answered`() {
        var state = game()
        state = ThisOrThatEngine.apply(state, isSelf = true, promptId = 1, pickedLeft = true)
        // Self answered but partner has not — still on round 0.
        assertEquals(0, state.roundIndex)
        assertTrue(state.selfAnsweredCurrent)
        assertFalse(state.partnerAnsweredCurrent)

        state = ThisOrThatEngine.apply(state, isSelf = false, promptId = 1, pickedLeft = true)
        assertEquals(1, state.roundIndex)
    }

    @Test
    fun `matching answers count toward compatibility`() {
        var state = game()
        state = ThisOrThatEngine.apply(state, isSelf = true, promptId = 1, pickedLeft = true)
        state = ThisOrThatEngine.apply(state, isSelf = false, promptId = 1, pickedLeft = true)
        assertEquals(1, state.matches)
        assertEquals(100, state.compatibilityPercent)
    }

    @Test
    fun `mismatched answers do not count as a match`() {
        var state = game()
        state = ThisOrThatEngine.apply(state, isSelf = true, promptId = 1, pickedLeft = true)
        state = ThisOrThatEngine.apply(state, isSelf = false, promptId = 1, pickedLeft = false)
        assertEquals(0, state.matches)
        assertEquals(0, state.compatibilityPercent)
        assertEquals(1, state.revealed.size)
    }

    @Test
    fun `order of answers does not matter`() {
        // Partner answers first, then self — the round should still reveal and advance.
        var state = game()
        state = ThisOrThatEngine.apply(state, isSelf = false, promptId = 1, pickedLeft = true)
        assertEquals(0, state.roundIndex)
        state = ThisOrThatEngine.apply(state, isSelf = true, promptId = 1, pickedLeft = true)
        assertEquals(1, state.roundIndex)
        assertEquals(1, state.matches)
    }

    @Test
    fun `answering every round finishes the game`() {
        var state = game()
        deck.forEach { prompt ->
            state = ThisOrThatEngine.apply(state, isSelf = true, promptId = prompt.id, pickedLeft = true)
            state = ThisOrThatEngine.apply(state, isSelf = false, promptId = prompt.id, pickedLeft = prompt.id != 2)
        }
        assertTrue(state.finished)
        // Two of three matched (prompt 2 differed), so 2/3 = 66%.
        assertEquals(2, state.matches)
        assertEquals(66, state.compatibilityPercent)
    }

    @Test
    fun `compatibility is zero before any round is revealed`() {
        assertEquals(0, game().compatibilityPercent)
    }
}
