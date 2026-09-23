package com.duetify.app.games.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Whatshot

/** Every duet game the hub can show. Order here is the order they appear on the Play tab. */
enum class GameId(
    val displayTitle: String,
    val tagline: String,
    val icon: ImageVector,
    /** false = shown as a locked "coming soon" tile; the tile still renders so the roadmap is visible. */
    val available: Boolean,
) {
    THIS_OR_THAT(
        displayTitle = "This or That",
        tagline = "Answer in sync — see how alike you really are",
        icon = Icons.Filled.Favorite,
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
