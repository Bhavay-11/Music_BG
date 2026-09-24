package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private val UTTT_LINES = listOf(
    intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
    intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
    intArrayOf(0, 4, 8), intArrayOf(2, 4, 6),
)

private fun winner3(cells: List<Char?>): Char? {
    for (l in UTTT_LINES) {
        val a = cells[l[0]]
        if (a != null && a == cells[l[1]] && a == cells[l[2]]) return a
    }
    return null
}

@Composable
fun UltimateTicTacToeGame(onBack: () -> Unit) {
    GameScaffold(title = "Ultimate Tic-Tac-Toe", onBack = onBack) { modifier ->
        var boards by remember { mutableStateOf(List(9) { List<Char?>(9) { null } }) }
        var xTurn by remember { mutableStateOf(true) }
        var active by remember { mutableStateOf<Int?>(null) }

        val miniWinners = boards.map { winner3(it) }
        fun decided(b: Int) = miniWinners[b] != null || boards[b].none { it == null }
        val metaWinner = winner3(miniWinners)
        val over = metaWinner != null || (0 until 9).all { decided(it) }

        fun tap(b: Int, i: Int) {
            if (over) return
            val allowed = (active == null || active == b) && !decided(b)
            if (!allowed || boards[b][i] != null) return
            val mark = if (xTurn) 'X' else 'O'
            boards = boards.mapIndexed { bi, bd -> if (bi == b) bd.mapIndexed { ci, cc -> if (ci == i) mark else cc } else bd }
            active = if (decided(i)) null else i
            xTurn = !xTurn
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    metaWinner != null -> "$metaWinner wins the game! 🎉"
                    over -> "Draw!"
                    else -> "${if (xTurn) "X" else "O"} to move" + if (active == null) " (any board)" else ""
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (metaWinner != null) Coral else OnSurfaceLight,
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainer).padding(3.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (br in 0 until 3) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (bc in 0 until 3) {
                                val b = br * 3 + bc
                                MiniBoard(
                                    cells = boards[b],
                                    winner = miniWinners[b],
                                    highlighted = !over && (active == b || (active == null && !decided(b))),
                                    modifier = Modifier.fillMaxHeight().weight(1f),
                                ) { i -> tap(b, i) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { boards = List(9) { List<Char?>(9) { null } }; xTurn = true; active = null },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Your move sends your opponent to the matching board.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MiniBoard(cells: List<Char?>, winner: Char?, highlighted: Boolean, modifier: Modifier = Modifier, onCell: (Int) -> Unit) {
    Box(
        modifier = modifier.padding(2.dp).clip(RoundedCornerShape(6.dp)).background(if (highlighted) SurfaceHigh else SurfaceContainer).padding(2.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 3) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    for (c in 0 until 3) {
                        val i = r * 3 + c
                        val mark = cells[i]
                        Box(
                            modifier = Modifier.fillMaxHeight().weight(1f).padding(1.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceContainer.copy(alpha = 0.7f)).clickable(enabled = mark == null && winner == null) { onCell(i) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (mark != null) Text("$mark", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (mark == 'X') Coral else Lavender)
                        }
                    }
                }
            }
        }
        if (winner != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("$winner", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = if (winner == 'X') Coral else Lavender)
            }
        }
    }
}
