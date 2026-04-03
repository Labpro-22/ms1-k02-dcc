package com.tubes.nimons360.core.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context, "nimons_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String, expiresAt: String) {
        prefs.edit().putString("token", token).putString("expiresAt", expiresAt).apply()
    }
    fun getToken(): String? = prefs.getString("token", null)
    fun getExpiresAt(): String? = prefs.getString("expiresAt", null)
    fun clearToken() = prefs.edit().clear().apply()
    fun isLoggedIn(): Boolean = getToken() != null
}
