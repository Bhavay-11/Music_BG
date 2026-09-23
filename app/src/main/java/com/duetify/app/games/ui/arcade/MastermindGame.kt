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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlin.random.Random

private val PEGS = listOf(
    Color(0xFFE5484D), Color(0xFF2FBF71), Color(0xFF4C7DF0),
    Color(0xFFF2C14E), Color(0xFFB86BFF), Color(0xFF23C4C4),
)
private const val CODE_LEN = 4
private const val MAX_GUESSES = 10

private data class Guess(val code: List<Int>, val black: Int, val white: Int)

private fun secretCode(): List<Int> = List(CODE_LEN) { Random.nextInt(PEGS.size) }

private fun score(guess: List<Int>, secret: List<Int>): Pair<Int, Int> {
    val black = guess.indices.count { guess[it] == secret[it] }
    var total = 0
    for (color in PEGS.indices) {
        total += minOf(guess.count { it == color }, secret.count { it == color })
    }
    return black to (total - black)
}

@Composable
fun MastermindGame(onBack: () -> Unit) {
    GameScaffold(title = "Mastermind", onBack = onBack) { modifier ->
        var secret by remember { mutableStateOf(secretCode()) }
        var current by remember { mutableStateOf(List<Int?>(CODE_LEN) { null }) }
        var history by remember { mutableStateOf(listOf<Guess>()) }

        val solved = history.lastOrNull()?.black == CODE_LEN
        val outOfGuesses = history.size >= MAX_GUESSES && !solved
        val over = solved || outOfGuesses

        fun addPeg(color: Int) {
            if (over) return
            val i = current.indexOfFirst { it == null }
            if (i >= 0) current = current.toMutableList().also { it[i] = color }
        }
        fun submit() {
            if (over || current.any { it == null }) return
            val g = current.map { it!! }
            val (b, w) = score(g, secret)
            history = history + Guess(g, b, w)
            current = List(CODE_LEN) { null }
        }
        fun reset() {
            secret = secretCode(); current = List(CODE_LEN) { null }; history = emptyList()
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    solved -> "Cracked it in ${history.size}! 🎉"
                    outOfGuesses -> "Out of guesses — code shown below"
                    else -> "Guess ${history.size + 1} / $MAX_GUESSES"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (solved) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(10.dp))

            // History (scrolls).
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                history.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        g.code.forEach { Peg(PEGS[it]) }
                        Spacer(Modifier.size(10.dp))
                        Text("● ${g.black}", color = Teal, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.size(8.dp))
                        Text("○ ${g.white}", color = OnSurfaceVariantPink, fontWeight = FontWeight.Bold)
                    }
                }
                if (outOfGuesses) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Code: ", color = OnSurfaceVariantPink)
                        secret.forEach { Peg(PEGS[it]) }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (!over) {
                // Current guess slots.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    current.forEach { c ->
                        Box(
                            modifier = Modifier.padding(3.dp).size(34.dp).clip(CircleShape).background(c?.let { PEGS[it] } ?: SurfaceHigh),
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Button(
                        onClick = { submit() },
                        enabled = current.none { it == null },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                    ) { Text("Guess") }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PEGS.forEachIndexed { i, color ->
                        Box(
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(color).clickable { addPeg(i) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("● right spot · ○ right colour, wrong spot", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            } else {
                Button(
                    onClick = { reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Play again") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Peg(color: Color) {
    Box(modifier = Modifier.padding(2.dp).size(24.dp).clip(CircleShape).background(color))
}
