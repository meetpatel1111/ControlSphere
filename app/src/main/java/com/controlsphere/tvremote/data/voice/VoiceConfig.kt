package com.controlsphere.tvremote.data.voice

/**
 * Configuration for different Gemini models and their capabilities
 * Updated to latest Gemini model catalog (March 2026)
 */
object VoiceConfig {
    
    // Primary models for voice processing
    const val TRANSCRIPTION_MODEL = "gemini-flash-latest"              // Fast, cost-efficient transcription
    const val COMMAND_PROCESSING_MODEL = "gemini-flash-latest"         // Low-latency command parsing
    const val ADVANCED_REASONING_MODEL = "gemini-2.5-pro"           // Complex reasoning & multi-step tasks
    const val LIVE_AUDIO_MODEL = "gemini-2.5-flash-native-audio-preview-12-2025"  // Real-time bidirectional audio
    const val REAL_TIME_AUDIO_MODEL = "gemini-2.5-flash-native-audio-preview-12-2025" // Live API streaming
    const val TTS_MODEL = "gemini-2.5-flash-preview-tts"            // Controllable text-to-speech
    const val COMPUTER_USE_MODEL = "gemini-2.5-computer-use-preview-10-2025" // Screen vision & UI automation
    
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
        var ENABLE_REAL_TIME_AUDIO = true
        var ENABLE_NATIVE_AUDIO_STREAMING = true
        var ENABLE_TTS_FEEDBACK = true
        var ENABLE_ADVANCED_REASONING = true
        var ENABLE_LIVE_API = true
        var ENABLE_CONTINUOUS_LISTENING = false
        var ENABLE_COMPUTER_USE = true
        var ENABLE_VISUAL_AUTOMATION = true
        var ENABLE_SMART_NAVIGATION = true
    }
}

