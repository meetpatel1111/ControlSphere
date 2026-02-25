package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.media.MediaRecorder
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.Part
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiVoiceService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson = Gson()
) : VoiceService {
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    override fun getRecordingState(): StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    override suspend fun transcribeAudio(apiKey: String, audioData: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            // Upload file to Gemini Files API (Recommended)
            val tempFile = File(context.cacheDir, "temp_transcript_audio.3gp")
            FileOutputStream(tempFile).use { it.write(audioData) }
            
            val uploadedFile = client.files.upload(tempFile.absolutePath, null)
            
            val content = Content.fromParts(
                Part.fromText("Transcribe this audio message for a TV remote control. Return only the text."),
                Part.fromUri(uploadedFile.name().get(), uploadedFile.mimeType().get())
            )
            
            val response = client.models.generateContent("gemini-2.5-flash", content, null)
            val transcribedText = response.text() ?: ""
            
            // Cleanup temp file
            tempFile.delete()
            
            Result.success(transcribedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun processVoiceCommand(apiKey: String, command: String): Result<VoiceCommand> = withContext(Dispatchers.IO) {
        try {
            val client = Client.builder().apiKey(apiKey).build()
            
            val prompt = """
                Analyze the following voice command for a TV remote control and categorize it.
                Return a JSON object with:
                - "action": one of ["search", "launch", "navigate", "media", "volume", "text", "settings", "power"]
                - "text": the relevant text/content from the command
                - "confidence": confidence score 0-1
                
                Command: "$command"
            """.trimIndent()
            
            val response = client.models.generateContent("gemini-2.5-flash", prompt, null)
            val jsonResponse = response.text() ?: ""
            
            // Parse using Gson
            val voiceCommand = try {
                val cleanJson = jsonResponse.substringAfter("{").substringBeforeLast("}") 
                val wrappedJson = "{$cleanJson}"
                gson.fromJson(wrappedJson, VoiceCommand::class.java)
            } catch (e: Exception) {
                // Fallback to manual extraction if JSON is malformed
                val action = extractJsonValue(jsonResponse, "action") ?: "text"
                val text = extractJsonValue(jsonResponse, "text") ?: command
                val confidence = extractJsonValue(jsonResponse, "confidence")?.toFloatOrNull() ?: 0.5f
                VoiceCommand(action, text, confidence)
            }
            
            Result.success(voiceCommand)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun startRecording(): Result<String> = try {
        _recordingState.value = RecordingState.RECORDING
        audioFile = File(context.cacheDir, "voice_recording_${System.currentTimeMillis()}.3gp")
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
        
        Result.success("Recording started")
    } catch (e: Exception) {
        _recordingState.value = RecordingState.ERROR
        Result.failure(e)
    }
    
    override fun stopRecording(): Result<ByteArray> = try {
        _recordingState.value = RecordingState.PROCESSING
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        
        val audioData = audioFile?.readBytes() ?: byteArrayOf()
        audioFile?.delete()
        
        _recordingState.value = RecordingState.IDLE
        Result.success(audioData)
    } catch (e: Exception) {
        _recordingState.value = RecordingState.ERROR
        Result.failure(e)
    } finally {
        mediaRecorder = null
        audioFile = null
    }
    
    private fun extractJsonValue(json: String, key: String): String? {
        return try {
            val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val matchResult = pattern.find(json)
            matchResult?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
