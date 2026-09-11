package org.openflux.app.ui.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.openflux.app.R
import org.openflux.app.vpn.OpenFluxVpnService
import org.openflux.app.vpn.TunnelLogEntry
import org.openflux.app.vpn.TunnelLogKind

/**
 * Shows the tunnel's connection activity: overall start/stop/error plus
 * every individual (re)connection attempt the transport makes in the
 * background - see MobileCallback.onLogEvent, fed by transport.Event* in
 * the Go engine. Newest entry first, since this is meant for "what's
 * happening right now", not a scroll-to-the-bottom transcript.
 */
@Composable
fun TunnelLogsScreen() {
    val entries by OpenFluxVpnService.callback.log.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.nav_logs), style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { OpenFluxVpnService.callback.clearLog() }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.logs_clear))
            }
        }

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.logs_empty))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(top = 8.dp)) {
                items(entries.asReversed()) { entry -> LogRow(entry) }
            }
        }
    }
}

@Composable
private fun LogRow(entry: TunnelLogEntry) {
    val tone = toneFor(entry.kind)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(tone.icon, contentDescription = null, tint = tone.color, modifier = Modifier.padding(top = 2.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(logText(entry))
            Text(
                formatTime(entry.timestampMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class LogTone(val icon: ImageVector, val color: Color)

@Composable
private fun toneFor(kind: TunnelLogKind): LogTone = when (kind) {
    TunnelLogKind.STARTING, TunnelLogKind.ATTEMPT_CONNECTING ->
        LogTone(Icons.Filled.Info, MaterialTheme.colorScheme.onSurfaceVariant)
    TunnelLogKind.STARTED, TunnelLogKind.ATTEMPT_CONNECTED ->
        LogTone(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.primary)
    TunnelLogKind.STOPPED ->
        LogTone(Icons.Filled.Info, MaterialTheme.colorScheme.onSurfaceVariant)
    TunnelLogKind.ATTEMPT_RETRY ->
        LogTone(Icons.Filled.Warning, MaterialTheme.colorScheme.tertiary)
    TunnelLogKind.ERROR ->
        LogTone(Icons.Filled.Close, MaterialTheme.colorScheme.error)
}

@Composable
private fun logText(entry: TunnelLogEntry): String = when (entry.kind) {
    TunnelLogKind.STARTING -> stringResource(R.string.logs_starting)
    TunnelLogKind.STARTED -> stringResource(R.string.logs_started)
    TunnelLogKind.STOPPED -> stringResource(R.string.logs_stopped)
    TunnelLogKind.ERROR -> stringResource(R.string.logs_error, entry.detail)
    TunnelLogKind.ATTEMPT_CONNECTING -> stringResource(R.string.logs_attempt_connecting, entry.attempt)
    TunnelLogKind.ATTEMPT_CONNECTED -> stringResource(R.string.logs_attempt_connected, entry.attempt)
    TunnelLogKind.ATTEMPT_RETRY -> stringResource(
        R.string.logs_attempt_retry,
        entry.attempt,
        reasonLabel(entry.reasonCode),
        entry.delaySeconds,
    )
}

@Composable
private fun reasonLabel(code: String): String = when (code) {
    "fetch_failed" -> stringResource(R.string.logs_reason_fetch_failed)
    "dial_failed" -> stringResource(R.string.logs_reason_dial_failed)
    "handshake_failed" -> stringResource(R.string.logs_reason_handshake_failed)
    "send_failed" -> stringResource(R.string.logs_reason_send_failed)
    "read_error" -> stringResource(R.string.logs_reason_read_error)
    else -> code
}

private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

private fun formatTime(millis: Long): String = timeFormatter.format(Date(millis))
