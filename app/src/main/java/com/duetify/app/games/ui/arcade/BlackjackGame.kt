package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private fun bjRank(v: Int): String = when (v) { 14 -> "A"; 13 -> "K"; 12 -> "Q"; 11 -> "J"; else -> v.toString() }

private fun handValue(cards: List<Int>): Int {
    var sum = 0; var aces = 0
    for (c in cards) {
        sum += when { c == 14 -> 11; c >= 11 -> 10; else -> c }
        if (c == 14) aces++
    }
    while (sum > 21 && aces > 0) { sum -= 10; aces-- }
    return sum
}

@Composable
fun BlackjackGame(onBack: () -> Unit) {
    GameScaffold(title = "Blackjack", onBack = onBack) { modifier ->
        val deck = remember { mutableListOf<Int>() }
        var player by remember { mutableStateOf(listOf<Int>()) }
        var dealer by remember { mutableStateOf(listOf<Int>()) }
        var stood by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }
        var wins by remember { mutableIntStateOf(0) }
        var losses by remember { mutableIntStateOf(0) }

        fun draw(): Int = deck.removeAt(deck.lastIndex)

        fun newRound() {
            deck.clear()
            deck.addAll((2..14).flatMap { v -> List(4) { v } }.shuffled())
            player = listOf(draw(), draw())
            dealer = listOf(draw(), draw())
            stood = false
            message = ""
            if (handValue(player) == 21) { // Natural blackjack.
                stood = true
                message = "Blackjack! 🎉"
                wins++
            }
        }

        LaunchedEffect(Unit) { newRound() }

        fun settle() {
            val pv = handValue(player)
            val dv = handValue(dealer)
            message = when {
                pv > 21 -> { losses++; "Bust! Dealer wins" }
                dv > 21 -> { wins++; "Dealer busts — you win! 🎉" }
                pv > dv -> { wins++; "You win! 🎉" }
                pv < dv -> { losses++; "Dealer wins" }
                else -> "Push (tie)"
            }
        }

        fun hit() {
            if (stood || message.isNotEmpty()) return
            player = player + draw()
            if (handValue(player) > 21) { stood = true; settle() }
        }
        fun stand() {
            if (stood) return
            stood = true
            var d = dealer
            while (handValue(d) < 17) d = d + draw()
            dealer = d
            settle()
        }

        val roundOver = message.isNotEmpty()

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text("You $wins · Dealer $losses", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(16.dp))

            Text("Dealer" + if (stood) " · ${handValue(dealer)}" else "", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dealer.forEachIndexed { i, c ->
                    if (i == 0 || stood) CardFace(bjRank(c)) else CardFace("?", hidden = true)
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                message.ifEmpty { "Your move" },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (message.contains("win") || message.contains("Blackjack")) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.weight(1f))

            Text("You · ${handValue(player)}", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                player.forEach { CardFace(bjRank(it)) }
            }
            Spacer(Modifier.height(16.dp))

            if (roundOver) {
                Button(
                    onClick = { newRound() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Deal again") }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { hit() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                    ) { Text("Hit") }
                    Button(
                        onClick = { stand() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = OnAccent),
                    ) { Text("Stand") }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CardFace(label: String, hidden: Boolean = false) {
    Box(
        modifier = Modifier.size(width = 54.dp, height = 76.dp).clip(RoundedCornerShape(10.dp)).background(if (hidden) SurfaceHigh else OnAccent),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (hidden) OnSurfaceVariantPink else Coral)
    }
}
