package com.controlsphere.tvremote.data.adb

import kotlinx.coroutines.flow.Flow

interface AdbConnection {
    suspend fun connect(ipAddress: String, port: Int = 5555): Result<Boolean>
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
    suspend fun isAuthorized(): Boolean
    suspend fun sendCommand(command: String): Result<String>
    suspend fun sendKeyEvent(keyCode: Int): Result<Unit>
    suspend fun sendText(text: String): Result<Unit>
    suspend fun launchApp(packageName: String): Result<Unit>
    suspend fun forceStopApp(packageName: String): Result<Unit>
    suspend fun getInstalledApps(): Result<List<String>>
    fun getConnectionStatus(): Flow<ConnectionStatus>
    suspend fun captureScreen(): Result<ByteArray>
}

data class ConnectionStatus(
    val isConnected: Boolean,
    val isAuthorized: Boolean,
    val errorMessage: String? = null
)
