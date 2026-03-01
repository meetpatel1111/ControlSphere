package com.controlsphere.tvremote.data.voice

import android.content.Context
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini-based Text-to-Speech service using Gemini 2.5 Flash TTS and Pro TTS
 */
@Singleton
class GeminiTTSService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlaybackManager: AudioPlaybackManager
) {
    
    /**
     * Generate speech from text using Gemini 2.5 Flash TTS (fast, cost-effective)
     */
    suspend fun generateSpeech(
        apiKey: String,
        text: String,
        voiceName: String = "Kore",
        stylePrompt: String = ""
    ): Result<TTSResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            val fullPrompt = if (stylePrompt.isNotBlank()) {
                "$stylePrompt\n\n$text"
            } else {
                text
            }
            
            val config = GenerateContentConfig.builder()
                .responseMimeType("audio/mp3")
                .build()
            
            val response = client.models.generateContent(VoiceConfig.TTS_MODEL, fullPrompt, config)
            
            // Extract audio data from response
            val audioData: ByteArray = (response.candidates().get().firstOrNull()
                ?.content()?.get()?.parts()?.get()?.firstOrNull()
                ?.inlineData()?.get()?.data() as? ByteArray) ?: byteArrayOf()
            
            val result = TTSResult(
                audioData = audioData,
                duration = estimateDuration(text),
                voiceStyle = VoiceStyle.fromVoiceName(voiceName),
                text = text,
                voiceName = voiceName
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate high-quality speech using Gemini 2.5 Pro TTS (premium quality)
     */
    suspend fun generateHighQualitySpeech(
        apiKey: String,
        text: String,
        voiceName: String = "Kore",
        stylePrompt: String = ""
    ): Result<TTSResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            val fullPrompt = if (stylePrompt.isNotBlank()) {
                "$stylePrompt\n\n$text"
            } else {
                text
            }
            
            val config = GenerateContentConfig.builder()
                .responseMimeType("audio/mp3")
                .build()
            
            val response = client.models.generateContent("gemini-2.5-pro-preview-tts", fullPrompt, config)
            
            val audioData = (response.candidates().get().firstOrNull()
                ?.content()?.get()?.parts()?.get()?.firstOrNull()
                ?.inlineData()?.get()?.data() as? ByteArray) ?: byteArrayOf()
            
            val result = TTSResult(
                audioData = audioData,
                duration = estimateDuration(text),
                voiceStyle = VoiceStyle.fromVoiceName(voiceName),
                text = text,
                voiceName = voiceName
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate multi-speaker conversation TTS
     */
    suspend fun generateMultiSpeakerSpeech(
        apiKey: String,
        conversation: String,
        speakerConfigs: Map<String, String> // speakerName -> voiceName
    ): Result<TTSResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            val config = GenerateContentConfig.builder()
                .responseMimeType("audio/mp3")
                .build()
            
            val response = client.models.generateContent(VoiceConfig.TTS_MODEL, conversation, config)
            
            val audioData = (response.candidates().get().firstOrNull()
                ?.content()?.get()?.parts()?.get()?.firstOrNull()
                ?.inlineData()?.get()?.data() as? ByteArray) ?: byteArrayOf()
            
            Result.success(
                TTSResult(
                    audioData = audioData,
                    duration = estimateDuration(conversation),
                    voiceStyle = VoiceStyle.MULTI_SPEAKER,
                    text = conversation,
                    voiceName = "Multi-Speaker"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate TV response with style control
     */
    suspend fun generateTVResponse(
        apiKey: String,
        action: String,
        result: String,
        voiceName: String = "Kore",
        responseStyle: TVResponseStyle = TVResponseStyle.FRIENDLY
    ): Result<TTSResult> = withContext(Dispatchers.IO) {
        try {
            val stylePrompt = when (responseStyle) {
                TVResponseStyle.FRIENDLY -> "Say cheerfully and friendly:"
                TVResponseStyle.PROFESSIONAL -> "Say professionally and clearly:"
                TVResponseStyle.SUCCESS -> "Say with success and confidence:"
                TVResponseStyle.ERROR -> "Say with apology and helpfulness:"
                TVResponseStyle.NEUTRAL -> "Say neutrally:"
            }
            
            val responseText = buildTVResponseText(action, result)
            
            generateSpeech(
                apiKey = apiKey,
                text = responseText,
                voiceName = voiceName,
                stylePrompt = stylePrompt
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate conversational response with persona
     */
    suspend fun generateConversationalResponse(
        apiKey: String,
        userCommand: String,
        context: String = "",
        persona: ConversationalPersona = ConversationalPersona.FRIENDLY_ASSISTANT
    ): Result<TTSResult> = withContext(Dispatchers.IO) {
        try {
            val personaPrompt = when (persona) {
                ConversationalPersona.FRIENDLY_ASSISTANT -> "Say as a friendly and helpful assistant:"
                ConversationalPersona.PROFESSIONAL_ASSISTANT -> "Say as a professional and efficient assistant:"
                ConversationalPersona.CASUAL_FRIEND -> "Say as a casual friend:"
                ConversationalPersona.ENTHUSIASTIC_GUIDE -> "Say with enthusiasm and energy:"
            }
            
            val responseText = buildConversationalResponse(userCommand, context)
            
            generateSpeech(
                apiKey = apiKey,
                text = responseText,
                voiceName = persona.recommendedVoice,
                stylePrompt = personaPrompt
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate notification with appropriate voice
     */
    suspend fun generateNotification(
        apiKey: String,
        message: String,
        notificationType: NotificationType = NotificationType.INFO
    ): Result<TTSResult> = withContext(Dispatchers.IO) {
        try {
            val (voiceName, stylePrompt) = when (notificationType) {
                NotificationType.SUCCESS -> "Puck" to "Say cheerfully:"
                NotificationType.ERROR -> "Charon" to "Say with concern:"
                NotificationType.WARNING -> "Kore" to "Say with caution:"
                NotificationType.INFO -> "Aoede" to "Say informatively:"
            }
            
            val notificationText = when (notificationType) {
                NotificationType.SUCCESS -> "Success! $message"
                NotificationType.ERROR -> "Error: $message"
                NotificationType.WARNING -> "Warning: $message"
                NotificationType.INFO -> message
            }
            
            generateSpeech(
                apiKey = apiKey,
                text = notificationText,
                voiceName = voiceName,
                stylePrompt = stylePrompt
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildTVResponseText(action: String, result: String): String {
        return when {
            result.contains("success", ignoreCase = true) -> {
                "$action completed successfully."
            }
            result.contains("error", ignoreCase = true) -> {
                "Sorry, I couldn't $action. Please try again."
            }
            action.contains("launch", ignoreCase = true) -> {
                "Launching $result"
            }
            action.contains("search", ignoreCase = true) -> {
                "Searching for $result"
            }
            action.contains("volume", ignoreCase = true) -> {
                "Volume $result"
            }
            else -> {
                "$action: $result"
            }
        }
    }
    
    private fun buildConversationalResponse(userCommand: String, context: String): String {
        val responses = listOf(
            "Got it! $userCommand.",
            "Okay, $userCommand.",
            "Sure, $userCommand.",
            "Right away, $userCommand.",
            "On it, $userCommand."
        )
        
        return responses.random()
    }
    
    private fun estimateDuration(text: String): Long {
        val wordsPerMinute = 140 
        val wordCount = text.split(" ").size
        return (wordCount * 60L / wordsPerMinute * 1000) 
    }
}
