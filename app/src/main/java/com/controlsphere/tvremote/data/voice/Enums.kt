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
