package org.openflux.app.data

import android.net.Uri
import android.util.Base64
import java.nio.charset.StandardCharsets
import org.json.JSONObject

/**
 * Lets a profile be shared as a link: openflux://import?data=<base64url-json>.
 *
 * The payload deliberately excludes the profile id - importing always
 * produces a fresh draft - and is only ever decoded into the edit screen's
 * in-memory form for the user to review and explicitly save. A deep link
 * can be triggered by any other app or a web page (the intent-filter is
 * BROWSABLE), so nothing here is persisted or connected to automatically.
 */
object ProfileDeepLink {
    const val SCHEME = "openflux"
    const val HOST = "import"
    private const val DATA_PARAM = "data"

    fun buildUri(profile: Profile): Uri =
        Uri.parse("$SCHEME://$HOST").buildUpon()
            .appendQueryParameter(DATA_PARAM, encode(profile))
            .build()

    fun parse(uri: Uri): Profile? {
        if (!uri.scheme.equals(SCHEME, ignoreCase = true) || uri.host != HOST) return null
        val data = uri.getQueryParameter(DATA_PARAM) ?: return null
        return decode(data)
    }

    private fun encode(profile: Profile): String {
        val json = JSONObject().apply {
            put("name", profile.name)
            put("mode", if (profile.mode == ProfileMode.MANUAL) "manual" else "key")
            put("control_url", profile.controlUrl)
            put("key_token", profile.keyToken)
            put("doc_url", profile.docUrl)
            put("mtu", profile.mtu)
            put("dns_upstream", profile.dnsUpstream)
            put("auto_reconnect", profile.autoReconnect)
        }
        val bytes = json.toString().toByteArray(StandardCharsets.UTF_8)
        return Base64.encodeToString(bytes, BASE64_FLAGS)
    }

    private fun decode(data: String): Profile? = runCatching {
        val bytes = Base64.decode(data, BASE64_FLAGS)
        val json = JSONObject(String(bytes, StandardCharsets.UTF_8))
        Profile(
            id = "",
            name = json.optString("name", ""),
            mode = if (json.optString("mode") == "manual") ProfileMode.MANUAL else ProfileMode.KEY,
            controlUrl = json.optString("control_url", ""),
            keyToken = json.optString("key_token", ""),
            docUrl = json.optString("doc_url", ""),
            mtu = json.optInt("mtu", 1400),
            dnsUpstream = json.optString("dns_upstream", "77.88.8.8"),
            autoReconnect = json.optBoolean("auto_reconnect", true),
        )
    }.getOrNull()

    private val BASE64_FLAGS = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
}
