package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

private fun snapRank(v: Int): String = when (v) { 1 -> "A"; 11 -> "J"; 12 -> "Q"; 13 -> "K"; else -> "$v" }

@Composable
fun SnapGame(onBack: () -> Unit) {
    GameScaffold(title = "Snap", onBack = onBack) { modifier ->
        var deck by remember { mutableStateOf((1..13).flatMap { v -> List(4) { v } }.shuffled()) }
        var idx by remember { mutableIntStateOf(0) }
        var claimed by remember { mutableStateOf(false) }
        var running by remember { mutableStateOf(false) }
        var p1 by remember { mutableIntStateOf(0) }
        var p2 by remember { mutableIntStateOf(0) }

        val matchActive = running && idx > 0 && deck[idx] == deck[idx - 1] && !claimed
        val over = idx >= deck.size - 1 && !running

        LaunchedEffect(running) {
            while (running && idx < deck.size - 1) {
                kotlinx.coroutines.delay(1150)
                idx++
                claimed = false
            }
            if (idx >= deck.size - 1) running = false
        }

        fun snap(player: Int) {
            if (!running || claimed) return
            if (matchActive) { if (player == 1) p1++ else p2++ } else { if (player == 1) p2++ else p1++ }
            claimed = true
        }
        fun start() { deck = (1..13).flatMap { v -> List(4) { v } }.shuffled(); idx = 0; claimed = false; p1 = 0; p2 = 0; running = true }

        Column(modifier = modifier) {
            // Player 2 zone (top).
            SnapZone("Player 2 · $p2", enabled = running, color = Teal, modifier = Modifier.fillMaxWidth().weight(1f)) { snap(2) }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(16.dp)).background(if (matchActive) Coral else OnAccent),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (running || over) {
                        Text(snapRank(deck[idx.coerceIn(0, deck.size - 1)]), fontSize = 44.sp, fontWeight = FontWeight.Bold, color = if (matchActive) OnAccent else Coral)
                        if (matchActive) Text("SNAP!", fontWeight = FontWeight.Bold, color = OnAccent)
                    } else {
                        Text("Tap Start", color = Coral, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            // Player 1 zone (bottom).
            SnapZone("Player 1 · $p1", enabled = running, color = Coral, modifier = Modifier.fillMaxWidth().weight(1f)) { snap(1) }
            Spacer(Modifier.height(10.dp))
            if (!running) {
                Button(onClick = { start() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) {
                    Text(if (over) "Play again (P${if (p1 >= p2) 1 else 2} won ${maxOf(p1, p2)}–${minOf(p1, p2)})" else "Start")
                }
            } else {
                Text("Tap your side when two cards match. Wrong snap gives a point away.", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SnapZone(label: String, enabled: Boolean, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier, onTap: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = if (enabled) 0.85f else 0.4f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnAccent)
    }
}
