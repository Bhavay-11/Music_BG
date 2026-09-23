package com.duetify.app.games.ui.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.Teal

private const val STEP = 0.028f

@Composable
fun TugOfWarGame(onBack: () -> Unit) {
    GameScaffold(title = "Tug of War", onBack = onBack) { modifier ->
        // 0f = Coral (left) fully wins, 1f = Teal (right) fully wins. Start centred.
        var pos by remember { mutableFloatStateOf(0.5f) }
        var winner by remember { mutableStateOf<String?>(null) }

        fun pull(towardRight: Boolean) {
            if (winner != null) return
            pos = (pos + if (towardRight) STEP else -STEP).coerceIn(0f, 1f)
            if (pos <= 0f) winner = "Left"
            if (pos >= 1f) winner = "Right"
        }

        Column(modifier = modifier) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = winner?.let { "$it side wins! 🎉" } ?: "Tap your side as fast as you can!",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = if (winner != null) Teal else OnSurfaceVariantPink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))

            // The rope: a track with the marker's fill showing who's ahead.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Teal),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pos.coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(50))
                        .background(Coral),
                )
            }
            Spacer(Modifier.height(16.dp))

            if (winner == null) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    TapSide(label = "LEFT", color = Coral, modifier = Modifier.weight(1f)) { pull(towardRight = false) }
                    Spacer(Modifier.width(12.dp))
                    TapSide(label = "RIGHT", color = Teal, modifier = Modifier.weight(1f)) { pull(towardRight = true) }
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { pos = 0.5f; winner = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
                    ) { Text("Rematch") }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Two players, one phone — each hammers their side.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantPink,
            )
        }
    }
}

@Composable
private fun TapSide(label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier, onTap: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(color.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = OnAccent)
    }
}
