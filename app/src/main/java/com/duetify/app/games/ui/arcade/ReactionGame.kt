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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class ReactionPhase { IDLE, WAITING, GO, RESULT, TOO_SOON }

@Composable
fun ReactionGame(onBack: () -> Unit) {
    GameScaffold(title = "Reaction Duel", onBack = onBack) { modifier ->
        var phase by remember { mutableStateOf(ReactionPhase.IDLE) }
        var goAt by remember { mutableLongStateOf(0L) }
        var lastMs by remember { mutableIntStateOf(0) }
        var best by remember { mutableIntStateOf(0) }

        // Arm the timer: after a random delay the panel turns green. Re-arms whenever we enter WAITING.
        LaunchedEffect(phase) {
            if (phase == ReactionPhase.WAITING) {
                delay(Random.nextLong(1200, 3500))
                if (phase == ReactionPhase.WAITING) {
                    goAt = System.currentTimeMillis()
                    phase = ReactionPhase.GO
                }
            }
        }

        val (bg, headline, sub) = when (phase) {
            ReactionPhase.IDLE -> Triple(Color(0xFF2A2440), "Tap to start", "Then wait for green")
            ReactionPhase.WAITING -> Triple(Color(0xFFB23A48), "Wait…", "Tap the moment it turns green")
            ReactionPhase.GO -> Triple(Color(0xFF2FBF71), "TAP!", "")
            ReactionPhase.RESULT -> Triple(Color(0xFF2A2440), "$lastMs ms", "Tap to try again")
            ReactionPhase.TOO_SOON -> Triple(Color(0xFF8A5A00), "Too soon!", "Tap to try again")
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (best > 0) "Best: $best ms" else "Beat your best reaction time",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        when (phase) {
                            ReactionPhase.IDLE, ReactionPhase.RESULT, ReactionPhase.TOO_SOON ->
                                phase = ReactionPhase.WAITING
                            ReactionPhase.WAITING -> phase = ReactionPhase.TOO_SOON
                            ReactionPhase.GO -> {
                                lastMs = (System.currentTimeMillis() - goAt).toInt()
                                if (best == 0 || lastMs < best) best = lastMs
                                phase = ReactionPhase.RESULT
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(headline, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                if (sub.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(sub, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Couple mode: pass the phone and see who's quicker.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
        }
    }
}
