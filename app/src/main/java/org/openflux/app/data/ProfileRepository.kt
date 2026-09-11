package org.openflux.app.data

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class ProfileRepository(
    private val dao: ProfileDao,
    private val secrets: SecretsStore,
) {
    fun observeAll(): Flow<List<Profile>> =
        dao.observeAll().map { entities -> entities.map { it.toProfile(secrets.load(it.id)) } }

    suspend fun getById(id: String): Profile? =
        dao.getById(id)?.let { it.toProfile(secrets.load(it.id)) }

    /** Inserts a new profile (empty id) or updates an existing one. */
    suspend fun save(profile: Profile): Profile {
        val id = profile.id.ifBlank { UUID.randomUUID().toString() }
        val existing = if (profile.id.isBlank()) null else dao.getById(id)

        dao.upsert(
            ProfileEntity(
                id = id,
                name = profile.name,
                mode = profile.mode.name,
                manualTransport = profile.manualTransport.name,
                mtu = profile.mtu,
                dnsUpstream = profile.dnsUpstream,
                autoReconnect = profile.autoReconnect,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            ),
        )
        secrets.save(
            id,
            ProfileSecrets(
                controlUrl = profile.controlUrl,
                keyToken = profile.keyToken,
                docUrl = profile.docUrl,
                maxToken = profile.maxToken,
                maxUid = profile.maxUid,
            ),
        )
        return profile.copy(id = id)
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
        secrets.delete(id)
    }

    private fun ProfileEntity.toProfile(s: ProfileSecrets): Profile = Profile(
        id = id,
        name = name,
        mode = runCatching { ProfileMode.valueOf(mode) }.getOrDefault(ProfileMode.MANUAL),
        controlUrl = s.controlUrl,
        keyToken = s.keyToken,
        manualTransport = runCatching { ManualTransport.valueOf(manualTransport) }.getOrDefault(ManualTransport.YANDEX),
        docUrl = s.docUrl,
        maxToken = s.maxToken,
        maxUid = s.maxUid,
        mtu = mtu,
        dnsUpstream = dnsUpstream,
        autoReconnect = autoReconnect,
    )
}

/**
 * Builds the JSON contract the `mobile` Go package's StartTunnel expects
 * (see mobile/mobile.go's Config struct) from this profile.
 */
fun Profile.toStartTunnelConfigJson(): String = JSONObject().apply {
    when (mode) {
        ProfileMode.KEY -> {
            put("mode", "key")
            put("control_url", controlUrl)
            put("key_token", keyToken)
        }
        ProfileMode.MANUAL -> {
            put("mode", "manual")
            when (manualTransport) {
                ManualTransport.YANDEX -> {
                    put("transport", "yandex")
                    put("doc_url", docUrl)
                }
                ManualTransport.MAX -> {
                    put("transport", "max")
                    put("max_token", maxToken)
                    put("max_uid", maxUid)
                }
            }
        }
    }
    put("mtu", mtu)
    put("dns_upstream", dnsUpstream)
}.toString()
