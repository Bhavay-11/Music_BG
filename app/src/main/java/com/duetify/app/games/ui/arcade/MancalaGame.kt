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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.duetify.app.ui.theme.Teal

// Board: 0..5 P1 pits, 6 P1 store, 7..12 P2 pits, 13 P2 store.
private fun freshBoard(): List<Int> = List(14) { if (it == 6 || it == 13) 0 else 4 }

@Composable
fun MancalaGame(onBack: () -> Unit) {
    GameScaffold(title = "Mancala", onBack = onBack) { modifier ->
        var board by remember { mutableStateOf(freshBoard()) }
        var player by remember { mutableIntStateOf(1) }
        var over by remember { mutableStateOf(false) }

        fun sow(start: Int) {
            if (over) return
            val myPits = if (player == 1) 0..5 else 7..12
            if (start !in myPits || board[start] == 0) return
            val b = board.toMutableList()
            var seeds = b[start]; b[start] = 0
            var pos = start
            val skipStore = if (player == 1) 13 else 6
            while (seeds > 0) {
                pos = (pos + 1) % 14
                if (pos == skipStore) continue
                b[pos]++; seeds--
            }
            val ownStore = if (player == 1) 6 else 13
            val extra = pos == ownStore
            if (!extra && pos in myPits && b[pos] == 1) {
                val opposite = 12 - pos
                if (b[opposite] > 0) { b[ownStore] += b[opposite] + 1; b[opposite] = 0; b[pos] = 0 }
            }
            // End check + sweep.
            val p1empty = (0..5).all { b[it] == 0 }
            val p2empty = (7..12).all { b[it] == 0 }
            if (p1empty || p2empty) {
                for (i in 0..5) { b[6] += b[i]; b[i] = 0 }
                for (i in 7..12) { b[13] += b[i]; b[i] = 0 }
                over = true
            } else if (!extra) {
                player = if (player == 1) 2 else 1
            }
            board = b
        }
        fun reset() { board = freshBoard(); player = 1; over = false }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    over && board[6] > board[13] -> "Player 1 wins ${board[6]}–${board[13]}!"
                    over && board[13] > board[6] -> "Player 2 wins ${board[13]}–${board[6]}!"
                    over -> "Tie ${board[6]}–${board[13]}"
                    else -> "Player $player's turn"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (over) Teal else OnSurfaceLight,
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Store(board[13], "P2", active = player == 2 && !over)
                Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(12, 11, 10, 9, 8, 7).forEach { i ->
                            Pit(board[i], enabled = player == 2 && !over && board[i] > 0, p2 = true, modifier = Modifier.weight(1f)) { sow(i) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (0..5).forEach { i ->
                            Pit(board[i], enabled = player == 1 && !over && board[i] > 0, p2 = false, modifier = Modifier.weight(1f)) { sow(i) }
                        }
                    }
                }
                Store(board[6], "P1", active = player == 1 && !over)
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Sow from your side; land in your store to go again; land in an empty pit to capture.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Store(count: Int, label: String, active: Boolean) {
    Column(
        modifier = Modifier.size(width = 46.dp, height = 150.dp).clip(RoundedCornerShape(24.dp)).background(if (active) SurfaceHigh else SurfaceHigh.copy(alpha = 0.5f)).padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("$count", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantPink)
    }
}

@Composable
private fun Pit(count: Int, enabled: Boolean, p2: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(CircleShape)
            .background(if (enabled) (if (p2) Teal else Coral) else SurfaceHigh)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (enabled) OnAccent else OnSurfaceLight)
    }
}
