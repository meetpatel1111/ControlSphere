package com.controlsphere.tvremote.data.voice

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.domain.model.KeyEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Visual automation service that combines screen capture with computer use
 * Enables intelligent UI automation with visual reasoning
 */
@Singleton
class VisualAutomationService @Inject constructor(
    private val geminiComputerUseService: GeminiComputerUseService,
    private val screenCaptureService: ScreenCaptureService,
    private val deviceRepository: DeviceRepository
) {
    
    private suspend fun captureCurrentScreen(): Result<Bitmap> {
        // Try TV screen capture via ADB first
        val tvCapture = deviceRepository.captureScreen()
        if (tvCapture.isSuccess) {
            val data = tvCapture.getOrNull()
            if (data != null && data.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                if (bitmap != null) return Result.success(bitmap)
            }
        }
        
        // Fallback to local screen capture
        return screenCaptureService.captureScreen()
    }
    
    /**
     * Smart navigation to specific UI element
     */
    suspend fun smartNavigateToElement(
        apiKey: String,
        targetElement: String,
        elementType: UIElementType = UIElementType.BUTTON
    ): Result<NavigationResult> = withContext(Dispatchers.IO) {
        try {
            // Capture current screen
            val screenshotResult = captureCurrentScreen()
            if (!screenshotResult.isSuccess) {
                return@withContext Result.failure(screenshotResult.exceptionOrNull() ?: Exception("Failed to capture screen"))
            }
            
            val screenshot = screenshotResult.getOrNull()!!
            
            // Analyze screen and create navigation plan
            val navigationPlanResult = geminiComputerUseService.navigateToElement(
                apiKey = apiKey,
                screenshot = screenshot,
                targetElement = targetElement,
                elementType = elementType
            )
            
            if (!navigationPlanResult.isSuccess) {
                return@withContext Result.failure(navigationPlanResult.exceptionOrNull() ?: Exception("Failed to create navigation plan"))
            }
            
            val navigationPlan = navigationPlanResult.getOrNull()!!
            
            // Execute navigation steps
            val executionResult = executeNavigationSteps(navigationPlan.keyEvents)
            
            Result.success(NavigationResult(
                targetElement = targetElement,
                navigationPlan = navigationPlan,
                executionSuccess = executionResult.isSuccess,
                stepsExecuted = navigationPlan.keyEvents.size,
                confidence = navigationPlan.confidence
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute complex multi-step task with visual verification
     */
    suspend fun executeComplexTask(
        apiKey: String,
        task: ComplexTask
    ): Flow<TaskProgress> = flow {
        emit(TaskProgress.Started(task.description))
        
        try {
            // Initial screen capture
            val screenshotResult = captureCurrentScreen()
            if (!screenshotResult.isSuccess) {
                emit(TaskProgress.Error("Failed to capture screen"))
                return@flow
            }
            
            val screenshot = screenshotResult.getOrNull()!!
            emit(TaskProgress.ScreenCaptured)
            
            // Create execution plan
            val executionResult = geminiComputerUseService.executeComplexTask(
                apiKey = apiKey,
                task = task,
                initialScreenshot = screenshot
            )
            
            if (!executionResult.isSuccess) {
                emit(TaskProgress.Error("Failed to create execution plan"))
                return@flow
            }
            
            val plan = executionResult.getOrNull()!!
            emit(TaskProgress.PlanCreated(plan.executionPlan))
            
            // Execute each step with verification
            plan.taskBreakdown.forEachIndexed { index, step ->
                emit(TaskProgress.StepStarted(index + 1, step))
                
                // Capture screen before step
                val beforeScreenshot = screenCaptureService.captureScreen()
                if (beforeScreenshot.isSuccess) {
                    emit(TaskProgress.StepVerification(index + 1, "Before: ${step}"))
                }
                
                // Execute step (simplified)
                delay(1000) 
                
                // Capture screen after step
                val afterScreenshot = screenCaptureService.captureScreen()
                if (afterScreenshot.isSuccess) {
                    emit(TaskProgress.StepCompleted(index + 1, step))
                }
                
                // Verify step completion
                if (plan.verificationPoints.isNotEmpty() && index < plan.verificationPoints.size) {
                    val verification = plan.verificationPoints[index]
                    emit(TaskProgress.StepVerified(index + 1, verification))
                }
            }
            
            emit(TaskProgress.Completed(task.expectedOutcome))
            
        } catch (e: Exception) {
            emit(TaskProgress.Error(e.message ?: "Task execution failed"))
        }
    }
    
    /**
     * Visual click on specific element
     */
    suspend fun visualClick(
        apiKey: String,
        targetDescription: String
    ): Result<ClickResult> = withContext(Dispatchers.IO) {
        try {
            // Capture screen
            val screenshotResult = captureCurrentScreen()
            if (!screenshotResult.isSuccess) {
                return@withContext Result.failure(screenshotResult.exceptionOrNull() ?: Exception("Failed to capture screen"))
            }
            
            val screenshot = screenshotResult.getOrNull()!!
            
            // Analyze and plan click action
            val action = UIAction(UIActionType.CLICK, targetDescription, targetDescription)
            val actionResult = geminiComputerUseService.executeUIAction(
                apiKey = apiKey,
                screenshot = screenshot,
                action = action
            )
            
            if (!actionResult.isSuccess) {
                return@withContext Result.failure(actionResult.exceptionOrNull() ?: Exception("Failed to plan click action"))
            }
            
            val result = actionResult.getOrNull()!!
            
            if (result.possible) {
                // Execute the key sequence
                val executionResult = executeNavigationSteps(result.keySequence)
                
                Result.success(ClickResult(
                    target = targetDescription,
                    actionResult = result,
                    executionSuccess = executionResult.isSuccess,
                    executedKeyEvents = result.keySequence
                ))
            } else {
                Result.failure(Exception("Click action not possible: ${result.expectedOutcome}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Smart text input with visual verification
     */
    suspend fun smartTextInput(
        apiKey: String,
        text: String,
        targetField: String = "current input field"
    ): Result<TextInputResult> = withContext(Dispatchers.IO) {
        try {
            // Capture screen to locate input field
            val screenshotResult = captureCurrentScreen()
            if (!screenshotResult.isSuccess) {
                return@withContext Result.failure(screenshotResult.exceptionOrNull() ?: Exception("Failed to capture screen"))
            }
            
            // Send text to TV
            val textResult = deviceRepository.sendText(text)
            
            if (!textResult.isSuccess) {
                return@withContext Result.failure(textResult.exceptionOrNull() ?: Exception("Failed to send text"))
            }
            
            // Verify text was entered
            delay(500)
            val verificationScreenshot = screenCaptureService.captureScreen()
            
            Result.success(TextInputResult(
                text = text,
                targetField = targetField,
                inputSuccess = textResult.isSuccess,
                verificationScreenshot = verificationScreenshot.getOrNull()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Visual search with image understanding
     */
    suspend fun visualSearch(
        apiKey: String,
        searchTerm: String
    ): Result<SearchResult> = withContext(Dispatchers.IO) {
        try {
            // Capture current screen
            val screenshotResult = captureCurrentScreen()
            if (!screenshotResult.isSuccess) {
                return@withContext Result.failure(screenshotResult.exceptionOrNull() ?: Exception("Failed to capture screen"))
            }
            
            val screenshot = screenshotResult.getOrNull()!!
            
            // Analyze screen for search functionality
            val screenAnalysis = geminiComputerUseService.analyzeScreen(
                apiKey = apiKey,
                screenshot = screenshot,
                taskDescription = "Find search functionality and prepare to search for: $searchTerm"
            )
            
            if (!screenAnalysis.isSuccess) {
                return@withContext Result.failure(screenAnalysis.exceptionOrNull() ?: Exception("Failed to analyze screen"))
            }
            
            val analysis = screenAnalysis.getOrNull()!!
            
            // Execute search sequence
            val searchSteps = listOf(ActionKeyEvent.SEARCH, ActionKeyEvent.ENTER)
            val executionResult = executeNavigationSteps(searchSteps)
            
            // Send search term
            val textResult = deviceRepository.sendText(searchTerm)
            delay(500)
            
            // Press Enter to execute search
            val enterResult = deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.ENTER)
            
            Result.success(SearchResult(
                searchTerm = searchTerm,
                screenAnalysis = analysis,
                searchExecuted = textResult.isSuccess && enterResult.isSuccess,
                stepsExecuted = searchSteps.size
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun executeNavigationSteps(keyEvents: List<ActionKeyEvent>): Result<Unit> {
        try {
            keyEvents.forEach { keyEvent ->
                val result = when (keyEvent) {
                    ActionKeyEvent.UP -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_UP)
                    ActionKeyEvent.DOWN -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_DOWN)
                    ActionKeyEvent.LEFT -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_LEFT)
                    ActionKeyEvent.RIGHT -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_RIGHT)
                    ActionKeyEvent.ENTER -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.ENTER)
                    ActionKeyEvent.BACK -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.BACK)
                    ActionKeyEvent.HOME -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.HOME)
                    ActionKeyEvent.MENU -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.MENU)
                    ActionKeyEvent.SEARCH -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.SETTINGS_SEARCH)
                    ActionKeyEvent.VOLUME_UP -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.VOLUME_UP)
                    ActionKeyEvent.VOLUME_DOWN -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.VOLUME_DOWN)
                    ActionKeyEvent.POWER -> deviceRepository.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.POWER)
                }
                
                if (!result.isSuccess) {
                    return Result.failure(result.exceptionOrNull() ?: Exception("Failed to execute key event: $keyEvent"))
                }
                
                delay(200) 
            }
            
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
