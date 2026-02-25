package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.media.MediaRecorder
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.Part
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced voice service using Gemini 2.5 Flash for real-time audio processing
 */
@Singleton
class GeminiAudioService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    
    /**
     * Uses Gemini 2.5 Flash for audio transcription
     */
    suspend fun transcribeAudioRealTime(apiKey: String, audioData: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            val content = Content.fromParts(
                Part.fromText("Transcribe this audio clip for a TV remote control. Return only the transcribed text."),
                Part.builder().inlineData(Blob.builder().mimeType("audio/3gp").data(audioData).build()).build()
            )
            
            val response = client.models.generateContent("gemini-2.5-flash", content, null)
            Result.success(response.text() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Enhanced command processing using Gemini 2.5 Flash for reasoning
     */
    suspend fun processAdvancedCommand(apiKey: String, command: String): Result<VoiceCommand> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            val prompt = """
                Analyze this TV remote command and categorize it.
                Command: "$command"
                
                Return JSON with:
                - "action": ["search", "launch", "navigate", "media", "volume", "text", "settings", "power"]
                - "text": content
                - "confidence": score 0-1
            """.trimIndent()
            
            val response = client.models.generateContent("gemini-2.5-flash", prompt, null)
            val jsonResponse = response.text() ?: ""
            
            val action = extractJsonValue(jsonResponse, "action") ?: "text"
            val text = extractJsonValue(jsonResponse, "text") ?: command
            val confidence = extractJsonValue(jsonResponse, "confidence")?.toFloatOrNull() ?: 0.5f
            
            Result.success(VoiceCommand(action, text, confidence))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Start recording
     */
    fun startRecording(): Result<String> = try {
        audioFile = File(context.cacheDir, "adv_voice_${System.currentTimeMillis()}.3gp")
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
        
        Result.success("Recording started")
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Stop recording
     */
    fun stopRecording(): Result<ByteArray> = try {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        
        val audioData = audioFile?.readBytes() ?: byteArrayOf()
        audioFile?.delete()
        
        Result.success(audioData)
    } catch (e: Exception) {
        Result.failure(e)
    } finally {
        mediaRecorder = null
        audioFile = null
    }
    
    private fun extractJsonValue(json: String, key: String): String? {
        return try {
            val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val matchResult = pattern.find(json)
            matchResult?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
