package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlinx.coroutines.delay

private const val START = 21

@Composable
fun NimGame(onBack: () -> Unit) {
    GameScaffold(title = "Nim · Don't Take the Last", onBack = onBack) { modifier ->
        var pile by remember { mutableIntStateOf(START) }
        var yourTurn by remember { mutableStateOf(true) }
        var winner by remember { mutableStateOf<String?>(null) }

        fun take(n: Int, byYou: Boolean) {
            if (winner != null) return
            val t = n.coerceAtMost(pile)
            pile -= t
            if (pile == 0) { winner = if (byYou) "Robin" else "You"; return } // taker of last loses
            yourTurn = !byYou
        }

        // Bot move: try to leave pile ≡ 1 (mod 4) so you're forced to take the last.
        LaunchedEffect(yourTurn, winner) {
            if (!yourTurn && winner == null) {
                delay(700)
                val target = (1..3).firstOrNull { (pile - it) % 4 == 1 && pile - it >= 0 } ?: 1
                take(target, byYou = false)
            }
        }

        fun reset() { pile = START; yourTurn = true; winner = null }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = winner?.let { "$it takes the last — ${if (it == "You") "Robin" else "You"} win! 🎉" }
                    ?: if (yourTurn) "Your turn" else "Robin is thinking…",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (winner != null) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Sticks laid out in rows of 7.
                    (0 until pile).chunked(7).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { _ ->
                                Box(modifier = Modifier.padding(3.dp).size(width = 10.dp, height = 44.dp).clip(RoundedCornerShape(4.dp)).background(Coral))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("$pile left", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                }
            }
            Text("Whoever takes the last stick loses.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(12.dp))
            if (winner == null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1, 2, 3).forEach { n ->
                        Button(
                            onClick = { take(n, byYou = true) },
                            enabled = yourTurn && pile >= n,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                        ) { Text("Take $n") }
                    }
                }
            } else {
                Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("Play again") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
