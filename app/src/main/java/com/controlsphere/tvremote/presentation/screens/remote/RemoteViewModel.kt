package com.controlsphere.tvremote.presentation.screens.remote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.domain.model.KeyEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    var uiState by mutableStateOf(RemoteUiState())
        private set

    init {
        // Observe connection status
        viewModelScope.launch {
            deviceRepository.getConnectionStatus().collect { status ->
                uiState = uiState.copy(
                    isConnected = status.isConnected,
                    isAuthorized = status.isAuthorized,
                    errorMessage = status.errorMessage
                )
            }
        }
    }

    fun sendKeyEvent(keyEvent: KeyEvent) {
        viewModelScope.launch {
            val result = deviceRepository.sendKeyEvent(keyEvent)
            if (!result.isSuccess) {
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Command failed"
                )
            }
        }
    }

    fun sendText(text: String) {
        viewModelScope.launch {
            val result = deviceRepository.sendText(text)
            if (!result.isSuccess) {
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Text input failed"
                )
            }
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            val result = deviceRepository.launchApp(packageName)
            if (!result.isSuccess) {
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "App launch failed"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

data class RemoteUiState(
    val isConnected: Boolean = false,
    val isAuthorized: Boolean = false,
    val errorMessage: String? = null
)
