package com.duetify.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.duetify.app.data.local.dao.ArtistDao
import com.duetify.app.data.local.dao.BackupDao
import com.duetify.app.data.local.dao.DownloadDao
import com.duetify.app.data.local.dao.HistoryDao
import com.duetify.app.data.local.dao.LikeDao
import com.duetify.app.data.local.dao.PlaylistDao
import com.duetify.app.data.local.dao.SongDao
import com.duetify.app.data.local.dao.StatsDao
import com.duetify.app.data.local.entity.DownloadEntity
import com.duetify.app.data.local.entity.LikedArtistEntity
import com.duetify.app.data.local.entity.LikedSongEntity
import com.duetify.app.data.local.entity.PlayEventEntity
import com.duetify.app.data.local.entity.PlayHistoryEntity
import com.duetify.app.data.local.entity.PlaylistEntity
import com.duetify.app.data.local.entity.PlaylistSongCrossRef
import com.duetify.app.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        LikedSongEntity::class,
        LikedArtistEntity::class,
        PlayHistoryEntity::class,
        PlayEventEntity::class,
        DownloadEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun likeDao(): LikeDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun artistDao(): ArtistDao
    abstract fun historyDao(): HistoryDao
    abstract fun statsDao(): StatsDao
    abstract fun downloadDao(): DownloadDao
    abstract fun backupDao(): BackupDao
}
