package org.openflux.app.data

enum class ProfileMode { KEY, MANUAL }

// Despite the name, this is set for both profile modes: a KEY-mode profile
// gets it from an imported deep link (or typed in by hand, same as
// docUrl) rather than a live controlplane request - see ProfileEditScreen.
enum class ManualTransport { YANDEX, VOLGA, MAX }

/** The wire name mobile.Config and deep links use for a transport - see mobile/mobile.go. */
fun transportName(t: ManualTransport): String = when (t) {
    ManualTransport.YANDEX -> "yandex"
    ManualTransport.VOLGA -> "volga"
    ManualTransport.MAX -> "max"
}

fun parseTransportName(name: String): ManualTransport = when (name) {
    "volga" -> ManualTransport.VOLGA
    "max" -> ManualTransport.MAX
    else -> ManualTransport.YANDEX
}

/**
 * A profile as the rest of the app sees it: [ProfileEntity]'s non-secret
 * fields joined with the secret material [SecretsStore] holds for the same
 * id. Never persisted as a single object - see ProfileRepository.
 */
data class Profile(
    val id: String,
    val name: String,
    val mode: ProfileMode,
    val controlUrl: String = "",
    val keyToken: String = "",
    val manualTransport: ManualTransport = ManualTransport.YANDEX,
    val docUrl: String = "",
    val maxToken: String = "",
    val maxUid: Long = 0,
    val mtu: Int = 1400,
    val dnsUpstream: String = "77.88.8.8",
    val autoReconnect: Boolean = true,
)
