package com.controlsphere.tvremote.presentation.screens.devicemanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.data.voice.DeviceProfile
import com.controlsphere.tvremote.data.voice.DeviceGroup
import com.controlsphere.tvremote.data.voice.DeviceType
import com.controlsphere.tvremote.domain.model.KeyEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DeviceManagementViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeviceManagementUiState())
    val uiState: StateFlow<DeviceManagementUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Collect current device
            deviceRepository.currentDevice.collect { currentDevice ->
                _uiState.value = _uiState.value.copy(currentDevice = currentDevice)
            }
        }
        
        viewModelScope.launch {
            // Collect device profiles
            deviceRepository.deviceProfiles.collect { profiles ->
                _uiState.value = _uiState.value.copy(deviceProfiles = profiles)
            }
        }
        
        viewModelScope.launch {
            // Collect device groups
            deviceRepository.deviceGroups.collect { groups ->
                _uiState.value = _uiState.value.copy(deviceGroups = groups)
            }
        }
        
        viewModelScope.launch {
            // Collect room devices
            deviceRepository.roomDevices.collect { roomDevices ->
                _uiState.value = _uiState.value.copy(roomDevices = roomDevices)
            }
        }
        
        viewModelScope.launch {
            // Collect recent devices
            deviceRepository.recentDevices.collect { recentDevices ->
                _uiState.value = _uiState.value.copy(recentDevices = recentDevices)
            }
        }
        
        viewModelScope.launch {
            // Collect active group
            deviceRepository.activeGroup.collect { activeGroup ->
                _uiState.value = _uiState.value.copy(activeGroup = activeGroup)
            }
        }
    }
    
    // Enhanced multi-device management methods
    
    fun addDevice(deviceProfile: DeviceProfile) {
        viewModelScope.launch {
            val result = deviceRepository.addDeviceProfile(deviceProfile)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    /**
     * Quick Device Switching - Seamless transition between connected TVs
     */
    fun switchToDevice(deviceId: String) {
        viewModelScope.launch {
            val result = deviceRepository.switchToDevice(deviceId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    /**
     * Group Control - Control multiple TVs simultaneously
     */
    fun sendKeyEventToGroup(groupId: String, keyEvent: KeyEvent) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = deviceRepository.sendKeyEventToGroup(groupId, keyEvent)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                groupOperationResult = result.getOrNull()
            )
        }
    }
    
    /**
     * Room-based Organization
     */
    fun getDevicesByRoom(room: String): List<DeviceProfile> {
        return deviceRepository.getDevicesByRoom(room)
    }
    
    fun getAllRooms(): List<String> {
        return deviceRepository.getAllRooms()
    }
    
    fun executeOnRoom(room: String, action: (DeviceProfile) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = deviceRepository.executeOnRoom(room) { device ->
                action(device)
                Result.success(Unit)
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                roomOperationResult = result.getOrNull()
            )
        }
    }
    
    /**
     * Save and switch between multiple TV configurations
     */
    fun saveDeviceConfiguration(deviceId: String, configurationName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = deviceRepository.saveDeviceConfiguration(deviceId, configurationName)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }
    
    /**
     * Batch device operations for home theater setup
     */
    fun setupHomeTheater(
        mainTvId: String,
        surroundSoundId: String? = null,
        streamingDeviceId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = deviceRepository.setupHomeTheater(mainTvId, surroundSoundId, streamingDeviceId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }
    
    /**
     * Get devices organized by room and status
     */
    fun getOrganizedDevices(): Map<String, List<DeviceProfile>> {
        return deviceRepository.getOrganizedDevices()
    }
    
    /**
     * Clear error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear operation results
     */
    fun clearOperationResults() {
        _uiState.value = _uiState.value.copy(
            groupOperationResult = null,
            roomOperationResult = null
        )
    }
    
    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            val result = deviceRepository.removeDeviceProfile(deviceId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun refreshDeviceStatus(deviceId: String) {
        viewModelScope.launch {
            deviceRepository.refreshDeviceStatus(deviceId)
        }
    }
    
    fun refreshAllDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val result = deviceRepository.refreshAllDevicesStatus()
            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }
    
    fun createDeviceGroup(name: String, deviceIds: List<String>, room: String) {
        viewModelScope.launch {
            val result = deviceRepository.createDeviceGroup(name, deviceIds, room)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun deleteDeviceGroup(groupId: String) {
        viewModelScope.launch {
            val result = deviceRepository.deleteDeviceGroup(groupId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun setFilter(index: Int) {
        _uiState.value = _uiState.value.copy(selectedFilterIndex = index)
    }
    
    // Helper method to create a new device profile
    fun createDeviceProfile(
        name: String,
        ipAddress: String,
        port: Int = 5555,
        deviceType: DeviceType,
        model: String,
        manufacturer: String,
        osVersion: String,
        room: String? = null,
        nickname: String? = null
    ): DeviceProfile {
        return DeviceProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            ipAddress = ipAddress,
            port = port,
            deviceType = deviceType,
            model = model,
            manufacturer = manufacturer,
            osVersion = osVersion,
            room = room,
            nickname = nickname
        )
    }
}

data class DeviceManagementUiState(
    val currentDevice: DeviceProfile? = null,
    val deviceProfiles: List<DeviceProfile> = emptyList(),
    val deviceGroups: List<DeviceGroup> = emptyList(),
    val roomDevices: Map<String, List<DeviceProfile>> = emptyMap(),
    val recentDevices: List<DeviceProfile> = emptyList(),
    val activeGroup: DeviceGroup? = null,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val selectedFilterIndex: Int = 0,
    val filterOptions: List<String> = listOf("All", "Online", "Offline", "By Room", "Recent"),
    val errorMessage: String? = null,
    val groupOperationResult: Map<String, Result<Unit>>? = null,
    val roomOperationResult: Map<String, Result<Unit>>? = null
)
