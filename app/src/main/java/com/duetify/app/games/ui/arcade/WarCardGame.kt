package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

private fun freshHalves(): Pair<List<Int>, List<Int>> {
    val deck = (2..14).flatMap { v -> List(4) { v } }.shuffled()
    return deck.take(26) to deck.drop(26)
}

private fun rank(v: Int): String = when (v) {
    14 -> "A"; 13 -> "K"; 12 -> "Q"; 11 -> "J"; else -> v.toString()
}

@Composable
fun WarCardGame(onBack: () -> Unit) {
    GameScaffold(title = "War", onBack = onBack) { modifier ->
        val start = remember { freshHalves() }
        var d1 by remember { mutableStateOf(start.first) }
        var d2 by remember { mutableStateOf(start.second) }
        var pot by remember { mutableStateOf(listOf<Int>()) }
        var a by remember { mutableStateOf<Int?>(null) }
        var b by remember { mutableStateOf<Int?>(null) }
        var message by remember { mutableStateOf("Flip to battle!") }

        val over = d1.isEmpty() || d2.isEmpty()

        fun flip() {
            if (over) return
            val ca = d1.first()
            val cb = d2.first()
            a = ca; b = cb
            var n1 = d1.drop(1)
            var n2 = d2.drop(1)
            val newPot = pot + ca + cb
            when {
                ca > cb -> { n1 = n1 + newPot; pot = emptyList(); message = "Player 1 takes ${newPot.size}" }
                cb > ca -> { n2 = n2 + newPot; pot = emptyList(); message = "Player 2 takes ${newPot.size}" }
                else -> { pot = newPot; message = "War! ${newPot.size} in the pot" }
            }
            d1 = n1; d2 = n2
        }
        fun reset() {
            val f = freshHalves(); d1 = f.first; d2 = f.second; pot = emptyList(); a = null; b = null; message = "Flip to battle!"
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Pile("Player 1", d1.size, Modifier.weight(1f))
                Pile("Player 2", d2.size, Modifier.weight(1f))
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        FaceCard(a)
                        FaceCard(b)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = when {
                            d1.isEmpty() -> "Player 2 wins the war! 🎉"
                            d2.isEmpty() -> "Player 1 wins the war! 🎉"
                            else -> message
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (over) Teal else OnSurfaceLight,
                    )
                }
            }
            if (!over) {
                Button(
                    onClick = { flip() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Flip") }
            } else {
                Button(
                    onClick = { reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("New game") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Higher card wins the flip. Ties build the pot.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Pile(name: String, count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(SurfaceHigh).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(name, style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariantPink)
        Text("$count cards", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FaceCard(v: Int?) {
    Box(
        modifier = Modifier.size(width = 72.dp, height = 100.dp).clip(RoundedCornerShape(12.dp)).background(if (v == null) SurfaceHigh else OnAccent),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (v == null) "?" else rank(v), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = if (v == null) OnSurfaceVariantPink else Coral)
    }
}
