package com.controlsphere.tvremote.presentation.screens.voice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.security.SecureStorage
import com.controlsphere.tvremote.data.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val secureStorage: SecureStorage,
    private val multiLanguageSpeechService: MultiLanguageSpeechService
) : ViewModel() {

    var uiState by mutableStateOf(VoiceUiState())
        private set

    init {
        // Load API key from secure storage
        uiState = uiState.copy(apiKey = secureStorage.getDeviceKey("gemini_api") ?: "")
        
        // Observe recording state
        viewModelScope.launch {
            voiceRepository.getRecordingState().collect { state ->
                uiState = uiState.copy(recordingState = state)
            }
        }
        
        // Observe playback state
        viewModelScope.launch {
            voiceRepository.getPlaybackState().collect { state ->
                uiState = uiState.copy(playbackState = state)
            }
        }
        
        // Observe current audio
        viewModelScope.launch {
            voiceRepository.getCurrentAudio().collect { audio ->
                uiState = uiState.copy(currentAudio = audio)
            }
        }
    }

    fun startVoiceCommand() {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        uiState = uiState.copy(
            isProcessing = true,
            errorMessage = null,
            lastResult = null
        )

        viewModelScope.launch {
            val result = if (VoiceConfig.FeatureFlags.ENABLE_ADVANCED_REASONING) {
                voiceRepository.startAdvancedVoiceCommand(uiState.apiKey)
            } else {
                voiceRepository.startVoiceCommand(uiState.apiKey)
            }
            
            uiState = uiState.copy(
                isProcessing = false,
                lastResult = result.getOrNull(),
                errorMessage = if (!result.isSuccess) {
                    result.exceptionOrNull()?.message ?: "Voice command failed"
                } else null
            )
        }
    }

    fun saveApiKey(apiKey: String) {
        uiState = uiState.copy(apiKey = apiKey)
        
        // Save to secure storage
        if (apiKey.isNotBlank()) {
            secureStorage.saveDeviceKey("gemini_api", apiKey)
        } else {
            secureStorage.removeDeviceKey("gemini_api")
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
    
    fun generateTTS(text: String, voiceStyle: VoiceStyle, speechRate: SpeechRate) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the TTS service
                // For now, we simulate the TTS generation
                uiState = uiState.copy(
                    currentAudio = TTSResult(
                        audioData = byteArrayOf(),
                        duration = 2000L,
                        voiceStyle = voiceStyle,
                        text = text
                    )
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "TTS generation failed: ${e.message}"
                )
            }
        }
    }
    
    fun generateAdvancedTTS(text: String, voiceName: String, stylePrompt: String) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the advanced TTS service
                uiState = uiState.copy(
                    currentAudio = TTSResult(
                        audioData = byteArrayOf(),
                        duration = 2000L,
                        voiceStyle = VoiceStyle.fromVoiceName(voiceName),
                        text = text,
                        voiceName = voiceName
                    )
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "TTS generation failed: ${e.message}"
                )
            }
        }
    }
    
    fun generateMultiSpeakerTTS(conversation: String, speakerConfigs: Map<String, String>) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the multi-speaker TTS service
                uiState = uiState.copy(
                    currentAudio = TTSResult(
                        audioData = byteArrayOf(),
                        duration = 3000L,
                        voiceStyle = VoiceStyle.MULTI_SPEAKER,
                        text = conversation,
                        voiceName = "Multi-Speaker"
                    )
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Multi-speaker TTS generation failed: ${e.message}"
                )
            }
        }
    }
    
    fun analyzeAudio(audioData: ByteArray, mimeType: String, analysisType: AudioAnalysisType) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the audio analysis service
                uiState = uiState.copy(
                    errorMessage = "Audio analysis feature coming soon!"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Audio analysis failed: ${e.message}"
                )
            }
        }
    }
    
    fun transcribeAudio(
        audioData: ByteArray, 
        mimeType: String, 
        includeTimestamps: Boolean, 
        includeEmotionDetection: Boolean, 
        includeTranslation: Boolean
    ) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the transcription service
                uiState = uiState.copy(
                    errorMessage = "Audio transcription feature coming soon!"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Audio transcription failed: ${e.message}"
                )
            }
        }
    }
    
    fun detectSpeakers(audioData: ByteArray, mimeType: String) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the speaker detection service
                uiState = uiState.copy(
                    errorMessage = "Speaker detection feature coming soon!"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Speaker detection failed: ${e.message}"
                )
            }
        }
    }
    
    fun getSegmentTranscript(audioData: ByteArray, mimeType: String, startTime: String, endTime: String) {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Please set your Gemini API key first"
            )
            return
        }

        viewModelScope.launch {
            try {
                // In a real implementation, this would call the segment transcription service
                uiState = uiState.copy(
                    errorMessage = "Segment transcription feature coming soon!"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Segment transcription failed: ${e.message}"
                )
            }
        }
    }
    
    fun pauseTTS() {
        viewModelScope.launch {
            voiceRepository.stopAudioPlayback()
        }
    }
    
    fun resumeTTS() {
        viewModelScope.launch {
            // Resume playback logic
        }
    }
    
    fun stopTTS() {
        viewModelScope.launch {
            voiceRepository.stopAudioPlayback()
        }
    }
    
    // Multi-language methods
    fun startMultiLanguageVoiceCommand() {
        if (uiState.apiKey.isBlank()) {
            uiState = uiState.copy(errorMessage = "API key required for voice commands")
            return
        }
        
        viewModelScope.launch {
            try {
                uiState = uiState.copy(
                    isProcessing = true,
                    errorMessage = null
                )
                
                // Set the language
                multiLanguageSpeechService.setLanguage(uiState.currentLanguage)
                
                // Start listening for voice input
                val listeningStarted = multiLanguageSpeechService.startListening()
                if (!listeningStarted) {
                    uiState = uiState.copy(
                        isProcessing = false,
                        errorMessage = "Failed to start speech recognition"
                    )
                    return@launch
                }
                
                // Wait for recognition result
                var recognizedText = ""
                var recognitionComplete = false
                
                // Collect recognition results
                multiLanguageSpeechService.recognizedText.collect { text ->
                    recognizedText = text
                }
                
                multiLanguageSpeechService.recognitionState.collect { state ->
                    when (state) {
                        MultiLanguageSpeechService.RecognitionState.SUCCESS -> {
                            recognitionComplete = true
                        }
                        MultiLanguageSpeechService.RecognitionState.ERROR -> {
                            recognitionComplete = true
                        }
                        else -> { /* Continue listening */ }
                    }
                }
                
                // Wait for completion (in a real app, this would be handled with proper coroutine flow)
                kotlinx.coroutines.delay(5000) // Max 5 seconds listening
                
                if (recognizedText.isBlank()) {
                    uiState = uiState.copy(
                        isProcessing = false,
                        errorMessage = "No speech recognized"
                    )
                    return@launch
                }
                
                // Parse the command using multi-language translations
                val action = VoiceCommandTranslations.parseVoiceCommand(recognizedText, uiState.currentLanguage)
                
                if (action == null) {
                    uiState = uiState.copy(
                        isProcessing = false,
                        errorMessage = "Command not recognized: $recognizedText"
                    )
                    return@launch
                }
                
                // Create voice command
                val voiceCommand = VoiceCommand(
                    action = action,
                    text = recognizedText,
                    confidence = 0.8f // Android SpeechRecognizer provides confidence
                )
                
                // Execute the command using existing repository method
                val executionResult = voiceRepository.executeVoiceCommand(voiceCommand)
                
                // Create result
                val commandResult = VoiceCommandResult(
                    transcribedText = recognizedText,
                    command = voiceCommand,
                    executionSuccess = executionResult.isSuccess,
                    executionError = if (executionResult.isSuccess) null else "Command failed"
                )
                
                uiState = uiState.copy(
                    isProcessing = false,
                    lastCommand = voiceCommand,
                    lastResult = commandResult
                )
                
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isProcessing = false,
                    errorMessage = e.message ?: "Voice command failed"
                )
            }
        }
    }
    
    fun setVoiceLanguage(language: VoiceLanguage) {
        multiLanguageSpeechService.setLanguage(language)
        uiState = uiState.copy(currentLanguage = language)
    }
    
    fun getCurrentLanguage(): VoiceLanguage {
        return uiState.currentLanguage
    }
}

data class VoiceUiState(
    val apiKey: String = "",
    val recordingState: RecordingState = RecordingState.IDLE,
    val isProcessing: Boolean = false,
    val lastResult: VoiceCommandResult? = null,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentAudio: TTSResult? = null,
    val errorMessage: String? = null,
    val currentLanguage: VoiceLanguage = VoiceLanguage.ENGLISH,
    val lastCommand: VoiceCommand? = null
)
