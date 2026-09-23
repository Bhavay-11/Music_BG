package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.duetify.app.ui.theme.Lavender
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private const val SIZE = 6
private const val TARGET = 5

private fun hasFiveInRow(b: List<Char?>): Boolean {
    val dirs = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
    for (r in 0 until SIZE) for (c in 0 until SIZE) {
        val cell = b[r * SIZE + c] ?: continue
        for ((dr, dc) in dirs) {
            var k = 1
            while (k < TARGET) {
                val nr = r + dr * k
                val nc = c + dc * k
                if (nr !in 0 until SIZE || nc !in 0 until SIZE) break
                if (b[nr * SIZE + nc] != cell) break
                k++
            }
            if (k == TARGET) return true
        }
    }
    return false
}

@Composable
fun OrderAndChaosGame(onBack: () -> Unit) {
    GameScaffold(title = "Order and Chaos", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(List<Char?>(SIZE * SIZE) { null }) }
        var orderTurn by remember { mutableStateOf(true) } // Order moves first.
        var symbol by remember { mutableStateOf('X') }

        val orderWon = hasFiveInRow(board)
        val full = board.none { it == null }
        val chaosWon = full && !orderWon
        val over = orderWon || chaosWon

        fun place(index: Int) {
            if (over || board[index] != null) return
            board = board.toMutableList().also { it[index] = symbol }
            orderTurn = !orderTurn
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    orderWon -> "Order wins — five in a row! 🎉"
                    chaosWon -> "Chaos wins — board filled! 🎉"
                    orderTurn -> "Order's turn (make 5 in a row)"
                    else -> "Chaos's turn (stop the line)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (over) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(10.dp))
            if (!over) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SymbolChip("X", symbol == 'X') { symbol = 'X' }
                    SymbolChip("O", symbol == 'O') { symbol = 'O' }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainer).padding(4.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until SIZE) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (c in 0 until SIZE) {
                                val mark = board[r * SIZE + c]
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceHigh)
                                        .clickable(enabled = mark == null && !over) { place(r * SIZE + c) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (mark != null) {
                                        Text("$mark", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (mark == 'X') Coral else Lavender)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { board = List(SIZE * SIZE) { null }; orderTurn = true; symbol = 'X' },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Both players place either symbol. Order = 5 in a row; Chaos = fill the board.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SymbolChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Coral else SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (selected) OnAccent else OnSurfaceLight)
    }
}
