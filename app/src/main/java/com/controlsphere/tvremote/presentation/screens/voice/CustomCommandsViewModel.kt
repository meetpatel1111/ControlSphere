package com.controlsphere.tvremote.presentation.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.data.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomCommandsViewModel @Inject constructor(
    private val commandRepository: CustomVoiceCommandRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CustomCommandsUiState())
    val uiState: StateFlow<CustomCommandsUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Collect custom commands
            commandRepository.customCommands.collect { commands ->
                _uiState.value = _uiState.value.copy(
                    allCommands = commands,
                    enabledCommands = commands.filter { it.isEnabled },
                    globalCommands = commands.filter { it.deviceId == null }
                )
                updateFilteredCommands()
            }
        }
        
        viewModelScope.launch {
            // Collect device profiles for device-specific commands
            deviceRepository.deviceProfiles.collect { devices ->
                _uiState.value = _uiState.value.copy(availableDevices = devices)
                updateFilteredCommands()
            }
        }
        
        // Load command templates
        _uiState.value = _uiState.value.copy(
            commandTemplates = commandRepository.getCommandTemplates()
        )
        
        // Load most used commands
        loadMostUsedCommands()
    }
    
    private fun updateFilteredCommands() {
        val currentState = _uiState.value
        val filtered = when (currentState.selectedCategoryIndex) {
            0 -> currentState.allCommands // All
            1 -> currentState.allCommands.filter { it.category == CommandCategory.NAVIGATION }
            2 -> currentState.allCommands.filter { it.category == CommandCategory.MEDIA }
            3 -> currentState.allCommands.filter { it.category == CommandCategory.SEARCH }
            4 -> currentState.allCommands.filter { it.category == CommandCategory.APP_CONTROL }
            5 -> currentState.allCommands.filter { it.category == CommandCategory.AUTOMATION }
            6 -> currentState.allCommands.filter { it.category == CommandCategory.SYSTEM }
            7 -> currentState.allCommands.filter { it.category == CommandCategory.CUSTOM }
            else -> currentState.allCommands
        }
        
        _uiState.value = currentState.copy(filteredCommands = filtered)
    }
    
    private fun loadMostUsedCommands() {
        viewModelScope.launch {
            val mostUsed = commandRepository.getMostUsedCommands(10)
            _uiState.value = _uiState.value.copy(mostUsedCommands = mostUsed)
        }
    }
    
    fun createCommand(command: CustomVoiceCommand) {
        viewModelScope.launch {
            val result = commandRepository.createCommand(
                name = command.name,
                triggerPhrases = command.triggerPhrases,
                actionSequence = command.actionSequence,
                description = command.description,
                category = command.category,
                deviceId = command.deviceId
            )
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun updateCommand(command: CustomVoiceCommand) {
        viewModelScope.launch {
            val result = commandRepository.updateCommand(command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun deleteCommand(commandId: String) {
        viewModelScope.launch {
            val result = commandRepository.deleteCommand(commandId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun toggleCommand(commandId: String) {
        viewModelScope.launch {
            val result = commandRepository.toggleCommand(commandId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun duplicateCommand(command: CustomVoiceCommand) {
        viewModelScope.launch {
            val duplicatedCommand = command.copy(
                id = "", // Will be generated as new
                name = "${command.name} (Copy)",
                usageCount = 0,
                createdTime = System.currentTimeMillis()
            )
            
            val result = commandRepository.createCommand(
                name = duplicatedCommand.name,
                triggerPhrases = duplicatedCommand.triggerPhrases,
                actionSequence = duplicatedCommand.actionSequence,
                description = duplicatedCommand.description,
                category = duplicatedCommand.category,
                deviceId = duplicatedCommand.deviceId
            )
            
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun executeCommand(commandId: String) {
        viewModelScope.launch {
            val result = commandRepository.executeCommand(commandId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            } else {
                // Show success message or execution result
                _uiState.value = _uiState.value.copy(
                    executionResult = result.getOrNull()
                )
            }
        }
    }
    
    fun editCommand(command: CustomVoiceCommand) {
        _uiState.value = _uiState.value.copy(editingCommand = command)
    }
    
    fun clearEditingCommand() {
        _uiState.value = _uiState.value.copy(editingCommand = null)
    }
    
    fun setCategoryFilter(index: Int) {
        _uiState.value = _uiState.value.copy(selectedCategoryIndex = index)
        updateFilteredCommands()
    }
    
    fun refreshCommands() {
        // Commands are already collected via StateFlow, but we can refresh most used
        loadMostUsedCommands()
    }
    
    fun createFromTemplate(template: CommandTemplate) {
        val command = CustomVoiceCommand(
            id = "", // Will be generated
            name = template.name,
            triggerPhrases = template.triggerPhrases,
            actionSequence = template.actionSequence,
            description = template.description,
            category = template.category
        )
        _uiState.value = _uiState.value.copy(editingCommand = command)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearExecutionResult() {
        _uiState.value = _uiState.value.copy(executionResult = null)
    }
    
    // Helper methods for creating commands
    fun createSimpleCommand(
        name: String,
        triggerPhrase: String,
        actionType: ActionType,
        parameters: Map<String, String>,
        category: CommandCategory,
        deviceId: String? = null
    ): CustomVoiceCommand {
        return CustomVoiceCommand(
            id = "", // Will be generated
            name = name,
            triggerPhrases = listOf(triggerPhrase.lowercase().trim()),
            actionSequence = listOf(
                CommandAction(actionType, parameters)
            ),
            description = "Simple $category command",
            category = category,
            deviceId = deviceId
        )
    }
    
    fun createMultiActionCommand(
        name: String,
        triggerPhrases: List<String>,
        actions: List<CommandAction>,
        description: String,
        category: CommandCategory,
        deviceId: String? = null
    ): CustomVoiceCommand {
        return CustomVoiceCommand(
            id = "", // Will be generated
            name = name,
            triggerPhrases = triggerPhrases.map { it.lowercase().trim() },
            actionSequence = actions,
            description = description,
            category = category,
            deviceId = deviceId
        )
    }
}

data class CustomCommandsUiState(
    val allCommands: List<CustomVoiceCommand> = emptyList(),
    val enabledCommands: List<CustomVoiceCommand> = emptyList(),
    val globalCommands: List<CustomVoiceCommand> = emptyList(),
    val filteredCommands: List<CustomVoiceCommand> = emptyList(),
    val mostUsedCommands: List<CustomVoiceCommand> = emptyList(),
    val commandTemplates: List<CommandTemplate> = emptyList(),
    val availableDevices: List<DeviceProfile> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val categoryOptions: List<String> = listOf(
        "All", "Navigation", "Media", "Search", "App Control", "Automation", "System", "Custom"
    ),
    val editingCommand: CustomVoiceCommand? = null,
    val executionResult: CommandExecutionResult? = null,
    val errorMessage: String? = null
)
