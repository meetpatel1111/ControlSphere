package com.controlsphere.tvremote.data.voice

/**
 * Supported languages for voice recognition
 */
enum class VoiceLanguage(
    val code: String,
    val displayName: String,
    val locale: String,
    val flag: String
) {
    ENGLISH("en", "English", "en-US", "🇺🇸"),
    SPANISH("es", "Español", "es-ES", "🇪🇸"),
    FRENCH("fr", "Français", "fr-FR", "🇫🇷"),
    GERMAN("de", "Deutsch", "de-DE", "🇩🇪"),
    HINDI("hi", "हिन्दी", "hi-IN", "🇮🇳"),
    CHINESE("zh", "中文", "zh-CN", "🇨🇳"),
    JAPANESE("ja", "日本語", "ja-JP", "🇯🇵"),
    PORTUGUESE("pt", "Português", "pt-BR", "🇧🇷"),
    RUSSIAN("ru", "Русский", "ru-RU", "🇷🇺"),
    ARABIC("ar", "العربية", "ar-SA", "🇸🇦");
    
    companion object {
        fun fromCode(code: String): VoiceLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }
        
        fun getSupportedLanguages(): List<VoiceLanguage> {
            return listOf(ENGLISH, SPANISH, FRENCH, GERMAN, HINDI)
        }
    }
}
