package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlinx.coroutines.delay

private val SUITS = listOf("♠", "♥", "♦", "♣")
private fun suitOf(c: Int) = c / 13
private fun valOf(c: Int) = c % 13 + 1
private fun rankLabel(c: Int): String = when (valOf(c)) { 1 -> "A"; 11 -> "J"; 12 -> "Q"; 13 -> "K"; else -> "${valOf(c)}" }
private fun isRed(suit: Int) = suit == 1 || suit == 2

@Composable
fun CrazyEightsGame(onBack: () -> Unit) {
    GameScaffold(title = "Crazy Eights", onBack = onBack) { modifier ->
        var hand by remember { mutableStateOf(listOf<Int>()) }
        var bot by remember { mutableStateOf(listOf<Int>()) }
        var pile by remember { mutableStateOf(listOf<Int>()) }
        var discard by remember { mutableStateOf(listOf<Int>()) }
        var currentSuit by remember { mutableIntStateOf(0) }
        var yourTurn by remember { mutableStateOf(true) }
        var pendingSuit by remember { mutableStateOf(false) }
        var winner by remember { mutableStateOf<String?>(null) }
        var seed by remember { mutableIntStateOf(0) }

        LaunchedEffect(seed) {
            val deck = (0 until 52).shuffled()
            hand = deck.take(5)
            bot = deck.drop(5).take(5)
            val rest = deck.drop(10)
            discard = listOf(rest.first())
            pile = rest.drop(1)
            currentSuit = suitOf(rest.first())
            yourTurn = true; pendingSuit = false; winner = null
        }

        val top = discard.lastOrNull() ?: 0
        fun valid(c: Int) = suitOf(c) == currentSuit || valOf(c) == valOf(top) || valOf(c) == 8

        fun ensurePile() {
            if (pile.isEmpty() && discard.size > 1) {
                val keep = discard.last()
                pile = discard.dropLast(1).shuffled()
                discard = listOf(keep)
            }
        }

        fun playerPlay(c: Int) {
            if (!yourTurn || winner != null || pendingSuit || !valid(c)) return
            hand = hand - c
            discard = discard + c
            if (hand.isEmpty()) { winner = "You"; return }
            if (valOf(c) == 8) pendingSuit = true else { currentSuit = suitOf(c); yourTurn = false }
        }
        fun pickSuit(s: Int) { currentSuit = s; pendingSuit = false; yourTurn = false }
        fun drawForYou() {
            if (!yourTurn || winner != null || pendingSuit) return
            ensurePile()
            if (pile.isNotEmpty()) { hand = hand + pile.first(); pile = pile.drop(1) }
            yourTurn = false
        }

        // Bot turn.
        LaunchedEffect(yourTurn, winner, seed) {
            if (!yourTurn && winner == null) {
                delay(800)
                val playable = bot.filter { suitOf(it) == currentSuit || valOf(it) == valOf(top) || valOf(it) == 8 }
                val choice = playable.firstOrNull { valOf(it) != 8 } ?: playable.firstOrNull()
                if (choice != null) {
                    bot = bot - choice
                    discard = discard + choice
                    currentSuit = if (valOf(choice) == 8) (bot.groupingBy { suitOf(it) }.eachCount().maxByOrNull { it.value }?.key ?: suitOf(choice)) else suitOf(choice)
                    if (bot.isEmpty()) winner = "Robin" else yourTurn = true
                } else {
                    ensurePile()
                    if (pile.isNotEmpty()) { bot = bot + pile.first(); pile = pile.drop(1) }
                    yourTurn = true
                }
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(6.dp))
            Text("Robin: ${bot.size} cards", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                CardView(top, big = true)
                Spacer(Modifier.size(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(SUITS[currentSuit], fontSize = 34.sp, color = if (isRed(currentSuit)) Coral else OnSurfaceLight, fontWeight = FontWeight.Bold)
                    Text("suit", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantPink)
                }
                Spacer(Modifier.size(16.dp))
                Box(
                    modifier = Modifier.size(width = 54.dp, height = 76.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceContainer).clickable { drawForYou() },
                    contentAlignment = Alignment.Center,
                ) { Text("Draw\n${pile.size}", color = OnSurfaceLight, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = winner?.let { "$it wins! 🎉" } ?: if (pendingSuit) "Pick a suit" else if (yourTurn) "Your turn" else "Robin's turn",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (winner != null) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(8.dp))
            if (pendingSuit) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SUITS.forEachIndexed { i, s ->
                        Button(onClick = { pickSuit(i) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = if (isRed(i)) Coral else OnSurfaceLight)) { Text(s, fontSize = 20.sp) }
                    }
                }
            } else if (winner == null) {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    hand.forEach { c ->
                        Box(modifier = Modifier.padding(end = 8.dp).clickable(enabled = yourTurn && valid(c)) { playerPlay(c) }) {
                            CardView(c, big = false, dim = !(yourTurn && valid(c)))
                        }
                    }
                }
            } else {
                Button(onClick = { seed++ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CardView(c: Int, big: Boolean, dim: Boolean = false) {
    val w = if (big) 64.dp else 52.dp
    val h = if (big) 90.dp else 74.dp
    Box(
        modifier = Modifier.size(width = w, height = h).clip(RoundedCornerShape(10.dp)).background(if (dim) SurfaceHigh else OnAccent),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(rankLabel(c), fontSize = if (big) 26.sp else 20.sp, fontWeight = FontWeight.Bold, color = if (dim) OnSurfaceVariantPink else if (isRed(suitOf(c))) Coral else Color(0xFF222222))
            Text(SUITS[suitOf(c)], fontSize = if (big) 22.sp else 18.sp, color = if (dim) OnSurfaceVariantPink else if (isRed(suitOf(c))) Coral else Color(0xFF222222))
        }
    }
}
