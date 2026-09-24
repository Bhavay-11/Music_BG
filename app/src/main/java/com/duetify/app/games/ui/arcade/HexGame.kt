package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private const val HN = 6

private fun hexNeighbors(i: Int): List<Int> {
    val r = i / HN; val c = i % HN
    return listOf(r to c - 1, r to c + 1, r - 1 to c, r - 1 to c + 1, r + 1 to c - 1, r + 1 to c)
        .filter { (rr, cc) -> rr in 0 until HN && cc in 0 until HN }
        .map { (rr, cc) -> rr * HN + cc }
}

// 'C' connects top-bottom, 'T' connects left-right.
private fun hexWins(board: List<Char?>, p: Char): Boolean {
    val visited = BooleanArray(HN * HN)
    val stack = ArrayDeque<Int>()
    if (p == 'C') {
        for (c in 0 until HN) if (board[c] == p) { stack.addLast(c); visited[c] = true }
    } else {
        for (r in 0 until HN) { val idx = r * HN; if (board[idx] == p) { stack.addLast(idx); visited[idx] = true } }
    }
    while (stack.isNotEmpty()) {
        val cur = stack.removeLast()
        val r = cur / HN; val c = cur % HN
        if (p == 'C' && r == HN - 1) return true
        if (p == 'T' && c == HN - 1) return true
        for (nb in hexNeighbors(cur)) if (!visited[nb] && board[nb] == p) { visited[nb] = true; stack.addLast(nb) }
    }
    return false
}

@Composable
fun HexGame(onBack: () -> Unit) {
    GameScaffold(title = "Hex", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(List<Char?>(HN * HN) { null }) }
        var turn by remember { mutableStateOf('C') }
        var winner by remember { mutableStateOf<Char?>(null) }

        fun tap(i: Int) {
            if (winner != null || board[i] != null) return
            val nb = board.toMutableList().also { it[i] = turn }
            board = nb
            if (hexWins(nb, turn)) winner = turn else turn = if (turn == 'C') 'T' else 'C'
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    winner == 'C' -> "Coral connects top–bottom! 🎉"
                    winner == 'T' -> "Teal connects left–right! 🎉"
                    turn == 'C' -> "Coral's turn (join top & bottom)"
                    else -> "Teal's turn (join left & right)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = winner?.let { if (it == 'C') Coral else Teal } ?: OnSurfaceLight,
            )
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column {
                    for (r in 0 until HN) {
                        Row {
                            Spacer(Modifier.width((r * 16).dp))
                            for (c in 0 until HN) {
                                val idx = r * HN + c
                                val cell = board[idx]
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(when (cell) { 'C' -> Coral; 'T' -> Teal; else -> SurfaceHigh })
                                        .clickable(enabled = cell == null && winner == null) { tap(idx) },
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { board = List(HN * HN) { null }; turn = 'C'; winner = null },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Coral joins top to bottom; Teal joins left to right. No draws possible.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}
