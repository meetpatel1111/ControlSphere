package com.controlsphere.tvremote.data.voice

/**
 * Configuration for different Gemini models and their capabilities
 */
object VoiceConfig {
    
    // Primary models for voice processing (Updated to 1.5 Flash as standard)
    const val TRANSCRIPTION_MODEL = "gemini-1.5-flash"
    const val COMMAND_PROCESSING_MODEL = "gemini-1.5-flash"
    const val ADVANCED_REASONING_MODEL = "gemini-1.5-flash"
    const val LIVE_AUDIO_MODEL = "gemini-1.5-flash"
    const val REAL_TIME_AUDIO_MODEL = "gemini-1.5-flash"
    const val TTS_MODEL = "gemini-1.5-flash"
    const val COMPUTER_USE_MODEL = "gemini-1.5-flash"
    
    // Model capabilities
    object Capabilities {
        const val SUPPORTS_NATIVE_AUDIO = "native_audio"
        const val SUPPORTS_STREAMING = "streaming"
        const val SUPPORTS_TTS = "text_to_speech"
        const val SUPPORTS_REASONING = "advanced_reasoning"
        const val SUPPORTS_COMPUTER_USE = "computer_use"
        const val SUPPORTS_VISUAL_REASONING = "visual_reasoning"
        const val SUPPORTS_UI_AUTOMATION = "ui_automation"
        const val LOW_LATENCY = "low_latency"
        const val HIGH_ACCURACY = "high_accuracy"
    }
    
    // Feature flags for enabling different capabilities
    object FeatureFlags {
        var ENABLE_REAL_TIME_AUDIO = false
        var ENABLE_NATIVE_AUDIO_STREAMING = false
        var ENABLE_TTS_FEEDBACK = false
        var ENABLE_ADVANCED_REASONING = true
        var ENABLE_LIVE_API = false
        var ENABLE_CONTINUOUS_LISTENING = false
        var ENABLE_COMPUTER_USE = false
        var ENABLE_VISUAL_AUTOMATION = false
        var ENABLE_SMART_NAVIGATION = false
    }
}
