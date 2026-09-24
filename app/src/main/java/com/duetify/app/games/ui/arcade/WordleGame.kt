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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private val ANSWERS = listOf(
    "MUSIC", "PIANO", "CHORD", "TEMPO", "LYRIC", "SOUND", "TUNES", "BEATS", "VINYL", "RADIO",
    "HEART", "LOVER", "DANCE", "SMILE", "LIGHT", "DREAM", "HAPPY", "SUGAR", "HONEY", "CANDY",
    "OCEAN", "BEACH", "PLANT", "RIVER", "STONE", "CLOUD", "STORM", "FLAME", "SPARK", "GLOWS",
)
private val AMBER = Color(0xFFF2C14E)
private val GRAY = Color(0xFF3A3350)

private fun statuses(guess: String, answer: String): List<Int> {
    val res = IntArray(5) { 0 }
    val counts = HashMap<Char, Int>()
    for (ch in answer) counts[ch] = (counts[ch] ?: 0) + 1
    for (i in 0 until 5) if (guess[i] == answer[i]) { res[i] = 2; counts[guess[i]] = counts[guess[i]]!! - 1 }
    for (i in 0 until 5) if (res[i] == 0) {
        val ch = guess[i]
        if ((counts[ch] ?: 0) > 0) { res[i] = 1; counts[ch] = counts[ch]!! - 1 }
    }
    return res.toList()
}

@Composable
fun WordleGame(onBack: () -> Unit) {
    GameScaffold(title = "Word Guess", onBack = onBack) { modifier ->
        var answer by remember { mutableStateOf(ANSWERS.random()) }
        var guesses by remember { mutableStateOf(listOf<String>()) }
        var current by remember { mutableStateOf("") }

        val won = guesses.lastOrNull() == answer
        val over = won || guesses.size >= 6

        val keyState = remember(guesses, answer) {
            val m = HashMap<Char, Int>()
            guesses.forEach { g ->
                val st = statuses(g, answer)
                g.forEachIndexed { i, ch -> m[ch] = maxOf(m[ch] ?: -1, st[i]) }
            }
            m
        }

        fun type(ch: Char) { if (!over && current.length < 5) current += ch }
        fun back() { if (current.isNotEmpty()) current = current.dropLast(1) }
        fun enter() {
            if (over || current.length != 5) return
            guesses = guesses + current
            current = ""
        }
        fun reset() { answer = ANSWERS.random(); guesses = emptyList(); current = "" }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    won -> "Solved in ${guesses.size}! 🎉"
                    over -> "Out of guesses — it was $answer"
                    else -> "Guess the 5-letter word"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (won) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(12.dp))
            // 6 rows.
            for (row in 0 until 6) {
                val guess = guesses.getOrNull(row)
                val text = guess ?: if (row == guesses.size) current else ""
                val st = guess?.let { statuses(it, answer) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0 until 5) {
                        val ch = text.getOrNull(i)
                        val bg = when {
                            st == null -> SurfaceHigh
                            st[i] == 2 -> Teal
                            st[i] == 1 -> AMBER
                            else -> GRAY
                        }
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (ch != null) Text("$ch", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (st == null) OnSurfaceLight else OnAccent)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.weight(1f))

            if (over) {
                Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New word") }
            } else {
                listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEachIndexed { ri, rowKeys ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (ri == 2) KeyBox("⌫", Modifier.weight(1.5f), Coral) { back() }
                        rowKeys.forEach { ch ->
                            val bg = when (keyState[ch]) { 2 -> Teal; 1 -> AMBER; 0 -> GRAY; else -> SurfaceHigh }
                            KeyBox("$ch", Modifier.weight(1f), bg) { type(ch) }
                        }
                        if (ri == 2) KeyBox("⏎", Modifier.weight(1.5f), Coral) { enter() }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun KeyBox(label: String, modifier: Modifier = Modifier, bg: Color, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(6.dp)).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = OnSurfaceLight, fontSize = 15.sp)
    }
}
