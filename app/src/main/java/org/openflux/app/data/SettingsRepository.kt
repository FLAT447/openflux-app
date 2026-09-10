package org.openflux.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "openflux_settings")

/** App-level (not per-profile) flexible settings. */
class SettingsRepository(private val context: Context) {

    val activeProfileId: Flow<String?> =
        context.dataStore.data.map { it[Keys.ACTIVE_PROFILE_ID] }

    val startOnBoot: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.START_ON_BOOT] ?: false }

    val defaultMtu: Flow<Int> =
        context.dataStore.data.map { it[Keys.DEFAULT_MTU] ?: 1400 }

    val defaultDns: Flow<String> =
        context.dataStore.data.map { it[Keys.DEFAULT_DNS] ?: "77.88.8.8" }

    suspend fun setActiveProfileId(id: String?) {
        context.dataStore.edit {
            if (id == null) it.remove(Keys.ACTIVE_PROFILE_ID) else it[Keys.ACTIVE_PROFILE_ID] = id
        }
    }

    suspend fun setStartOnBoot(enabled: Boolean) {
        context.dataStore.edit { it[Keys.START_ON_BOOT] = enabled }
    }

    suspend fun setDefaultMtu(mtu: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_MTU] = mtu }
    }

    suspend fun setDefaultDns(dns: String) {
        context.dataStore.edit { it[Keys.DEFAULT_DNS] = dns }
    }

    private object Keys {
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
        val DEFAULT_MTU = intPreferencesKey("default_mtu")
        val DEFAULT_DNS = stringPreferencesKey("default_dns")
    }
}
