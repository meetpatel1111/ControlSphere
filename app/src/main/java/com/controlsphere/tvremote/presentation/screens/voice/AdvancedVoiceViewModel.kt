package com.controlsphere.tvremote.presentation.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvancedVoiceViewModel @Inject constructor(
    private val advancedVoiceRepository: AdvancedVoiceRepository,
    private val advancedVoiceService: AdvancedVoiceService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdvancedVoiceUiState())
    val uiState: StateFlow<AdvancedVoiceUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Collect voice profiles
            advancedVoiceRepository.voiceProfiles.collect { profiles ->
                _uiState.value = _uiState.value.copy(voiceProfiles = profiles)
            }
        }
        
        viewModelScope.launch {
            // Collect current profile
            advancedVoiceRepository.currentProfile.collect { profile ->
                _uiState.value = _uiState.value.copy(currentProfile = profile)
            }
        }
        
        viewModelScope.launch {
            // Collect ambient mode config
            advancedVoiceRepository.ambientModeConfig.collect { config ->
                _uiState.value = _uiState.value.copy(ambientModeConfig = config)
            }
        }
        
        viewModelScope.launch {
            // Collect wake word state
            advancedVoiceRepository.wakeWordState.collect { state ->
                _uiState.value = _uiState.value.copy(wakeWordState = state)
            }
        }
        
        viewModelScope.launch {
            // Collect ambient mode active status
            advancedVoiceService.ambientModeActive.collect { isActive ->
                _uiState.value = _uiState.value.copy(isAmbientModeActive = isActive)
            }
        }
        
        viewModelScope.launch {
            // Collect listening status
            advancedVoiceService.isListening.collect { isListening ->
                _uiState.value = _uiState.value.copy(isListening = isListening)
            }
        }
        
        viewModelScope.launch {
            // Collect current language
            advancedVoiceService.currentLanguage.collect { language ->
                _uiState.value = _uiState.value.copy(currentLanguage = language)
            }
        }
        
        viewModelScope.launch {
            // Collect recognized commands
            advancedVoiceService.recognizedCommand.collect { command ->
                _uiState.value = _uiState.value.copy(
                    recognizedCommand = command,
                    lastRecognitionTime = if (command != null) System.currentTimeMillis() else null
                )
            }
        }
        
        viewModelScope.launch {
            // Collect personalized responses
            advancedVoiceService.personalizedResponse.collect { response ->
                _uiState.value = _uiState.value.copy(personalizedResponse = response)
            }
        }
        
        viewModelScope.launch {
            // Collect multi-language commands
            advancedVoiceRepository.multiLanguageCommands.collect { commands ->
                _uiState.value = _uiState.value.copy(multiLanguageCommands = commands)
            }
        }
        
        viewModelScope.launch {
            // Collect language switch context
            advancedVoiceRepository.languageSwitchContext.collect { context ->
                _uiState.value = _uiState.value.copy(languageSwitchContext = context)
            }
        }
        
        viewModelScope.launch {
            // Collect recognition results
            advancedVoiceRepository.recognitionResults.collect { results ->
                _uiState.value = _uiState.value.copy(recognitionResults = results)
            }
        }
        
        viewModelScope.launch {
            // Collect personalized responses
            advancedVoiceRepository.personalizedResponses.collect { responses ->
                _uiState.value = _uiState.value.copy(personalizedResponses = responses)
            }
        }
    }
    
    // Voice Profile Management
    fun createVoiceProfile(
        name: String,
        userId: String,
        preferredLanguage: VoiceLanguage,
        accent: String? = null,
        pitch: Float = 1.0f,
        speed: Float = 1.0f
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceRepository.createVoiceProfile(
                name = name,
                userId = userId,
                preferredLanguage = preferredLanguage,
                accent = accent,
                pitch = pitch,
                speed = speed
            )
            
            if (result.isSuccess) {
                // Automatically switch to the new profile
                advancedVoiceRepository.switchToProfile(result.getOrNull()!!)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun switchToProfile(profileId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceRepository.switchToProfile(profileId)
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun updateVoiceProfile(profile: VoiceProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceRepository.updateVoiceProfile(profile)
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun deleteVoiceProfile(profileId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceRepository.deleteVoiceProfile(profileId)
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    // Voice Training
    fun startVoiceTraining(profileId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTraining = true)
            val result = advancedVoiceService.startVoiceTraining(profileId)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    trainingComplete = true,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message,
                    trainingComplete = false
                )
            }
            
            _uiState.value = _uiState.value.copy(isTraining = false)
        }
    }
    
    // Ambient Mode Management
    fun enableAmbientMode(
        wakeWord: String = "Hey ControlSphere",
        sensitivity: Float = 0.7f,
        continuousListening: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceRepository.enableAmbientMode(
                wakeWord = wakeWord,
                sensitivity = sensitivity,
                continuousListening = continuousListening
            )
            
            if (result.isSuccess) {
                advancedVoiceService.startAmbientMode()
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun disableAmbientMode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceService.stopAmbientMode()
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun updateAmbientModeConfig(config: AmbientModeConfig) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceRepository.updateAmbientModeConfig(config)
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    // Multi-language Support
    fun switchLanguage(targetLanguage: VoiceLanguage) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.currentProfile
            if (currentProfile != null) {
                val result = advancedVoiceRepository.switchLanguage(
                    previousLanguage = currentProfile.preferredLanguage,
                    newLanguage = targetLanguage,
                    triggerPhrase = "Manual language switch",
                    confidence = 1.0f,
                    context = "User initiated language switch"
                )
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    // Personalized Response Management
    fun createPersonalizedResponse(
        template: String,
        responseType: ResponseType,
        variables: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.currentProfile
            if (currentProfile != null) {
                val result = advancedVoiceRepository.createPersonalizedResponse(
                    template = template,
                    profileId = currentProfile.id,
                    responseType = responseType,
                    variables = variables
                )
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No voice profile selected"
                )
            }
        }
    }
    
    // Custom Command Execution with Multi-language Support
    fun executeCustomCommand(
        commandId: String,
        language: VoiceLanguage = _uiState.value.currentLanguage
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = advancedVoiceService.executeCustomCommand(
                commandId = commandId,
                profileId = _uiState.value.currentProfile?.id,
                language = language
            )
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    lastExecutionResult = result.getOrNull(),
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    // Utility Methods
    fun getProfilesByUser(userId: String): List<VoiceProfile> {
        return advancedVoiceRepository.getProfilesByUser(userId)
    }
    
    fun getRecentRecognitionResults(limit: Int = 10): List<VoiceRecognitionResult> {
        return advancedVoiceRepository.getRecentRecognitionResults(limit)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearRecognizedCommand() {
        _uiState.value = _uiState.value.copy(recognizedCommand = null)
    }
    
    fun clearPersonalizedResponse() {
        _uiState.value = _uiState.value.copy(personalizedResponse = null)
    }
    
    fun resetTrainingState() {
        _uiState.value = _uiState.value.copy(
            trainingComplete = false,
            isTraining = false
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        advancedVoiceService.cleanup()
    }
}

data class AdvancedVoiceUiState(
    // Voice Profiles
    val voiceProfiles: List<VoiceProfile> = emptyList(),
    val currentProfile: VoiceProfile? = null,
    
    // Ambient Mode
    val ambientModeConfig: AmbientModeConfig = AmbientModeConfig(),
    val isAmbientModeActive: Boolean = false,
    val wakeWordState: WakeWordState = WakeWordState.INACTIVE,
    
    // Voice Recognition
    val isListening: Boolean = false,
    val currentLanguage: VoiceLanguage = VoiceLanguage.ENGLISH,
    val recognizedCommand: String? = null,
    val lastRecognitionTime: Long? = null,
    val personalizedResponse: String? = null,
    
    // Multi-language Support
    val multiLanguageCommands: List<MultiLanguageCommand> = emptyList(),
    val languageSwitchContext: LanguageSwitchContext? = null,
    
    // Training and Results
    val isTraining: Boolean = false,
    val trainingComplete: Boolean = false,
    val recognitionResults: List<VoiceRecognitionResult> = emptyList(),
    val personalizedResponses: List<PersonalizedResponse> = emptyList(),
    
    // Execution Results
    val lastExecutionResult: CommandExecutionResult? = null,
    
    // UI State
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
