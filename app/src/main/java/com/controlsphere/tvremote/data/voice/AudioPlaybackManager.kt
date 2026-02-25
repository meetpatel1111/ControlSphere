package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream

/**
 * Manages audio playback for TTS responses and system sounds
 */
@Singleton
class AudioPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: Flow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentAudio = MutableStateFlow<TTSResult?>(null)
    val currentAudio: Flow<TTSResult?> = _currentAudio.asStateFlow()
    
    /**
     * Play TTS audio response
     */
    suspend fun playTTSResponse(ttsResult: TTSResult): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            // Stop any currently playing audio
            stopPlayback()
            
            _playbackState.value = PlaybackState.LOADING
            _currentAudio.value = ttsResult
            
            // In a real implementation, this would play the actual audio data
            // For now, we simulate the playback
            simulateAudioPlayback(ttsResult)
            
            Result.success(Unit)
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Play system notification sound
     */
    suspend fun playNotificationSound(notificationType: NotificationType): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val soundResourceId = when (notificationType) {
                NotificationType.SUCCESS -> android.R.drawable.ic_menu_save
                NotificationType.ERROR -> android.R.drawable.ic_dialog_alert
                NotificationType.WARNING -> android.R.drawable.ic_dialog_info
                NotificationType.INFO -> android.R.drawable.ic_dialog_info
                else -> android.R.drawable.ic_dialog_info
            }
            
            // Play system sound
            playSystemSound(soundResourceId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Stop current playback
     */
    suspend fun stopPlayback(): Unit = withContext(Dispatchers.Main) {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
            _playbackState.value = PlaybackState.IDLE
            _currentAudio.value = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Pause current playback
     */
    suspend fun pausePlayback(): Unit = withContext(Dispatchers.Main) {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _playbackState.value = PlaybackState.PAUSED
                }
            }
        } catch (e: Exception) {
            // Ignore pause errors
        }
    }
    
    /**
     * Resume paused playback
     */
    suspend fun resumePlayback(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    _playbackState.value = PlaybackState.PLAYING
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set playback volume
     */
    suspend fun setVolume(volume: Float): Unit = withContext(Dispatchers.Main) {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            // Ignore volume setting errors
        }
    }
    
    /**
     * Get current playback position
     */
    fun getCurrentPosition(): Long {
        return (mediaPlayer?.currentPosition ?: 0).toLong()
    }
    
    /**
     * Get total duration
     */
    fun getDuration(): Long {
        return (mediaPlayer?.duration ?: 0).toLong()
    }
    
    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
    
    private suspend fun simulateAudioPlayback(ttsResult: TTSResult) = withContext(Dispatchers.IO) {
        try {
            _playbackState.value = PlaybackState.PLAYING
            
            // Create temporary file to store audio data
            val tempFile = File.createTempFile("tts_response", ".mp3", context.cacheDir)
            tempFile.writeBytes(ttsResult.audioData)
            
            withContext(Dispatchers.Main) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        _playbackState.value = PlaybackState.COMPLETED
                        _currentAudio.value = null
                        tempFile.delete()
                        
                        // Auto-reset to IDLE after completion
                        it.release()
                        mediaPlayer = null
                        _playbackState.value = PlaybackState.IDLE
                    }
                    setOnErrorListener { _, _, _ ->
                        _playbackState.value = PlaybackState.ERROR
                        tempFile.delete()
                        false
                    }
                }
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
        }
    }

    private fun playSystemSound(resourceId: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
                start()
                setOnCompletionListener {
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            // Ignore sound errors
        }
    }
}

