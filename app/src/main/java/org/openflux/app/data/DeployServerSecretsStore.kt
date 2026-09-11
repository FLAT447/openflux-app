package org.openflux.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Sensitive half of a deploy server: SSH password/private-key/passphrase,
 * plus the admin token and Postgres password the app itself generates for
 * that server's controlplane before every deploy (see DeployServerRepository
 * and deployssh's DeployOptions doc comment for why they're app-generated
 * rather than left for install.sh to invent). A separate encrypted file
 * from SecretsStore's, same Keystore-backed approach.
 */
class DeployServerSecretsStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "openflux_deploy_secrets",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun save(id: String, secrets: DeployServerSecrets) {
        prefs.edit()
            .putString(key(id, "ssh_password"), secrets.sshPassword)
            .putString(key(id, "ssh_private_key"), secrets.sshPrivateKeyPem)
            .putString(key(id, "ssh_passphrase"), secrets.sshPassphrase)
            .putString(key(id, "admin_token"), secrets.adminToken)
            .putString(key(id, "db_password"), secrets.dbPassword)
            .apply()
    }

    fun load(id: String): DeployServerSecrets = DeployServerSecrets(
        sshPassword = prefs.getString(key(id, "ssh_password"), "") ?: "",
        sshPrivateKeyPem = prefs.getString(key(id, "ssh_private_key"), "") ?: "",
        sshPassphrase = prefs.getString(key(id, "ssh_passphrase"), "") ?: "",
        adminToken = prefs.getString(key(id, "admin_token"), "") ?: "",
        dbPassword = prefs.getString(key(id, "db_password"), "") ?: "",
    )

    fun delete(id: String) {
        prefs.edit()
            .remove(key(id, "ssh_password"))
            .remove(key(id, "ssh_private_key"))
            .remove(key(id, "ssh_passphrase"))
            .remove(key(id, "admin_token"))
            .remove(key(id, "db_password"))
            .apply()
    }

    private fun key(id: String, field: String) = "$id.$field"
}

data class DeployServerSecrets(
    val sshPassword: String = "",
    val sshPrivateKeyPem: String = "",
    val sshPassphrase: String = "",
    val adminToken: String = "",
    val dbPassword: String = "",
)
