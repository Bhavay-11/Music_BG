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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlin.random.Random

private enum class Move(val emoji: String, val label: String) {
    ROCK("🪨", "Rock"), PAPER("📄", "Paper"), SCISSORS("✂️", "Scissors")
}

private fun beats(a: Move, b: Move): Boolean =
    (a == Move.ROCK && b == Move.SCISSORS) ||
        (a == Move.PAPER && b == Move.ROCK) ||
        (a == Move.SCISSORS && b == Move.PAPER)

@Composable
fun RockPaperScissorsGame(onBack: () -> Unit) {
    GameScaffold(title = "Rock Paper Scissors", onBack = onBack) { modifier ->
        var you by remember { mutableStateOf<Move?>(null) }
        var bot by remember { mutableStateOf<Move?>(null) }
        var result by remember { mutableStateOf("") }
        var wins by remember { mutableIntStateOf(0) }
        var losses by remember { mutableIntStateOf(0) }
        var draws by remember { mutableIntStateOf(0) }

        fun play(move: Move) {
            val botMove = Move.entries[Random.nextInt(Move.entries.size)]
            you = move
            bot = botMove
            result = when {
                move == botMove -> { draws++; "Draw!" }
                beats(move, botMove) -> { wins++; "You win! 🎉" }
                else -> { losses++; "Robin wins" }
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                "You $wins · Robin $losses · Draws $draws",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                FaceBox(label = "You", emoji = you?.emoji ?: "❔")
                Text("vs", style = MaterialTheme.typography.titleLarge, color = OnSurfaceVariantPink)
                FaceBox(label = "Robin", emoji = bot?.emoji ?: "❔")
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = result.ifEmpty { "Pick your move" },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (result.startsWith("You win")) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Move.entries.forEach { move ->
                    MoveButton(move = move, modifier = Modifier.weight(1f)) { play(move) }
                }
            }
        }
    }
}

@Composable
private fun FaceBox(label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)).background(SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 48.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariantPink)
    }
}

@Composable
private fun MoveButton(move: Move, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(move.emoji, fontSize = 34.sp)
        Spacer(Modifier.height(6.dp))
        Text(move.label, style = MaterialTheme.typography.labelLarge, color = OnSurfaceLight)
    }
}
