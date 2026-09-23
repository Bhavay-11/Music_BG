package com.duetify.app.games.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duetify.app.games.model.GameId
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.CoralDark
import com.duetify.app.ui.theme.GlassStroke
import com.duetify.app.ui.theme.Lavender
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

/**
 * The "Play" tab: a Playables-style grid of duet games. Available games open; the rest render as
 * locked tiles so the roadmap is visible in-app.
 */
@Composable
fun GamesHubScreen(
    onOpenGame: (GameId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 140.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    text = "Play together",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Little games for two — near or far.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariantPink,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        items(GameId.catalog) { game ->
            GameTile(game = game, onClick = { if (game.available) onOpenGame(game) })
        }
    }
}

@Composable
private fun GameTile(game: GameId, onClick: () -> Unit) {
    // A soft two-tone wash per tile keeps the grid lively without needing artwork.
    val wash = Brush.linearGradient(
        listOf(
            SurfaceHigh,
            SurfaceContainer,
        ),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(24.dp))
            .background(wash)
            .clickable(enabled = game.available, onClick = onClick)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentFor(game)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = OnSurfaceLight,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column {
                Text(
                    text = game.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = game.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantPink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!game.available) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50))
                    .background(GlassStroke)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = OnSurfaceVariantPink,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "Soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariantPink,
                )
            }
        }
    }
}

@Composable
private fun accentFor(game: GameId) = when (game) {
    GameId.THIS_OR_THAT -> Coral
    GameId.TRUTH_OR_DARE -> CoralDark
    GameId.WOULD_YOU_RATHER -> Lavender
    GameId.EMOJI_SONG -> Teal
    GameId.MUSIC_TRIVIA -> Coral
    GameId.MEMORY_MATCH -> Lavender
    GameId.SIMON -> Teal
    GameId.REACTION -> CoralDark
    GameId.WHACK -> Coral
    GameId.TAP_FRENZY -> Teal
    GameId.TUG_OF_WAR -> Lavender
    GameId.TIC_TAC_TOE -> Teal
    GameId.CONNECT_FOUR -> Coral
    GameId.ROCK_PAPER_SCISSORS -> Lavender
    GameId.HIGHER_LOWER -> CoralDark
    GameId.GOMOKU -> Teal
    GameId.NOTAKTO -> Coral
    GameId.ORDER_CHAOS -> Lavender
    GameId.HANGMAN -> CoralDark
    GameId.MASTERMIND -> Teal
    GameId.BULLS_COWS -> Coral
    GameId.PIG -> Lavender
    GameId.RPS_LIZARD_SPOCK -> Teal
    GameId.WAR -> Coral
    GameId.REVERSI -> Lavender
    GameId.BLACKJACK -> CoralDark
    GameId.SNAKE -> Teal
    GameId.TAP_DUET -> Teal
    GameId.DRAW_TOGETHER -> Lavender
}
