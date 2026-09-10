package org.openflux.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.openflux.app.LocalOpenFluxApp
import org.openflux.app.R
import org.openflux.app.data.Profile
import org.openflux.app.data.ProfileRepository
import org.openflux.app.data.SettingsRepository
import org.openflux.app.ui.profiles.ProfileRow
import org.openflux.app.vpn.OpenFluxVpnService
import org.openflux.app.vpn.TunnelStatus

internal data class HomeState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String? = null,
) {
    val activeProfile: Profile? get() = profiles.find { it.id == activeProfileId }
}

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    internal val state: StateFlow<HomeState> = combine(
        profileRepository.observeAll(),
        settingsRepository.activeProfileId,
    ) { profiles, activeId -> HomeState(profiles, activeId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState(),
        )

    fun setActive(id: String) {
        viewModelScope.launch { settingsRepository.setActiveProfileId(id) }
    }
}

@Composable
fun HomeScreen(
    onConnectRequested: (String) -> Unit,
    onDisconnectRequested: () -> Unit,
    onManageProfiles: () -> Unit,
    onEditProfile: (String) -> Unit,
) {
    val app = LocalOpenFluxApp.current
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel(app.profileRepository, app.settingsRepository) }
        },
    )

    val homeState by viewModel.state.collectAsState()
    val activeProfile = homeState.activeProfile
    val status by OpenFluxVpnService.callback.status.collectAsState()
    val stats by OpenFluxVpnService.callback.stats.collectAsState()
    val connected = status is TunnelStatus.Connected || status is TunnelStatus.Connecting

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
    ) {
        Text(statusLabel(status), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Text(
            if (activeProfile != null) {
                stringResource(R.string.home_active_profile, activeProfile.name)
            } else {
                stringResource(R.string.home_no_profile)
            },
        )
        Spacer(Modifier.height(16.dp))

        if (connected) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.home_traffic_sent, formatBytes(stats.bytesSent)))
                    Text(stringResource(R.string.home_traffic_received, formatBytes(stats.bytesReceived)))
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (connected) {
            OutlinedButton(onClick = onDisconnectRequested, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_disconnect))
            }
        } else {
            Button(
                onClick = { activeProfile?.let { onConnectRequested(it.id) } },
                enabled = activeProfile != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.home_connect))
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.nav_profiles), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (homeState.profiles.isEmpty()) {
            OutlinedButton(onClick = onManageProfiles, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.profiles_add))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(homeState.profiles, key = { it.id }) { profile ->
                    ProfileRow(
                        profile = profile,
                        active = profile.id == homeState.activeProfileId,
                        onSelect = { viewModel.setActive(profile.id) },
                        onEdit = { onEditProfile(profile.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: TunnelStatus): String = when (status) {
    is TunnelStatus.Stopped -> stringResource(R.string.home_status_stopped)
    is TunnelStatus.Connecting -> stringResource(R.string.home_status_connecting)
    is TunnelStatus.Connected -> stringResource(R.string.home_status_connected)
    is TunnelStatus.Error -> stringResource(R.string.home_status_error, status.message)
}

private fun formatBytes(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(value, units[unitIndex])
}
