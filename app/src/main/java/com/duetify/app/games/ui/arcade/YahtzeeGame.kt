package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlin.random.Random

private enum class Cat(val label: String) {
    ONES("Ones"), TWOS("Twos"), THREES("Threes"), FOURS("Fours"), FIVES("Fives"), SIXES("Sixes"),
    THREE_KIND("Three of a Kind"), FOUR_KIND("Four of a Kind"), FULL_HOUSE("Full House"),
    SMALL_STRAIGHT("Small Straight"), LARGE_STRAIGHT("Large Straight"), YAHTZEE("Yahtzee"), CHANCE("Chance")
}

private fun scoreFor(cat: Cat, dice: List<Int>): Int {
    val counts = IntArray(7); dice.forEach { if (it in 1..6) counts[it]++ }
    val sum = dice.sum()
    val set = dice.filter { it > 0 }.toSet()
    fun contains(vararg v: Int) = v.all { it in set }
    return when (cat) {
        Cat.ONES -> counts[1] * 1
        Cat.TWOS -> counts[2] * 2
        Cat.THREES -> counts[3] * 3
        Cat.FOURS -> counts[4] * 4
        Cat.FIVES -> counts[5] * 5
        Cat.SIXES -> counts[6] * 6
        Cat.THREE_KIND -> if (counts.any { it >= 3 }) sum else 0
        Cat.FOUR_KIND -> if (counts.any { it >= 4 }) sum else 0
        Cat.FULL_HOUSE -> if (counts.any { it == 3 } && counts.any { it == 2 }) 25 else 0
        Cat.SMALL_STRAIGHT -> if (contains(1, 2, 3, 4) || contains(2, 3, 4, 5) || contains(3, 4, 5, 6)) 30 else 0
        Cat.LARGE_STRAIGHT -> if (contains(1, 2, 3, 4, 5) || contains(2, 3, 4, 5, 6)) 40 else 0
        Cat.YAHTZEE -> if (counts.any { it == 5 }) 50 else 0
        Cat.CHANCE -> sum
    }
}

@Composable
fun YahtzeeGame(onBack: () -> Unit) {
    GameScaffold(title = "Yahtzee", onBack = onBack) { modifier ->
        var dice by remember { mutableStateOf(List(5) { 0 }) }
        var held by remember { mutableStateOf(List(5) { false }) }
        var rollsLeft by remember { mutableIntStateOf(3) }
        var scores by remember { mutableStateOf(List<Int?>(Cat.entries.size) { null }) }

        val rolled = dice.any { it > 0 }
        val filledCount = scores.count { it != null }
        val over = filledCount == Cat.entries.size
        val upper = (0..5).sumOf { scores[it] ?: 0 }
        val bonus = if (upper >= 63) 35 else 0
        val total = scores.sumOf { it ?: 0 } + bonus

        fun roll() {
            if (rollsLeft <= 0 || over) return
            dice = dice.mapIndexed { i, v -> if (held[i] && v > 0) v else Random.nextInt(1, 7) }
            rollsLeft -= 1
        }
        fun toggleHold(i: Int) { if (rolled && !over) held = held.mapIndexed { j, h -> if (j == i) !h else h } }
        fun scoreInto(cat: Cat) {
            if (!rolled || over || scores[cat.ordinal] != null) return
            scores = scores.mapIndexed { i, s -> if (i == cat.ordinal) scoreFor(cat, dice) else s }
            dice = List(5) { 0 }; held = List(5) { false }; rollsLeft = 3
        }
        fun reset() { dice = List(5) { 0 }; held = List(5) { false }; rollsLeft = 3; scores = List(Cat.entries.size) { null } }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Total: $total", style = MaterialTheme.typography.titleMedium, color = Coral, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(if (over) "Game over 🎉" else "Rolls left: $rollsLeft", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until 5) {
                    Box(
                        modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp)).background(if (held[i]) Teal else SurfaceHigh).clickable { toggleHold(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (dice[i] > 0) "${dice[i]}" else "–", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (held[i]) OnAccent else OnSurfaceLight)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            if (!over) {
                Button(onClick = { roll() }, enabled = rollsLeft > 0, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) {
                    Text(if (rolled) "Roll (${rollsLeft} left) · tap dice to hold" else "Roll dice")
                }
            } else {
                Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            }
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Cat.entries.forEach { cat ->
                    val filled = scores[cat.ordinal]
                    val potential = if (rolled && filled == null) scoreFor(cat, dice) else null
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(10.dp))
                            .background(SurfaceHigh)
                            .clickable(enabled = filled == null && rolled) { scoreInto(cat) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(cat.label, color = OnSurfaceLight, modifier = Modifier.weight(1f))
                        when {
                            filled != null -> Text("$filled", color = OnSurfaceVariantPink, fontWeight = FontWeight.Bold)
                            potential != null -> Text("+$potential", color = Teal, fontWeight = FontWeight.Bold)
                            else -> Text("—", color = OnSurfaceVariantPink)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 14.dp)) {
                    Text("Upper bonus", color = OnSurfaceVariantPink, modifier = Modifier.weight(1f))
                    Text(if (bonus > 0) "+35" else "$upper/63", color = OnSurfaceVariantPink, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
