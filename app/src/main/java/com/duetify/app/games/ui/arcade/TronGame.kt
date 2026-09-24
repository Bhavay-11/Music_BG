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
import androidx.compose.runtime.LaunchedEffect
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
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlinx.coroutines.delay

private const val TG = 15

@Composable
fun TronGame(onBack: () -> Unit) {
    GameScaffold(title = "Tron Light Cycles", onBack = onBack) { modifier ->
        val mid = TG / 2
        var p1 by remember { mutableStateOf(mid * TG + 2) }
        var p2 by remember { mutableStateOf(mid * TG + (TG - 3)) }
        val d1 = remember { mutableStateOf(0 to 1) }   // right
        val d2 = remember { mutableStateOf(0 to -1) }  // left
        var trail1 by remember { mutableStateOf(setOf(mid * TG + 2)) }
        var trail2 by remember { mutableStateOf(setOf(mid * TG + (TG - 3))) }
        var running by remember { mutableStateOf(false) }
        var winner by remember { mutableStateOf<String?>(null) }

        fun oob(r: Int, c: Int) = r !in 0 until TG || c !in 0 until TG

        LaunchedEffect(running) {
            while (running) {
                delay(150)
                val occupied = trail1 + trail2
                val (dr1, dc1) = d1.value
                val (dr2, dc2) = d2.value
                val n1r = p1 / TG + dr1; val n1c = p1 % TG + dc1
                val n2r = p2 / TG + dr2; val n2c = p2 % TG + dc2
                val n1 = n1r * TG + n1c
                val n2 = n2r * TG + n2c
                val crash1 = oob(n1r, n1c) || n1 in occupied || n1 == n2
                val crash2 = oob(n2r, n2c) || n2 in occupied || n1 == n2
                when {
                    crash1 && crash2 -> { winner = "Draw"; running = false }
                    crash1 -> { winner = "Teal"; running = false }
                    crash2 -> { winner = "Coral"; running = false }
                    else -> { p1 = n1; p2 = n2; trail1 = trail1 + n1; trail2 = trail2 + n2 }
                }
            }
        }

        fun turn(d: androidx.compose.runtime.MutableState<Pair<Int, Int>>, left: Boolean) {
            if (!running) return
            val (dr, dc) = d.value
            d.value = if (left) -dc to dr else dc to -dr
        }
        fun start() {
            p1 = mid * TG + 2; p2 = mid * TG + (TG - 3)
            d1.value = 0 to 1; d2.value = 0 to -1
            trail1 = setOf(p1); trail2 = setOf(p2)
            winner = null; running = true
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = winner?.let { if (it == "Draw") "Head-on crash — draw!" else "$it wins! 🎉" } ?: if (running) "Go!" else "Tap Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = winner?.let { if (it == "Coral") Coral else if (it == "Teal") Teal else OnSurfaceLight } ?: OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(SurfaceContainer).padding(3.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until TG) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (c in 0 until TG) {
                                val i = r * TG + c
                                val color = when {
                                    i == p1 -> OnSurfaceLight
                                    i == p2 -> OnSurfaceLight
                                    i in trail1 -> Coral
                                    i in trail2 -> Teal
                                    else -> SurfaceHigh
                                }
                                Box(modifier = Modifier.fillMaxHeight().weight(1f).padding(1.dp).clip(RoundedCornerShape(2.dp)).background(color))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (!running) {
                Button(onClick = { start() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent)) { Text(if (winner != null) "Rematch" else "Start") }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TurnBtn("P1 ↺", Coral, Modifier.weight(1f)) { turn(d1, true) }
                    TurnBtn("P1 ↻", Coral, Modifier.weight(1f)) { turn(d1, false) }
                    TurnBtn("P2 ↺", Teal, Modifier.weight(1f)) { turn(d2, true) }
                    TurnBtn("P2 ↻", Teal, Modifier.weight(1f)) { turn(d2, false) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TurnBtn(label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(52.dp).clip(RoundedCornerShape(14.dp)).background(color).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, fontWeight = FontWeight.Bold, color = OnAccent) }
}
