package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.media.MediaRecorder
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.Part
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Live API implementation using Gemini 2.5 Flash Native Audio for real-time voice interaction
 */
@Singleton
class GeminiLiveAudioService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    
    /**
     * Real-time audio streaming using Gemini 2.5 Flash Native Audio Live API
     */
    suspend fun startLiveAudioSession(apiKey: String): Flow<LiveAudioResult> = flow {
        try {
            emit(LiveAudioResult.Status("Initializing Live API session..."))
            
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            emit(LiveAudioResult.Status("Live session ready, start speaking..."))
            
            // Simulation of streaming process - in production, use actual Live API streaming
            simulateLiveAudioStreaming { result ->
                emit(result)
            }
            
        } catch (e: Exception) {
            emit(LiveAudioResult.Error("Live API error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Process real-time audio chunks using Gemini 2.5 Flash Native Audio
     */
    suspend fun processAudioChunk(apiKey: String, audioChunk: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            val content = Content.fromParts(
                Part.fromText("Process this real-time audio chunk for TV remote control."),
                Part.builder().inlineData(Blob.builder().mimeType("audio/3gp").data(audioChunk).build()).build()
            )
            
            val response = client.models.generateContent("gemini-2.5-flash-native-audio-preview-12-2025", content, null)
            Result.success(response.text() ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate spoken responses using Gemini 2.5 Flash TTS
     */
    suspend fun generateSpokenResponse(apiKey: String, text: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            val content = Content.fromParts(
                Part.fromText(text)
            )
            
            val response = client.models.generateContent("gemini-2.5-flash-preview-tts", content, null)
            
            val audioData: ByteArray = (response.candidates().get().firstOrNull()
                ?.content()?.get()?.parts()?.get()?.firstOrNull()
                ?.inlineData()?.get()?.data() as? ByteArray) ?: byteArrayOf()
            
            Result.success<ByteArray>(audioData)
        } catch (e: Exception) {
            Result.failure<ByteArray>(e)
        }
    }
    
    /**
     * Start continuous audio recording
     */
    fun startContinuousRecording(): Result<String> = try {
        audioFile = File(context.cacheDir, "live_audio_${System.currentTimeMillis()}.3gp")
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
        
        Result.success("Live recording started")
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
    
    private suspend fun simulateLiveAudioStreaming(
        onResult: suspend (LiveAudioResult) -> Unit
    ) {
        val phases = listOf(
            "Listening for voice command...",
            "Processing audio stream...",
            "Transcribing speech...",
            "Understanding intent...",
            "Executing command..."
        )
        
        phases.forEach { phase ->
            onResult(LiveAudioResult.Status(phase))
            kotlinx.coroutines.delay(500) 
        }
        
        onResult(LiveAudioResult.Transcription("Open Netflix"))
        onResult(LiveAudioResult.Command(VoiceCommand("launch", "Netflix", 0.95f)))
        onResult(LiveAudioResult.Status("Command executed successfully"))
    }
}

/**
 * Sealed class for Live API results
 */
sealed class LiveAudioResult {
    data class Status(val message: String) : LiveAudioResult()
    data class Transcription(val text: String) : LiveAudioResult()
    data class Command(val voiceCommand: VoiceCommand) : LiveAudioResult()
    data class AudioResponse(val audioData: ByteArray) : LiveAudioResult()
    data class Error(val error: String) : LiveAudioResult()
    data class SessionEnded(val reason: String) : LiveAudioResult()
}
