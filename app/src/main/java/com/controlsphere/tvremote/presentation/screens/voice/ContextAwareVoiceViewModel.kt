package com.controlsphere.tvremote.presentation.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContextAwareVoiceViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val advancedVoiceRepository: AdvancedVoiceRepository,
    private val customVoiceCommandRepository: CustomVoiceCommandRepository
) : ViewModel() {
    
    // Temporarily create service manually for testing
    private val contextAwareVoiceService = ContextAwareVoiceService(
        voiceRepository = voiceRepository,
        advancedVoiceRepository = advancedVoiceRepository,
        customVoiceCommandRepository = customVoiceCommandRepository
    )
    
    private val _uiState = MutableStateFlow(ContextAwareVoiceUiState())
    val uiState: StateFlow<ContextAwareVoiceUiState> = _uiState.asStateFlow()
    
    // Expose service flows
    val conversationContext = contextAwareVoiceService.conversationContext
    val voiceShortcuts = contextAwareVoiceService.voiceShortcuts
    val sessionStatistics = MutableStateFlow(contextAwareVoiceService.getSessionStatistics())
    
    init {
        // Load initial data
        loadVoiceShortcuts()
        loadCurrentProfile()
        generateContextualSuggestions()
        
        // Collect voice shortcuts
        viewModelScope.launch {
            contextAwareVoiceService.voiceShortcuts.collect { shortcuts ->
                _uiState.update { it.copy(voiceShortcuts = shortcuts) }
            }
        }
    }
    
    private fun loadVoiceShortcuts() {
        viewModelScope.launch {
            // Load existing shortcuts (in real app, this would be from storage)
            val shortcuts = listOf(
                VoiceShortcut(
                    id = "shortcut1",
                    name = "Movie Night",
                    triggerPhrases = listOf("movie time", "let's watch a movie", "movie night"),
                    intents = listOf(
                        CommandIntent(
                            type = IntentType.LAUNCH_APP,
                            parameters = mapOf("app" to "netflix"),
                            confidence = 0.9f
                        ),
                        CommandIntent(
                            type = IntentType.VOLUME_CONTROL,
                            parameters = mapOf("level" to "50%"),
                            confidence = 0.85f
                        )
                    ),
                    createdTime = System.currentTimeMillis(),
                    usageCount = 12
                ),
                VoiceShortcut(
                    id = "shortcut2",
                    name = "Music Mode",
                    triggerPhrases = listOf("play some music", "music time", "spotify"),
                    intents = listOf(
                        CommandIntent(
                            type = IntentType.LAUNCH_APP,
                            parameters = mapOf("app" to "spotify"),
                            confidence = 0.9f
                        ),
                        CommandIntent(
                            type = IntentType.MEDIA_CONTROL,
                            parameters = mapOf("action" to "play"),
                            confidence = 0.85f
                        )
                    ),
                    createdTime = System.currentTimeMillis(),
                    usageCount = 8
                )
            )
            
            // Update service with shortcuts
            shortcuts.forEach { shortcut ->
                contextAwareVoiceService.createVoiceShortcut(
                    shortcut.name,
                    shortcut.triggerPhrases,
                    shortcut.intents
                )
            }
        }
    }
    
    private fun loadCurrentProfile() {
        viewModelScope.launch {
            val profiles = advancedVoiceRepository.voiceProfiles.value
            if (profiles.isNotEmpty()) {
                _uiState.update { it.copy(currentProfile = profiles.first()) }
            }
        }
    }
    
    private fun generateContextualSuggestions() {
        viewModelScope.launch {
            val context = contextAwareVoiceService.conversationContext.value
            val suggestions = mutableListOf<String>()
            
            // Generate suggestions based on context
            if (context.commandHistory.isEmpty()) {
                suggestions.addAll(listOf(
                    "Open Netflix and browse movies",
                    "Search for trending shows",
                    "Set volume to comfortable level"
                ))
            } else {
                val lastCommand = context.lastCommand.lowercase()
                
                when {
                    lastCommand.contains("netflix") -> {
                        suggestions.addAll(listOf(
                            "Search for new releases",
                            "Play my list",
                            "Browse by genre"
                        ))
                    }
                    lastCommand.contains("volume") -> {
                        suggestions.addAll(listOf(
                            "Adjust audio settings",
                            "Enable subtitles",
                            "Change audio language"
                        ))
                    }
                    lastCommand.contains("search") -> {
                        suggestions.addAll(listOf(
                            "Filter by rating",
                            "Sort by release date",
                            "Add to watchlist"
                        ))
                    }
                    else -> {
                        suggestions.addAll(listOf(
                            "Continue watching",
                            "Explore recommendations",
                            "Check what's new"
                        ))
                    }
                }
            }
            
            _uiState.update { it.copy(contextualSuggestions = suggestions) }
        }
    }
    
    fun startContextAwareRecording() {
        viewModelScope.launch {
            _uiState.update { it.copy(recordingState = RecordingState.RECORDING) }
            
            // Start recording (implementation would use actual voice recording)
            // For now, simulate with a delay
            kotlinx.coroutines.delay(2000)
            
            // Simulate a command
            val simulatedCommand = when {
                _uiState.value.contextualSuggestions.isNotEmpty() -> {
                    _uiState.value.contextualSuggestions.first()
                }
                else -> {
                    "Open YouTube and search for music videos"
                }
            }
            
            processCommand(simulatedCommand)
        }
    }
    
    fun stopContextAwareRecording() {
        _uiState.update { it.copy(recordingState = RecordingState.PROCESSING) }
        
        viewModelScope.launch {
            // Stop recording and process
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(recordingState = RecordingState.IDLE) }
        }
    }
    
    fun processCommand(command: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(recordingState = RecordingState.PROCESSING) }
            
            try {
                val apiKey = "your-gemini-api-key" // In real app, get from secure storage
                val result = contextAwareVoiceService.processContextAwareCommand(
                    apiKey = apiKey,
                    transcribedText = command,
                    userProfile = _uiState.value.currentProfile
                )
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            recordingState = RecordingState.IDLE,
                            lastCommandResult = result.getOrNull()
                        )
                    }
                    
                    // Generate new contextual suggestions
                    generateContextualSuggestions()
                    
                    // Show success feedback
                    playNotificationSound(NotificationType.SUCCESS)
                } else {
                    _uiState.update { 
                        it.copy(
                            recordingState = RecordingState.ERROR,
                            lastError = result.exceptionOrNull()?.message
                        )
                    }
                    
                    playNotificationSound(NotificationType.ERROR)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        recordingState = RecordingState.ERROR,
                        lastError = e.message
                    )
                }
                
                playNotificationSound(NotificationType.ERROR)
            }
        }
    }
    
    fun processExampleCommand(example: String) {
        processCommand(example)
    }
    
    fun processSuggestion(suggestion: String) {
        processCommand(suggestion)
    }
    
    fun executeShortcut(shortcut: VoiceShortcut) {
        viewModelScope.launch {
            try {
                // Execute the shortcut intents
                val result = ContextAwareCommandResult(
                    originalCommand = "Shortcut: ${shortcut.name}",
                    intents = shortcut.intents,
                    executionPlan = createExecutionPlan(shortcut.intents),
                    contextualEnhancements = mapOf("shortcut" to shortcut.name),
                    confidence = 0.95f,
                    isShortcut = true
                )
                
                _uiState.update { it.copy(lastCommandResult = result) }
                
                // Increment usage count
                // In real implementation, update storage
                playNotificationSound(NotificationType.SUCCESS)
                
            } catch (e: Exception) {
                _uiState.update { it.copy(lastError = e.message) }
                playNotificationSound(NotificationType.ERROR)
            }
        }
    }
    
    fun createVoiceShortcut(shortcut: VoiceShortcutData) {
        viewModelScope.launch {
            try {
                val intents = shortcut.commands.map { command ->
                    CommandIntent(
                        type = mapCommandToIntentType(command.type),
                        parameters = command.parameters,
                        confidence = 0.9f
                    )
                }
                
                val result = contextAwareVoiceService.createVoiceShortcut(
                    shortcut.name,
                    shortcut.triggerPhrases,
                    intents
                )
                
                if (result.isSuccess) {
                    playNotificationSound(NotificationType.SUCCESS)
                } else {
                    _uiState.update { it.copy(lastError = "Failed to create shortcut") }
                    playNotificationSound(NotificationType.ERROR)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(lastError = e.message) }
                playNotificationSound(NotificationType.ERROR)
            }
        }
    }
    
    fun deleteShortcut(shortcutId: String) {
        viewModelScope.launch {
            try {
                // In real implementation, delete from storage
                playNotificationSound(NotificationType.SUCCESS)
            } catch (e: Exception) {
                _uiState.update { it.copy(lastError = e.message) }
                playNotificationSound(NotificationType.ERROR)
            }
        }
    }
    
    fun updateVoiceProfile(profile: VoiceProfile) {
        viewModelScope.launch {
            try {
                advancedVoiceRepository.updateVoiceProfile(profile)
                _uiState.update { it.copy(currentProfile = profile) }
                playNotificationSound(NotificationType.SUCCESS)
            } catch (e: Exception) {
                _uiState.update { it.copy(lastError = e.message) }
                playNotificationSound(NotificationType.ERROR)
            }
        }
    }
    
    fun toggleContextAwareness() {
        _uiState.update { it.copy(isContextAwareEnabled = !it.isContextAwareEnabled) }
    }
    
    fun clearConversationContext() {
        viewModelScope.launch {
            contextAwareVoiceService.clearConversationContext()
            generateContextualSuggestions()
            playNotificationSound(NotificationType.INFO)
        }
    }
    
    private suspend fun playNotificationSound(type: NotificationType) {
        try {
            voiceRepository.playNotification(type)
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    private fun mapCommandToIntentType(type: String): IntentType {
        return when (type.lowercase()) {
            "launch_app" -> IntentType.LAUNCH_APP
            "search" -> IntentType.SEARCH
            "media_control" -> IntentType.MEDIA_CONTROL
            "volume_control" -> IntentType.VOLUME_CONTROL
            "navigation" -> IntentType.NAVIGATION
            "system_control" -> IntentType.SYSTEM_CONTROL
            "text_input" -> IntentType.TEXT_INPUT
            else -> IntentType.TEXT_INPUT
        }
    }
    
    private fun createExecutionPlan(intents: List<CommandIntent>): ExecutionPlan {
        val steps = intents.mapIndexed { index, intent ->
            ExecutionStep(
                id = java.util.UUID.randomUUID().toString(),
                order = index + 1,
                intent = intent,
                delay = when (intent.type) {
                    IntentType.LAUNCH_APP -> 2000L
                    IntentType.SEARCH -> 1500L
                    IntentType.MEDIA_CONTROL -> 500L
                    else -> 1000L
                },
                dependencies = if (index > 0) listOf(intents[index - 1].id) else emptyList()
            )
        }
        
        return ExecutionPlan(
            id = java.util.UUID.randomUUID().toString(),
            steps = steps,
            estimatedDuration = steps.sumOf { it.delay + 1000L },
            parallelizable = false
        )
    }
}

data class ContextAwareVoiceUiState(
    val recordingState: RecordingState = RecordingState.IDLE,
    val lastCommandResult: ContextAwareCommandResult? = null,
    val lastError: String? = null,
    val isContextAwareEnabled: Boolean = true,
    val currentProfile: VoiceProfile? = null,
    val contextualSuggestions: List<String> = emptyList(),
    val voiceShortcuts: List<VoiceShortcut> = emptyList()
)

data class VoiceShortcutData(
    val name: String,
    val triggerPhrases: List<String>,
    val commands: List<ShortcutCommand>
)

data class ShortcutCommand(
    val type: String,
    val parameters: Map<String, String>
)
