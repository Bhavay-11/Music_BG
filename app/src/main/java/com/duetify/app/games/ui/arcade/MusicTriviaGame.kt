package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private data class Trivia(val q: String, val answer: String, val options: List<String>)

private val TRIVIA = listOf(
    Trivia("Which instrument has 88 keys?", "Piano", listOf("Piano", "Harp", "Accordion", "Organ")),
    Trivia("How many strings does a standard guitar have?", "6", listOf("6", "4", "7", "12")),
    Trivia("‘Bohemian Rhapsody’ is by which band?", "Queen", listOf("Queen", "The Beatles", "Led Zeppelin", "The Who")),
    Trivia("A group of four musicians is a…", "Quartet", listOf("Quartet", "Trio", "Quintet", "Duo")),
    Trivia("Which term means ‘gradually louder’?", "Crescendo", listOf("Crescendo", "Legato", "Staccato", "Forte")),
    Trivia("‘The King of Pop’ is…", "Michael Jackson", listOf("Michael Jackson", "Elvis", "Prince", "James Brown")),
    Trivia("How many notes in a standard octave (inclusive)?", "8", listOf("8", "7", "12", "5")),
    Trivia("Which genre originated in Jamaica?", "Reggae", listOf("Reggae", "Blues", "Jazz", "Soul")),
    Trivia("‘Shake It Off’ is a song by…", "Taylor Swift", listOf("Taylor Swift", "Adele", "Katy Perry", "Rihanna")),
    Trivia("What does DJ stand for?", "Disc Jockey", listOf("Disc Jockey", "Digital Jam", "Dance Jockey", "Drum Jam")),
)

@Composable
fun MusicTriviaGame(onBack: () -> Unit) {
    GameScaffold(title = "Music Trivia", onBack = onBack) { modifier ->
        val quiz = remember { TRIVIA.shuffled() }
        var index by remember { mutableIntStateOf(0) }
        var score by remember { mutableIntStateOf(0) }
        var picked by remember { mutableStateOf<String?>(null) }

        val done = index >= quiz.size
        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (done) "Final score" else "Question ${index + 1} / ${quiz.size}",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(4.dp))
            Text("Score: $score", style = MaterialTheme.typography.titleMedium, color = Teal, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            if (done) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("$score / ${quiz.size}", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Coral)
                }
                Button(
                    onClick = { index = 0; score = 0; picked = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Play again") }
                return@GameScaffold
            }

            val item = quiz[index]
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceHigh).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.q, style = MaterialTheme.typography.titleLarge, color = OnSurfaceLight, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(18.dp))

            item.options.forEach { option ->
                val bg = when {
                    picked == null -> SurfaceHigh
                    option == item.answer -> Teal
                    option == picked -> Coral
                    else -> SurfaceHigh
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bg)
                        .clickable(enabled = picked == null) {
                            picked = option
                            if (option == item.answer) score++
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Text(
                        option,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (picked == null) OnSurfaceLight else OnAccent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (picked != null) {
                Button(
                    onClick = { index++; picked = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text(if (index == quiz.size - 1) "See score" else "Next") }
            }
        }
    }
}
