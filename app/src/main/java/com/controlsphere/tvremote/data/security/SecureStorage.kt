package com.controlsphere.tvremote.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "controlsphere_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveDeviceKey(deviceId: String, publicKey: String) {
        encryptedPrefs.edit()
            .putString("device_key_$deviceId", publicKey)
            .apply()
    }
    
    fun getDeviceKey(deviceId: String): String? {
        return encryptedPrefs.getString("device_key_$deviceId", null)
    }
    
    fun removeDeviceKey(deviceId: String) {
        encryptedPrefs.edit()
            .remove("device_key_$deviceId")
            .apply()
    }
    
    fun saveFavoriteApps(packageNames: Set<String>) {
        encryptedPrefs.edit()
            .putStringSet("favorite_apps", packageNames)
            .apply()
    }
    
    fun getFavoriteApps(): Set<String> {
        return encryptedPrefs.getStringSet("favorite_apps", emptySet()) ?: emptySet()
    }
    
    fun saveRecentSearches(searches: Set<String>) {
        encryptedPrefs.edit()
            .putStringSet("recent_searches", searches)
            .apply()
    }
    
    fun getRecentSearches(): Set<String> {
        return encryptedPrefs.getStringSet("recent_searches", emptySet()) ?: emptySet()
    }
    
    fun clearAllData() {
        encryptedPrefs.edit().clear().apply()
    }
}
