package com.controlsphere.tvremote.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.controlsphere.tvremote.data.voice.VoiceLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preferences including language settings
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "controlsphere_preferences"
        private const val KEY_VOICE_LANGUAGE = "voice_language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_VOICE_FEEDBACK_ENABLED = "voice_feedback_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }
    
    // Language Preferences
    fun getVoiceLanguage(): VoiceLanguage {
        val languageCode = sharedPreferences.getString(KEY_VOICE_LANGUAGE, VoiceLanguage.ENGLISH.code)
        return VoiceLanguage.fromCode(languageCode ?: VoiceLanguage.ENGLISH.code)
    }
    
    fun setVoiceLanguage(language: VoiceLanguage) {
        sharedPreferences.edit()
            .putString(KEY_VOICE_LANGUAGE, language.code)
            .apply()
    }
    
    // Theme Preferences
    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, "system") ?: "system"
    }
    
    fun setThemeMode(mode: String) {
        sharedPreferences.edit()
            .putString(KEY_THEME_MODE, mode)
            .apply()
    }
    
    // Security Preferences
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }
    
    // Voice Feedback Preferences
    fun isVoiceFeedbackEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VOICE_FEEDBACK_ENABLED, true)
    }
    
    fun setVoiceFeedbackEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_VOICE_FEEDBACK_ENABLED, enabled)
            .apply()
    }
    
    // App State
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setFirstLaunchComplete() {
        sharedPreferences.edit()
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .apply()
    }
    
    // Utility Methods
    fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun exportPreferences(): Map<String, Any?> {
        return mapOf(
            KEY_VOICE_LANGUAGE to getVoiceLanguage().code,
            KEY_THEME_MODE to getThemeMode(),
            KEY_BIOMETRIC_ENABLED to isBiometricEnabled(),
            KEY_VOICE_FEEDBACK_ENABLED to isVoiceFeedbackEnabled()
        )
    }
}
