package com.duetify.app.games.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Whatshot

/** Every game the hub can show. Order here is the order they appear on the Play tab. */
enum class GameId(
    val displayTitle: String,
    val tagline: String,
    val icon: ImageVector,
    /** false = shown as a locked "coming soon" tile; the tile still renders so the roadmap is visible. */
    val available: Boolean,
) {
    THIS_OR_THAT(
        displayTitle = "This or That",
        tagline = "Answer in sync — see how alike you are",
        icon = Icons.Filled.Favorite,
        available = true,
    ),
    EMOJI_SONG(
        displayTitle = "Emoji Song Quiz",
        tagline = "Guess the track from emoji clues",
        icon = Icons.Filled.Quiz,
        available = true,
    ),
    MEMORY_MATCH(
        displayTitle = "Memory Match",
        tagline = "Flip cards, find the pairs",
        icon = Icons.Filled.GridView,
        available = true,
    ),
    REACTION(
        displayTitle = "Reaction Duel",
        tagline = "Tap the instant it turns green",
        icon = Icons.Filled.Bolt,
        available = true,
    ),
    TAP_FRENZY(
        displayTitle = "Tap Frenzy",
        tagline = "Most taps in ten seconds wins",
        icon = Icons.Filled.TouchApp,
        available = true,
    ),
    TIC_TAC_TOE(
        displayTitle = "Tic-Tac-Toe",
        tagline = "Classic X and O for two",
        icon = Icons.Filled.Tag,
        available = true,
    ),
    ROCK_PAPER_SCISSORS(
        displayTitle = "Rock Paper Scissors",
        tagline = "Best of the age-old duel",
        icon = Icons.Filled.Casino,
        available = true,
    ),
    TAP_DUET(
        displayTitle = "Tap Duet",
        tagline = "Tap the beat together and score your sync",
        icon = Icons.Filled.GraphicEq,
        available = false,
    ),
    TRUTH_OR_DARE(
        displayTitle = "Truth or Dare",
        tagline = "Take turns — playful prompts for two",
        icon = Icons.Filled.Whatshot,
        available = false,
    ),
    DRAW_TOGETHER(
        displayTitle = "Doodle Duet",
        tagline = "Draw on one shared canvas from anywhere",
        icon = Icons.Filled.Brush,
        available = false,
    );

    companion object {
        val catalog: List<GameId> get() = entries
    }
}
