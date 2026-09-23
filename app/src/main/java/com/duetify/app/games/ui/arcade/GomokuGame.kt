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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private const val N = 9
private const val NEED = 5

private fun gomokuWinner(b: List<Char?>): Char? {
    val dirs = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
    for (r in 0 until N) for (c in 0 until N) {
        val cell = b[r * N + c] ?: continue
        for ((dr, dc) in dirs) {
            var k = 1
            while (k < NEED) {
                val nr = r + dr * k
                val nc = c + dc * k
                if (nr !in 0 until N || nc !in 0 until N) break
                if (b[nr * N + nc] != cell) break
                k++
            }
            if (k == NEED) return cell
        }
    }
    return null
}

@Composable
fun GomokuGame(onBack: () -> Unit) {
    GameScaffold(title = "Gomoku · Five in a Row", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(List<Char?>(N * N) { null }) }
        var blackTurn by remember { mutableStateOf(true) }

        val win = gomokuWinner(board)
        val full = board.none { it == null }
        val status = when {
            win == 'B' -> "Black wins! ⚫"
            win == 'W' -> "White wins! ⚪"
            full -> "Draw — new game?"
            else -> if (blackTurn) "Black's turn ⚫" else "White's turn ⚪"
        }

        fun place(index: Int) {
            if (win != null || board[index] != null) return
            board = board.toMutableList().also { it[index] = if (blackTurn) 'B' else 'W' }
            blackTurn = !blackTurn
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(status, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (win != null) Teal else OnSurfaceLight)
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .padding(4.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until N) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (c in 0 until N) {
                                Stone(mark = board[r * N + c], modifier = Modifier.fillMaxHeight().weight(1f)) { place(r * N + c) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { board = List(N * N) { null }; blackTurn = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Get five in a row. Two players, one phone.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Stone(mark: Char?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(3.dp),
    ) {
        if (mark != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (mark == 'B') OnSurfaceLight else Coral),
            )
        }
    }
}
