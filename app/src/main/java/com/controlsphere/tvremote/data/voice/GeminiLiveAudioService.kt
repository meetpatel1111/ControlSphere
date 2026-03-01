package com.controlsphere.tvremote.data.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.google.genai.Client
import com.google.genai.types.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Live API implementation using Gemini 2.5 Flash Native Audio for real-time bidirectional voice interaction
 */
@Singleton
class GeminiLiveAudioService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository
) {
    companion object {
        private const val TAG = "LiveAudioService"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
    
    /**
     * Real-time audio streaming using Gemini 2.5 Flash Native Audio Live API
     */
    @SuppressLint("MissingPermission")
    suspend fun startLiveAudioSession(apiKey: String): Flow<LiveAudioResult> = callbackFlow {
        trySend(LiveAudioResult.Status("Initializing Live API session..."))
        
        var audioRecord: AudioRecord? = null
        var isSessionActive = true
        var videoJob: Job? = null
        var audioJob: Job? = null
        
        val client = Client.builder()
            .apiKey(apiKey)
            .build()
            
        try {
            // Configure the Live API settings
            val config = LiveConnectConfig.builder()
                .build() // Use default options
                
            val futureSession = client.async.live.connect(VoiceConfig.LIVE_AUDIO_MODEL, config)
            val session = futureSession.get() // Wait for connection

            trySend(LiveAudioResult.Status("Live session ready, start speaking..."))

            // Handle incoming server messages
            session.receive { message ->
                try {
                    message.serverContent().ifPresent { content ->
                        content.modelTurn().ifPresent { turn ->
                            turn.parts().ifPresent { partsList ->
                                partsList.forEach { part ->
                                    // Text transcripts
                                    part.text().ifPresent { textChunk ->
                                        trySend(LiveAudioResult.Transcription(textChunk))
                                    }
                                    
                                    // Audio responses
                                    part.inlineData().ifPresent { blob ->
                                        blob.data().ifPresent { data ->
                                            if (data.isNotEmpty()) {
                                                trySend(LiveAudioResult.AudioResponse(data))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing server message", e)
                }
            }
            
            // 1. Start Audio Recording (Microphone)
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 4
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording()
                audioJob = launch(Dispatchers.IO) {
                    val buffer = ByteArray(bufferSize)
                    while (isActive && isSessionActive) {
                        val readResult = audioRecord.read(buffer, 0, buffer.size)
                        if (readResult > 0) {
                            val chunkBytes = buffer.copyOf(readResult)
                            val audioBlob = Blob.builder()
                                .mimeType("audio/pcm;rate=16000")
                                .data(chunkBytes)
                                .build()
                            
                            val inputParams = LiveSendRealtimeInputParameters.builder()
                                .audio(audioBlob)
                                .build()
                                
                            try {
                                session.sendRealtimeInput(inputParams)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error sending audio chunk", e)
                                isSessionActive = false
                            }
                        }
                    }
                }
            } else {
                trySend(LiveAudioResult.Error("Failed to initialize microphone"))
            }

            // 2. Start Video Recording (TV Screen Polling at ~1 FPS)
            videoJob = launch(Dispatchers.IO) {
                while (isActive && isSessionActive) {
                    try {
                        val screenshotResult = deviceRepository.captureScreen()
                        if (screenshotResult.isSuccess) {
                            val jpegBytes = screenshotResult.getOrNull()
                            if (jpegBytes != null && jpegBytes.isNotEmpty()) {
                                val videoBlob = Blob.builder()
                                    .mimeType("image/jpeg")
                                    .data(jpegBytes)
                                    .build()
                                
                                val videoParams = LiveSendRealtimeInputParameters.builder()
                                    .video(videoBlob)
                                    .build()
                                
                                session.sendRealtimeInput(videoParams)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error capturing screen for Live session", e)
                    }
                    
                    delay(1000) // 1 FPS
                }
            }
            
            // Wait until cancelled by the UI Flow collection
            awaitCancellation()

        } catch (e: Exception) {
            trySend(LiveAudioResult.Error("Live API error: ${e.message}"))
        } finally {
            isSessionActive = false
            audioJob?.cancel()
            videoJob?.cancel()
            audioRecord?.stop()
            audioRecord?.release()
        }
    }.flowOn(Dispatchers.IO)

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
            
            val response = client.models.generateContent(VoiceConfig.TTS_MODEL, content, null)
            
            var audioData = byteArrayOf()
            response.candidates().ifPresent { candidates ->
                candidates.firstOrNull()?.content()?.ifPresent { c ->
                    c.parts().ifPresent { parts ->
                        parts.firstOrNull()?.inlineData()?.ifPresent { blob ->
                            blob.data().ifPresent { data ->
                                audioData = data
                            }
                        }
                    }
                }
            }
            
            Result.success(audioData)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
