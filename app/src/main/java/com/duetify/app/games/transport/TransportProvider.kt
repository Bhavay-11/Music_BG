package com.duetify.app.games.transport

import android.content.Context
import com.google.firebase.FirebaseApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Picks the right [GameTransport] at runtime: Firebase for real cross-device play when the app is
 * configured, the in-process transport otherwise. Games ask for online or offline; they never know
 * which concrete transport they got.
 */
@Singleton
class TransportProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val local: LocalGameTransport,
    private val firebase: FirebaseGameTransport,
) {
    /** True only when a google-services.json has been added, so a default FirebaseApp exists. */
    fun onlineAvailable(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    fun transport(online: Boolean): GameTransport =
        if (online && onlineAvailable()) firebase else local
}
