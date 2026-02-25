package com.controlsphere.tvremote.presentation.screens.textinput

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextInputViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    var uiState by mutableStateOf(TextInputUiState())
        private set

    init {
        // Observe connection status
        viewModelScope.launch {
            deviceRepository.getConnectionStatus().collect { status ->
                uiState = uiState.copy(
                    isConnected = status.isConnected,
                    errorMessage = status.errorMessage?.takeIf { !status.isConnected }
                )
            }
        }
    }

    fun updateText(text: String) {
        uiState = uiState.copy(currentText = text, errorMessage = null)
    }

    fun clearText() {
        uiState = uiState.copy(currentText = "", errorMessage = null)
    }

    fun sendText() {
        if (uiState.currentText.isBlank() || !uiState.isConnected) return

        uiState = uiState.copy(isSending = true, errorMessage = null)

        viewModelScope.launch {
            val result = deviceRepository.sendText(uiState.currentText)
            
            uiState = uiState.copy(isSending = false)
            
            result.fold(
                onSuccess = {
                    uiState = uiState.copy(textSent = true)
                },
                onFailure = { exception ->
                    uiState = uiState.copy(
                        errorMessage = exception.message ?: "Failed to send text"
                    )
                }
            )
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

data class TextInputUiState(
    val isConnected: Boolean = false,
    val currentText: String = "",
    val isSending: Boolean = false,
    val textSent: Boolean = false,
    val errorMessage: String? = null
)
