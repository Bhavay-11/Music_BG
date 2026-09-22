package com.duetify.app.data.repository

import com.duetify.app.data.spotify.SpotifyPublicClient
import com.duetify.app.domain.repository.ImportResult
import com.duetify.app.domain.repository.LibraryRepository
import com.duetify.app.domain.repository.MusicRepository
import com.duetify.app.domain.repository.PlaylistImportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyImportRepositoryImpl @Inject constructor(
    private val publicClient: SpotifyPublicClient,
    private val musicRepository: MusicRepository,
    private val libraryRepository: LibraryRepository,
) : PlaylistImportRepository {

    override suspend fun importFromLink(
        link: String,
        onProgress: (done: Int, total: Int) -> Unit,
    ): Result<ImportResult> = runCatching {
        val playlist = publicClient.fetchPlaylist(link)
        android.util.Log.d("SpotifyImport", "Link import: '${playlist.name}' ${playlist.tracks.size} tracks")
        val playlistId = libraryRepository.createPlaylist(playlist.name)
        playlist.coverUrl?.let { libraryRepository.setPlaylistArtwork(playlistId, it) }
        var matched = 0
        playlist.tracks.forEachIndexed { index, track ->
            val song = runCatching { musicRepository.search(track.searchQuery).songs.firstOrNull() }
                .onFailure { android.util.Log.w("SpotifyImport", "search failed for '${track.searchQuery}'", it) }
                .getOrNull()
            if (song != null) {
                libraryRepository.addToPlaylist(playlistId, song)
                matched++
            }
            onProgress(index + 1, playlist.tracks.size)
        }
        ImportResult(playlistId, playlist.name, matched, playlist.tracks.size)
    }
}
