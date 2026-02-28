package com.controlsphere.tvremote.data.voice

import android.graphics.Bitmap

enum class NotificationType {
    INFO, SUCCESS, WARNING, ERROR
}

enum class SpeechRate {
    SLOW, NORMAL, FAST
}

enum class AudioAnalysisType {
    GENERAL, EMOTION, CONTENT_SUMMARY, SPEAKER_ANALYSIS, MUSIC_ANALYSIS
}

enum class PlaybackState {
    IDLE, LOADING, PLAYING, PAUSED, BUFFERING, COMPLETED, ERROR
}

enum class UIElementType {
    BUTTON, TEXT, ICON, IMAGE, LIST, MENU, DIALOG, INPUT, CONTAINER, TEXT_FIELD, MENU_ITEM, SWITCH
}

enum class ScreenType {
    HOME, APPS, APP_CONTENT, SETTINGS, SEARCH, ERROR, LOADING
}

enum class UIActionType {
    CLICK, NAVIGATE, INPUT, SCROLL, BACK, HOME, SEARCH, LAUNCH
}

enum class ActionKeyEvent {
    UP, DOWN, LEFT, RIGHT, ENTER, BACK, HOME, MENU, SEARCH, VOLUME_UP, VOLUME_DOWN, POWER
}

enum class RecordingState {
    IDLE, RECORDING, PROCESSING, ERROR
}

data class VoiceCommand(
    val action: String,
    val text: String,
    val confidence: Float
)

data class TTSResult(
    val audioData: ByteArray,
    val duration: Long, // in milliseconds
    val voiceStyle: VoiceStyle,
    val text: String,
    val voiceName: String = ""
)

data class VoiceCommandResult(
    val transcribedText: String,
    val command: VoiceCommand?,
    val executionSuccess: Boolean,
    val executionError: String? = null
)

enum class GeminiVoice(val voiceName: String, val description: String) {
    ZEPHYR("Zephyr", "Bright"),
    PUCK("Puck", "Upbeat"),
    CHARON("Charon", "Informative"),
    KORE("Kore", "Firm"),
    FENRIR("Fenrir", "Excitable"),
    LEDA("Leda", "Youthful"),
    ORUS("Orus", "Firm"),
    AOEDE("Aoede", "Breezy"),
    CALLIRHOE("Callirrhoe", "Easy-going"),
    AUTONOE("Autonoe", "Bright"),
    ENCELADUS("Enceladus", "Breathy"),
    IAPETUS("Iapetus", "Clear"),
    UMBRIEL("Umbriel", "Easy-going"),
    ALGIEBA("Algieba", "Smooth"),
    DESPINA("Test", "Test")
}

enum class VoiceStyle(val description: String) {
    NATURAL("Natural speaking voice"),
    FRIENDLY("Friendly and warm"),
    PROFESSIONAL("Professional and clear"),
    CALM("Calm and soothing"),
    CONFIDENT("Confident and assertive"),
    CONVERSATIONAL("Conversational and casual"),
    CONCERNED("Concerned and gentle"),
    EXCITED("Excited and energetic"),
    MULTI_SPEAKER("Multi-speaker conversation");
    
    companion object {
        fun fromVoiceName(voiceName: String): VoiceStyle {
            return when (voiceName) {
                "Puck", "Laomedeia", "Fenrir", "Sadachbia" -> EXCITED
                "Aoede", "Callirrhoe", "Umbriel", "Zubenelgenubi" -> FRIENDLY
                "Kore", "Orus", "Alnilam", "Schedar" -> PROFESSIONAL
                "Achernar", "Enceladus", "Despina", "Algieba" -> CALM
                "Charon", "Rasalgethi", "Sadaltager" -> CONFIDENT
                "Achird", "Vindemiatrix", "Sulafat" -> CONVERSATIONAL
                "Autonoe", "Iapetus", "Erinome", "Gacrux" -> NATURAL
                else -> NATURAL
            }
        }
    }
}

enum class TVResponseStyle {
    FRIENDLY, PROFESSIONAL, SUCCESS, ERROR, NEUTRAL
}

enum class ConversationalPersona(val recommendedVoice: String) {
    FRIENDLY_ASSISTANT("Aoede"),
    PROFESSIONAL_ASSISTANT("Kore"),
    CASUAL_FRIEND("Achird"),
    ENTHUSIASTIC_GUIDE("Puck")
}

// Computer Use and Automation Types
data class ScreenAnalysis(
    val screenshot: Bitmap,
    val elements: List<UIElement>,
    val focusedElement: UIElement,
    val appName: String,
    val screenType: ScreenType,
    val navigationPath: List<String>,
    val confidence: Float
)

data class UIElement(
    val type: UIElementType,
    val label: String,
    val position: Position,
    val description: String
)

data class Position(val x: Int, val y: Int)

data class UIAction(
    val type: UIActionType,
    val description: String,
    val target: String
)

data class UIActionResult(
    val action: UIAction,
    val possible: Boolean,
    val keySequence: List<ActionKeyEvent>,
    val expectedOutcome: String,
    val confidence: Float,
    val alternativeActions: List<UIAction>
)

data class NavigationPlan(
    val targetElement: String,
    val elementType: UIElementType,
    val currentLocation: String,
    val targetLocation: String,
    val navigationSteps: List<String>,
    val keyEvents: List<ActionKeyEvent>,
    val landmarks: List<String>,
    val stepCount: Int,
    val confidence: Float
)

data class ComplexTask(
    val description: String,
    val steps: List<String>,
    val expectedOutcome: String
)

data class TaskExecutionResult(
    val task: ComplexTask,
    val taskBreakdown: List<String>,
    val executionPlan: String,
    val verificationPoints: List<String>,
    val fallbackStrategies: List<String>,
    val estimatedTime: Long,
    val successProbability: Float
)

// Result classes for visual automation
data class NavigationResult(
    val targetElement: String,
    val navigationPlan: NavigationPlan,
    val executionSuccess: Boolean,
    val stepsExecuted: Int,
    val confidence: Float
)

data class ClickResult(
    val target: String,
    val actionResult: UIActionResult,
    val executionSuccess: Boolean,
    val executedKeyEvents: List<ActionKeyEvent>
)

data class TextInputResult(
    val text: String,
    val targetField: String,
    val inputSuccess: Boolean,
    val verificationScreenshot: Bitmap?
)

data class SearchResult(
    val searchTerm: String,
    val screenAnalysis: ScreenAnalysis,
    val searchExecuted: Boolean,
    val stepsExecuted: Int
)

// Task progress events
sealed class TaskProgress {
    data class Started(val description: String) : TaskProgress()
    data object ScreenCaptured : TaskProgress()
    data class PlanCreated(val plan: String) : TaskProgress()
    data class StepStarted(val stepNumber: Int, val description: String) : TaskProgress()
    data class StepVerification(val stepNumber: Int, val verification: String) : TaskProgress()
    data class StepCompleted(val stepNumber: Int, val description: String) : TaskProgress()
    data class StepVerified(val stepNumber: Int, val verification: String) : TaskProgress()
    data class Completed(val outcome: String) : TaskProgress()
    data class Error(val message: String) : TaskProgress()
}

// Enhanced Computer Use Vision Types
data class ErrorDetectionResult(
    val hasErrors: Boolean,
    val issues: List<ScreenIssue>,
    val recommendedActions: List<String>,
    val autoResolvable: Boolean
)

data class ScreenIssue(
    val type: IssueType,
    val description: String,
    val resolution: List<ActionKeyEvent>,
    val priority: IssuePriority,
    val position: Position? = null
)

enum class IssueType {
    ERROR, WARNING, LOADING, PERMISSION, NETWORK, CRASH, SYSTEM_ALERT
}

enum class IssuePriority {
    HIGH, MEDIUM, LOW
}

data class VisualSearchResult(
    val searchQuery: String,
    val searchFieldFound: Boolean,
    val searchFieldLocation: Position?,
    val navigationToSearch: List<ActionKeyEvent>,
    val contentMatches: List<String>,
    val confidence: Float,
    val alternativeSearchMethods: List<String>
)

data class GesturePlan(
    val gestureDescription: String,
    val gestureRecognized: Boolean,
    val keySequence: List<ActionKeyEvent>,
    val timingIntervals: List<Long>, // delays in ms
    val expectedResult: String,
    val confidence: Float
)

// Multi-Device Management Types
data class DeviceProfile(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 5555,
    val deviceType: DeviceType,
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val isOnline: Boolean = false,
    val lastConnected: Long = 0L,
    val favoriteApps: List<String> = emptyList(),
    val customCommands: List<CustomVoiceCommand> = emptyList(),
    val room: String? = null,
    val nickname: String? = null
)

enum class DeviceType {
    ANDROID_TV, GOOGLE_TV, CHROMECAST, NVIDIA_SHIELD, SONY_TV, TCL_TV, UNKNOWN
}

data class DeviceGroup(
    val id: String,
    val name: String,
    val deviceIds: List<String>,
    val room: String,
    val allowSimultaneousControl: Boolean = false
)

// Custom Voice Commands Types
data class CustomVoiceCommand(
    val id: String,
    val name: String,
    val triggerPhrases: List<String>,
    val actionSequence: List<CommandAction>,
    val description: String,
    val isEnabled: Boolean = true,
    val deviceId: String? = null, // null means global command
    val category: CommandCategory,
    val createdTime: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
)

data class CommandAction(
    val type: ActionType,
    val parameters: Map<String, String> = emptyMap(),
    val delay: Long = 0L // delay in ms before this action
)

enum class ActionType {
    KEY_EVENT,
    TEXT_INPUT,
    APP_LAUNCH,
    VOICE_COMMAND,
    VISUAL_SEARCH,
    COMPUTER_USE,
    DELAY,
    CONDITIONAL
}

enum class CommandCategory {
    NAVIGATION, MEDIA, SEARCH, APP_CONTROL, AUTOMATION, SYSTEM, CUSTOM
}

// Advanced Voice Features
data class VoiceProfile(
    val id: String,
    val name: String,
    val userId: String,
    val voiceSamples: List<VoiceSample>,
    val preferredLanguage: VoiceLanguage,
    val personalizedResponses: Boolean = true,
    val wakeWordSensitivity: Float = 0.7f,
    val accent: String? = null,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val createdTime: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis()
)

data class VoiceSample(
    val id: String,
    val profileId: String,
    val audioData: ByteArray,
    val sampleType: VoiceSampleType,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VoiceSample
        return id == other.id && profileId == other.profileId
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class VoiceSampleType {
    WAKE_WORD,
    COMMAND_PHRASE,
    NATURAL_SPEECH,
    ENROLLMENT_SAMPLE
}

data class AmbientModeConfig(
    val isEnabled: Boolean = false,
    val wakeWord: String = "Hey ControlSphere",
    val sensitivity: Float = 0.7f,
    val responseDelay: Long = 500L,
    val continuousListening: Boolean = false,
    val batteryOptimization: Boolean = true,
    val nightMode: NightModeConfig = NightModeConfig(),
    val privacyMode: Boolean = true
)

data class NightModeConfig(
    val isEnabled: Boolean = false,
    val startTime: String = "22:00",
    val endTime: String = "07:00",
    val reducedSensitivity: Float = 0.5f,
    val silentResponses: Boolean = true
)

data class MultiLanguageCommand(
    val id: String,
    val basePhrase: String,
    val translations: Map<VoiceLanguage, String>,
    val detectedLanguage: VoiceLanguage,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class LanguageSwitchContext(
    val previousLanguage: VoiceLanguage,
    val newLanguage: VoiceLanguage,
    val triggerPhrase: String,
    val confidence: Float,
    val context: String
)

enum class WakeWordState {
    INACTIVE,
    LISTENING,
    PROCESSING,
    RESPONDING,
    ERROR
}

data class VoiceRecognitionResult(
    val text: String,
    val confidence: Float,
    val language: VoiceLanguage,
    val profileId: String?,
    val alternatives: List<String> = emptyList(),
    val processingTime: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class PersonalizedResponse(
    val template: String,
    val variables: Map<String, String> = emptyMap(),
    val profileId: String,
    val responseType: ResponseType
)

enum class ResponseType {
    CONFIRMATION,
    ACKNOWLEDGMENT,
    ERROR,
    INFO,
    CASUAL,
    PROFESSIONAL
}

data class VoiceCommandContext(
    val command: String,
    val language: VoiceLanguage,
    val profileId: String?,
    val deviceContext: DeviceProfile?,
    val previousCommands: List<String> = emptyList(),
    val sessionDuration: Long,
    val confidence: Float
)

data class CommandExecutionResult(
    val commandId: String,
    val success: Boolean,
    val executedActions: List<ExecutedAction>,
    val error: String? = null,
    val executionTime: Long
)

data class ExecutedAction(
    val action: CommandAction,
    val success: Boolean,
    val result: String? = null,
    val error: String? = null
)
