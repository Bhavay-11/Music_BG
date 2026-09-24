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

private const val B = 3 // boxes per side (3x3 boxes, 4x4 dots)

@Composable
fun DotsAndBoxesGame(onBack: () -> Unit) {
    GameScaffold(title = "Dots and Boxes", onBack = onBack) { modifier ->
        // h[row 0..B][col 0..B-1], v[row 0..B-1][col 0..B]
        var h by remember { mutableStateOf(List((B + 1) * B) { false }) }
        var v by remember { mutableStateOf(List(B * (B + 1)) { false }) }
        var owners by remember { mutableStateOf(List(B * B) { 0 }) }
        var player by remember { mutableIntStateOf(1) }

        fun boxComplete(hh: List<Boolean>, vv: List<Boolean>, br: Int, bc: Int): Boolean {
            val top = hh[br * B + bc]
            val bottom = hh[(br + 1) * B + bc]
            val left = vv[br * (B + 1) + bc]
            val right = vv[br * (B + 1) + bc + 1]
            return top && bottom && left && right
        }

        fun afterDraw(nh: List<Boolean>, nv: List<Boolean>) {
            var gained = 0
            val no = owners.toMutableList()
            for (br in 0 until B) for (bc in 0 until B) {
                val idx = br * B + bc
                if (no[idx] == 0 && boxComplete(nh, nv, br, bc)) { no[idx] = player; gained++ }
            }
            h = nh; v = nv; owners = no
            if (gained == 0) player = if (player == 1) 2 else 1
        }

        val over = h.all { it } && v.all { it }
        val s1 = owners.count { it == 1 }
        val s2 = owners.count { it == 2 }

        fun drawH(i: Int) { if (!over && !h[i]) afterDraw(h.toMutableList().also { it[i] = true }, v) }
        fun drawV(i: Int) { if (!over && !v[i]) afterDraw(h, v.toMutableList().also { it[i] = true }) }
        fun reset() { h = List((B + 1) * B) { false }; v = List(B * (B + 1)) { false }; owners = List(B * B) { 0 }; player = 1 }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("P1: $s1", color = Coral, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    when { over && s1 > s2 -> "Player 1 wins!"; over && s2 > s1 -> "Player 2 wins!"; over -> "Tie!"; else -> "Player $player's turn" },
                    color = if (over) Teal else OnSurfaceLight, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text("P2: $s2", color = Teal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column {
                    for (r in 0..B) {
                        // Dot row: dot, hEdge, dot, hEdge, ...
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Dot()
                            for (c in 0 until B) {
                                HEdge(drawn = h[r * B + c]) { drawH(r * B + c) }
                                Dot()
                            }
                        }
                        if (r < B) {
                            // Edge row: vEdge, box, vEdge, box, ...
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VEdge(drawn = v[r * (B + 1)]) { drawV(r * (B + 1)) }
                                for (c in 0 until B) {
                                    BoxCell(owner = owners[r * B + c])
                                    VEdge(drawn = v[r * (B + 1) + c + 1]) { drawV(r * (B + 1) + c + 1) }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            Spacer(Modifier.height(8.dp))
            Text("Complete a box to score and go again.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(8.dp))
        }
    }
}

private val DOT = 16.dp
private val LEN = 60.dp
private val THICK = 16.dp

@Composable
private fun Dot() {
    Box(modifier = Modifier.size(DOT).clip(CircleShape).background(OnSurfaceVariantPink))
}

@Composable
private fun HEdge(drawn: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(width = LEN, height = THICK).clickable(enabled = !drawn, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(width = LEN - 6.dp, height = 6.dp).clip(RoundedCornerShape(50)).background(if (drawn) OnSurfaceLight else SurfaceHigh))
    }
}

@Composable
private fun VEdge(drawn: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(width = THICK, height = LEN).clickable(enabled = !drawn, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(width = 6.dp, height = LEN - 6.dp).clip(RoundedCornerShape(50)).background(if (drawn) OnSurfaceLight else SurfaceHigh))
    }
}

@Composable
private fun BoxCell(owner: Int) {
    val color = when (owner) { 1 -> Coral.copy(alpha = 0.8f); 2 -> Teal.copy(alpha = 0.8f); else -> Color.Transparent }
    Box(modifier = Modifier.size(width = LEN, height = LEN).clip(RoundedCornerShape(6.dp)).background(color))
}
