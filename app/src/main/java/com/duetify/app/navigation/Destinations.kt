package com.duetify.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import com.duetify.app.R

/** Route constants + builders for the whole app. */
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val GAMES = "games"

    /** Individual games, opened from the games hub. */
    const val GAME_THIS_OR_THAT = "games/this_or_that"
    const val GAME_EMOJI_SONG = "games/emoji_song"
    const val GAME_MEMORY_MATCH = "games/memory_match"
    const val GAME_REACTION = "games/reaction"
    const val GAME_TAP_FRENZY = "games/tap_frenzy"
    const val GAME_TIC_TAC_TOE = "games/tic_tac_toe"
    const val GAME_RPS = "games/rock_paper_scissors"
    const val GAME_TRUTH_OR_DARE = "games/truth_or_dare"
    const val GAME_WOULD_YOU_RATHER = "games/would_you_rather"
    const val GAME_MUSIC_TRIVIA = "games/music_trivia"
    const val GAME_SIMON = "games/simon"
    const val GAME_WHACK = "games/whack"
    const val GAME_TUG_OF_WAR = "games/tug_of_war"
    const val GAME_CONNECT_FOUR = "games/connect_four"
    const val GAME_HIGHER_LOWER = "games/higher_lower"
    const val GAME_GOMOKU = "games/gomoku"
    const val GAME_NOTAKTO = "games/notakto"
    const val GAME_ORDER_CHAOS = "games/order_chaos"
    const val GAME_HANGMAN = "games/hangman"
    const val GAME_MASTERMIND = "games/mastermind"
    const val GAME_BULLS_COWS = "games/bulls_cows"
    const val GAME_PIG = "games/pig"
    const val GAME_RPS_LS = "games/rps_lizard_spock"
    const val GAME_WAR = "games/war"
    const val GAME_REVERSI = "games/reversi"
    const val GAME_BLACKJACK = "games/blackjack"
    const val GAME_SNAKE = "games/snake"
    const val GAME_KNUCKLEBONES = "games/knucklebones"
    const val GAME_ULTIMATE_TTT = "games/ultimate_ttt"
    const val GAME_DOTS_BOXES = "games/dots_boxes"
    const val GAME_MANCALA = "games/mancala"
    const val GAME_HEX = "games/hex"
    const val GAME_2048 = "games/2048"
    const val GAME_WORDLE = "games/wordle"
    const val GAME_MINESWEEPER = "games/minesweeper"
    const val GAME_YAHTZEE = "games/yahtzee"

    const val PLAYER = "player"
    const val QUEUE = "queue"

    const val ALBUM = "album/{albumId}"
    const val ARTIST = "artist/{artistId}"
    const val LOCAL_PLAYLIST = "local_playlist/{playlistId}"
    const val IMPORT = "import"
    const val DOWNLOADS = "downloads"
    const val LOCAL = "local"
    const val SETTINGS = "settings"
    const val STATS = "stats"
    const val EQUALIZER = "equalizer"
    const val SHARED_PLAYLIST = "shared_playlist"

    fun album(albumId: String) = "album/${Uri.encode(albumId)}"
    fun artist(artistId: String) = "artist/${Uri.encode(artistId)}"
    fun localPlaylist(playlistId: Long) = "local_playlist/$playlistId"

    /** Sentinel playlist id for the built-in "Liked Songs" collection. */
    const val LIKED_PLAYLIST_ID = -1L
    fun liked() = localPlaylist(LIKED_PLAYLIST_ID)

    const val ARG_ALBUM_ID = "albumId"
    const val ARG_ARTIST_ID = "artistId"
    const val ARG_PLAYLIST_ID = "playlistId"
}

/** The bottom-navigation tabs. */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(Routes.HOME, R.string.nav_listen, Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    LIBRARY(Routes.LIBRARY, R.string.nav_library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    SEARCH(Routes.SEARCH, R.string.nav_search, Icons.Filled.Search, Icons.Outlined.Search),
    PLAY(Routes.GAMES, R.string.nav_play, Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
}
