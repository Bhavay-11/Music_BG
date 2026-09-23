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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private const val SZ = 8
private val DIRS = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)

private fun initialBoard(): List<Char?> {
    val b = MutableList<Char?>(SZ * SZ) { null }
    b[3 * SZ + 3] = 'T'; b[3 * SZ + 4] = 'C'
    b[4 * SZ + 3] = 'C'; b[4 * SZ + 4] = 'T'
    return b
}

private fun flipsFor(b: List<Char?>, idx: Int, p: Char): List<Int> {
    if (b[idx] != null) return emptyList()
    val r = idx / SZ; val c = idx % SZ
    val opp = if (p == 'C') 'T' else 'C'
    val out = mutableListOf<Int>()
    for ((dr, dc) in DIRS) {
        val line = mutableListOf<Int>()
        var nr = r + dr; var nc = c + dc
        while (nr in 0 until SZ && nc in 0 until SZ && b[nr * SZ + nc] == opp) {
            line += nr * SZ + nc; nr += dr; nc += dc
        }
        if (line.isNotEmpty() && nr in 0 until SZ && nc in 0 until SZ && b[nr * SZ + nc] == p) out += line
    }
    return out
}

private fun validMoves(b: List<Char?>, p: Char): Set<Int> =
    (0 until SZ * SZ).filter { flipsFor(b, it, p).isNotEmpty() }.toSet()

@Composable
fun ReversiGame(onBack: () -> Unit) {
    GameScaffold(title = "Reversi", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(initialBoard()) }
        var turn by remember { mutableStateOf('C') }

        val moves = validMoves(board, turn)
        val oppMoves = validMoves(board, if (turn == 'C') 'T' else 'C')
        val over = moves.isEmpty() && oppMoves.isEmpty()
        val coral = board.count { it == 'C' }
        val teal = board.count { it == 'T' }

        fun play(idx: Int) {
            val flips = flipsFor(board, idx, turn)
            if (flips.isEmpty()) return
            val nb = board.toMutableList()
            nb[idx] = turn
            flips.forEach { nb[it] = turn }
            board = nb
            val next = if (turn == 'C') 'T' else 'C'
            // Skip the next player if they have no legal move.
            turn = if (validMoves(nb, next).isNotEmpty()) next else turn
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("● $coral", color = Coral, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    when {
                        over && coral > teal -> "Coral wins!"
                        over && teal > coral -> "Teal wins!"
                        over -> "Tie!"
                        turn == 'C' -> "Coral's turn"
                        else -> "Teal's turn"
                    },
                    color = if (over) OnSurfaceLight else OnSurfaceVariantPink,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text("● $teal", color = Teal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1E6B4F)).padding(3.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until SZ) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (c in 0 until SZ) {
                                val idx = r * SZ + c
                                Cell(mark = board[idx], hint = !over && idx in moves, modifier = Modifier.fillMaxHeight().weight(1f)) { play(idx) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { board = initialBoard(); turn = 'C' },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("New game") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Cell(mark: Char?, hint: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.padding(1.dp).background(Color(0xFF2A8F6B)).clickable(enabled = mark == null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            mark != null -> Box(modifier = Modifier.fillMaxSize().padding(3.dp).clip(CircleShape).background(if (mark == 'C') Coral else Teal))
            hint -> Box(modifier = Modifier.fillMaxSize(0.28f).clip(CircleShape).background(SurfaceHigh))
        }
    }
}
