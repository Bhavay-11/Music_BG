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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
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

private const val GOAL = 100
private val DICE_FACES = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

@Composable
fun PigDiceGame(onBack: () -> Unit) {
    GameScaffold(title = "Pig · Push Your Luck", onBack = onBack) { modifier ->
        val scores = remember { mutableStateOf(intArrayOf(0, 0)) }
        var player by remember { mutableIntStateOf(0) }
        var turnPoints by remember { mutableIntStateOf(0) }
        var lastRoll by remember { mutableIntStateOf(0) }
        var winner by remember { mutableIntStateOf(-1) }

        fun scoresArr() = scores.value

        fun roll() {
            if (winner >= 0) return
            val d = Random.nextInt(1, 7)
            lastRoll = d
            if (d == 1) {
                turnPoints = 0
                player = 1 - player
            } else {
                turnPoints += d
            }
        }
        fun bank() {
            if (winner >= 0 || turnPoints == 0) return
            val s = scoresArr().copyOf()
            s[player] += turnPoints
            scores.value = s
            if (s[player] >= GOAL) winner = player
            turnPoints = 0
            player = 1 - player
        }
        fun reset() {
            scores.value = intArrayOf(0, 0); player = 0; turnPoints = 0; lastRoll = 0; winner = -1
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoreCard("Player 1", scoresArr()[0], active = player == 0 && winner < 0, Modifier.weight(1f))
                ScoreCard("Player 2", scoresArr()[1], active = player == 1 && winner < 0, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("First to $GOAL wins", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (lastRoll > 0) DICE_FACES[lastRoll - 1] else "🎲", fontSize = 96.sp, color = if (lastRoll == 1) Coral else OnSurfaceLight)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when {
                            winner >= 0 -> "Player ${winner + 1} wins! 🎉"
                            lastRoll == 1 -> "Rolled a 1 — turn lost!"
                            else -> "Turn total: $turnPoints"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (winner >= 0) Teal else OnSurfaceLight,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (winner < 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { roll() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                    ) { Text("Roll") }
                    Button(
                        onClick = { bank() },
                        enabled = turnPoints > 0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = OnAccent),
                    ) { Text("Bank ($turnPoints)") }
                }
            } else {
                Button(
                    onClick = { reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("New game") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ScoreCard(name: String, score: Int, active: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Coral else SurfaceHigh)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(name, style = MaterialTheme.typography.labelLarge, color = if (active) OnAccent else OnSurfaceVariantPink)
        Text("$score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = if (active) OnAccent else OnSurfaceLight)
    }
}
