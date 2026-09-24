package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.random.Random

private fun compress(line: List<Int>): Pair<List<Int>, Int> {
    val nz = line.filter { it != 0 }
    val out = mutableListOf<Int>()
    var gained = 0
    var i = 0
    while (i < nz.size) {
        if (i + 1 < nz.size && nz[i] == nz[i + 1]) {
            val v = nz[i] * 2; out.add(v); gained += v; i += 2
        } else { out.add(nz[i]); i++ }
    }
    while (out.size < 4) out.add(0)
    return out to gained
}

private fun transpose(g: List<List<Int>>): List<List<Int>> =
    (0 until 4).map { c -> (0 until 4).map { r -> g[r][c] } }

private fun spawn(g: List<List<Int>>): List<List<Int>> {
    val empties = mutableListOf<Pair<Int, Int>>()
    for (r in 0 until 4) for (c in 0 until 4) if (g[r][c] == 0) empties.add(r to c)
    if (empties.isEmpty()) return g
    val (r, c) = empties[Random.nextInt(empties.size)]
    val v = if (Random.nextInt(10) == 0) 4 else 2
    return g.mapIndexed { ri, row -> if (ri == r) row.mapIndexed { ci, x -> if (ci == c) v else x } else row }
}

private fun tileColor(v: Int): Color = when (v) {
    0 -> Color.Transparent
    2 -> Color(0xFF3A3350)
    4 -> Color(0xFF4A3F63)
    8 -> Color(0xFF6D4B7A)
    16 -> Color(0xFF8A4E77)
    32 -> Color(0xFFB2506E)
    64 -> Color(0xFFD65A64)
    128 -> Color(0xFFE07A5F)
    256 -> Color(0xFFE8965A)
    512 -> Color(0xFFEDB458)
    1024 -> Color(0xFFF2D45C)
    else -> Color(0xFFF7E463)
}

@Composable
fun Game2048(onBack: () -> Unit) {
    GameScaffold(title = "2048", onBack = onBack) { modifier ->
        var grid by remember { mutableStateOf(spawn(spawn(List(4) { List(4) { 0 } }))) }
        var score by remember { mutableIntStateOf(0) }
        var best by remember { mutableIntStateOf(0) }

        fun movesLeft(g: List<List<Int>>): Boolean {
            if (g.any { row -> row.any { it == 0 } }) return true
            for (r in 0 until 4) for (c in 0 until 4) {
                if (c < 3 && g[r][c] == g[r][c + 1]) return true
                if (r < 3 && g[r][c] == g[r + 1][c]) return true
            }
            return false
        }
        val over = !movesLeft(grid)
        val won = grid.any { row -> row.any { it >= 2048 } }

        fun move(dir: Int) {
            if (over) return
            var gained = 0
            var g = grid
            when (dir) {
                0 -> g = g.map { val (l, gg) = compress(it); gained += gg; l } // left
                1 -> g = g.map { val (l, gg) = compress(it.reversed()); gained += gg; l.reversed() } // right
                2 -> { val t = transpose(g).map { val (l, gg) = compress(it); gained += gg; l }; g = transpose(t) } // up
                3 -> { val t = transpose(g).map { val (l, gg) = compress(it.reversed()); gained += gg; l.reversed() }; g = transpose(t) } // down
            }
            if (g != grid) {
                score += gained
                if (score > best) best = score
                grid = spawn(g)
            }
        }
        fun reset() { grid = spawn(spawn(List(4) { List(4) { 0 } })); score = 0 }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Score: $score", style = MaterialTheme.typography.titleMedium, color = Coral, fontWeight = FontWeight.Bold)
                Text(if (won) "2048! Keep going 🎉" else if (over) "No moves left" else "Best: $best", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainer).padding(6.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (r in 0 until 4) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (c in 0 until 4) {
                                val v = grid[r][c]
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)).background(if (v == 0) SurfaceHigh else tileColor(v)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (v != 0) Text("$v", fontSize = if (v < 128) 26.sp else if (v < 1024) 22.sp else 18.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Pad(Icons.Filled.KeyboardArrowUp) { move(2) }
                Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                    Pad(Icons.Filled.KeyboardArrowLeft) { move(0) }
                    Pad(Icons.Filled.KeyboardArrowRight) { move(1) }
                }
                Pad(Icons.Filled.KeyboardArrowDown) { move(3) }
            }
            Spacer(Modifier.height(6.dp))
            if (over) {
                Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Pad(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier.padding(3.dp).size(56.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceHigh).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = OnSurfaceLight, modifier = Modifier.size(30.dp)) }
}
