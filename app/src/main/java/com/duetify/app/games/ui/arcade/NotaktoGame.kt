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

private val LINES = listOf(
    intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
    intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
    intArrayOf(0, 4, 8), intArrayOf(2, 4, 6),
)

private fun madeLine(filled: List<Boolean>): Boolean =
    LINES.any { line -> line.all { filled[it] } }

@Composable
fun NotaktoGame(onBack: () -> Unit) {
    GameScaffold(title = "Notakto", onBack = onBack) { modifier ->
        var filled by remember { mutableStateOf(List(9) { false }) }
        var player by remember { mutableIntStateOf(1) }
        var loser by remember { mutableIntStateOf(0) }

        fun tap(index: Int) {
            if (loser != 0 || filled[index]) return
            val next = filled.toMutableList().also { it[index] = true }
            filled = next
            if (madeLine(next)) {
                loser = player // In misère play, completing a line LOSES.
            } else {
                player = if (player == 1) 2 else 1
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (loser != 0) "Player $loser made a line and loses! Player ${if (loser == 1) 2 else 1} wins 🎉" else "Player $player's turn",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (loser != 0) Coral else OnSurfaceLight,
            )
            Spacer(Modifier.height(6.dp))
            Text("Rule: make three in a row and you LOSE.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(20.dp))
            for (r in 0 until 3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in 0 until 3) {
                        val index = r * 3 + c
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceHigh)
                                .clickable(enabled = !filled[index] && loser == 0) { tap(index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (filled[index]) Text("✕", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Coral)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { filled = List(9) { false }; player = 1; loser = 0 },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
        }
    }
}
