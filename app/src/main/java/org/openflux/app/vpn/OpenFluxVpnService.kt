package org.openflux.app.vpn

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mobile.Mobile
import org.openflux.app.MainActivity
import org.openflux.app.OpenFluxApplication
import org.openflux.app.R
import org.openflux.app.data.toStartTunnelConfigJson

/**
 * Owns the single running tunnel. Only one profile can be connected at a
 * time (matches how a device has one active VPN connection), so tunnel
 * state - and the [MobileCallback] the UI observes - lives on the
 * companion object rather than needing a bound-service/Messenger dance.
 */
class OpenFluxVpnService : VpnService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var establishedFd: Int? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                disconnect()
                return START_NOT_STICKY
            }
            ACTION_CONNECT -> {
                val profileId = intent.getStringExtra(EXTRA_PROFILE_ID)
                if (profileId != null) connect(profileId)
                return START_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun connect(profileId: String) {
        startForeground(NOTIFICATION_ID, buildNotification(getString(R.string.vpn_notification_connecting)))
        callback.reset()

        serviceScope.launch {
            val app = application as OpenFluxApplication
            val profile = app.profileRepository.getById(profileId)
            if (profile == null) {
                callback.onStatus("error:profile not found")
                stopSelf()
                return@launch
            }

            val builder = Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(profile.mtu)
                .addAddress(VPN_ADDRESS_V4, 24)
                .addAddress(VPN_ADDRESS_V6, 128)
                .addRoute("0.0.0.0", 0)
                // No IPv6 data path exists yet (see gateway package) - routing
                // it here too means it's captured and dropped, not leaked
                // outside the tunnel.
                .addRoute("::", 0)
                .addDnsServer(profile.dnsUpstream)

            val pfd = try {
                builder.establish()
            } catch (t: Throwable) {
                callback.onStatus("error:${t.message}")
                stopSelf()
                return@launch
            }
            if (pfd == null) {
                callback.onStatus("error:VPN permission not granted")
                stopSelf()
                return@launch
            }

            // detachFd() hands raw fd ownership to Go; Mobile.stopTunnel()
            // closes it on disconnect. Do not use `pfd` after this point.
            val fd = pfd.detachFd()
            establishedFd = fd

            try {
                Mobile.startTunnel(fd.toLong(), profile.toStartTunnelConfigJson(), callback)
            } catch (t: Throwable) {
                callback.onStatus("error:${t.message}")
                stopSelf()
                return@launch
            }

            updateNotification(getString(R.string.vpn_notification_connected, profile.name))
        }
    }

    private fun disconnect() {
        serviceScope.launch {
            runCatching { Mobile.stopTunnel() }
            establishedFd = null
            callback.onStatus("stopped")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onRevoke() {
        disconnect()
        super.onRevoke()
    }

    override fun onDestroy() {
        runCatching { Mobile.stopTunnel() }
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val disconnectIntent = PendingIntent.getService(
            this, 0, Intent(this, OpenFluxVpnService::class.java).setAction(ACTION_DISCONNECT),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, OpenFluxApplication.VPN_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(openApp)
            .addAction(0, getString(R.string.vpn_notification_disconnect_action), disconnectIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        const val ACTION_CONNECT = "org.openflux.app.action.CONNECT"
        const val ACTION_DISCONNECT = "org.openflux.app.action.DISCONNECT"
        const val EXTRA_PROFILE_ID = "profile_id"

        private const val NOTIFICATION_ID = 1
        private const val VPN_ADDRESS_V4 = "10.111.0.2"
        private const val VPN_ADDRESS_V6 = "fd00:6f70:666c::2"

        /**
         * Shared across the app's lifetime: the UI observes this to render
         * connection status/traffic regardless of whether an Activity is
         * currently bound to the service.
         */
        val callback = MobileCallback()
    }
}
