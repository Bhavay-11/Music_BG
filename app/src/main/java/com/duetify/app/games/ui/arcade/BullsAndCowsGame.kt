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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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

private const val LEN = 4

private fun secretDigits(): List<Int> = (0..9).shuffled().take(LEN)

private data class BcGuess(val digits: List<Int>, val bulls: Int, val cows: Int)

private fun bullsCows(guess: List<Int>, secret: List<Int>): Pair<Int, Int> {
    val bulls = guess.indices.count { guess[it] == secret[it] }
    var total = 0
    for (d in 0..9) total += minOf(guess.count { it == d }, secret.count { it == d })
    return bulls to (total - bulls)
}

@Composable
fun BullsAndCowsGame(onBack: () -> Unit) {
    GameScaffold(title = "Bulls and Cows", onBack = onBack) { modifier ->
        var secret by remember { mutableStateOf(secretDigits()) }
        var current by remember { mutableStateOf(listOf<Int>()) }
        var history by remember { mutableStateOf(listOf<BcGuess>()) }

        val solved = history.lastOrNull()?.bulls == LEN

        fun addDigit(d: Int) {
            if (solved) return
            if (current.size < LEN) current = current + d
        }
        fun back() { if (current.isNotEmpty()) current = current.dropLast(1) }
        fun submit() {
            if (solved || current.size != LEN) return
            val (b, c) = bullsCows(current, secret)
            history = history + BcGuess(current, b, c)
            current = emptyList()
        }
        fun reset() { secret = secretDigits(); current = emptyList(); history = emptyList() }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (solved) "Solved in ${history.size}! 🎉" else "Crack the ${LEN}-digit code (all different)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (solved) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(10.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                history.forEach { g ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(g.digits.joinToString(" "), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                        Spacer(Modifier.weight(1f))
                        Text("🐂 ${g.bulls}", color = Teal, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Text("🐄 ${g.cows}", color = OnSurfaceVariantPink, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = current.joinToString(" ").ifEmpty { "– – – –" },
                modifier = Modifier.fillMaxWidth(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Coral,
            )
            Spacer(Modifier.height(10.dp))
            if (!solved) {
                (0..9).chunked(5).forEach { rowDigits ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowDigits.forEach { d ->
                            DigitKey("$d", Modifier.weight(1f)) { addDigit(d) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { back() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurfaceLight),
                    ) { Text("Delete") }
                    Button(
                        onClick = { submit() },
                        enabled = current.size == LEN,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                    ) { Text("Guess") }
                }
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
private fun DigitKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
    }
}
