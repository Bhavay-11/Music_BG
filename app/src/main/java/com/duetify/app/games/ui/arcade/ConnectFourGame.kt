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
import androidx.compose.ui.Alignment
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

private const val ROWS = 6
private const val COLS = 7

private fun winnerOf(b: List<Char?>): Char? {
    val dirs = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
    for (r in 0 until ROWS) for (c in 0 until COLS) {
        val cell = b[r * COLS + c] ?: continue
        for ((dr, dc) in dirs) {
            var k = 1
            while (k < 4) {
                val nr = r + dr * k
                val nc = c + dc * k
                if (nr !in 0 until ROWS || nc !in 0 until COLS) break
                if (b[nr * COLS + nc] != cell) break
                k++
            }
            if (k == 4) return cell
        }
    }
    return null
}

@Composable
fun ConnectFourGame(onBack: () -> Unit) {
    GameScaffold(title = "Connect Four", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(List<Char?>(ROWS * COLS) { null }) }
        var redTurn by remember { mutableStateOf(true) }

        val win = winnerOf(board)
        val full = board.none { it == null }
        val status = when {
            win == 'R' -> "Red wins! 🔴"
            win == 'Y' -> "Yellow wins! 🟡"
            full -> "Draw — new game?"
            else -> if (redTurn) "Red's turn" else "Yellow's turn"
        }

        fun drop(col: Int) {
            if (win != null) return
            for (r in ROWS - 1 downTo 0) {
                if (board[r * COLS + col] == null) {
                    board = board.toMutableList().also { it[r * COLS + col] = if (redTurn) 'R' else 'Y' }
                    redTurn = !redTurn
                    return
                }
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                status,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (win != null) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceContainer).padding(6.dp),
            ) {
                Column {
                    for (r in 0 until ROWS) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (c in 0 until COLS) {
                                Disc(mark = board[r * COLS + c], modifier = Modifier.weight(1f)) { drop(c) }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { board = List(ROWS * COLS) { null }; redTurn = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap a column to drop your disc. Two players, one phone.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Disc(mark: Char?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = when (mark) {
        'R' -> Coral
        'Y' -> Teal
        else -> SurfaceHigh
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
    )
}
