package com.duetify.app.privacy

import com.duetify.app.data.prefs.AppPreferences
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

/**
 * Brave-style network shield. Sits on the app's shared OkHttp client and short-circuits any request
 * to a known ad/tracker host with an empty response, so nothing ad-related ever leaves the device.
 *
 * When Shields is off (user toggle), it is a no-op pass-through. Legitimate hosts always pass.
 */
class ShieldsInterceptor @Inject constructor(
    private val preferences: AppPreferences,
    private val stats: ShieldsStats,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (preferences.shieldsEnabledNow && AdHosts.isBlocked(request.url.host)) {
            stats.record()
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(204)
                .message("Blocked by Duetify Shields")
                .body(ByteArray(0).toResponseBody(null))
                .build()
        }
        return chain.proceed(request)
    }
}
