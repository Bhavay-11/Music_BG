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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.CoralDark
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val GRID = 15

private fun randomFreeCell(snake: List<Int>): Int {
    val free = (0 until GRID * GRID).filter { it !in snake }
    return if (free.isEmpty()) -1 else free[Random.nextInt(free.size)]
}

@Composable
fun SnakeGame(onBack: () -> Unit) {
    GameScaffold(title = "Snake", onBack = onBack) { modifier ->
        var snake by remember { mutableStateOf(listOf(7 * GRID + 7)) }
        val dir = remember { mutableStateOf(0 to 1) }
        var food by remember { mutableStateOf(7 * GRID + 11) }
        var running by remember { mutableStateOf(false) }
        var over by remember { mutableStateOf(false) }
        var score by remember { mutableIntStateOf(0) }

        LaunchedEffect(running) {
            while (running && !over) {
                delay(170)
                val (dr, dc) = dir.value
                val head = snake.first()
                val hr = head / GRID + dr
                val hc = head % GRID + dc
                if (hr !in 0 until GRID || hc !in 0 until GRID) { over = true; running = false; break }
                val nh = hr * GRID + hc
                if (nh in snake) { over = true; running = false; break }
                val ate = nh == food
                val newSnake = listOf(nh) + if (ate) snake else snake.dropLast(1)
                snake = newSnake
                if (ate) {
                    score++
                    food = randomFreeCell(newSnake)
                    if (food == -1) { over = true; running = false; break }
                }
            }
        }

        fun turn(nd: Pair<Int, Int>) {
            val (cr, cc) = dir.value
            if (nd.first == -cr && nd.second == -cc) return // no reversing
            dir.value = nd
        }
        fun start() {
            snake = listOf(7 * GRID + 7); dir.value = 0 to 1; food = 7 * GRID + 11; score = 0; over = false; running = true
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (over) "Game over — score $score" else "Score: $score",
                style = MaterialTheme.typography.titleMedium,
                color = if (over) Coral else OnSurfaceVariantPink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(SurfaceContainer).padding(3.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until GRID) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (c in 0 until GRID) {
                                val idx = r * GRID + c
                                val cellColor = when {
                                    idx == snake.firstOrNull() -> Coral
                                    idx in snake -> CoralDark
                                    idx == food -> Teal
                                    else -> SurfaceHigh
                                }
                                Box(modifier = Modifier.fillMaxHeight().weight(1f).padding(1.dp).clip(RoundedCornerShape(3.dp)).background(cellColor))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))

            if (!running && over || (!running && !over)) {
                Button(
                    onClick = { start() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text(if (over) "Play again" else "Start") }
                Spacer(Modifier.height(10.dp))
            }
            // D-pad.
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                DirBtn(Icons.Filled.KeyboardArrowUp) { turn(-1 to 0) }
                Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                    DirBtn(Icons.Filled.KeyboardArrowLeft) { turn(0 to -1) }
                    DirBtn(Icons.Filled.KeyboardArrowRight) { turn(0 to 1) }
                }
                DirBtn(Icons.Filled.KeyboardArrowDown) { turn(1 to 0) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DirBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(3.dp)
            .size(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = OnSurfaceLight, modifier = Modifier.size(32.dp))
    }
}
