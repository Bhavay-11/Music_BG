package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import kotlinx.coroutines.delay

private data class MemCard(val id: Int, val emoji: String, val faceUp: Boolean, val matched: Boolean)

private val EMOJIS = listOf("🎵", "❤️", "🎮", "🌙", "⭐", "🎧", "🔥", "🍀")

private fun freshDeck(): List<MemCard> =
    (EMOJIS + EMOJIS)
        .shuffled()
        .mapIndexed { index, emoji -> MemCard(id = index, emoji = emoji, faceUp = false, matched = false) }

@Composable
fun MemoryMatchGame(onBack: () -> Unit) {
    GameScaffold(title = "Memory Match", onBack = onBack) { modifier ->
        var cards by remember { mutableStateOf(freshDeck()) }
        var firstIndex by remember { mutableStateOf<Int?>(null) }
        var busy by remember { mutableStateOf(false) }
        var moves by remember { mutableIntStateOf(0) }

        val won = cards.all { it.matched }

        // After a mismatch, briefly show both cards then flip the unmatched ones back down.
        LaunchedEffect(busy) {
            if (busy) {
                delay(750)
                cards = cards.map { if (it.matched) it else it.copy(faceUp = false) }
                firstIndex = null
                busy = false
            }
        }

        fun onTap(index: Int) {
            if (busy || won) return
            val card = cards[index]
            if (card.faceUp || card.matched) return
            cards = cards.mapIndexed { i, c -> if (i == index) c.copy(faceUp = true) else c }
            val first = firstIndex
            if (first == null) {
                firstIndex = index
            } else {
                moves++
                if (cards[first].emoji == cards[index].emoji) {
                    cards = cards.mapIndexed { i, c ->
                        if (i == first || i == index) c.copy(matched = true) else c
                    }
                    firstIndex = null
                } else {
                    busy = true
                }
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Moves: $moves", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
                if (won) {
                    Text("Solved!", style = MaterialTheme.typography.titleMedium, color = Teal, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            ) {
                itemsIndexed(cards) { index, card ->
                    CardTile(card = card, onClick = { onTap(index) })
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    cards = freshDeck(); firstIndex = null; busy = false; moves = 0
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text(if (won) "Play again" else "Shuffle & restart") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CardTile(card: MemCard, onClick: () -> Unit) {
    val faceShown = card.faceUp || card.matched
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (faceShown) SurfaceHigh else Coral)
            .clickable(enabled = !faceShown, onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (faceShown) card.emoji else "?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = if (faceShown) OnSurfaceLight else OnAccent,
        )
    }
}
