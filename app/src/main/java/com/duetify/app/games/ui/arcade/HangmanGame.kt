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

private val WORDS = listOf(
    "MELODY", "GUITAR", "RHYTHM", "CONCERT", "PLAYLIST", "HARMONY", "VINYL", "ACOUSTIC",
    "FESTIVAL", "KARAOKE", "SYMPHONY", "CHORUS", "TEMPO", "REMIX", "LYRICS", "ENCORE",
)
private const val MAX_WRONG = 6

@Composable
fun HangmanGame(onBack: () -> Unit) {
    GameScaffold(title = "Hangman", onBack = onBack) { modifier ->
        var word by remember { mutableStateOf(WORDS.random()) }
        var guessed by remember { mutableStateOf(setOf<Char>()) }
        var round by remember { mutableIntStateOf(0) }

        val wrong = guessed.filter { it !in word }
        val lost = wrong.size >= MAX_WRONG
        val won = word.all { it in guessed }
        val over = lost || won

        fun guess(ch: Char) {
            if (over || ch in guessed) return
            guessed = guessed + ch
        }
        fun newRound() {
            word = WORDS.random(); guessed = emptySet(); round++
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Lives: ${MAX_WRONG - wrong.size} / $MAX_WRONG",
                style = MaterialTheme.typography.titleMedium,
                color = if (wrong.size >= MAX_WRONG - 2) Coral else OnSurfaceVariantPink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = word.map { if (it in guessed || !it.isLetter()) it else '_' }.joinToString(" "),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = if (won) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = when {
                    won -> "You got it! 🎉"
                    lost -> "Out of lives — it was $word"
                    wrong.isEmpty() -> "Guess a letter"
                    else -> "Wrong: ${wrong.joinToString(" ")}"
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.weight(1f))

            if (over) {
                Button(
                    onClick = { newRound() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Next word") }
            } else {
                ('A'..'Z').chunked(7).forEach { rowLetters ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowLetters.forEach { ch ->
                            Key(ch = ch, used = ch in guessed, modifier = Modifier.weight(1f)) { guess(ch) }
                        }
                        // Pad the last (shorter) row so keys keep their width.
                        repeat(7 - rowLetters.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Key(ch: Char, used: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (used) SurfaceHigh.copy(alpha = 0.4f) else SurfaceHigh)
            .clickable(enabled = !used, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("$ch", fontWeight = FontWeight.Bold, color = if (used) OnSurfaceVariantPink else OnSurfaceLight)
    }
}
