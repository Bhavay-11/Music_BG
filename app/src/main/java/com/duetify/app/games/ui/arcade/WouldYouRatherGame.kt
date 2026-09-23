package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.Lavender
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceVariantPink

private val DILEMMAS = listOf(
    "Only ever listen to one album" to "Only ever watch one movie",
    "Have unlimited concert tickets" to "Have unlimited travel flights",
    "Read minds" to "Be invisible",
    "Always be 10 minutes late" to "Always be 20 minutes early",
    "Give up coffee forever" to "Give up desserts forever",
    "Relive your best day" to "Skip to your best day ahead",
    "Never lose your keys again" to "Never lose your phone again",
    "Have a rewind button" to "Have a pause button",
    "Only text" to "Only call",
    "Sing every word you say" to "Dance everywhere you walk",
    "Know every song's lyrics" to "Play any instrument",
    "Always have great hair" to "Always have perfect playlists",
)

@Composable
fun WouldYouRatherGame(onBack: () -> Unit) {
    GameScaffold(title = "Would You Rather", onBack = onBack) { modifier ->
        val order = remember { DILEMMAS.indices.shuffled() }
        var pos by remember { mutableIntStateOf(0) }
        var answered by remember { mutableIntStateOf(0) }
        var pickedLeft by remember { mutableStateOf<Boolean?>(null) }

        val dilemma = DILEMMAS[order[pos % order.size]]

        fun next() {
            pickedLeft = null
            pos = (pos + 1) % order.size
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text("Answered: $answered", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                OptionCard(
                    text = dilemma.first,
                    highlighted = pickedLeft == true,
                    dimmed = pickedLeft == false,
                ) { if (pickedLeft == null) { pickedLeft = true; answered++ } }
                Spacer(Modifier.height(16.dp))
                Text(
                    "or",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceVariantPink,
                )
                Spacer(Modifier.height(16.dp))
                OptionCard(
                    text = dilemma.second,
                    highlighted = pickedLeft == false,
                    dimmed = pickedLeft == true,
                ) { if (pickedLeft == null) { pickedLeft = false; answered++ } }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (pickedLeft != null) Coral else Coral.copy(alpha = 0.4f))
                    .clickable(enabled = pickedLeft != null) { next() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (pickedLeft == null) "Pick one to continue" else "Next dilemma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnAccent,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Great for two — argue it out, then tap Next.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
        }
    }
}

@Composable
private fun OptionCard(text: String, highlighted: Boolean, dimmed: Boolean, onClick: () -> Unit) {
    val bg = when {
        highlighted -> Coral
        dimmed -> Lavender.copy(alpha = 0.25f)
        else -> Lavender.copy(alpha = 0.9f)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OnAccent,
            textAlign = TextAlign.Center,
        )
    }
}
