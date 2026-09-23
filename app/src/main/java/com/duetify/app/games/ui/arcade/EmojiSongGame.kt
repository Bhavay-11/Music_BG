package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

private data class EmojiQuestion(val clue: String, val answer: String, val options: List<String>)

private val QUESTIONS = listOf(
    EmojiQuestion("🌧️💜", "Purple Rain", listOf("Purple Rain", "Singin' in the Rain", "Set Fire to the Rain", "November Rain")),
    EmojiQuestion("👊🎶💤", "Mr. Brightside", listOf("Mr. Brightside", "Boulevard of Broken Dreams", "Wake Me Up", "Sweet Dreams")),
    EmojiQuestion("🚀👨", "Rocket Man", listOf("Rocket Man", "Starman", "Space Oddity", "Major Tom")),
    EmojiQuestion("💃👑", "Dancing Queen", listOf("Dancing Queen", "Killer Queen", "Queen Bee", "Material Girl")),
    EmojiQuestion("👋❤️", "Say You Love Me", listOf("Say You Love Me", "Hello", "Hey There", "Wave to Me")),
    EmojiQuestion("🔥🎂", "This Girl Is on Fire", listOf("Girl on Fire", "Firework", "Burn", "Sugar")),
    EmojiQuestion("🌙🚶", "Moonwalk", listOf("Billie Jean", "Thriller", "Bad", "Smooth Criminal")),
    EmojiQuestion("💎👀", "Diamonds", listOf("Diamonds", "Shine Bright", "Gold", "Firework")),
)

@Composable
fun EmojiSongGame(onBack: () -> Unit) {
    GameScaffold(title = "Emoji Song Quiz", onBack = onBack) { modifier ->
        val questions = remember { QUESTIONS.shuffled() }
        var index by remember { mutableIntStateOf(0) }
        var score by remember { mutableIntStateOf(0) }
        var picked by remember { mutableStateOf<String?>(null) }

        val done = index >= questions.size
        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (done) "Final score" else "Question ${index + 1} / ${questions.size}",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(4.dp))
            Text("Score: $score", style = MaterialTheme.typography.titleMedium, color = Teal, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            if (done) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("$score / ${questions.size}", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Coral)
                }
                Button(
                    onClick = { index = 0; score = 0; picked = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Play again") }
                return@GameScaffold
            }

            val q = questions[index]
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceHigh).padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(q.clue, fontSize = 56.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(20.dp))

            q.options.forEach { option ->
                val state = when {
                    picked == null -> OptionState.NEUTRAL
                    option == q.answer -> OptionState.CORRECT
                    option == picked -> OptionState.WRONG
                    else -> OptionState.NEUTRAL
                }
                OptionRow(text = option, state = state, enabled = picked == null) {
                    picked = option
                    if (option == q.answer) score++
                }
                Spacer(Modifier.height(10.dp))
            }

            if (picked != null) {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { index++; picked = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text(if (index == questions.size - 1) "See score" else "Next") }
            }
        }
    }
}

private enum class OptionState { NEUTRAL, CORRECT, WRONG }

@Composable
private fun OptionRow(text: String, state: OptionState, enabled: Boolean, onClick: () -> Unit) {
    val bg = when (state) {
        OptionState.NEUTRAL -> SurfaceHigh
        OptionState.CORRECT -> Teal
        OptionState.WRONG -> Coral
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (state == OptionState.NEUTRAL) OnSurfaceLight else OnAccent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
