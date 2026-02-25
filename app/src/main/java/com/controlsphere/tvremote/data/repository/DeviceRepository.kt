package com.controlsphere.tvremote.data.repository

import com.controlsphere.tvremote.data.adb.AdbConnection
import com.controlsphere.tvremote.domain.model.AppInfo
import com.controlsphere.tvremote.domain.model.Device
import com.controlsphere.tvremote.domain.model.KeyEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val adbConnection: AdbConnection
) {
    
    suspend fun connectToDevice(ipAddress: String, port: Int = 5555): Result<Boolean> {
        return adbConnection.connect(ipAddress, port)
    }
    
    suspend fun disconnectFromDevice() {
        adbConnection.disconnect()
    }
    
    suspend fun sendKeyEvent(keyEvent: KeyEvent): Result<Unit> {
        return adbConnection.sendKeyEvent(keyEvent.code)
    }
    
    suspend fun sendText(text: String): Result<Unit> {
        return adbConnection.sendText(text)
    }
    
    suspend fun launchApp(packageName: String): Result<Unit> {
        return adbConnection.launchApp(packageName)
    }
    
    suspend fun forceStopApp(packageName: String): Result<Unit> {
        return adbConnection.forceStopApp(packageName)
    }
    
    suspend fun getInstalledApps(): Result<List<String>> {
        return adbConnection.getInstalledApps()
    }
    
    fun getConnectionStatus(): StateFlow<com.controlsphere.tvremote.data.adb.ConnectionStatus> {
        return adbConnection.getConnectionStatus() as StateFlow<com.controlsphere.tvremote.data.adb.ConnectionStatus>
    }
    
    suspend fun isConnected(): Boolean {
        return adbConnection.isConnected()
    }
    
    suspend fun isAuthorized(): Boolean {
        return adbConnection.isAuthorized()
    }
    
    suspend fun captureScreen(): Result<ByteArray> {
        return adbConnection.captureScreen()
    }
}
