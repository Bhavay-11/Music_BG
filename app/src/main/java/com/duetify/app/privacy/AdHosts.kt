package com.duetify.app.privacy

/**
 * Curated ad / tracker / analytics host list for Shields — Duetify's Brave-style network blocker.
 *
 * Deliberately conservative: it lists only dedicated advertising, tracking and analytics domains.
 * None of the hosts the app actually needs — YouTube/googlevideo streams, ytimg/ggpht thumbnails,
 * googleapis, GitHub, Spotify — appear here, so blocking can be on by default without breaking
 * playback, artwork or updates.
 *
 * Matching is suffix-based: a host is blocked if it equals a listed domain or is a subdomain of it.
 * That is why `adservice.google.com` can be listed without touching `google.com` itself.
 */
object AdHosts {

    private val blocked: Set<String> = setOf(
        // Google advertising / analytics (not core services).
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "googletagmanager.com",
        "googletagservices.com",
        "google-analytics.com",
        "adservice.google.com",
        "app-measurement.com",
        "2mdn.net",
        "admob.com",
        // Crash / analytics SDK backends.
        "crashlytics.com",
        "settings.crashlytics.com",
        // Third-party ad networks & trackers.
        "adnxs.com",
        "adsafeprotected.com",
        "amazon-adsystem.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "moatads.com",
        "scorecardresearch.com",
        "quantserve.com",
        "mixpanel.com",
        "segment.io",
        "segment.com",
        "amplitude.com",
        "appsflyer.com",
        "adjust.com",
        "applovin.com",
        "unityads.unity3d.com",
        "ironsrc.com",
        "flurry.com",
        "branch.io",
        "bugsnag.com",
        "sentry.io",
    )

    /** True when [host] is (or is a subdomain of) a blocked ad/tracker domain. */
    fun isBlocked(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val h = host.lowercase()
        return blocked.any { domain -> h == domain || h.endsWith(".$domain") }
    }
}
