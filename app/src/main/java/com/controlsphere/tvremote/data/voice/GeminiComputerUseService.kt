package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.Part
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiComputerUseService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson = Gson()
) {
    
    /**
     * Analyze TV screen and identify interactive elements using Gemini 2.5 Computer Use
     */
    suspend fun analyzeScreen(
        apiKey: String,
        screenshot: Bitmap,
        taskDescription: String = "Analyze this TV screen and identify all interactive elements"
    ): Result<ScreenAnalysis> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            // Upload screenshot to Gemini Files API
            val tempFile = File(context.cacheDir, "temp_screen_analysis.png")
            FileOutputStream(tempFile).use { 
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = """
                Analyze this TV screen image and provide detailed UI analysis.
                
                Task: $taskDescription
                
                Please identify and describe:
                1. All visible UI elements (buttons, text, icons, menus)
                2. Interactive elements with their positions (x, y) coordinates
                3. Current focused element
                4. Navigation structure
                5. Any error messages or dialogs
                6. App identification if possible
                
                Return ONLY a JSON object with:
                - "elements": array of UI elements with "type" (enum: BUTTON, TEXT, ICON, etc.), "label", "position": {"x": int, "y": int}, "description"
                - "focused_element": currently selected element object
                - "app_name": identified application
                - "screen_type": type of screen (HOME, APPS, SETTINGS, etc.)
                - "navigation_path": suggested navigation path array
                - "confidence": confidence score 0-1
            """.trimIndent()
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-2.5-computer-use-preview-10-2025", content, null)
            val jsonResponse = cleanJsonResponse(response.text() ?: "")
            
            tempFile.delete()
            
            val screenAnalysis = parseScreenAnalysis(jsonResponse, screenshot)
            Result.success(screenAnalysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute UI action based on visual analysis
     */
    suspend fun executeUIAction(
        apiKey: String,
        screenshot: Bitmap,
        action: UIAction
    ): Result<UIActionResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            // Upload screenshot
            val tempFile = File(context.cacheDir, "temp_action_analysis.png")
            FileOutputStream(tempFile).use { 
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = """
                Execute the following UI action on this TV screen:
                
                Action: ${action.type} - ${action.description}
                Target: ${action.target}
                
                Please analyze the current screen state and determine:
                1. If the action is possible
                2. The exact sequence of key events needed (use enums: UP, DOWN, LEFT, RIGHT, ENTER, BACK, HOME, etc.)
                3. Expected outcome
                
                Return ONLY a JSON object with:
                - "possible": boolean
                - "key_sequence": array of key events (UP, DOWN, LEFT, RIGHT, ENTER, etc.)
                - "expected_outcome": description
                - "confidence": 0-1
                - "alternative_actions": array of alternative UIAction objects
            """.trimIndent()
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-2.5-computer-use-preview-10-2025", content, null)
            val jsonResponse = cleanJsonResponse(response.text() ?: "")
            
            tempFile.delete()
            
            val actionResult = parseUIActionResult(jsonResponse, action)
            Result.success(actionResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Smart navigation to specific UI element
     */
    suspend fun navigateToElement(
        apiKey: String,
        screenshot: Bitmap,
        targetElement: String,
        elementType: UIElementType = UIElementType.BUTTON
    ): Result<NavigationPlan> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            val tempFile = File(context.cacheDir, "temp_nav_analysis.png")
            FileOutputStream(tempFile).use { 
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = """
                Navigate to the following element on this TV screen:
                
                Target: "$targetElement"
                Type: ${elementType.name}
                
                Return ONLY a JSON object with:
                - "current_location": string
                - "target_location": string
                - "navigation_steps": array of instructions
                - "key_events": array of events (UP, DOWN, LEFT, RIGHT, ENTER, etc.)
                - "landmarks": array of markers
                - "step_count": int
                - "confidence": 0-1
            """.trimIndent()
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-2.5-computer-use-preview-10-2025", content, null)
            val jsonResponse = cleanJsonResponse(response.text() ?: "")
            
            tempFile.delete()
            
            val navigationPlan = parseNavigationPlan(jsonResponse, targetElement, elementType)
            Result.success(navigationPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Automated task execution with visual reasoning
     */
    suspend fun executeComplexTask(
        apiKey: String,
        task: ComplexTask,
        initialScreenshot: Bitmap
    ): Result<TaskExecutionResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            val tempFile = File(context.cacheDir, "temp_task_analysis.png")
            FileOutputStream(tempFile).use { 
                initialScreenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = """
                Execute this complex TV remote task using visual reasoning:
                
                Task: ${task.description}
                Expected Outcome: ${task.expectedOutcome}
                
                Return ONLY a JSON object with:
                - "task_breakdown": array of strings
                - "execution_plan": detailed string
                - "verification_points": array of strings
                - "fallback_strategies": array of strings
                - "estimated_time": long (ms)
                - "success_probability": 0-1
            """.trimIndent()
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-2.5-computer-use-preview-10-2025", content, null)
            val jsonResponse = cleanJsonResponse(response.text() ?: "")
            
            tempFile.delete()
            
            val executionResult = parseTaskExecutionResult(jsonResponse, task)
            Result.success(executionResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun cleanJsonResponse(response: String): String {
        return response.substringAfter("```json").substringAfter("{").substringBeforeLast("```").substringBeforeLast("}")
            .let { "{$it}" }
    }
    
    private fun parseScreenAnalysis(jsonResponse: String, screenshot: Bitmap): ScreenAnalysis {
        return try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
            
            val elements = (data["elements"] as? List<Map<String, Any>>)?.map {
                UIElement(
                    type = UIElementType.valueOf(it["type"] as String),
                    label = it["label"] as String,
                    position = (it["position"] as Map<String, Any>).let { pos ->
                        Position((pos["x"] as Number).toInt(), (pos["y"] as Number).toInt())
                    },
                    description = it["description"] as String
                )
            } ?: emptyList()
            
            val focusedMap = data["focused_element"] as? Map<String, Any>
            val focusedElement = focusedMap?.let {
                UIElement(
                    type = UIElementType.valueOf(it["type"] as String),
                    label = it["label"] as String,
                    position = (it["position"] as Map<String, Any>).let { pos ->
                        Position((pos["x"] as Number).toInt(), (pos["y"] as Number).toInt())
                    },
                    description = it["description"] as String
                )
            } ?: elements.firstOrNull() ?: UIElement(UIElementType.BUTTON, "Unknown", Position(0,0), "")

            ScreenAnalysis(
                screenshot = screenshot,
                elements = elements,
                focusedElement = focusedElement,
                appName = data["app_name"] as? String ?: "Unknown",
                screenType = ScreenType.valueOf(data["screen_type"] as? String ?: "HOME"),
                navigationPath = (data["navigation_path"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                confidence = (data["confidence"] as? Number)?.toFloat() ?: 0.5f
            )
        } catch (e: Exception) {
            // Re-throw or return a safe default
            throw e
        }
    }
    
    private fun parseUIActionResult(jsonResponse: String, action: UIAction): UIActionResult {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val data: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
        
        return UIActionResult(
            action = action,
            possible = data["possible"] as? Boolean ?: false,
            keySequence = (data["key_sequence"] as? List<*>)?.map { ActionKeyEvent.valueOf(it.toString()) } ?: emptyList(),
            expectedOutcome = data["expected_outcome"] as? String ?: "",
            confidence = (data["confidence"] as? Number)?.toFloat() ?: 0.5f,
            alternativeActions = (data["alternative_actions"] as? List<Map<String, Any>>)?.map {
                UIAction(
                    type = UIActionType.valueOf(it["type"] as String),
                    description = it["description"] as String,
                    target = it["target"] as String
                )
            } ?: emptyList()
        )
    }
    
    private fun parseNavigationPlan(jsonResponse: String, target: String, type: UIElementType): NavigationPlan {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val data: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
        
        return NavigationPlan(
            targetElement = target,
            elementType = type,
            currentLocation = data["current_location"] as? String ?: "",
            targetLocation = data["target_location"] as? String ?: "",
            navigationSteps = (data["navigation_steps"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            keyEvents = (data["key_events"] as? List<*>)?.map { ActionKeyEvent.valueOf(it.toString()) } ?: emptyList(),
            landmarks = (data["landmarks"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            stepCount = (data["step_count"] as? Number)?.toInt() ?: 0,
            confidence = (data["confidence"] as? Number)?.toFloat() ?: 0.5f
        )
    }
    
    private fun parseTaskExecutionResult(jsonResponse: String, task: ComplexTask): TaskExecutionResult {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val data: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
        
        return TaskExecutionResult(
            task = task,
            taskBreakdown = (data["task_breakdown"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            executionPlan = data["execution_plan"] as? String ?: "",
            verificationPoints = (data["verification_points"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            fallbackStrategies = (data["fallback_strategies"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            estimatedTime = (data["estimated_time"] as? Number)?.toLong() ?: 0L,
            successProbability = (data["success_probability"] as? Number)?.toFloat() ?: 0.5f
        )
    }
}
