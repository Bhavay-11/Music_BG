package com.duetify.app.domain.repository

import com.duetify.app.domain.model.Song

/** Access to audio files stored on the device (scanned from MediaStore). */
interface LocalMusicRepository {

    /**
     * Every music track on the device, sorted by title. Each [Song.id] is prefixed so playback and
     * the rest of the app can tell a local file apart from a YouTube track. Requires the audio-read
     * permission to have been granted; returns an empty list otherwise.
     */
    suspend fun localSongs(): List<Song>
}
