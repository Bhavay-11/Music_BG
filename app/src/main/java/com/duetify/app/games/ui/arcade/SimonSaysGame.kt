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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class SimonPhase { READY, SHOWING, INPUT, GAME_OVER }

private val PAD_COLORS = listOf(
    Color(0xFFE5484D), // red
    Color(0xFF2FBF71), // green
    Color(0xFF4C7DF0), // blue
    Color(0xFFF2C14E), // amber
)

@Composable
fun SimonSaysGame(onBack: () -> Unit) {
    GameScaffold(title = "Simon Says", onBack = onBack) { modifier ->
        var phase by remember { mutableStateOf(SimonPhase.READY) }
        var pattern by remember { mutableStateOf(listOf<Int>()) }
        var inputPos by remember { mutableIntStateOf(0) }
        var lit by remember { mutableIntStateOf(-1) }
        var tapTick by remember { mutableIntStateOf(0) }
        var best by remember { mutableIntStateOf(0) }

        // Play back the current sequence, then hand control to the player.
        LaunchedEffect(phase) {
            if (phase == SimonPhase.SHOWING) {
                delay(500)
                for (pad in pattern) {
                    lit = pad
                    delay(480)
                    lit = -1
                    delay(220)
                }
                phase = SimonPhase.INPUT
            }
        }

        // Briefly light a pad the player taps, without disturbing playback (INPUT-only).
        LaunchedEffect(tapTick) {
            if (tapTick > 0 && phase == SimonPhase.INPUT) {
                delay(180)
                lit = -1
            }
        }

        fun start() {
            pattern = listOf(Random.nextInt(PAD_COLORS.size))
            inputPos = 0
            phase = SimonPhase.SHOWING
        }

        fun onPad(index: Int) {
            if (phase != SimonPhase.INPUT) return
            lit = index
            tapTick++
            if (pattern[inputPos] == index) {
                inputPos++
                if (inputPos == pattern.size) {
                    // Round cleared — extend the sequence and replay.
                    if (pattern.size > best) best = pattern.size
                    pattern = pattern + Random.nextInt(PAD_COLORS.size)
                    inputPos = 0
                    phase = SimonPhase.SHOWING
                }
            } else {
                phase = SimonPhase.GAME_OVER
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (phase) {
                    SimonPhase.READY -> if (best > 0) "Best level: $best" else "Watch, then repeat the sequence"
                    SimonPhase.SHOWING -> "Watch closely…"
                    SimonPhase.INPUT -> "Your turn — level ${pattern.size}"
                    SimonPhase.GAME_OVER -> "Oops! You reached level ${pattern.size}"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (phase == SimonPhase.GAME_OVER) Coral else OnSurfaceLight,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                for (r in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (c in 0 until 2) {
                            val index = r * 2 + c
                            Pad(
                                color = PAD_COLORS[index],
                                lit = lit == index,
                                enabled = phase == SimonPhase.INPUT,
                                modifier = Modifier.weight(1f),
                            ) { onPad(index) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (phase == SimonPhase.READY || phase == SimonPhase.GAME_OVER) {
                Button(
                    onClick = { start() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text(if (phase == SimonPhase.GAME_OVER) "Try again" else "Start") }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Each round adds one step. How far can you get?",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Pad(color: Color, lit: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (lit) color else color.copy(alpha = 0.32f))
            .clickable(enabled = enabled, onClick = onClick),
    )
}
