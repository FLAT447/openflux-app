package org.openflux.app.ui.profiles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

internal data class ProfileListState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String? = null,
)

class ProfileListViewModel(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    internal val state: StateFlow<ProfileListState> = combine(
        profileRepository.observeAll(),
        settingsRepository.activeProfileId,
    ) { profiles, activeId -> ProfileListState(profiles, activeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileListState())

    fun setActive(id: String) {
        viewModelScope.launch { settingsRepository.setActiveProfileId(id) }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            profileRepository.delete(id)
            if (state.value.activeProfileId == id) settingsRepository.setActiveProfileId(null)
        }
    }
}

@Composable
fun ProfileListScreen(onAddProfile: () -> Unit, onEditProfile: (String) -> Unit) {
    val app = LocalOpenFluxApp.current
    val viewModel: ProfileListViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ProfileListViewModel(app.profileRepository, app.settingsRepository) }
        },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProfile) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.profiles_add))
            }
        },
    ) { padding ->
        if (state.profiles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.profiles_empty))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(state.profiles, key = { it.id }) { profile ->
                    ProfileRow(
                        profile = profile,
                        active = profile.id == state.activeProfileId,
                        onSelect = { viewModel.setActive(profile.id) },
                        onEdit = { onEditProfile(profile.id) },
                        onDelete = { viewModel.delete(profile.id) },
                    )
                }
            }
        }
    }
}
