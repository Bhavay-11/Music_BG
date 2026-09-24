package com.duetify.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.duetify.app.games.model.GameId
import com.duetify.app.games.ui.GamesHubScreen
import com.duetify.app.games.ui.arcade.BlackjackGame
import com.duetify.app.games.ui.arcade.BullsAndCowsGame
import com.duetify.app.games.ui.arcade.ConnectFourGame
import com.duetify.app.games.ui.arcade.CrazyEightsGame
import com.duetify.app.games.ui.arcade.DotsAndBoxesGame
import com.duetify.app.games.ui.arcade.EmojiSongGame
import com.duetify.app.games.ui.arcade.Game2048
import com.duetify.app.games.ui.arcade.GomokuGame
import com.duetify.app.games.ui.arcade.HangmanGame
import com.duetify.app.games.ui.arcade.HexGame
import com.duetify.app.games.ui.arcade.HigherLowerGame
import com.duetify.app.games.ui.arcade.KnucklebonesGame
import com.duetify.app.games.ui.arcade.MancalaGame
import com.duetify.app.games.ui.arcade.MastermindGame
import com.duetify.app.games.ui.arcade.MemoryMatchGame
import com.duetify.app.games.ui.arcade.MinesweeperGame
import com.duetify.app.games.ui.arcade.MusicTriviaGame
import com.duetify.app.games.ui.arcade.NimGame
import com.duetify.app.games.ui.arcade.NotaktoGame
import com.duetify.app.games.ui.arcade.OrderAndChaosGame
import com.duetify.app.games.ui.arcade.PigDiceGame
import com.duetify.app.games.ui.arcade.ReactionGame
import com.duetify.app.games.ui.arcade.ReversiGame
import com.duetify.app.games.ui.arcade.RockPaperScissorsGame
import com.duetify.app.games.ui.arcade.RpsLizardSpockGame
import com.duetify.app.games.ui.arcade.SimonSaysGame
import com.duetify.app.games.ui.arcade.SnakeGame
import com.duetify.app.games.ui.arcade.SnapGame
import com.duetify.app.games.ui.arcade.TapFrenzyGame
import com.duetify.app.games.ui.arcade.TicTacToeGame
import com.duetify.app.games.ui.arcade.TronGame
import com.duetify.app.games.ui.arcade.TruthOrDareGame
import com.duetify.app.games.ui.arcade.UltimateTicTacToeGame
import com.duetify.app.games.ui.arcade.TugOfWarGame
import com.duetify.app.games.ui.arcade.WarCardGame
import com.duetify.app.games.ui.arcade.WhackATapGame
import com.duetify.app.games.ui.arcade.WordleGame
import com.duetify.app.games.ui.arcade.WouldYouRatherGame
import com.duetify.app.games.ui.arcade.YahtzeeGame
import com.duetify.app.games.ui.thisorthat.ThisOrThatScreen
import com.duetify.app.ui.album.AlbumDetailScreen
import com.duetify.app.ui.artist.ArtistDetailScreen
import com.duetify.app.ui.home.HomeScreen
import com.duetify.app.ui.importer.ImportPlaylistScreen
import com.duetify.app.ui.library.LibraryScreen
import com.duetify.app.ui.library.LocalMusicScreen
import com.duetify.app.ui.library.PlaylistDetailScreen
import com.duetify.app.ui.player.DownloadsScreen
import com.duetify.app.ui.player.PlayerViewModel
import com.duetify.app.ui.search.SearchScreen
import com.duetify.app.ui.settings.EqualizerScreen
import com.duetify.app.ui.settings.SettingsScreen
import com.duetify.app.ui.share.SharedPlaylistScreen
import com.duetify.app.ui.stats.StatsScreen

@Composable
fun DuetifyNavHost(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onExpandPlayer: () -> Unit,
    onScanCode: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME,
) {
    val dur = 300
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur))
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(onPlaySongs = { songs, index -> playerViewModel.play(songs, index) })
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                playerViewModel = playerViewModel,
                onPlaySong = { playerViewModel.playWithRadio(it) },
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
            )
        }
        composable(Routes.GAMES) {
            GamesHubScreen(
                onOpenGame = { game ->
                    val route = when (game) {
                        GameId.THIS_OR_THAT -> Routes.GAME_THIS_OR_THAT
                        GameId.EMOJI_SONG -> Routes.GAME_EMOJI_SONG
                        GameId.MEMORY_MATCH -> Routes.GAME_MEMORY_MATCH
                        GameId.REACTION -> Routes.GAME_REACTION
                        GameId.TAP_FRENZY -> Routes.GAME_TAP_FRENZY
                        GameId.TIC_TAC_TOE -> Routes.GAME_TIC_TAC_TOE
                        GameId.ROCK_PAPER_SCISSORS -> Routes.GAME_RPS
                        GameId.TRUTH_OR_DARE -> Routes.GAME_TRUTH_OR_DARE
                        GameId.WOULD_YOU_RATHER -> Routes.GAME_WOULD_YOU_RATHER
                        GameId.MUSIC_TRIVIA -> Routes.GAME_MUSIC_TRIVIA
                        GameId.SIMON -> Routes.GAME_SIMON
                        GameId.WHACK -> Routes.GAME_WHACK
                        GameId.TUG_OF_WAR -> Routes.GAME_TUG_OF_WAR
                        GameId.CONNECT_FOUR -> Routes.GAME_CONNECT_FOUR
                        GameId.HIGHER_LOWER -> Routes.GAME_HIGHER_LOWER
                        GameId.GOMOKU -> Routes.GAME_GOMOKU
                        GameId.NOTAKTO -> Routes.GAME_NOTAKTO
                        GameId.ORDER_CHAOS -> Routes.GAME_ORDER_CHAOS
                        GameId.HANGMAN -> Routes.GAME_HANGMAN
                        GameId.MASTERMIND -> Routes.GAME_MASTERMIND
                        GameId.BULLS_COWS -> Routes.GAME_BULLS_COWS
                        GameId.PIG -> Routes.GAME_PIG
                        GameId.RPS_LIZARD_SPOCK -> Routes.GAME_RPS_LS
                        GameId.WAR -> Routes.GAME_WAR
                        GameId.REVERSI -> Routes.GAME_REVERSI
                        GameId.BLACKJACK -> Routes.GAME_BLACKJACK
                        GameId.SNAKE -> Routes.GAME_SNAKE
                        GameId.KNUCKLEBONES -> Routes.GAME_KNUCKLEBONES
                        GameId.ULTIMATE_TTT -> Routes.GAME_ULTIMATE_TTT
                        GameId.DOTS_BOXES -> Routes.GAME_DOTS_BOXES
                        GameId.MANCALA -> Routes.GAME_MANCALA
                        GameId.HEX -> Routes.GAME_HEX
                        GameId.G2048 -> Routes.GAME_2048
                        GameId.WORDLE -> Routes.GAME_WORDLE
                        GameId.MINESWEEPER -> Routes.GAME_MINESWEEPER
                        GameId.YAHTZEE -> Routes.GAME_YAHTZEE
                        GameId.NIM -> Routes.GAME_NIM
                        GameId.SNAP -> Routes.GAME_SNAP
                        GameId.TRON -> Routes.GAME_TRON
                        GameId.CRAZY_EIGHTS -> Routes.GAME_CRAZY_EIGHTS
                        else -> null // Locked games have no destination yet.
                    }
                    route?.let { navController.navigate(it) }
                },
            )
        }
        composable(
            route = Routes.GAME_THIS_OR_THAT,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            ThisOrThatScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.GAME_EMOJI_SONG) { EmojiSongGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_MEMORY_MATCH) { MemoryMatchGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_REACTION) { ReactionGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_TAP_FRENZY) { TapFrenzyGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_TIC_TAC_TOE) { TicTacToeGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_RPS) { RockPaperScissorsGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_TRUTH_OR_DARE) { TruthOrDareGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_WOULD_YOU_RATHER) { WouldYouRatherGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_MUSIC_TRIVIA) { MusicTriviaGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_SIMON) { SimonSaysGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_WHACK) { WhackATapGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_TUG_OF_WAR) { TugOfWarGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_CONNECT_FOUR) { ConnectFourGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_HIGHER_LOWER) { HigherLowerGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_GOMOKU) { GomokuGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_NOTAKTO) { NotaktoGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_ORDER_CHAOS) { OrderAndChaosGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_HANGMAN) { HangmanGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_MASTERMIND) { MastermindGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_BULLS_COWS) { BullsAndCowsGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_PIG) { PigDiceGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_RPS_LS) { RpsLizardSpockGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_WAR) { WarCardGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_REVERSI) { ReversiGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_BLACKJACK) { BlackjackGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_SNAKE) { SnakeGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_KNUCKLEBONES) { KnucklebonesGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_ULTIMATE_TTT) { UltimateTicTacToeGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_DOTS_BOXES) { DotsAndBoxesGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_MANCALA) { MancalaGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_HEX) { HexGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_2048) { Game2048(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_WORDLE) { WordleGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_MINESWEEPER) { MinesweeperGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_YAHTZEE) { YahtzeeGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_NIM) { NimGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_SNAP) { SnapGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_TRON) { TronGame(onBack = { navController.popBackStack() }) }
        composable(Routes.GAME_CRAZY_EIGHTS) { CrazyEightsGame(onBack = { navController.popBackStack() }) }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                playerViewModel = playerViewModel,
                onOpenLiked = { navController.navigate(Routes.liked()) },
                onOpenPlaylist = { navController.navigate(Routes.localPlaylist(it)) },
                onImportPlaylist = { navController.navigate(Routes.IMPORT) },
                onScanCode = onScanCode,
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenLocal = { navController.navigate(Routes.LOCAL) },
            )
        }
        composable(
            route = Routes.LOCAL,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            LocalMusicScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.STATS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            StatsScreen(
                onBack = { navController.popBackStack() },
                onPlaySongs = { songs, index -> playerViewModel.play(songs, index) },
                onOpenSearch = {
                    navController.navigate(Routes.SEARCH) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = Routes.SHARED_PLAYLIST,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            SharedPlaylistScreen(
                onBack = { navController.popBackStack() },
                onOpenPlaylist = { id ->
                    // Replace the preview: backing out should not offer to import again.
                    navController.navigate(Routes.localPlaylist(id)) {
                        popUpTo(Routes.SHARED_PLAYLIST) { inclusive = true }
                    }
                },
                onPlay = { songs, index -> playerViewModel.play(songs, index) },
            )
        }
        composable(
            route = Routes.SETTINGS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenEqualizer = { navController.navigate(Routes.EQUALIZER) },
            )
        }
        composable(
            route = Routes.EQUALIZER,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            EqualizerScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.IMPORT,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            ImportPlaylistScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.DOWNLOADS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            DownloadsScreen(playerViewModel = playerViewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.ALBUM,
            arguments = listOf(navArgument(Routes.ARG_ALBUM_ID) { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            AlbumDetailScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.ARTIST,
            arguments = listOf(navArgument(Routes.ARG_ARTIST_ID) { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            ArtistDetailScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() },
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
            )
        }
        composable(
            route = Routes.LOCAL_PLAYLIST,
            arguments = listOf(navArgument(Routes.ARG_PLAYLIST_ID) { type = NavType.LongType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(dur)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(dur)) },
        ) {
            PlaylistDetailScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
