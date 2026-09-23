package com.duetify.app.games.di

import com.duetify.app.games.transport.GameTransport
import com.duetify.app.games.transport.LocalGameTransport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Wires the games layer's transport. Bound to [LocalGameTransport] today (no backend required);
 * Phase 2 swaps in a Firebase-backed transport here without touching any game or UI code.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class GamesModule {

    @Binds
    @Singleton
    abstract fun bindGameTransport(impl: LocalGameTransport): GameTransport
}
