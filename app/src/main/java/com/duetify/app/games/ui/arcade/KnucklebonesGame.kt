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
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlin.random.Random

private fun columnScore(col: List<Int>): Int =
    col.groupingBy { it }.eachCount().entries.sumOf { (v, n) -> v * n * n }

private fun gridScore(g: List<List<Int>>): Int = g.sumOf { columnScore(it) }

@Composable
fun KnucklebonesGame(onBack: () -> Unit) {
    GameScaffold(title = "Knucklebones", onBack = onBack) { modifier ->
        var p1 by remember { mutableStateOf(List(3) { listOf<Int>() }) }
        var p2 by remember { mutableStateOf(List(3) { listOf<Int>() }) }
        var current by remember { mutableIntStateOf(1) }
        var die by remember { mutableIntStateOf(0) }

        val over = p1.all { it.size == 3 } || p2.all { it.size == 3 }
        val s1 = gridScore(p1)
        val s2 = gridScore(p2)

        fun roll() { if (die == 0 && !over) die = Random.nextInt(1, 7) }

        fun place(col: Int) {
            if (die == 0 || over) return
            val myGrid = if (current == 1) p1 else p2
            if (myGrid[col].size >= 3) return
            val newMy = myGrid.mapIndexed { i, c -> if (i == col) c + die else c }
            val oppGrid = if (current == 1) p2 else p1
            val newOpp = oppGrid.mapIndexed { i, c -> if (i == col) c.filter { it != die } else c }
            if (current == 1) { p1 = newMy; p2 = newOpp } else { p2 = newMy; p1 = newOpp }
            die = 0
            current = if (current == 1) 2 else 1
        }
        fun reset() { p1 = List(3) { listOf() }; p2 = List(3) { listOf() }; current = 1; die = 0 }

        Column(modifier = modifier) {
            Spacer(Modifier.height(6.dp))
            PlayerGrid("Player 2", p2, active = current == 2 && !over, score = s2, tappable = current == 2 && die != 0 && !over) { place(it) }
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (over) {
                        Text(
                            if (s1 == s2) "Tie!" else "Player ${if (s1 > s2) 1 else 2} wins!",
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Teal,
                        )
                    } else {
                        Text(if (die != 0) "$die" else "🎲", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Coral)
                        Text(
                            if (die == 0) "Player $current: roll" else "Player $current: tap a column",
                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariantPink,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            PlayerGrid("Player 1", p1, active = current == 1 && !over, score = s1, tappable = current == 1 && die != 0 && !over) { place(it) }
            Spacer(Modifier.height(12.dp))
            if (over) {
                Button(onClick = { reset() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("New game") }
            } else {
                Button(onClick = { roll() }, enabled = die == 0, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text("Roll die") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlayerGrid(name: String, grid: List<List<Int>>, active: Boolean, score: Int, tappable: Boolean, onColumn: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (active) SurfaceHigh else SurfaceContainer).padding(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(name, style = MaterialTheme.typography.labelLarge, color = if (active) Coral else OnSurfaceVariantPink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("$score", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (col in 0 until 3) {
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(SurfaceContainer.copy(alpha = 0.6f)).clickable(enabled = tappable) { onColumn(col) }.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (slot in 0 until 3) {
                        val v = grid[col].getOrNull(slot)
                        Box(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(if (v != null) Coral else SurfaceHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (v != null) Text("$v", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnAccent)
                        }
                    }
                }
            }
        }
    }
}
