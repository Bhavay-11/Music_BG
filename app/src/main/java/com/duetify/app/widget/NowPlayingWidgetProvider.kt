package com.duetify.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.duetify.app.data.prefs.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Home-screen "now playing" widget. The system calls [onUpdate] when the widget is added,
 * resized or restored after a reboot; live updates while playing are pushed by
 * [com.duetify.app.playback.NowPlayingPublisher].
 */
@AndroidEntryPoint
class NowPlayingWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var preferences: AppPreferences

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        NowPlayingWidget.updateAll(context, preferences.nowPlaying())
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        NowPlayingWidget.updateAll(context, preferences.nowPlaying())
    }
}
