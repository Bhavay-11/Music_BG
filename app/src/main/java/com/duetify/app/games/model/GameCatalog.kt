package com.duetify.app.games.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot

/** Every game the hub can show. Order here is the order they appear on the Play tab. */
enum class GameId(
    val displayTitle: String,
    val tagline: String,
    val icon: ImageVector,
    /** false = shown as a locked "coming soon" tile; the tile still renders so the roadmap is visible. */
    val available: Boolean,
) {
    THIS_OR_THAT("This or That", "Answer in sync — see how alike you are", Icons.Filled.Favorite, true),
    TRUTH_OR_DARE("Truth or Dare", "Playful prompts — take turns", Icons.Filled.Whatshot, true),
    WOULD_YOU_RATHER("Would You Rather", "Impossible choices, together", Icons.Filled.CompareArrows, true),
    EMOJI_SONG("Emoji Song Quiz", "Guess the track from emoji clues", Icons.Filled.Quiz, true),
    MUSIC_TRIVIA("Music Trivia", "How well do you know music?", Icons.Filled.MusicNote, true),
    MEMORY_MATCH("Memory Match", "Flip cards, find the pairs", Icons.Filled.GridView, true),
    SIMON("Simon Says", "Repeat the growing sequence", Icons.Filled.Lightbulb, true),
    REACTION("Reaction Duel", "Tap the instant it turns green", Icons.Filled.Bolt, true),
    WHACK("Whack-a-Tap", "Hit the glowing tile, fast", Icons.Filled.Adjust, true),
    TAP_FRENZY("Tap Frenzy", "Most taps in ten seconds", Icons.Filled.TouchApp, true),
    TUG_OF_WAR("Tug of War", "Two thumbs, one rope", Icons.Filled.SwapHoriz, true),
    TIC_TAC_TOE("Tic-Tac-Toe", "Classic X and O for two", Icons.Filled.Tag, true),
    CONNECT_FOUR("Connect Four", "Drop discs, line up four", Icons.Filled.Circle, true),
    ROCK_PAPER_SCISSORS("Rock Paper Scissors", "The age-old duel", Icons.Filled.Casino, true),
    HIGHER_LOWER("Higher or Lower", "Guess the secret number", Icons.Filled.TrendingUp, true),

    // Roadmap — locked tiles, no destination yet.
    TAP_DUET("Tap Duet", "Tap the beat together and score your sync", Icons.Filled.GraphicEq, false),
    DRAW_TOGETHER("Doodle Duet", "Draw on one shared canvas from anywhere", Icons.Filled.Brush, false);

    companion object {
        val catalog: List<GameId> get() = entries
    }
}
