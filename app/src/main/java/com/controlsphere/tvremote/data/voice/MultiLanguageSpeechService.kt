package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-language speech recognition service using Android's SpeechRecognizer
 */
@Singleton
class MultiLanguageSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentLanguage: VoiceLanguage = VoiceLanguage.ENGLISH
    
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.IDLE)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()
    
    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    enum class RecognitionState {
        IDLE, LISTENING, PROCESSING, SUCCESS, ERROR
    }
    
    fun setLanguage(language: VoiceLanguage) {
        currentLanguage = language
        // Restart recognizer if currently listening
        if (_isListening.value) {
            stopListening()
            startListening()
        }
    }
    
    fun startListening(): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _recognitionState.value = RecognitionState.ERROR
            return false
        }
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
            
            val intent = createRecognitionIntent()
            speechRecognizer?.startListening(intent)
            
            _isListening.value = true
            _recognitionState.value = RecognitionState.LISTENING
            return true
        } catch (e: Exception) {
            _recognitionState.value = RecognitionState.ERROR
            _isListening.value = false
            return false
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _recognitionState.value = RecognitionState.IDLE
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    private fun createRecognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Language configuration
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.locale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguage.locale)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLanguage.locale)
            
            // Recognition preferences
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            
            // Model preferences for better accuracy
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            
            // TV-specific optimizations
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500)
        }
    }
    
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.LISTENING
            }
            
            override fun onBeginningOfSpeech() {
                // Speech detected
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changes - could be used for visualization
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                _isListening.value = false
                _recognitionState.value = RecognitionState.PROCESSING
            }
            
            override fun onError(error: Int) {
                _isListening.value = false
                _recognitionState.value = RecognitionState.ERROR
                _recognizedText.value = ""
            }
            
            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _recognitionState.value = RecognitionState.SUCCESS
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // Get the most confident result
                    _recognizedText.value = matches[0].lowercase().trim()
                } else {
                    _recognizedText.value = ""
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // Update with partial result for real-time feedback
                    _recognizedText.value = matches[0].lowercase().trim()
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle various speech recognition events
            }
        }
    }
    
    fun getCurrentLanguage(): VoiceLanguage = currentLanguage
    
    fun getSupportedLanguages(): List<VoiceLanguage> {
        return VoiceLanguage.getSupportedLanguages()
    }
}
