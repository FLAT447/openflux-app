package org.openflux.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import org.openflux.app.LocalOpenFluxApp
import org.openflux.app.R
import org.openflux.app.data.SettingsRepository

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val startOnBoot = repository.startOnBoot
    val defaultMtu = repository.defaultMtu
    val defaultDns = repository.defaultDns

    fun setStartOnBoot(enabled: Boolean) = viewModelScope.launch { repository.setStartOnBoot(enabled) }
    fun setDefaultMtu(mtu: Int) = viewModelScope.launch { repository.setDefaultMtu(mtu) }
    fun setDefaultDns(dns: String) = viewModelScope.launch { repository.setDefaultDns(dns) }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val app = LocalOpenFluxApp.current
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(app.settingsRepository) } },
    )

    val startOnBoot by viewModel.startOnBoot.collectAsState(initial = false)
    val defaultMtu by viewModel.defaultMtu.collectAsState(initial = 1400)
    val defaultDns by viewModel.defaultDns.collectAsState(initial = "77.88.8.8")

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.settings_start_on_boot),
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                Switch(checked = startOnBoot, onCheckedChange = viewModel::setStartOnBoot)
            }

            OutlinedTextField(
                value = defaultMtu.toString(),
                onValueChange = { it.toIntOrNull()?.let(viewModel::setDefaultMtu) },
                label = { Text(stringResource(R.string.settings_default_mtu)) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            OutlinedTextField(
                value = defaultDns,
                onValueChange = viewModel::setDefaultDns,
                label = { Text(stringResource(R.string.settings_default_dns)) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
        }
    }
}
