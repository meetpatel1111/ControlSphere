package com.controlsphere.tvremote.data.voice

import android.content.Context
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
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
class GeminiAudioAnalysisService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson = Gson()
) {
    
    /**
     * Transcribe audio to text with speaker diarization and emotion detection using Gemini 3 Flash
     */
    suspend fun transcribeAudio(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3",
        includeTimestamps: Boolean = true,
        includeEmotionDetection: Boolean = true,
        includeTranslation: Boolean = false
    ): Result<AudioTranscriptionResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            // Upload audio to Gemini Files API
            val tempFile = File(context.cacheDir, "temp_audio_analysis.mp3")
            FileOutputStream(tempFile).use { it.write(audioData) }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = buildTranscriptionPrompt(includeTimestamps, includeEmotionDetection, includeTranslation)
            
            val config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .build()
            
            val content = Content.fromParts(
                Part.fromText("Process the audio file and generate a detailed transcription.\n$prompt"),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-3-flash-preview", content, config)
            val jsonResponse = cleanJsonResponse(response.text() ?: "")
            
            tempFile.delete()
            
            val transcriptionResult = parseTranscriptionResponse(jsonResponse, audioData.size)
            Result.success(transcriptionResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Analyze audio content and provide summary using Gemini 3 Flash
     */
    suspend fun analyzeAudio(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3",
        analysisType: AudioAnalysisType = AudioAnalysisType.GENERAL
    ): Result<AudioAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            // Upload audio
            val tempFile = File(context.cacheDir, "temp_audio_general.mp3")
            FileOutputStream(tempFile).use { it.write(audioData) }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = when (analysisType) {
                AudioAnalysisType.GENERAL -> "Provide a comprehensive analysis of this audio content including the main topics, speakers, and overall mood."
                AudioAnalysisType.EMOTION -> "Analyze the emotional content of this audio. Identify the primary emotions present and any emotional changes over time."
                AudioAnalysisType.CONTENT_SUMMARY -> "Summarize the key points and main topics discussed in this audio content."
                AudioAnalysisType.SPEAKER_ANALYSIS -> "Analyze the speakers in this audio. Identify different speakers, their roles, and speaking patterns."
                AudioAnalysisType.MUSIC_ANALYSIS -> "If this audio contains music, analyze the musical elements, genre, instruments, and overall style."
            }
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-3-flash-preview", content, null)
            
            tempFile.delete()
            
            Result.success(
                AudioAnalysisResult(
                    analysis = response.text() ?: "",
                    analysisType = analysisType,
                    audioLength = audioData.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Detect speakers and provide diarization using Gemini 3 Flash
     */
    suspend fun detectSpeakers(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3"
    ): Result<SpeakerDiarizationResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            // Upload audio
            val tempFile = File(context.cacheDir, "temp_audio_diarization.mp3")
            FileOutputStream(tempFile).use { it.write(audioData) }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = """
                Analyze this audio and identify all distinct speakers.
                For each speaker, provide:
                1. A speaker identifier (Speaker 1, Speaker 2, etc.)
                2. Approximate speaking time percentage
                3. Speaking characteristics (pace, tone, volume)
                4. Role or context if identifiable
                
                Format as JSON with a "speakers" array containing speaker information.
            """.trimIndent()
            
            val config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .build()
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-3-flash-preview", content, config)
            val jsonResponse = cleanJsonResponse(response.text() ?: "")
            
            tempFile.delete()
            
            val diarizationResult = parseSpeakerDiarizationResponse(jsonResponse)
            Result.success(diarizationResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get timestamp-based transcript of specific audio segment
     */
    suspend fun getSegmentTranscript(
        apiKey: String,
        audioData: ByteArray,
        mimeType: String = "audio/mp3",
        startTime: String, // Format: "MM:SS"
        endTime: String    // Format: "MM:SS"
    ): Result<SegmentTranscriptResult> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder()
                .apiKey(apiKey)
                .build()
            
            // Upload audio
            val tempFile = File(context.cacheDir, "temp_audio_segment.mp3")
            FileOutputStream(tempFile).use { it.write(audioData) }
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val prompt = "Provide a detailed transcript of the speech between $startTime and $endTime."
            
            val content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-3-flash-preview", content, null)
            
            tempFile.delete()
            
            Result.success(
                SegmentTranscriptResult(
                    startTime = startTime,
                    endTime = endTime,
                    transcript = response.text() ?: "",
                    audioData = audioData
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanJsonResponse(response: String): String {
        return response.substringAfter("```json").substringAfter("{").substringBeforeLast("```").substringBeforeLast("}")
            .let { "{$it}" }
    }
    
    private fun buildTranscriptionPrompt(
        includeTimestamps: Boolean,
        includeEmotionDetection: Boolean,
        includeTranslation: Boolean
    ): String {
        val requirements = mutableListOf<String>()
        
        requirements.add("1. Identify distinct speakers (e.g., Speaker 1, Speaker 2, or names if context allows).")
        
        if (includeTimestamps) {
            requirements.add("2. Provide accurate timestamps for each segment (Format: MM:SS).")
        }
        
        requirements.add("3. Detect the primary language of each segment.")
        
        if (includeTranslation) {
            requirements.add("4. If the segment is in a language different than English, also provide the English translation.")
        }
        
        if (includeEmotionDetection) {
            requirements.add("5. Identify the primary emotion of the speaker in this segment. Choose exactly one of: Happy, Sad, Angry, Neutral.")
        }
        
        requirements.add("6. Provide a brief summary of the entire audio at the beginning.")
        requirements.add("Return the result as a JSON object with 'summary' and 'segments' array.")
        
        return requirements.joinToString("\n")
    }
    
    private fun parseTranscriptionResponse(jsonResponse: String, audioLength: Int): AudioTranscriptionResult {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val data: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
        
        val segments = (data["segments"] as? List<Map<String, Any>>)?.map {
            AudioSegment(
                speaker = it["speaker"] as? String ?: "Unknown",
                timestamp = it["timestamp"] as? String ?: "00:00",
                content = it["content"] as? String ?: "",
                language = it["language"] as? String ?: "English",
                languageCode = it["languageCode"] as? String ?: "en",
                translation = it["translation"] as? String,
                emotion = it["emotion"] as? String ?: "neutral",
                confidence = (it["confidence"] as? Number)?.toFloat() ?: 0.5f
            )
        } ?: emptyList()

        return AudioTranscriptionResult(
            summary = data["summary"] as? String ?: "Audio transcription completed",
            segments = segments,
            audioLength = audioLength,
            processingTime = 0L // Could be estimated
        )
    }
    
    private fun parseSpeakerDiarizationResponse(jsonResponse: String): SpeakerDiarizationResult {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val data: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
        
        val speakers = (data["speakers"] as? List<Map<String, Any>>)?.map {
            SpeakerInfo(
                id = it["id"] as? String ?: "Unknown",
                percentage = (it["percentage"] as? Number)?.toFloat() ?: 0.0f,
                characteristics = it["characteristics"] as? String ?: "",
                role = it["role"] as? String ?: ""
            )
        } ?: emptyList()

        return SpeakerDiarizationResult(
            speakers = speakers,
            totalDuration = 0L
        )
    }
}

/**
 * Result from audio transcription
 */
data class AudioTranscriptionResult(
    val summary: String,
    val segments: List<AudioSegment>,
    val audioLength: Int,
    val processingTime: Long
)

/**
 * Individual audio segment
 */
data class AudioSegment(
    val speaker: String,
    val timestamp: String,
    val content: String,
    val language: String,
    val languageCode: String,
    val translation: String?,
    val emotion: String,
    val confidence: Float
)

/**
 * Result from audio analysis
 */
data class AudioAnalysisResult(
    val analysis: String,
    val analysisType: AudioAnalysisType,
    val audioLength: Int
)

/**
 * Result from speaker diarization
 */
data class SpeakerDiarizationResult(
    val speakers: List<SpeakerInfo>,
    val totalDuration: Long
)

/**
 * Information about a detected speaker
 */
data class SpeakerInfo(
    val id: String,
    val percentage: Float,
    val characteristics: String,
    val role: String
)

/**
 * Result from segment transcription
 */
data class SegmentTranscriptResult(
    val startTime: String,
    val endTime: String,
    val transcript: String,
    val audioData: ByteArray
)
