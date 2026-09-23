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

private enum class LsMove(val emoji: String, val label: String) {
    ROCK("🪨", "Rock"), PAPER("📄", "Paper"), SCISSORS("✂️", "Scissors"), LIZARD("🦎", "Lizard"), SPOCK("🖖", "Spock")
}

private val BEATS: Map<LsMove, Set<LsMove>> = mapOf(
    LsMove.ROCK to setOf(LsMove.SCISSORS, LsMove.LIZARD),
    LsMove.PAPER to setOf(LsMove.ROCK, LsMove.SPOCK),
    LsMove.SCISSORS to setOf(LsMove.PAPER, LsMove.LIZARD),
    LsMove.LIZARD to setOf(LsMove.SPOCK, LsMove.PAPER),
    LsMove.SPOCK to setOf(LsMove.SCISSORS, LsMove.ROCK),
)

@Composable
fun RpsLizardSpockGame(onBack: () -> Unit) {
    GameScaffold(title = "Rock Paper Scissors Lizard Spock", onBack = onBack) { modifier ->
        var you by remember { mutableStateOf<LsMove?>(null) }
        var bot by remember { mutableStateOf<LsMove?>(null) }
        var result by remember { mutableStateOf("") }
        var wins by remember { mutableIntStateOf(0) }
        var losses by remember { mutableIntStateOf(0) }
        var draws by remember { mutableIntStateOf(0) }

        fun play(move: LsMove) {
            val botMove = LsMove.entries[Random.nextInt(LsMove.entries.size)]
            you = move; bot = botMove
            result = when {
                move == botMove -> { draws++; "Draw!" }
                BEATS[move]?.contains(botMove) == true -> { wins++; "You win! 🎉" }
                else -> { losses++; "Robin wins" }
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text("You $wins · Robin $losses · Draws $draws", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Text(you?.emoji ?: "❔", fontSize = 56.sp)
                Text("vs", color = OnSurfaceVariantPink)
                Text(bot?.emoji ?: "❔", fontSize = 56.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                result.ifEmpty { "Choose your weapon" },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (result.startsWith("You win")) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.weight(1f))
            // Five moves wrap into rows of three then two.
            LsMove.entries.chunked(3).forEach { rowMoves ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowMoves.forEach { m ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceHigh)
                                .clickable { play(m) }
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(m.emoji, fontSize = 30.sp)
                            Spacer(Modifier.size(4.dp))
                            Text(m.label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceLight)
                        }
                    }
                    repeat(3 - rowMoves.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
