package org.openflux.app.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mobile.Callback

sealed interface TunnelStatus {
    data object Stopped : TunnelStatus
    data object Connecting : TunnelStatus
    data object Connected : TunnelStatus
    data class Error(val message: String) : TunnelStatus
}

data class TrafficStats(val bytesSent: Long = 0, val bytesReceived: Long = 0)

/**
 * Implements the gomobile-bound `mobile.Callback` interface (see
 * mobile/mobile.go) and republishes it as Kotlin StateFlows the UI can
 * collect. Go calls these methods from its own goroutines, so both flows
 * are safe to update from any thread.
 */
class MobileCallback : Callback {
    private val _status = MutableStateFlow<TunnelStatus>(TunnelStatus.Stopped)
    val status: StateFlow<TunnelStatus> = _status

    private val _stats = MutableStateFlow(TrafficStats())
    val stats: StateFlow<TrafficStats> = _stats

    override fun onStatus(status: String) {
        _status.value = when {
            status == "connecting" -> TunnelStatus.Connecting
            status == "connected" -> TunnelStatus.Connected
            status == "stopped" -> TunnelStatus.Stopped
            status.startsWith("error:") -> TunnelStatus.Error(status.removePrefix("error:"))
            else -> TunnelStatus.Error(status)
        }
    }

    override fun onStats(bytesSent: Long, bytesReceived: Long) {
        _stats.value = TrafficStats(bytesSent, bytesReceived)
    }

    fun reset() {
        _status.value = TunnelStatus.Stopped
        _stats.value = TrafficStats()
    }
}
