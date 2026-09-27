package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.Lavender
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import kotlin.random.Random

private val TRUTHS = listOf(
    "What was your first impression of me?",
    "What's a small thing that instantly makes your day?",
    "What song reminds you of us?",
    "What's something you've never told me?",
    "What's your favourite memory of us so far?",
    "What are you secretly proud of?",
    "If you could relive one day together, which?",
    "What's a habit of mine you secretly love?",
    "What's on your bucket list right now?",
    "When did you feel closest to me?",
)

private val DARES = listOf(
    "Send a voice note singing our song.",
    "Do your best impression of me.",
    "Text a compliment to the last person you messaged.",
    "Hold a plank for 30 seconds.",
    "Say three things you love, in a movie‑trailer voice.",
    "Do a 10‑second happy dance.",
    "Speak in an accent until your next turn.",
    "Take a goofy selfie and set it as your wallpaper for a day.",
    "Recreate an emoji with your face.",
    "Give a 20‑second toast to the other player.",
)

@Composable
fun TruthOrDareGame(onBack: () -> Unit) {
    GameScaffold(title = "Truth or Dare", onBack = onBack) { modifier ->
        var player by remember { mutableIntStateOf(1) }
        var prompt by remember { mutableStateOf<String?>(null) }
        var isTruth by remember { mutableStateOf(true) }

        fun pick(truth: Boolean) {
            isTruth = truth
            prompt = (if (truth) TRUTHS else DARES).let { it[Random.nextInt(it.size)] }
        }

        fun nextPlayer() {
            player = if (player == 1) 2 else 1
            prompt = null
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Player $player's turn",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceLight,
            )
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (prompt == null) {
                    Text(
                        "Pick Truth or Dare",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariantPink,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (isTruth) "TRUTH" else "DARE",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isTruth) Lavender else Coral,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceHigh)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                prompt!!,
                                style = MaterialTheme.typography.titleLarge,
                                color = OnSurfaceLight,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            if (prompt == null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { pick(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Lavender, contentColor = OnAccent),
                    ) { Text("Truth") }
                    Button(
                        onClick = { pick(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                    ) { Text("Dare") }
                }
            } else {
                Button(
                    onClick = { nextPlayer() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Done — next player") }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Pass‑and‑play: take turns on one phone.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
        }
    }
}
