package com.controlsphere.tvremote.data.voice

import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.domain.model.KeyEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepository @Inject constructor(
    private val voiceService: VoiceService,
    private val geminiAudioService: GeminiAudioService,
    private val geminiLiveAudioService: GeminiLiveAudioService,
    private val geminiTTSService: GeminiTTSService,
    private val geminiAudioAnalysisService: GeminiAudioAnalysisService,
    private val audioPlaybackManager: AudioPlaybackManager,
    private val deviceRepository: DeviceRepository
) {
    
    fun getRecordingState(): StateFlow<RecordingState> = voiceService.getRecordingState()
    
    fun getPlaybackState(): Flow<PlaybackState> = audioPlaybackManager.playbackState
    fun getCurrentAudio(): Flow<TTSResult?> = audioPlaybackManager.currentAudio
    
    suspend fun startAdvancedVoiceCommand(apiKey: String): Result<VoiceCommandResult> {
        return try {
            // Use advanced audio service for better processing
            val recordResult = geminiAudioService.startRecording()
            if (!recordResult.isSuccess) {
                return Result.failure(recordResult.exceptionOrNull() ?: Exception("Failed to start recording"))
            }
            
            // Stop recording and get audio data
            val audioDataResult = geminiAudioService.stopRecording()
            if (!audioDataResult.isSuccess) {
                return Result.failure(audioDataResult.exceptionOrNull() ?: Exception("Failed to stop recording"))
            }
            
            // Use real-time transcription if available
            val transcribeResult = if (VoiceConfig.FeatureFlags.ENABLE_REAL_TIME_AUDIO) {
                geminiAudioService.transcribeAudioRealTime(apiKey, audioDataResult.getOrNull() ?: byteArrayOf())
            } else {
                voiceService.transcribeAudio(apiKey, audioDataResult.getOrNull() ?: byteArrayOf())
            }
            
            if (!transcribeResult.isSuccess) {
                return Result.failure(transcribeResult.exceptionOrNull() ?: Exception("Failed to transcribe audio"))
            }
            
            val transcribedText = transcribeResult.getOrNull().orEmpty()
            if (transcribedText.isBlank()) {
                return Result.failure(Exception("No speech detected"))
            }
            
            // Use advanced command processing
            val processResult = if (VoiceConfig.FeatureFlags.ENABLE_ADVANCED_REASONING) {
                geminiAudioService.processAdvancedCommand(apiKey, transcribedText)
            } else {
                voiceService.processVoiceCommand(apiKey, transcribedText)
            }
            
            if (!processResult.isSuccess) {
                return Result.failure(processResult.exceptionOrNull() ?: Exception("Failed to process command"))
            }
            
            val voiceCommand = processResult.getOrNull()
            
            // Execute the command
            val executionResult = executeVoiceCommand(voiceCommand)
            
            Result.success(VoiceCommandResult(
                transcribedText = transcribedText,
                command = voiceCommand,
                executionSuccess = executionResult.isSuccess,
                executionError = if (!executionResult.isSuccess) executionResult.exceptionOrNull()?.message else null
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startVoiceCommand(apiKey: String): Result<VoiceCommandResult> {
        return try {
            // Start recording
            val recordResult = voiceService.startRecording()
            if (!recordResult.isSuccess) {
                return Result.failure(recordResult.exceptionOrNull() ?: Exception("Failed to start recording"))
            }
            
            // Stop recording and get audio data
            val audioDataResult = voiceService.stopRecording()
            if (!audioDataResult.isSuccess) {
                return Result.failure(audioDataResult.exceptionOrNull() ?: Exception("Failed to stop recording"))
            }
            
            // Transcribe audio
            val transcribeResult = voiceService.transcribeAudio(apiKey, audioDataResult.getOrNull() ?: byteArrayOf())
            if (!transcribeResult.isSuccess) {
                return Result.failure(transcribeResult.exceptionOrNull() ?: Exception("Failed to transcribe audio"))
            }
            
            val transcribedText = transcribeResult.getOrNull().orEmpty()
            if (transcribedText.isBlank()) {
                return Result.failure(Exception("No speech detected"))
            }
            
            // Process voice command
            val processResult = voiceService.processVoiceCommand(apiKey, transcribedText)
            if (!processResult.isSuccess) {
                return Result.failure(processResult.exceptionOrNull() ?: Exception("Failed to process command"))
            }
            
            val voiceCommand = processResult.getOrNull()
            
            // Execute the command
            val executionResult = executeVoiceCommand(voiceCommand)
            
            Result.success(VoiceCommandResult(
                transcribedText = transcribedText,
                command = voiceCommand,
                executionSuccess = executionResult.isSuccess,
                executionError = if (!executionResult.isSuccess) executionResult.exceptionOrNull()?.message else null
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun executeVoiceCommand(command: VoiceCommand?): Result<Unit> {
        if (command == null || command.confidence < 0.5f) {
            return Result.failure(Exception("Low confidence command"))
        }
        
        return when (command.action) {
            "search" -> {
                deviceRepository.sendText(command.text)
                deviceRepository.sendKeyEvent(KeyEvent.ENTER)
                Result.success(Unit)
            }
            
            "launch" -> {
                // Try to find app by package name or label
                deviceRepository.launchApp(command.text.lowercase())
            }
            
            "media" -> {
                when (command.text.lowercase()) {
                    "play", "pause" -> deviceRepository.sendKeyEvent(KeyEvent.PLAY_PAUSE)
                    "next", "forward" -> deviceRepository.sendKeyEvent(KeyEvent.MEDIA_NEXT)
                    "previous", "back" -> deviceRepository.sendKeyEvent(KeyEvent.MEDIA_PREVIOUS)
                    "rewind" -> deviceRepository.sendKeyEvent(KeyEvent.REWIND)
                    "fast forward" -> deviceRepository.sendKeyEvent(KeyEvent.FAST_FORWARD)
                    else -> Result.failure(Exception("Unknown media command: ${command.text}"))
                }
            }
            
            "volume" -> {
                when (command.text.lowercase()) {
                    "up", "increase" -> deviceRepository.sendKeyEvent(KeyEvent.VOLUME_UP)
                    "down", "decrease" -> deviceRepository.sendKeyEvent(KeyEvent.VOLUME_DOWN)
                    "mute", "unmute" -> deviceRepository.sendKeyEvent(KeyEvent.MUTE)
                    else -> Result.failure(Exception("Unknown volume command: ${command.text}"))
                }
            }
            
            "navigate" -> {
                when (command.text.lowercase()) {
                    "up" -> deviceRepository.sendKeyEvent(KeyEvent.DPAD_UP)
                    "down" -> deviceRepository.sendKeyEvent(KeyEvent.DPAD_DOWN)
                    "left" -> deviceRepository.sendKeyEvent(KeyEvent.DPAD_LEFT)
                    "right" -> deviceRepository.sendKeyEvent(KeyEvent.DPAD_RIGHT)
                    "select", "ok", "enter" -> deviceRepository.sendKeyEvent(KeyEvent.DPAD_CENTER)
                    "back" -> deviceRepository.sendKeyEvent(KeyEvent.BACK)
                    "home" -> deviceRepository.sendKeyEvent(KeyEvent.HOME)
                    else -> Result.failure(Exception("Unknown navigation command: ${command.text}"))
                }
            }
            
            "power" -> {
                deviceRepository.sendKeyEvent(KeyEvent.POWER)
            }
            
            "text" -> {
                deviceRepository.sendText(command.text)
            }
            
            else -> {
                // Default to sending as text
                deviceRepository.sendText(command.text)
            }
        }
    }
    
    /**
     * Generate and play TTS response for voice commands
     */
    private suspend fun generateAndPlayTTSResponse(
        apiKey: String, 
        voiceCommand: VoiceCommand?, 
        executionResult: Result<Unit>
    ) {
        try {
            val command = voiceCommand ?: return
            val action = command.action
            val result = if (executionResult.isSuccess) "success" else "failed"
            
            val ttsResult = geminiTTSService.generateTVResponse(
                apiKey = apiKey,
                action = action,
                result = result
            )
            
            if (ttsResult.isSuccess) {
                audioPlaybackManager.playTTSResponse(ttsResult.getOrNull()!!)
            }
        } catch (e: Exception) {
            // Ignore TTS errors to not interrupt main functionality
        }
    }
    
    /**
     * Play notification sound
     */
    suspend fun playNotification(notificationType: NotificationType): Result<Unit> {
        return audioPlaybackManager.playNotificationSound(notificationType)
    }
    
    /**
     * Stop audio playback
     */
    suspend fun stopAudioPlayback() {
        audioPlaybackManager.stopPlayback()
    }
    
    /**
     * Start live voice session with TTS feedback
     */
    suspend fun startLiveVoiceSessionWithTTS(apiKey: String): Flow<LiveAudioResult> {
        return geminiLiveAudioService.startLiveAudioSession(apiKey)
    }
    
    /**
     * Analyze recorded audio with Gemini 3 Flash
     */
    suspend fun analyzeRecordedAudio(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3",
        analysisType: AudioAnalysisType = AudioAnalysisType.GENERAL
    ): Result<AudioAnalysisResult> {
        return geminiAudioAnalysisService.analyzeAudio(apiKey, audioData, mimeType, analysisType)
    }
    
    /**
     * Transcribe audio to text with speaker diarization
     */
    suspend fun transcribeAudio(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3",
        includeTimestamps: Boolean = true,
        includeEmotionDetection: Boolean = true,
        includeTranslation: Boolean = false
    ): Result<AudioTranscriptionResult> {
        return geminiAudioAnalysisService.transcribeAudio(
            apiKey, audioData, mimeType, includeTimestamps, includeEmotionDetection, includeTranslation
        )
    }
    
    /**
     * Detect speakers in audio
     */
    suspend fun detectSpeakers(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3"
    ): Result<SpeakerDiarizationResult> {
        return geminiAudioAnalysisService.detectSpeakers(apiKey, audioData, mimeType)
    }
    
    /**
     * Get transcript of specific audio segment
     */
    suspend fun getSegmentTranscript(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3",
        startTime: String,
        endTime: String
    ): Result<SegmentTranscriptResult> {
        return geminiAudioAnalysisService.getSegmentTranscript(apiKey, audioData, mimeType, startTime, endTime)
    }
}
