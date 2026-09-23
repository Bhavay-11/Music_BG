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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.Lavender
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private val WIN_LINES = listOf(
    intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
    intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
    intArrayOf(0, 4, 8), intArrayOf(2, 4, 6),
)

private fun winner(board: List<Char?>): Char? {
    for (line in WIN_LINES) {
        val (a, b, c) = Triple(board[line[0]], board[line[1]], board[line[2]])
        if (a != null && a == b && b == c) return a
    }
    return null
}

@Composable
fun TicTacToeGame(onBack: () -> Unit) {
    GameScaffold(title = "Tic-Tac-Toe", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(List<Char?>(9) { null }) }
        var xTurn by remember { mutableStateOf(true) }

        val win = winner(board)
        val full = board.all { it != null }
        val status = when {
            win != null -> "$win wins! 🎉"
            full -> "Draw — play again?"
            else -> "${if (xTurn) "X" else "O"} to move"
        }

        fun tap(index: Int) {
            if (board[index] != null || win != null) return
            board = board.toMutableList().also { it[index] = if (xTurn) 'X' else 'O' }
            xTurn = !xTurn
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (win != null) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(20.dp))
            for (r in 0 until 3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (col in 0 until 3) {
                        val index = r * 3 + col
                        Cell(mark = board[index], modifier = Modifier.weight(1f)) { tap(index) }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { board = List(9) { null }; xTurn = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Two players, one phone — X and O take turns.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
        }
    }
}

@Composable
private fun Cell(mark: Char?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceHigh)
            .clickable(enabled = mark == null, onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (mark != null) {
            Text(
                text = mark.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (mark == 'X') Coral else Lavender,
            )
        }
    }
}
