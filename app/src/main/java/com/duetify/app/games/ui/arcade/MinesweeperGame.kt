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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

private const val W = 9
private const val MINES = 10

private fun neighbors(i: Int): List<Int> {
    val r = i / W; val c = i % W
    val out = mutableListOf<Int>()
    for (dr in -1..1) for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val nr = r + dr; val nc = c + dc
        if (nr in 0 until W && nc in 0 until W) out.add(nr * W + nc)
    }
    return out
}

private fun newMines(): Set<Int> {
    val s = mutableSetOf<Int>()
    while (s.size < MINES) s.add((0 until W * W).random())
    return s
}

@Composable
fun MinesweeperGame(onBack: () -> Unit) {
    GameScaffold(title = "Minesweeper", onBack = onBack) { modifier ->
        var mines by remember { mutableStateOf(newMines()) }
        var revealed by remember { mutableStateOf(setOf<Int>()) }
        var flagged by remember { mutableStateOf(setOf<Int>()) }
        var boom by remember { mutableStateOf(false) }
        var flagMode by remember { mutableStateOf(false) }

        fun count(i: Int): Int = neighbors(i).count { it in mines }
        val won = !boom && revealed.size == W * W - MINES

        fun revealFrom(start: Int) {
            val toReveal = mutableSetOf<Int>()
            val stack = ArrayDeque<Int>()
            stack.addLast(start)
            while (stack.isNotEmpty()) {
                val cur = stack.removeLast()
                if (cur in toReveal || cur in revealed || cur in flagged) continue
                toReveal.add(cur)
                if (count(cur) == 0) neighbors(cur).forEach { if (it !in toReveal) stack.addLast(it) }
            }
            revealed = revealed + toReveal
        }

        fun tap(i: Int) {
            if (boom || won || i in revealed) return
            if (flagMode) {
                flagged = if (i in flagged) flagged - i else flagged + i
                return
            }
            if (i in flagged) return
            if (i in mines) { boom = true; return }
            revealFrom(i)
        }
        fun reset() { mines = newMines(); revealed = emptySet(); flagged = emptySet(); boom = false }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when { won -> "Cleared! 🎉"; boom -> "Boom! 💥"; else -> "Mines: $MINES · Flags: ${flagged.size}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (won) Teal else if (boom) Coral else OnSurfaceVariantPink,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (flagMode) Coral else SurfaceHigh).clickable { flagMode = !flagMode }.padding(horizontal = 14.dp, vertical = 8.dp),
                ) { Text(if (flagMode) "🚩 Flag" else "⛏ Dig", color = if (flagMode) OnAccent else OnSurfaceLight, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(SurfaceContainer).padding(3.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until W) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (c in 0 until W) {
                                val i = r * W + c
                                Cell(
                                    revealed = i in revealed || (boom && i in mines),
                                    isMine = i in mines,
                                    flagged = i in flagged,
                                    count = count(i),
                                    modifier = Modifier.fillMaxHeight().weight(1f),
                                ) { tap(i) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Toggle Dig/Flag, then tap. Numbers show nearby mines.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

private val NUM_COLORS = listOf(
    Color.Transparent, Color(0xFF4C7DF0), Color(0xFF2FBF71), Color(0xFFE5484D),
    Color(0xFFB86BFF), Color(0xFFE8965A), Color(0xFF23C4C4), Color(0xFFF2C14E), Color(0xFFB2506E),
)

@Composable
private fun Cell(revealed: Boolean, isMine: Boolean, flagged: Boolean, count: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.padding(1.dp).clip(RoundedCornerShape(4.dp)).background(if (revealed) SurfaceContainer.copy(alpha = 0.4f) else SurfaceHigh).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            revealed && isMine -> Text("💣", fontSize = 14.sp)
            revealed && count > 0 -> Text("$count", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NUM_COLORS[count])
            !revealed && flagged -> Text("🚩", fontSize = 14.sp)
        }
    }
}
