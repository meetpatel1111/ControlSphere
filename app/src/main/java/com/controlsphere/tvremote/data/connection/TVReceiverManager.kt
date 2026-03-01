package com.controlsphere.tvremote.data.connection

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class TVReceiverManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var isRunning = false
    
    private val _receiverStatus = MutableStateFlow(
        ReceiverStatus(
            isReceiving = false,
            connectedDevice = null,
            deviceInfo = getDeviceInfo(),
            commandHistory = emptyList(),
            errorMessage = null
        )
    )
    val receiverStatus: StateFlow<ReceiverStatus> = _receiverStatus.asStateFlow()
    
    data class ReceiverStatus(
        val isReceiving: Boolean = false,
        val connectedDevice: ConnectedDevice? = null,
        val deviceInfo: DeviceInfo,
        val commandHistory: List<ReceivedCommand> = emptyList(),
        val errorMessage: String? = null
    )
    
    data class ConnectedDevice(
        val ip: String,
        val port: Int,
        val name: String
    )
    
    data class DeviceInfo(
        val name: String,
        val ip: String,
        val port: Int
    )
    
    data class ReceivedCommand(
        val timestamp: String,
        val command: String,
        val type: CommandType
    )
    
    enum class CommandType {
        KEY_EVENT,
        TEXT_INPUT,
        APP_LAUNCH,
        APP_STOP,
        VOICE_COMMAND,
        UNKNOWN
    }
    
    suspend fun startReceiver(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isRunning) {
                Log.d("TVReceiver", "TV Receiver already running, skipping start")
                return@withContext Result.success(true)
            }
            
            val port = 5556
            
            // Add a small delay to prevent rapid restarts
            kotlinx.coroutines.delay(500)
            
            // Close any existing socket first
            stopReceiver()
            
            // Create server socket with explicit binding to IPv4
            try {
                serverSocket = ServerSocket()
                serverSocket?.reuseAddress = true
                // Force IPv4 binding
                val inetAddress = java.net.InetAddress.getByName("0.0.0.0")
                serverSocket?.bind(java.net.InetSocketAddress(inetAddress, port), 50)
                isRunning = true
                
                _receiverStatus.value = _receiverStatus.value.copy(
                    isReceiving = true,
                    errorMessage = null
                )
                
                Log.d("TVReceiver", "TV Receiver started on port $port")
                Log.d("TVReceiver", "Socket bound successfully: ${serverSocket?.isBound}")
                Log.d("TVReceiver", "Socket local address: ${serverSocket?.localSocketAddress}")
                
                // Start listening for connections in a separate coroutine that doesn't get cancelled
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        while (isRunning) {
                            try {
                                serverSocket?.let { socket ->
                                    socket.soTimeout = 10000 // 10 second timeout
                                    Log.d("TVReceiver", "Waiting for client connection...")
                                    val client = socket.accept()
                                    if (client != null && isRunning) {
                                        Log.d("TVReceiver", "Client connected: ${client.inetAddress.hostAddress}")
                                        handleClientConnection(client)
                                    }
                                }
                            } catch (e: Exception) {
                                if (isRunning) {
                                    Log.e("TVReceiver", "Error accepting client connection", e)
                                    // Don't break the loop on socket timeout errors
                                    if (e is java.net.SocketTimeoutException) {
                                        Log.d("TVReceiver", "Socket timeout, continuing to wait...")
                                        continue
                                    } else {
                                        Log.e("TVReceiver", "Breaking receiver loop due to error", e)
                                        break
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TVReceiver", "Error in receiver loop", e)
                    }
                }
                
                Result.success(true)
            } catch (e: java.net.BindException) {
                Log.e("TVReceiver", "Port $port already in use", e)
                _receiverStatus.value = _receiverStatus.value.copy(
                    isReceiving = false,
                    errorMessage = "Port $port already in use"
                )
                Result.failure(e)
            }
            
        } catch (e: Exception) {
            Log.e("TVReceiver", "Failed to start receiver: ${e.message}", e)
            _receiverStatus.value = _receiverStatus.value.copy(
                isReceiving = false,
                errorMessage = "Failed to start receiver: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    suspend fun stopReceiver() = withContext(Dispatchers.IO) {
        try {
            isRunning = false
            Log.d("TVReceiver", "Stopping TV Receiver...")
            
            clientSocket?.let { client ->
                try {
                    client.close()
                    Log.d("TVReceiver", "Client socket closed")
                } catch (e: Exception) {
                    Log.e("TVReceiver", "Error closing client socket", e)
                }
                clientSocket = null
            }
            
            serverSocket?.let { server ->
                try {
                    server.close()
                    Log.d("TVReceiver", "Server socket closed")
                } catch (e: Exception) {
                    Log.e("TVReceiver", "Error closing server socket", e)
                }
                serverSocket = null
            }
            
            _receiverStatus.value = _receiverStatus.value.copy(
                isReceiving = false,
                connectedDevice = null,
                errorMessage = null
            )
            
            Log.d("TVReceiver", "TV Receiver stopped")
        } catch (e: Exception) {
            Log.e("TVReceiver", "Error stopping receiver: ${e.message}", e)
        }
    }
    
    private suspend fun handleClientConnection(client: Socket) = withContext(Dispatchers.IO) {
        try {
            clientSocket = client
            val clientAddress = client.inetAddress.hostAddress
            val clientPort = client.port
            
            val connectedDevice = ConnectedDevice(
                ip = clientAddress,
                port = clientPort,
                name = "Remote Control"
            )
            
            _receiverStatus.value = _receiverStatus.value.copy(
                connectedDevice = connectedDevice
            )
            
            writer = PrintWriter(client.getOutputStream(), true)
            reader = BufferedReader(InputStreamReader(client.getInputStream()))
            
            // Wait for PING from client and respond with OK
            val deviceInfo = getDeviceInfo()
            val responseMessage = "OK:ControlSphere ${deviceInfo.name} Connected"
            
            // Listen for commands
            while (isRunning) {
                val line = reader?.readLine()
                if (line == null) {
                    Log.d("TVReceiver", "Client disconnected (null read)")
                    break
                }
                
                when {
                    line == "PING" || line == "TEST_CONNECTION" -> {
                        // Respond to ping/test with OK
                        writer?.println(responseMessage)
                        addCommand("PING from $clientAddress - responded OK", CommandType.UNKNOWN)
                        Log.d("TVReceiver", "Responded to PING from $clientAddress")
                    }
                    else -> {
                        processCommand(line)
                        addCommand(line, getCommandType(line))
                        // Send response back so client doesn't hang
                        writer?.println("OK")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e("TVReceiver", "Error handling client: ${e.message}", e)
            addCommand("Client handling error: ${e.message}", CommandType.UNKNOWN)
        } finally {
            clientSocket?.close()
            _receiverStatus.value = _receiverStatus.value.copy(
                connectedDevice = null
            )
        }
    }
    
    private suspend fun processCommand(command: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("TVReceiver", "Processing command: $command")
            
            when {
                command.startsWith("input keyevent") -> {
                    val keyCode = command.substringAfter("input keyevent ").trim()
                    executeKeyEvent(keyCode.toIntOrNull() ?: 0)
                }
                command.startsWith("input text") -> {
                    val text = command.substringAfter("input text ").trim()
                    executeTextInput(text)
                }
                command.startsWith("monkey") -> {
                    val packageName = command.substringAfter("monkey -p ").substringBefore(" ").trim()
                    executeAppLaunch(packageName)
                }
                command.startsWith("am force-stop") -> {
                    val packageName = command.substringAfter("am force-stop ").trim()
                    executeAppStop(packageName)
                }
                command.startsWith("VOICE:") -> {
                    val voiceCommand = command.substringAfter("VOICE:").trim()
                    executeVoiceCommand(voiceCommand)
                }
                else -> {
                    Log.w("TVReceiver", "Unknown command: $command")
                }
            }
        } catch (e: Exception) {
            Log.e("TVReceiver", "Error processing command: ${e.message}", e)
        }
    }
    
    private suspend fun executeKeyEvent(keyCode: Int) = withContext(Dispatchers.IO) {
        try {
            // Execute key event on TV
            val process = Runtime.getRuntime().exec("input keyevent $keyCode")
            process.waitFor()
            Log.d("TVReceiver", "Key event executed: $keyCode")
        } catch (e: Exception) {
            Log.e("TVReceiver", "Failed to execute key event: ${e.message}", e)
        }
    }
    
    private suspend fun executeTextInput(text: String) = withContext(Dispatchers.IO) {
        try {
            val escapedText = text.replace(" ", "%s")
            val process = Runtime.getRuntime().exec("input text $escapedText")
            process.waitFor()
            Log.d("TVReceiver", "Text input executed: $text")
        } catch (e: Exception) {
            Log.e("TVReceiver", "Failed to execute text input: ${e.message}", e)
        }
    }
    
    private suspend fun executeAppLaunch(packageName: String) = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("monkey -p $packageName 1")
            process.waitFor()
            Log.d("TVReceiver", "App launched: $packageName")
        } catch (e: Exception) {
            Log.e("TVReceiver", "Failed to launch app: ${e.message}", e)
        }
    }
    
    private suspend fun executeAppStop(packageName: String) = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("am force-stop $packageName")
            process.waitFor()
            Log.d("TVReceiver", "App stopped: $packageName")
        } catch (e: Exception) {
            Log.e("TVReceiver", "Failed to stop app: ${e.message}", e)
        }
    }
    
    private suspend fun executeVoiceCommand(voiceCommand: String) = withContext(Dispatchers.IO) {
        try {
            // Handle voice commands - this would integrate with your voice system
            Log.d("TVReceiver", "Voice command received: $voiceCommand")
            
            // For now, just log it. In a real implementation, this would:
            // 1. Process the voice command
            // 2. Execute appropriate action
            // 3. Provide feedback
        } catch (e: Exception) {
            Log.e("TVReceiver", "Failed to execute voice command: ${e.message}", e)
        }
    }
    
    private fun getCommandType(command: String): CommandType {
        return when {
            command.startsWith("input keyevent") -> CommandType.KEY_EVENT
            command.startsWith("input text") -> CommandType.TEXT_INPUT
            command.startsWith("monkey") -> CommandType.APP_LAUNCH
            command.startsWith("am force-stop") -> CommandType.APP_STOP
            command.startsWith("VOICE:") -> CommandType.VOICE_COMMAND
            else -> CommandType.UNKNOWN
        }
    }
    
    private fun addCommand(command: String, type: CommandType) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newCommand = ReceivedCommand(timestamp, command, type)
        
        val currentHistory = _receiverStatus.value.commandHistory.toMutableList()
        currentHistory.add(newCommand)
        
        // Keep only last 50 commands
        if (currentHistory.size > 50) {
            currentHistory.removeAt(0)
        }
        
        _receiverStatus.value = _receiverStatus.value.copy(
            commandHistory = currentHistory
        )
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return try {
            // Get actual device information
            val deviceName = getDeviceName()
            val ipAddress = getLocalIpAddress()
            val port = 5556
            
            Log.d("TVReceiver", "Detected device: $deviceName at $ipAddress:$port")
            
            DeviceInfo(
                name = deviceName,
                ip = ipAddress,
                port = port
            )
        } catch (e: Exception) {
            Log.e("TVReceiver", "Error getting device info: ${e.message}", e)
            DeviceInfo(
                name = "Android TV",
                ip = "Unknown",
                port = 5556
            )
        }
    }
    
    private fun getDeviceName(): String {
        return try {
            // Get the actual model name from Build.MODEL
            val model = Build.MODEL
            
            // If model is empty or generic, try other methods
            when {
                model.isNotEmpty() && !model.equals("unknown", ignoreCase = true) -> {
                    // Return the actual model name
                    model
                }
                
                // Method 2: Build.PRODUCT
                Build.PRODUCT.isNotEmpty() && !Build.PRODUCT.equals("unknown", ignoreCase = true) -> {
                    Build.PRODUCT
                }
                
                // Method 3: Build.BRAND + Build.MODEL
                Build.BRAND.isNotEmpty() && Build.MODEL.isNotEmpty() -> {
                    "${Build.BRAND} ${Build.MODEL}"
                }
                
                // Method 4: Just brand if available
                Build.BRAND.isNotEmpty() -> {
                    "${Build.BRAND} Device"
                }
                
                // Fallback
                else -> "Android TV"
            }
        } catch (e: Exception) {
            Log.w("TVReceiver", "Could not get device name: ${e.message}")
            "Android TV"
        }
    }
    
    private fun getLocalIpAddress(): String {
        return try {
            // Method 1: Try to get WiFi IP address
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true) || 
                    networkInterface.name.equals("eth0", ignoreCase = true)) {
                    val addresses = networkInterface.inetAddresses
                    for (address in Collections.list(addresses)) {
                        if (!address.isLoopbackAddress && address.hostAddress?.contains(':') == false) {
                            return address.hostAddress ?: "Unknown"
                        }
                    }
                }
            }
            
            // Method 2: Fallback to InetAddress.getLocalHost()
            val localAddress = InetAddress.getLocalHost()
            localAddress.hostAddress ?: "Unknown"
            
        } catch (e: Exception) {
            Log.w("TVReceiver", "Could not get IP address: ${e.message}")
            "Unknown"
        }
    }
    
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val testMessage = "Test connection from TV - ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
            addCommand(testMessage, CommandType.UNKNOWN)
            Result.success(testMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
