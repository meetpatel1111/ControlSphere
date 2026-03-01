package com.controlsphere.tvremote.presentation.screens.devicepairing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.controlsphere.tvremote.data.discovery.DeviceDiscovery
import com.controlsphere.tvremote.data.discovery.DiscoveredDevice
import com.controlsphere.tvremote.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicePairingViewModel @Inject constructor(
    private val deviceDiscovery: DeviceDiscovery,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    var uiState by mutableStateOf(DevicePairingUiState())
        private set

    private var scanningJob: Job? = null

    init {
        // Observe connection status
        viewModelScope.launch {
            deviceRepository.getConnectionStatus().collect { status ->
                Log.d("DevicePairingViewModel", "Connection status updated: isConnected=${status.isConnected}, isAuthorized=${status.isAuthorized}")
                uiState = uiState.copy(
                    isConnected = status.isConnected,
                    isAuthorized = status.isAuthorized,
                    errorMessage = status.errorMessage
                )
            }
        }
    }

    fun startScanning() {
        stopScanning()
        uiState = uiState.copy(
            isScanning = true,
            discoveredDevices = emptyList(),
            errorMessage = null
        )

        scanningJob = viewModelScope.launch {
            deviceDiscovery.scanForDevices().collect { device ->
                val currentDevices = uiState.discoveredDevices.toMutableList()
                if (!currentDevices.any { it.ipAddress == device.ipAddress }) {
                    currentDevices.add(device)
                    uiState = uiState.copy(discoveredDevices = currentDevices)
                }
            }
        }
    }

    fun stopScanning() {
        scanningJob?.cancel()
        deviceDiscovery.stopScanning()
        uiState = uiState.copy(isScanning = false)
    }

    fun connectToDevice(ipAddress: String, port: Int = 5556) {
        Log.d("DevicePairingViewModel", "Attempting to connect to $ipAddress:$port")
        uiState = uiState.copy(
            connectingToDevice = ipAddress,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = deviceRepository.connectToDevice(ipAddress, port)
            
            Log.d("DevicePairingViewModel", "Connection result: ${result.isSuccess}")
            uiState = uiState.copy(connectingToDevice = null)
            
            if (!result.isSuccess) {
                Log.e("DevicePairingViewModel", "Connection failed: ${result.exceptionOrNull()?.message}")
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Connection failed"
                )
            } else {
                Log.d("DevicePairingViewModel", "Connection successful!")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}

data class DevicePairingUiState(
    val isScanning: Boolean = false,
    val discoveredDevices: List<DiscoveredDevice> = emptyList(),
    val connectingToDevice: String? = null,
    val isConnected: Boolean = false,
    val isAuthorized: Boolean = false,
    val errorMessage: String? = null
)
