package com.controlsphere.tvremote.data.connection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.PrintWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class WiFiConnectionManager @Inject constructor() {
    
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var ipAddress: String? = null
    private var port: Int = 5556
    
    private val _connectionStatus = MutableStateFlow(
        ConnectionStatus(
            isConnected = false,
            isAuthorized = false,
            connectionType = ConnectionType.WIFI,
            errorMessage = null,
            connectedDevice = null
        )
    )
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    enum class ConnectionType {
        WIFI,
        ADB,
        NONE
    }
    
    data class ConnectionStatus(
        val isConnected: Boolean = false,
        val isAuthorized: Boolean = false,
        val connectionType: ConnectionType = ConnectionType.NONE,
        val errorMessage: String? = null,
        val connectedDevice: ConnectedDevice? = null
    )
    
    data class ConnectedDevice(
        val ip: String,
        val port: Int,
        val name: String
    )
    
    suspend fun connect(ipAddress: String, port: Int = 5556): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            this@WiFiConnectionManager.ipAddress = ipAddress
            this@WiFiConnectionManager.port = port
            
            // Close existing connection
            disconnect()
            
            Log.d("WiFiConnection", "Attempting WiFi connection to $ipAddress:$port")
            
            // Create socket connection
            val localSocket = Socket()
            localSocket.connect(InetSocketAddress(ipAddress, port), 3000)
            socket = localSocket
            
            if (localSocket.isConnected) {
                // Set read timeout for handshake so we don't hang forever
                localSocket.soTimeout = 5000
                
                writer = PrintWriter(localSocket.getOutputStream(), true)
                reader = BufferedReader(InputStreamReader(localSocket.getInputStream()))
                
                // Test connection by sending a test command
                writer?.println("PING")
                val response = try {
                    reader?.readLine()
                } catch (e: java.net.SocketTimeoutException) {
                    Log.w("WiFiConnection", "Timeout waiting for PING response")
                    null
                }
                
                if (response != null && response.contains("OK")) {
                    // Clear timeout for normal operation (commands wait for response)
                    localSocket.soTimeout = 10000
                    
                    val connectedDevice = ConnectedDevice(
                        ip = ipAddress,
                        port = port,
                        name = getConnectedDeviceName(response)
                    )
                    
                    _connectionStatus.value = ConnectionStatus(
                        isConnected = true,
                        isAuthorized = true,
                        connectionType = ConnectionType.WIFI,
                        errorMessage = null,
                        connectedDevice = connectedDevice
                    )
                    
                    Log.d("WiFiConnection", "WiFi connection successful to $ipAddress:$port")
                    Result.success(true)
                } else {
                    localSocket.close()
                    _connectionStatus.value = ConnectionStatus(
                        isConnected = false,
                        isAuthorized = false,
                        connectionType = ConnectionType.NONE,
                        errorMessage = "TV receiver not responding"
                    )
                    return@withContext Result.failure(Exception("TV receiver not responding"))
                }
            } else {
                _connectionStatus.value = ConnectionStatus(
                    isConnected = false,
                    isAuthorized = false,
                    connectionType = ConnectionType.NONE,
                    errorMessage = "Socket connection failed"
                )
                return@withContext Result.failure(Exception("Socket connection failed"))
            }
        } catch (e: Exception) {
            Log.e("WiFiConnection", "WiFi connection failed: ${e.message}", e)
            _connectionStatus.value = ConnectionStatus(
                isConnected = false,
                isAuthorized = false,
                connectionType = ConnectionType.NONE,
                errorMessage = e.message
            )
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun disconnect(): Unit = withContext(Dispatchers.IO) {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignore cleanup errors
        } finally {
            socket = null
            writer = null
            reader = null
            
            _connectionStatus.value = ConnectionStatus(
                isConnected = false,
                isAuthorized = false,
                connectionType = ConnectionType.NONE,
                errorMessage = null,
                connectedDevice = null
            )
        }
    }
    
    suspend fun sendCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isConnected()) {
                return@withContext Result.failure(Exception("Not connected to TV"))
            }
            
            Log.d("WiFiConnection", "Sending command: $command")
            writer?.println(command)
            val response = reader?.readLine()
            
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("No response from TV"))
            }
        } catch (e: Exception) {
            Log.e("WiFiConnection", "Failed to send command: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun sendKeyEvent(keyCode: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val command = "input keyevent $keyCode"
        return@withContext sendCommand(command).map { Unit }
    }
    
    suspend fun sendText(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        val escapedText = text.replace(" ", "%s").replace("&", "\\&")
        val command = "input text $escapedText"
        return@withContext sendCommand(command).map { Unit }
    }
    
    suspend fun launchApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val command = "monkey -p $packageName 1"
        return@withContext sendCommand(command).map { Unit }
    }
    
    suspend fun forceStopApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val command = "am force-stop $packageName"
        return@withContext sendCommand(command).map { Unit }
    }
    
    suspend fun sendVoiceCommand(voiceCommand: String): Result<Unit> = withContext(Dispatchers.IO) {
        val command = "VOICE:$voiceCommand"
        return@withContext sendCommand(command).map { Unit }
    }
    
    suspend fun isConnected(): Boolean {
        return connectionStatus.value.isConnected
    }
    
    suspend fun isAuthorized(): Boolean {
        return connectionStatus.value.isAuthorized
    }
    
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isConnected()) {
                return@withContext Result.failure(Exception("Not connected to TV"))
            }
            
            val testCommand = "TEST_CONNECTION"
            val result = sendCommand(testCommand)
            result.map { "Test successful: $it" }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getConnectedDeviceName(response: String): String {
        return try {
            // Extract the actual device name from TV response
            // Response format: "OK:ControlSphere [DeviceName] Connected"
            val deviceName = response.substringAfter("OK:ControlSphere").substringBefore(" Connected").trim()
            
            // Return the device name as-is from the TV
            when {
                deviceName.isNotEmpty() && !deviceName.equals("ControlSphere", ignoreCase = true) -> {
                    deviceName
                }
                else -> "Smart TV"
            }
        } catch (e: Exception) {
            Log.w("WiFiConnection", "Could not extract device name from response: $response")
            "Smart TV"
        }
    }
}
