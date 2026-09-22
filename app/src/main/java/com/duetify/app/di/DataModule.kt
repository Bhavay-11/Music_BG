package com.duetify.app.di

import com.duetify.app.data.repository.BackupRepositoryImpl
import com.duetify.app.data.repository.DownloadRepositoryImpl
import com.duetify.app.data.repository.LibraryRepositoryImpl
import com.duetify.app.data.repository.LocalMusicRepositoryImpl
import com.duetify.app.data.repository.LyricsRepositoryImpl
import com.duetify.app.data.repository.MusicRepositoryImpl
import com.duetify.app.data.repository.SpotifyImportRepositoryImpl
import com.duetify.app.data.repository.StatsRepositoryImpl
import com.duetify.app.data.source.youtube.NewPipeMusicSource
import com.duetify.app.domain.repository.BackupRepository
import com.duetify.app.domain.repository.DownloadRepository
import com.duetify.app.domain.repository.LibraryRepository
import com.duetify.app.domain.repository.LocalMusicRepository
import com.duetify.app.domain.repository.LyricsRepository
import com.duetify.app.domain.repository.MusicRepository
import com.duetify.app.domain.repository.PlaylistImportRepository
import com.duetify.app.domain.repository.StatsRepository
import com.duetify.app.domain.source.MusicSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMusicSource(impl: NewPipeMusicSource): MusicSource

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistImportRepository(impl: SpotifyImportRepositoryImpl): PlaylistImportRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindLocalMusicRepository(impl: LocalMusicRepositoryImpl): LocalMusicRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
