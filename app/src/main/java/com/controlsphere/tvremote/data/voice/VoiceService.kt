package com.controlsphere.tvremote.data.voice

import kotlinx.coroutines.flow.Flow

interface VoiceService {
    suspend fun transcribeAudio(apiKey: String, audioData: ByteArray): Result<String>
    suspend fun processVoiceCommand(apiKey: String, command: String): Result<VoiceCommand>
    fun startRecording(): Result<String>
    fun stopRecording(): Result<ByteArray>
    fun getRecordingState(): kotlinx.coroutines.flow.StateFlow<RecordingState>
}
