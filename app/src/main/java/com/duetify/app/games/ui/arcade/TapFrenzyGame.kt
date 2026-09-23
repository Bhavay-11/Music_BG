package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

private enum class TapPhase { READY, RUNNING, DONE }

private const val ROUND_SECONDS = 10

@Composable
fun TapFrenzyGame(onBack: () -> Unit) {
    GameScaffold(title = "Tap Frenzy", onBack = onBack) { modifier ->
        var phase by remember { mutableStateOf(TapPhase.READY) }
        var count by remember { mutableIntStateOf(0) }
        var remaining by remember { mutableIntStateOf(ROUND_SECONDS) }
        var best by remember { mutableIntStateOf(0) }

        LaunchedEffect(phase) {
            if (phase == TapPhase.RUNNING) {
                remaining = ROUND_SECONDS
                while (remaining > 0) {
                    delay(1000)
                    remaining -= 1
                }
                if (count > best) best = count
                phase = TapPhase.DONE
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (phase) {
                    TapPhase.READY -> if (best > 0) "Best: $best taps" else "How fast can you tap in ${ROUND_SECONDS}s?"
                    TapPhase.RUNNING -> "$remaining s left"
                    TapPhase.DONE -> "Time! Best: $best"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (phase == TapPhase.RUNNING) Teal else OnSurfaceVariantPink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (phase == TapPhase.RUNNING) Coral else SurfaceHigh)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        when (phase) {
                            TapPhase.READY -> { count = 0; phase = TapPhase.RUNNING }
                            TapPhase.RUNNING -> count += 1
                            TapPhase.DONE -> { count = 0; phase = TapPhase.RUNNING }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "$count",
                    fontSize = 88.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (phase == TapPhase.RUNNING) OnAccent else OnSurfaceLight,
                )
                Text(
                    text = when (phase) {
                        TapPhase.READY -> "Tap to start"
                        TapPhase.RUNNING -> "TAP!"
                        TapPhase.DONE -> "Tap to go again"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (phase == TapPhase.RUNNING) OnAccent.copy(alpha = 0.9f) else OnSurfaceVariantPink,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Pass-and-play: take turns and compare tap counts.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
        }
    }
}
