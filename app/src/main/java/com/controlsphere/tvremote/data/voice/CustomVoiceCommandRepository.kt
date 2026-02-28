package com.controlsphere.tvremote.data.voice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomVoiceCommandRepository @Inject constructor() {
    
    private val _customCommands = MutableStateFlow<List<CustomVoiceCommand>>(emptyList())
    val customCommands: Flow<List<CustomVoiceCommand>> = _customCommands.asStateFlow()
    
    private val _globalCommands = MutableStateFlow<List<CustomVoiceCommand>>(emptyList())
    val globalCommands: Flow<List<CustomVoiceCommand>> = _globalCommands.asStateFlow()
    
    // Command management
    suspend fun createCommand(
        name: String,
        triggerPhrases: List<String>,
        actionSequence: List<CommandAction>,
        description: String,
        category: CommandCategory,
        deviceId: String? = null
    ): Result<String> {
        return try {
            val command = CustomVoiceCommand(
                id = UUID.randomUUID().toString(),
                name = name,
                triggerPhrases = triggerPhrases.map { it.lowercase().trim() },
                actionSequence = actionSequence,
                description = description,
                isEnabled = true,
                deviceId = deviceId,
                category = category
            )
            
            val updatedCommands = _customCommands.value.toMutableList()
            updatedCommands.add(command)
            _customCommands.value = updatedCommands
            
            // Update global commands if applicable
            if (deviceId == null) {
                val updatedGlobal = _globalCommands.value.toMutableList()
                updatedGlobal.add(command)
                _globalCommands.value = updatedGlobal
            }
            
            Result.success(command.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCommand(command: CustomVoiceCommand): Result<Unit> {
        return try {
            val updatedCommands = _customCommands.value.toMutableList()
            val index = updatedCommands.indexOfFirst { it.id == command.id }
            if (index >= 0) {
                updatedCommands[index] = command
                _customCommands.value = updatedCommands
                
                // Update global commands if applicable
                if (command.deviceId == null) {
                    val updatedGlobal = _globalCommands.value.toMutableList()
                    val globalIndex = updatedGlobal.indexOfFirst { it.id == command.id }
                    if (globalIndex >= 0) {
                        updatedGlobal[globalIndex] = command
                        _globalCommands.value = updatedGlobal
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCommand(commandId: String): Result<Unit> {
        return try {
            val updatedCommands = _customCommands.value.toMutableList()
            updatedCommands.removeAll { it.id == commandId }
            _customCommands.value = updatedCommands
            
            // Remove from global commands
            val updatedGlobal = _globalCommands.value.toMutableList()
            updatedGlobal.removeAll { it.id == commandId }
            _globalCommands.value = updatedGlobal
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleCommand(commandId: String): Result<Unit> {
        return try {
            val command = _customCommands.value.find { it.id == commandId }
                ?: return Result.failure(Exception("Command not found"))
            
            val updatedCommand = command.copy(isEnabled = !command.isEnabled)
            updateCommand(updatedCommand)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun incrementUsage(commandId: String): Result<Unit> {
        return try {
            val command = _customCommands.value.find { it.id == commandId }
                ?: return Result.failure(Exception("Command not found"))
            
            val updatedCommand = command.copy(usageCount = command.usageCount + 1)
            updateCommand(updatedCommand)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Command search and filtering
    fun findMatchingCommands(transcribedText: String, deviceId: String? = null): List<CustomVoiceCommand> {
        val normalizedText = transcribedText.lowercase().trim()
        
        return _customCommands.value.filter { command ->
            command.isEnabled && 
            (command.deviceId == null || command.deviceId == deviceId) &&
            command.triggerPhrases.any { phrase ->
                normalizedText.contains(phrase) || phrase.contains(normalizedText)
            }
        }
    }
    
    fun getCommandsByCategory(category: CommandCategory, deviceId: String? = null): List<CustomVoiceCommand> {
        return _customCommands.value.filter { command ->
            command.category == category && 
            (command.deviceId == null || command.deviceId == deviceId)
        }
    }
    
    fun getCommandsForDevice(deviceId: String): List<CustomVoiceCommand> {
        return _customCommands.value.filter { command ->
            command.deviceId == null || command.deviceId == deviceId
        }
    }
    
    fun getMostUsedCommands(limit: Int = 10): List<CustomVoiceCommand> {
        return _customCommands.value
            .sortedByDescending { it.usageCount }
            .take(limit)
    }
    
    fun getRecentCommands(limit: Int = 5): List<CustomVoiceCommand> {
        return _customCommands.value
            .sortedByDescending { it.createdTime }
            .take(limit)
    }
    
    // Predefined command templates
    fun getCommandTemplates(): List<CommandTemplate> {
        return listOf(
            CommandTemplate(
                name = "Movie Night",
                description = "Launch Netflix and dim lights",
                category = CommandCategory.AUTOMATION,
                actionSequence = listOf(
                    CommandAction(ActionType.APP_LAUNCH, mapOf("package" to "com.netflix.mediaclient")),
                    CommandAction(ActionType.DELAY, mapOf("duration" to "2000")),
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "DOWN")),
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "ENTER"))
                ),
                triggerPhrases = listOf("movie night", "start netflix", "watch movies")
            ),
            CommandTemplate(
                name = "Volume Control",
                description = "Set volume to specific level",
                category = CommandCategory.MEDIA,
                actionSequence = listOf(
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "VOLUME_UP")),
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "VOLUME_UP")),
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "VOLUME_UP"))
                ),
                triggerPhrases = listOf("volume up", "increase volume", "louder")
            ),
            CommandTemplate(
                name = "Search YouTube",
                description = "Open YouTube and search for content",
                category = CommandCategory.SEARCH,
                actionSequence = listOf(
                    CommandAction(ActionType.APP_LAUNCH, mapOf("package" to "com.google.android.youtube.tv")),
                    CommandAction(ActionType.DELAY, mapOf("duration" to "3000")),
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "SEARCH")),
                    CommandAction(ActionType.TEXT_INPUT, mapOf("text" to "{search_query}"))
                ),
                triggerPhrases = listOf("search youtube", "youtube search", "find on youtube")
            ),
            CommandTemplate(
                name = "Home Assistant",
                description = "Navigate to home screen",
                category = CommandCategory.NAVIGATION,
                actionSequence = listOf(
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "HOME")),
                    CommandAction(ActionType.DELAY, mapOf("duration" to "1000"))
                ),
                triggerPhrases = listOf("go home", "home screen", "main menu")
            ),
            CommandTemplate(
                name = "Power Off",
                description = "Turn off the TV",
                category = CommandCategory.SYSTEM,
                actionSequence = listOf(
                    CommandAction(ActionType.KEY_EVENT, mapOf("key" to "POWER"))
                ),
                triggerPhrases = listOf("turn off", "power off", "shut down")
            )
        )
    }
    
    // Command execution
    suspend fun executeCommand(
        commandId: String,
        context: Map<String, String> = emptyMap()
    ): Result<CommandExecutionResult> {
        return try {
            val command = _customCommands.value.find { it.id == commandId }
                ?: return Result.failure(Exception("Command not found"))
            
            val startTime = System.currentTimeMillis()
            val executedActions = mutableListOf<ExecutedAction>()
            var executionError: String? = null
            
            for (action in command.actionSequence) {
                val actionResult = executeAction(action, context)
                executedActions.add(actionResult)
                
                if (!actionResult.success) {
                    executionError = actionResult.error
                    break
                }
                
                // Apply delay if specified
                if (action.delay > 0) {
                    kotlinx.coroutines.delay(action.delay)
                }
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            val success = executionError == null
            
            // Increment usage count if successful
            if (success) {
                incrementUsage(commandId)
            }
            
            val result = CommandExecutionResult(
                commandId = commandId,
                success = success,
                executedActions = executedActions,
                error = executionError,
                executionTime = executionTime
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun executeAction(
        action: CommandAction,
        context: Map<String, String>
    ): ExecutedAction {
        return try {
            when (action.type) {
                ActionType.KEY_EVENT -> {
                    // This would be handled by the device repository
                    ExecutedAction(action, true, "Key event sent: ${action.parameters["key"]}")
                }
                ActionType.TEXT_INPUT -> {
                    val text = action.parameters["text"]?.replace("{search_query}", context["search_query"] ?: "")
                        ?: return ExecutedAction(action, false, null, "No text specified")
                    ExecutedAction(action, true, "Text input: $text")
                }
                ActionType.APP_LAUNCH -> {
                    val packageName = action.parameters["package"]
                        ?: return ExecutedAction(action, false, null, "No package specified")
                    ExecutedAction(action, true, "App launched: $packageName")
                }
                ActionType.DELAY -> {
                    val duration = action.parameters["duration"]?.toLongOrNull() ?: 1000L
                    kotlinx.coroutines.delay(duration)
                    ExecutedAction(action, true, "Delayed for ${duration}ms")
                }
                ActionType.VOICE_COMMAND -> {
                    val command = action.parameters["command"]
                        ?: return ExecutedAction(action, false, null, "No command specified")
                    ExecutedAction(action, true, "Voice command: $command")
                }
                ActionType.VISUAL_SEARCH -> {
                    val query = action.parameters["query"]
                        ?: return ExecutedAction(action, false, null, "No query specified")
                    ExecutedAction(action, true, "Visual search: $query")
                }
                ActionType.COMPUTER_USE -> {
                    val task = action.parameters["task"]
                        ?: return ExecutedAction(action, false, null, "No task specified")
                    ExecutedAction(action, true, "Computer use task: $task")
                }
                ActionType.CONDITIONAL -> {
                    // Complex conditional logic would go here
                    ExecutedAction(action, true, "Conditional action executed")
                }
            }
        } catch (e: Exception) {
            ExecutedAction(action, false, null, e.message)
        }
    }
}

data class CommandTemplate(
    val name: String,
    val description: String,
    val category: CommandCategory,
    val actionSequence: List<CommandAction>,
    val triggerPhrases: List<String>
)
