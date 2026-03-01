package com.controlsphere.tvremote.data.repository

import com.controlsphere.tvremote.data.adb.AdbConnection
import com.controlsphere.tvremote.data.adb.ConnectionStatus
import com.controlsphere.tvremote.data.connection.WiFiConnectionManager
import com.controlsphere.tvremote.data.voice.DeviceProfile
import com.controlsphere.tvremote.data.voice.DeviceGroup
import com.controlsphere.tvremote.data.voice.DeviceType
import com.controlsphere.tvremote.domain.model.AppInfo
import com.controlsphere.tvremote.domain.model.Device
import com.controlsphere.tvremote.domain.model.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val adbConnection: AdbConnection,
    private val wifiConnectionManager: WiFiConnectionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Unified connection status that combines both ADB and WiFi
    private val _unifiedConnectionStatus: StateFlow<ConnectionStatus> = 
        combine(
            adbConnection.getConnectionStatus(),
            wifiConnectionManager.connectionStatus
        ) { adbStatus, wifiStatus ->
            // If either connection is active, report as connected
            when {
                wifiStatus.isConnected -> ConnectionStatus(
                    isConnected = true,
                    isAuthorized = true,
                    errorMessage = null
                )
                adbStatus.isConnected -> adbStatus
                else -> ConnectionStatus(
                    isConnected = false,
                    isAuthorized = false,
                    errorMessage = adbStatus.errorMessage ?: wifiStatus.errorMessage
                )
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectionStatus(
                isConnected = false,
                isAuthorized = false
            )
        )
    
    private val _currentDevice = MutableStateFlow<DeviceProfile?>(null)
    val currentDevice: StateFlow<DeviceProfile?> = _currentDevice.asStateFlow()
    
    private val _deviceProfiles = MutableStateFlow<List<DeviceProfile>>(emptyList())
    val deviceProfiles: StateFlow<List<DeviceProfile>> = _deviceProfiles.asStateFlow()
    
    private val _deviceGroups = MutableStateFlow<List<DeviceGroup>>(emptyList())
    val deviceGroups: StateFlow<List<DeviceGroup>> = _deviceGroups.asStateFlow()
    
    // Room-based organization
    private val _roomDevices = MutableStateFlow<Map<String, List<DeviceProfile>>>(emptyMap())
    val roomDevices: StateFlow<Map<String, List<DeviceProfile>>> = _roomDevices.asStateFlow()
    
    // Group control state
    private val _activeGroup = MutableStateFlow<DeviceGroup?>(null)
    val activeGroup: StateFlow<DeviceGroup?> = _activeGroup.asStateFlow()
    
    // Quick device switching history
    private val _recentDevices = MutableStateFlow<List<DeviceProfile>>(emptyList())
    val recentDevices: StateFlow<List<DeviceProfile>> = _recentDevices.asStateFlow()
    
    private fun updateRoomOrganization(profiles: List<DeviceProfile>) {
        val roomMap = profiles
            .filter { it.room?.isNotBlank() == true }
            .groupBy { it.room!! }
        _roomDevices.value = roomMap
    }
    
    // Legacy single device methods (for backward compatibility)
    suspend fun connectToDevice(ipAddress: String, port: Int = 5556): Result<Boolean> {
        // Try WiFi connection first on port 5556 (for ControlSphere TV Receiver)
        val wifiPort = 5556
        val wifiResult = wifiConnectionManager.connect(ipAddress, wifiPort)
        if (wifiResult.isSuccess) {
            return wifiResult
        }
        
        // Fallback to ADB connection on the provided port (typically 5555)
        val adbPort = if (port == wifiPort) 5555 else port
        return adbConnection.connect(ipAddress, adbPort)
    }
    
    suspend fun disconnectFromDevice() {
        wifiConnectionManager.disconnect()
        adbConnection.disconnect()
        _currentDevice.value = null
    }
    
    // Enhanced multi-device management methods
    
    /**
     * Quick Device Switching - Seamless transition between connected TVs
     */
    suspend fun switchToDevice(deviceId: String): Result<DeviceProfile> {
        return try {
            val device = _deviceProfiles.value.find { it.id == deviceId }
                ?: return Result.failure(Exception("Device not found"))
            
            // Add to recent devices
            addToRecentDevices(device)
            
            // Disconnect from current device
            if (_currentDevice.value != null) {
                adbConnection.disconnect()
            }
            
            // Connect to new device
            val connectResult = adbConnection.connect(device.ipAddress, device.port)
            if (connectResult.isSuccess) {
                _currentDevice.value = device
                Result.success(device)
            } else {
                Result.failure(Exception("Failed to connect to device"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Group Control - Control multiple TVs simultaneously
     */
    suspend fun executeOnGroup(groupId: String, action: suspend (DeviceProfile) -> Result<Unit>): Result<Map<String, Result<Unit>>> {
        return try {
            val group = _deviceGroups.value.find { it.id == groupId }
                ?: return Result.failure(Exception("Group not found"))
            
            val groupDevices = _deviceProfiles.value.filter { it.id in group.deviceIds }
            
            val results = mutableMapOf<String, Result<Unit>>()
            
            coroutineScope {
                groupDevices.map { device ->
                    async {
                        val result = action(device)
                        results[device.id] = result
                        result
                    }
                }.awaitAll()
            }
            
            _activeGroup.value = group
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send key event to all devices in a group
     */
    suspend fun sendKeyEventToGroup(groupId: String, keyEvent: KeyEvent): Result<Map<String, Result<Unit>>> {
        return executeOnGroup(groupId) { device ->
            try {
                // Temporarily connect to device if not current
                val wasCurrent = _currentDevice.value?.id == device.id
                if (!wasCurrent) {
                    adbConnection.connect(device.ipAddress, device.port)
                }
                
                adbConnection.sendKeyEvent(keyEvent.code)
                
                // Disconnect if it wasn't the current device
                if (!wasCurrent) {
                    adbConnection.disconnect()
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Room-based Organization - Get devices by room
     */
    fun getDevicesByRoom(room: String): List<DeviceProfile> {
        return _roomDevices.value[room] ?: emptyList()
    }
    
    fun getAllRooms(): List<String> {
        return _roomDevices.value.keys.toList()
    }
    
    /**
     * Execute action on all devices in a room
     */
    suspend fun executeOnRoom(room: String, action: suspend (DeviceProfile) -> Result<Unit>): Result<Map<String, Result<Unit>>> {
        return try {
            val roomDevices = getDevicesByRoom(room)
            if (roomDevices.isEmpty()) {
                return Result.failure(Exception("No devices found in room: $room"))
            }
            
            val results = mutableMapOf<String, Result<Unit>>()
            
            coroutineScope {
                roomDevices.map { device ->
                    async {
                        val result = action(device)
                        results[device.id] = result
                        result
                    }
                }.awaitAll()
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Quick device switching history management
     */
    private fun addToRecentDevices(device: DeviceProfile) {
        val current = _recentDevices.value.toMutableList()
        current.removeAll { it.id == device.id }
        current.add(0, device)
        
        // Keep only last 10 devices
        if (current.size > 10) {
            current.removeAt(current.size - 1)
        }
        
        _recentDevices.value = current
    }
    
    /**
     * Get devices sorted by room and online status
     */
    fun getOrganizedDevices(): Map<String, List<DeviceProfile>> {
        return _deviceProfiles.value
            .sortedWith(compareBy<DeviceProfile> { it.room ?: "" }.thenBy { !it.isOnline })
            .groupBy { it.room?.ifBlank { "Unassigned" } ?: "Unassigned" }
    }
    
    /**
     * Save and switch between multiple TV configurations
     */
    suspend fun saveDeviceConfiguration(deviceId: String, configurationName: String): Result<String> {
        return try {
            val device = _deviceProfiles.value.find { it.id == deviceId }
                ?: return Result.failure(Exception("Device not found"))
            
            // Create a copy with configuration name
            val configDevice = device.copy(
                id = UUID.randomUUID().toString(),
                name = "$configurationName - ${device.name}",
                nickname = configurationName
            )
            
            addDeviceProfile(configDevice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Batch device operations for home theater setup
     */
    suspend fun setupHomeTheater(
        mainTvId: String,
        surroundSoundId: String? = null,
        streamingDeviceId: String? = null
    ): Result<String> {
        return try {
            val homeTheaterGroup = DeviceGroup(
                id = UUID.randomUUID().toString(),
                name = "Home Theater",
                deviceIds = listOfNotNull(mainTvId, surroundSoundId, streamingDeviceId),
                room = "Living Room"
            )
            
            val updatedGroups = _deviceGroups.value.toMutableList()
            updatedGroups.add(homeTheaterGroup)
            _deviceGroups.value = updatedGroups
            
            Result.success(homeTheaterGroup.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun addDeviceProfile(deviceProfile: DeviceProfile): Result<String> {
        return try {
            val updatedProfiles = _deviceProfiles.value.toMutableList()
            
            // Check if device with same IP already exists
            val existingDevice = updatedProfiles.find { it.ipAddress == deviceProfile.ipAddress }
            if (existingDevice != null) {
                return Result.failure(Exception("Device with this IP address already exists"))
            }
            
            updatedProfiles.add(deviceProfile)
            _deviceProfiles.value = updatedProfiles
            Result.success(deviceProfile.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeDeviceProfile(deviceId: String): Result<Unit> {
        return try {
            val updatedProfiles = _deviceProfiles.value.toMutableList()
            updatedProfiles.removeAll { it.id == deviceId }
            _deviceProfiles.value = updatedProfiles
            
            // Remove from groups
            val updatedGroups = _deviceGroups.value.map { group ->
                group.copy(deviceIds = group.deviceIds.filter { it != deviceId })
            }
            _deviceGroups.value = updatedGroups
            
            // If this was the current device, disconnect
            if (_currentDevice.value?.id == deviceId) {
                disconnectFromDevice()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateDeviceProfile(deviceProfile: DeviceProfile): Result<Unit> {
        return try {
            val updatedProfiles = _deviceProfiles.value.toMutableList()
            val index = updatedProfiles.indexOfFirst { it.id == deviceProfile.id }
            if (index >= 0) {
                updatedProfiles[index] = deviceProfile
                _deviceProfiles.value = updatedProfiles
                
                // Update current device if it's the same one
                if (_currentDevice.value?.id == deviceProfile.id) {
                    _currentDevice.value = deviceProfile
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createDeviceGroup(name: String, deviceIds: List<String>, room: String): Result<String> {
        return try {
            val groupId = UUID.randomUUID().toString()
            val group = DeviceGroup(
                id = groupId,
                name = name,
                deviceIds = deviceIds,
                room = room
            )
            
            val updatedGroups = _deviceGroups.value.toMutableList()
            updatedGroups.add(group)
            _deviceGroups.value = updatedGroups
            
            Result.success(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteDeviceGroup(groupId: String): Result<Unit> {
        return try {
            val updatedGroups = _deviceGroups.value.toMutableList()
            updatedGroups.removeAll { it.id == groupId }
            _deviceGroups.value = updatedGroups
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshDeviceStatus(deviceId: String): Result<DeviceProfile> {
        return try {
            val device = _deviceProfiles.value.find { it.id == deviceId }
                ?: return Result.failure(Exception("Device not found"))
            
            val isOnline = try {
                connectToDevice(device.ipAddress, device.port).getOrDefault(false)
            } catch (e: Exception) {
                false
            }
            
            val updatedDevice = device.copy(
                isOnline = isOnline,
                lastConnected = if (isOnline) System.currentTimeMillis() else device.lastConnected
            )
            
            updateDeviceProfile(updatedDevice)
            
            if (isOnline) {
                disconnectFromDevice() // Disconnect after checking
            }
            
            Result.success(updatedDevice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshAllDevicesStatus(): Result<List<DeviceProfile>> {
        return try {
            val updatedDevices = mutableListOf<DeviceProfile>()
            
            for (device in _deviceProfiles.value) {
                val refreshResult = refreshDeviceStatus(device.id)
                if (refreshResult.isSuccess) {
                    updatedDevices.add(refreshResult.getOrNull()!!)
                } else {
                    updatedDevices.add(device.copy(isOnline = false))
                }
            }
            
            _deviceProfiles.value = updatedDevices
            Result.success(updatedDevices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Device discovery and auto-detection
    fun detectDeviceType(model: String, manufacturer: String): DeviceType {
        return when {
            model.contains("Chromecast", ignoreCase = true) -> DeviceType.CHROMECAST
            model.contains("Shield", ignoreCase = true) -> DeviceType.NVIDIA_SHIELD
            manufacturer.contains("Sony", ignoreCase = true) -> DeviceType.SONY_TV
            manufacturer.contains("TCL", ignoreCase = true) -> DeviceType.TCL_TV
            model.contains("Google TV", ignoreCase = true) -> DeviceType.GOOGLE_TV
            model.contains("Android TV", ignoreCase = true) -> DeviceType.ANDROID_TV
            else -> DeviceType.UNKNOWN
        }
    }
    
    // Current device operations
    suspend fun sendKeyEvent(keyEvent: KeyEvent): Result<Unit> {
        // Try WiFi first, fallback to ADB
        if (wifiConnectionManager.isConnected()) {
            return wifiConnectionManager.sendKeyEvent(keyEvent.code)
        }
        return adbConnection.sendKeyEvent(keyEvent.code)
    }
    
    suspend fun sendText(text: String): Result<Unit> {
        // Try WiFi first, fallback to ADB
        if (wifiConnectionManager.isConnected()) {
            return wifiConnectionManager.sendText(text)
        }
        return adbConnection.sendText(text)
    }
    
    suspend fun launchApp(packageName: String): Result<Unit> {
        // Try WiFi first, fallback to ADB
        if (wifiConnectionManager.isConnected()) {
            return wifiConnectionManager.launchApp(packageName)
        }
        return adbConnection.launchApp(packageName)
    }
    
    suspend fun forceStopApp(packageName: String): Result<Unit> {
        // Try WiFi first, fallback to ADB
        if (wifiConnectionManager.isConnected()) {
            return wifiConnectionManager.forceStopApp(packageName)
        }
        return adbConnection.forceStopApp(packageName)
    }
    
    suspend fun getInstalledApps(): Result<List<String>> {
        // Try WiFi first, fallback to ADB
        if (wifiConnectionManager.isConnected()) {
            val wifiResult = wifiConnectionManager.getInstalledApps()
            if (wifiResult.isSuccess) return wifiResult
        }
        return adbConnection.getInstalledApps()
    }
    fun getConnectionStatus(): StateFlow<ConnectionStatus> = _unifiedConnectionStatus
    
    suspend fun isConnected(): Boolean {
        return wifiConnectionManager.isConnected() || adbConnection.isConnected()
    }
    
    suspend fun isAuthorized(): Boolean {
        return wifiConnectionManager.isAuthorized() || adbConnection.isAuthorized()
    }
    
    suspend fun captureScreen(): Result<ByteArray> {
        // Try WiFi first, fallback to ADB
        if (wifiConnectionManager.isConnected()) {
            val wifiResult = wifiConnectionManager.captureScreen()
            if (wifiResult.isSuccess) return wifiResult
        }
        return adbConnection.captureScreen()
    }
    
    // Getters for device management
    fun getDeviceById(deviceId: String): DeviceProfile? {
        return _deviceProfiles.value.find { it.id == deviceId }
    }
    
    fun getOnlineDevices(): List<DeviceProfile> {
        return _deviceProfiles.value.filter { it.isOnline }
    }
    
    fun getGroupById(groupId: String): DeviceGroup? {
        return _deviceGroups.value.find { it.id == groupId }
    }
}
