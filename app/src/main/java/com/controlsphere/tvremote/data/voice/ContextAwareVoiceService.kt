package com.controlsphere.tvremote.data.voice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Context-aware voice service that maintains conversational memory,
 * processes multi-intent commands, and manages personalized voice profiles
 */
@Singleton
class ContextAwareVoiceService @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val advancedVoiceRepository: AdvancedVoiceRepository,
    private val customVoiceCommandRepository: CustomVoiceCommandRepository
) {
    
    // Conversational Context Management
    private val _conversationContext = MutableStateFlow<ConversationContext>(ConversationContext())
    val conversationContext: StateFlow<ConversationContext> = _conversationContext.asStateFlow()
    
    // Voice Shortcuts
    private val _voiceShortcuts = MutableStateFlow<List<VoiceShortcut>>(emptyList())
    val voiceShortcuts: StateFlow<List<VoiceShortcut>> = _voiceShortcuts.asStateFlow()
    
    // Active Session
    private val _activeSession = MutableStateFlow<VoiceSession?>(null)
    val activeSession: StateFlow<VoiceSession?> = _activeSession.asStateFlow()
    
    /**
     * Process voice command with context awareness
     */
    suspend fun processContextAwareCommand(
        apiKey: String,
        transcribedText: String,
        userProfile: VoiceProfile? = null
    ): Result<ContextAwareCommandResult> {
        return try {
            // Start or continue session
            val session = getCurrentOrCreateSession()
            
            // Update conversation context
            updateConversationContext(transcribedText, session)
            
            // Check for voice shortcuts first
            val shortcutResult = checkVoiceShortcuts(transcribedText)
            if (shortcutResult.isSuccess) {
                return shortcutResult
            }
            
            // Process multi-intent commands
            val multiIntentResult = processMultiIntentCommand(apiKey, transcribedText, userProfile)
            if (multiIntentResult.isSuccess) {
                updateSessionWithCommand(session, multiIntentResult.getOrNull()!!)
                return multiIntentResult
            }
            
            // Fall back to standard voice processing with context
            val standardResult = voiceRepository.startVoiceCommand(apiKey)
            
            // Enhance with contextual information
            val enhancedResult = enhanceWithContext(standardResult, session, userProfile)
            
            updateSessionWithCommand(session, enhancedResult)
            Result.success(enhancedResult)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Process complex multi-intent commands
     */
    private suspend fun processMultiIntentCommand(
        apiKey: String,
        command: String,
        userProfile: VoiceProfile?
    ): Result<ContextAwareCommandResult> {
        val multiIntentPatterns = listOf(
            // App + Content patterns
            Regex("(?i)(open|launch|start)\\s+(.+?)\\s+and\\s+(play|watch|continue|resume)\\s+(.+)", RegexOption.IGNORE_CASE),
            // Search + Action patterns  
            Regex("(?i)(search|find)\\s+(.+?)\\s+and\\s+(open|play|watch)\\s+(.+)", RegexOption.IGNORE_CASE),
            // Volume + App patterns
            Regex("(?i)(set|adjust)\\s+(volume|sound)\\s+(.+?)\\s+and\\s+(open|launch)\\s+(.+)", RegexOption.IGNORE_CASE),
            // Navigation + Content patterns
            Regex("(?i)(go to|navigate)\\s+(.+?)\\s+and\\s+(search|find)\\s+(.+)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in multiIntentPatterns) {
            val match = pattern.find(command)
            if (match != null) {
                return executeMultiIntentCommand(match, userProfile)
            }
        }
        
        return Result.failure(Exception("No multi-intent pattern matched"))
    }
    
    /**
     * Execute multi-intent command
     */
    private suspend fun executeMultiIntentCommand(
        match: MatchResult,
        userProfile: VoiceProfile?
    ): Result<ContextAwareCommandResult> {
        val groups = match.groupValues
        val intents = mutableListOf<CommandIntent>()
        
        try {
            when {
                groups[1].lowercase() in listOf("open", "launch", "start") -> {
                    // App + Content intent
                    intents.add(CommandIntent(
                        type = IntentType.LAUNCH_APP,
                        parameters = mapOf("app" to groups[2].trim()),
                        confidence = 0.9f
                    ))
                    intents.add(CommandIntent(
                        type = IntentType.MEDIA_CONTROL,
                        parameters = mapOf("action" to groups[3].trim(), "content" to groups[4].trim()),
                        confidence = 0.85f
                    ))
                }
                
                groups[1].lowercase() in listOf("search", "find") -> {
                    // Search + Action intent
                    intents.add(CommandIntent(
                        type = IntentType.SEARCH,
                        parameters = mapOf("query" to groups[2].trim()),
                        confidence = 0.9f
                    ))
                    intents.add(CommandIntent(
                        type = IntentType.MEDIA_CONTROL,
                        parameters = mapOf("action" to groups[3].trim(), "content" to groups[4].trim()),
                        confidence = 0.85f
                    ))
                }
                
                groups[1].lowercase() in listOf("set", "adjust") -> {
                    // Volume + App intent
                    intents.add(CommandIntent(
                        type = IntentType.VOLUME_CONTROL,
                        parameters = mapOf("level" to groups[3].trim()),
                        confidence = 0.9f
                    ))
                    intents.add(CommandIntent(
                        type = IntentType.LAUNCH_APP,
                        parameters = mapOf("app" to groups[4].trim()),
                        confidence = 0.85f
                    ))
                }
                
                groups[1].lowercase() in listOf("go to", "navigate") -> {
                    // Navigation + Search intent
                    intents.add(CommandIntent(
                        type = IntentType.NAVIGATION,
                        parameters = mapOf("destination" to groups[2].trim()),
                        confidence = 0.9f
                    ))
                    intents.add(CommandIntent(
                        type = IntentType.SEARCH,
                        parameters = mapOf("query" to groups[4].trim()),
                        confidence = 0.85f
                    ))
                }
            }
            
            return Result.success(ContextAwareCommandResult(
                originalCommand = match.value,
                intents = intents,
                executionPlan = createExecutionPlan(intents),
                contextualEnhancements = getContextualEnhancements(userProfile),
                confidence = intents.map { it.confidence }.average().toFloat()
            ))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Check for voice shortcuts
     */
    private suspend fun checkVoiceShortcuts(command: String): Result<ContextAwareCommandResult> {
        val shortcuts = _voiceShortcuts.value
        
        for (shortcut in shortcuts) {
            for (trigger in shortcut.triggerPhrases) {
                if (command.lowercase().contains(trigger.lowercase())) {
                    return Result.success(ContextAwareCommandResult(
                        originalCommand = command,
                        intents = shortcut.intents,
                        executionPlan = createExecutionPlan(shortcut.intents),
                        contextualEnhancements = mapOf("shortcut" to shortcut.name),
                        confidence = 0.95f,
                        isShortcut = true
                    ))
                }
            }
        }
        
        return Result.failure(Exception("No shortcut matched"))
    }
    
    /**
     * Create voice shortcut
     */
    suspend fun createVoiceShortcut(
        name: String,
        triggerPhrases: List<String>,
        intents: List<CommandIntent>
    ): Result<String> {
        return try {
            val shortcut = VoiceShortcut(
                id = UUID.randomUUID().toString(),
                name = name,
                triggerPhrases = triggerPhrases.map { it.lowercase().trim() },
                intents = intents,
                createdTime = System.currentTimeMillis(),
                usageCount = 0
            )
            
            val updatedShortcuts = _voiceShortcuts.value.toMutableList()
            updatedShortcuts.add(shortcut)
            _voiceShortcuts.value = updatedShortcuts
            
            Result.success(shortcut.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update conversation context
     */
    private fun updateConversationContext(command: String, session: VoiceSession) {
        val currentContext = _conversationContext.value
        val updatedContext = currentContext.copy(
            lastCommand = command,
            commandHistory = currentContext.commandHistory + command,
            sessionDuration = System.currentTimeMillis() - session.startTime,
            intentHistory = currentContext.intentHistory + extractIntents(command)
        )
        
        _conversationContext.value = updatedContext
    }
    
    /**
     * Get or create current session
     */
    private fun getCurrentOrCreateSession(): VoiceSession {
        val currentSession = _activeSession.value
        return if (currentSession != null && !isSessionExpired(currentSession)) {
            currentSession
        } else {
            val newSession = VoiceSession(
                id = UUID.randomUUID().toString(),
                startTime = System.currentTimeMillis(),
                commands = emptyList()
            )
            _activeSession.value = newSession
            newSession
        }
    }
    
    /**
     * Update session with command
     */
    private fun updateSessionWithCommand(session: VoiceSession, result: ContextAwareCommandResult) {
        val updatedSession = session.copy(
            commands = session.commands + result,
            lastActivity = System.currentTimeMillis()
        )
        _activeSession.value = updatedSession
    }
    
    /**
     * Enhance standard result with context
     */
    private fun enhanceWithContext(
        standardResult: Result<VoiceCommandResult>,
        session: VoiceSession,
        userProfile: VoiceProfile?
    ): ContextAwareCommandResult {
        if (!standardResult.isSuccess) {
            throw standardResult.exceptionOrNull() ?: Exception("Standard processing failed")
        }
        
        val voiceResult = standardResult.getOrNull()!!
        val context = _conversationContext.value
        
        return ContextAwareCommandResult(
            originalCommand = voiceResult.transcribedText,
            intents = listOf(CommandIntent(
                type = mapToIntentType(voiceResult.command?.action ?: "text"),
                parameters = mapOf("content" to (voiceResult.command?.text ?: "")),
                confidence = voiceResult.command?.confidence ?: 0.5f
            )),
            executionPlan = createSimpleExecutionPlan(voiceResult.command),
            contextualEnhancements = getContextualEnhancements(userProfile) + mapOf(
                "session_commands" to session.commands.size,
                "recent_context" to context.lastCommand
            ),
            confidence = voiceResult.command?.confidence ?: 0.5f
        )
    }
    
    /**
     * Get contextual enhancements based on user profile and history
     */
    private fun getContextualEnhancements(userProfile: VoiceProfile?): Map<String, Any> {
        val enhancements = mutableMapOf<String, Any>()
        
        userProfile?.let { profile ->
            enhancements["preferred_language"] = profile.preferredLanguage.code
            enhancements["voice_pitch"] = profile.pitch
            enhancements["voice_speed"] = profile.speed
            enhancements["personalized_responses"] = profile.personalizedResponses
        }
        
        val context = _conversationContext.value
        enhancements["session_duration"] = context.sessionDuration
        enhancements["command_count"] = context.commandHistory.size
        
        return enhancements
    }
    
    /**
     * Create execution plan for intents
     */
    private fun createExecutionPlan(intents: List<CommandIntent>): ExecutionPlan {
        val steps = intents.mapIndexed { index, intent ->
            ExecutionStep(
                id = UUID.randomUUID().toString(),
                order = index + 1,
                intent = intent,
                delay = calculateDelay(intent, index),
                dependencies = if (index > 0) listOf(intents[index - 1].id) else emptyList()
            )
        }
        
        return ExecutionPlan(
            id = UUID.randomUUID().toString(),
            steps = steps,
            estimatedDuration = steps.sumOf { it.delay + 1000L }, // Base 1s per step
            parallelizable = checkParallelizable(intents)
        )
    }
    
    /**
     * Create simple execution plan for single command
     */
    private fun createSimpleExecutionPlan(command: VoiceCommand?): ExecutionPlan {
        val intent = CommandIntent(
            id = UUID.randomUUID().toString(),
            type = mapToIntentType(command?.action ?: "text"),
            parameters = mapOf("content" to (command?.text ?: "")),
            confidence = command?.confidence ?: 0.5f
        )
        
        val step = ExecutionStep(
            id = UUID.randomUUID().toString(),
            order = 1,
            intent = intent,
            delay = 0L,
            dependencies = emptyList()
        )
        
        return ExecutionPlan(
            id = UUID.randomUUID().toString(),
            steps = listOf(step),
            estimatedDuration = 1000L,
            parallelizable = false
        )
    }
    
    private fun mapToIntentType(action: String): IntentType {
        return when (action.lowercase()) {
            "launch" -> IntentType.LAUNCH_APP
            "search" -> IntentType.SEARCH
            "media" -> IntentType.MEDIA_CONTROL
            "volume" -> IntentType.VOLUME_CONTROL
            "navigate" -> IntentType.NAVIGATION
            "power" -> IntentType.SYSTEM_CONTROL
            else -> IntentType.TEXT_INPUT
        }
    }
    
    private fun extractIntents(command: String): List<String> {
        // Simple intent extraction - can be enhanced with NLP
        return listOf(command.lowercase())
    }
    
    private fun isSessionExpired(session: VoiceSession): Boolean {
        val sessionAge = System.currentTimeMillis() - session.lastActivity
        return sessionAge > 30 * 60 * 1000L // 30 minutes
    }
    
    private fun calculateDelay(intent: CommandIntent, index: Int): Long {
        return when (intent.type) {
            IntentType.LAUNCH_APP -> 2000L // Give app time to launch
            IntentType.SEARCH -> 1500L // Wait for search UI
            IntentType.MEDIA_CONTROL -> 500L // Quick media actions
            else -> 1000L // Default delay
        }
    }
    
    private fun checkParallelizable(intents: List<CommandIntent>): Boolean {
        // Check if intents can be executed in parallel
        return intents.size <= 2 && intents.all { it.type in listOf(IntentType.VOLUME_CONTROL, IntentType.SYSTEM_CONTROL) }
    }
    
    /**
     * Clear conversation context
     */
    fun clearConversationContext() {
        _conversationContext.value = ConversationContext()
        _activeSession.value = null
    }
    
    /**
     * Get session statistics
     */
    fun getSessionStatistics(): SessionStatistics {
        val session = _activeSession.value
        val context = _conversationContext.value
        
        return SessionStatistics(
            sessionDuration = session?.let { System.currentTimeMillis() - it.startTime } ?: 0L,
            commandCount = session?.commands?.size ?: 0,
            averageConfidence = session?.commands?.map { it.confidence }?.average() ?: 0.0,
            mostUsedIntents = context.intentHistory.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }.take(3)
        )
    }
}

// Data classes for context-aware voice processing
data class ConversationContext(
    val lastCommand: String = "",
    val commandHistory: List<String> = emptyList(),
    val intentHistory: List<String> = emptyList(),
    val sessionDuration: Long = 0L,
    val userPreferences: Map<String, Any> = emptyMap()
)

data class VoiceSession(
    val id: String,
    val startTime: Long,
    val lastActivity: Long = System.currentTimeMillis(),
    val commands: List<ContextAwareCommandResult> = emptyList()
)

data class VoiceShortcut(
    val id: String,
    val name: String,
    val triggerPhrases: List<String>,
    val intents: List<CommandIntent>,
    val createdTime: Long,
    var usageCount: Int = 0
)

data class CommandIntent(
    val id: String = UUID.randomUUID().toString(),
    val type: IntentType,
    val parameters: Map<String, String>,
    val confidence: Float
)

enum class IntentType {
    LAUNCH_APP,
    SEARCH,
    MEDIA_CONTROL,
    VOLUME_CONTROL,
    NAVIGATION,
    SYSTEM_CONTROL,
    TEXT_INPUT
}

data class ContextAwareCommandResult(
    val originalCommand: String,
    val intents: List<CommandIntent>,
    val executionPlan: ExecutionPlan,
    val contextualEnhancements: Map<String, Any>,
    val confidence: Float,
    val isShortcut: Boolean = false
)

data class ExecutionPlan(
    val id: String,
    val steps: List<ExecutionStep>,
    val estimatedDuration: Long,
    val parallelizable: Boolean
)

data class ExecutionStep(
    val id: String,
    val order: Int,
    val intent: CommandIntent,
    val delay: Long,
    val dependencies: List<String>
)

data class SessionStatistics(
    val sessionDuration: Long,
    val commandCount: Int,
    val averageConfidence: Double,
    val mostUsedIntents: List<Pair<String, Int>>
)
