package com.duetify.app.domain.repository

import com.duetify.app.domain.model.Lyrics
import com.duetify.app.domain.model.Song

/** Fetches lyrics for a track. Returns null when none are found. */
interface LyricsRepository {
    suspend fun forSong(song: Song): Lyrics?
}
