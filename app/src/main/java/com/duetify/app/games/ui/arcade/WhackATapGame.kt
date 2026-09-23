package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class WhackPhase { READY, RUNNING, DONE }

private const val ROUND_SECONDS = 20
private const val CELLS = 9

@Composable
fun WhackATapGame(onBack: () -> Unit) {
    GameScaffold(title = "Whack-a-Tap", onBack = onBack) { modifier ->
        var phase by remember { mutableStateOf(WhackPhase.READY) }
        var score by remember { mutableIntStateOf(0) }
        var best by remember { mutableIntStateOf(0) }
        var remaining by remember { mutableIntStateOf(ROUND_SECONDS) }
        var target by remember { mutableIntStateOf(0) }

        // Countdown for the round.
        LaunchedEffect(phase) {
            if (phase == WhackPhase.RUNNING) {
                remaining = ROUND_SECONDS
                while (remaining > 0) {
                    delay(1000)
                    remaining -= 1
                }
                if (score > best) best = score
                phase = WhackPhase.DONE
            }
        }

        // The target hops to a new cell if you don't tap it in time; re-armed whenever it moves.
        LaunchedEffect(phase, target) {
            if (phase == WhackPhase.RUNNING) {
                delay(850)
                if (phase == WhackPhase.RUNNING) {
                    target = nextCell(target)
                }
            }
        }

        fun start() {
            score = 0
            target = Random.nextInt(CELLS)
            phase = WhackPhase.RUNNING
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Score: $score", style = MaterialTheme.typography.titleMedium, color = Coral, fontWeight = FontWeight.Bold)
                Text(
                    when (phase) {
                        WhackPhase.RUNNING -> "$remaining s"
                        else -> if (best > 0) "Best: $best" else "Tap the glowing tile"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (phase == WhackPhase.RUNNING) Teal else OnSurfaceVariantPink,
                )
            }
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                for (r in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (c in 0 until 3) {
                            val index = r * 3 + c
                            val isTarget = phase == WhackPhase.RUNNING && index == target
                            Tile(active = isTarget, modifier = Modifier.weight(1f)) {
                                if (phase == WhackPhase.RUNNING && index == target) {
                                    score++
                                    target = nextCell(target)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (phase != WhackPhase.RUNNING) {
                Button(
                    onClick = { start() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text(if (phase == WhackPhase.DONE) "Play again" else "Start") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun nextCell(current: Int): Int {
    var n = Random.nextInt(CELLS)
    if (n == current) n = (n + 1) % CELLS
    return n
}

@Composable
private fun Tile(active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Coral else SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (active) Text("🎯", fontSize = 30.sp)
    }
}
