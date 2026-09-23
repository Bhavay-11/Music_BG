package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetify.app.games.ui.common.GameScaffold
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal
import kotlin.random.Random

private const val MAX_NUMBER = 100

@Composable
fun HigherLowerGame(onBack: () -> Unit) {
    GameScaffold(title = "Higher or Lower", onBack = onBack) { modifier ->
        var target by remember { mutableIntStateOf(Random.nextInt(1, MAX_NUMBER + 1)) }
        var low by remember { mutableIntStateOf(1) }
        var high by remember { mutableIntStateOf(MAX_NUMBER) }
        var guess by remember { mutableIntStateOf((1 + MAX_NUMBER) / 2) }
        var attempts by remember { mutableIntStateOf(0) }
        var best by remember { mutableIntStateOf(0) }
        var hint by remember { mutableStateOf("I'm thinking of a number from 1 to $MAX_NUMBER") }
        var solved by remember { mutableStateOf(false) }

        fun reset() {
            target = Random.nextInt(1, MAX_NUMBER + 1)
            low = 1; high = MAX_NUMBER; guess = (1 + MAX_NUMBER) / 2
            attempts = 0; solved = false
            hint = "I'm thinking of a number from 1 to $MAX_NUMBER"
        }

        fun submit() {
            if (solved) return
            attempts++
            when {
                guess == target -> {
                    solved = true
                    hint = "Got it in $attempts! 🎉"
                    if (best == 0 || attempts < best) best = attempts
                }
                guess < target -> { low = (guess + 1).coerceAtMost(high); hint = "Higher ⬆️"; guess = (low + high) / 2 }
                else -> { high = (guess - 1).coerceAtLeast(low); hint = "Lower ⬇️"; guess = (low + high) / 2 }
            }
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (best > 0) "Best: $best guesses · Attempts: $attempts" else "Attempts: $attempts",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceHigh).padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("$guess", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = if (solved) Teal else Coral)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                hint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight,
            )
            Spacer(Modifier.weight(1f))
            if (!solved) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Stepper("–", Modifier.weight(1f)) { guess = (guess - 1).coerceAtLeast(low) }
                    Stepper("+", Modifier.weight(1f)) { guess = (guess + 1).coerceAtMost(high) }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Range: $low – $high",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantPink,
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Guess") }
            } else {
                Button(
                    onClick = { reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                ) { Text("Play again") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Stepper(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHigh, contentColor = OnSurfaceLight),
    ) { Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
}
