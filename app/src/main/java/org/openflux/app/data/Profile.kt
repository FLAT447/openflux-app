package org.openflux.app.data

enum class ProfileMode { KEY, MANUAL }

/**
 * A profile as the rest of the app sees it: [ProfileEntity]'s non-secret
 * fields joined with the secret material [SecretsStore] holds for the same
 * id. Never persisted as a single object - see ProfileRepository.
 *
 * Only the Yandex Docs transport is supported here (mobile/mobile.go
 * intentionally drops the MAX/"oneme" transport - see its package comment),
 * so there is no transport field to choose.
 */
data class Profile(
    val id: String,
    val name: String,
    val mode: ProfileMode,
    val controlUrl: String = "",
    val keyToken: String = "",
    val docUrl: String = "",
    val mtu: Int = 1400,
    val dnsUpstream: String = "77.88.8.8",
    val autoReconnect: Boolean = true,
)
