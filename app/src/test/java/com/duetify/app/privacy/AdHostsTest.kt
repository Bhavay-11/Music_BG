package com.duetify.app.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Shields must block ad/tracker hosts while never touching the domains the app needs to work.
 * A false positive here silently breaks playback or artwork, so both sides are pinned.
 */
class AdHostsTest {

    @Test
    fun `blocks known ad and tracker domains`() {
        assertTrue(AdHosts.isBlocked("doubleclick.net"))
        assertTrue(AdHosts.isBlocked("pagead2.googlesyndication.com"))
        assertTrue(AdHosts.isBlocked("www.google-analytics.com"))
        assertTrue(AdHosts.isBlocked("app-measurement.com"))
        assertTrue(AdHosts.isBlocked("in.appcenter.criteo.com"))
    }

    @Test
    fun `matching is case-insensitive`() {
        assertTrue(AdHosts.isBlocked("DoubleClick.NET"))
    }

    @Test
    fun `never blocks the domains playback and artwork depend on`() {
        assertFalse(AdHosts.isBlocked("www.youtube.com"))
        assertFalse(AdHosts.isBlocked("rr3---sn-abc.googlevideo.com"))
        assertFalse(AdHosts.isBlocked("i.ytimg.com"))
        assertFalse(AdHosts.isBlocked("yt3.ggpht.com"))
        assertFalse(AdHosts.isBlocked("www.googleapis.com"))
        assertFalse(AdHosts.isBlocked("api.github.com"))
        assertFalse(AdHosts.isBlocked("open.spotify.com"))
        assertFalse(AdHosts.isBlocked("google.com"))
    }

    @Test
    fun `a domain is not blocked by a lookalike suffix`() {
        // "notdoubleclick.net" must not match "doubleclick.net".
        assertFalse(AdHosts.isBlocked("notdoubleclick.net"))
    }

    @Test
    fun `blank host is allowed`() {
        assertFalse(AdHosts.isBlocked(null))
        assertFalse(AdHosts.isBlocked(""))
    }
}
