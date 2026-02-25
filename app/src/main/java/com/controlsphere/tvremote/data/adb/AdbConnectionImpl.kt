package com.controlsphere.tvremote.data.adb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdbConnectionImpl @Inject constructor() : AdbConnection {
    
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var ipAddress: String? = null
    private var port: Int = 5555
    
    private val _connectionStatus = MutableStateFlow(
        ConnectionStatus(
            isConnected = false,
            isAuthorized = false
        )
    )
    
    override fun getConnectionStatus(): StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    override suspend fun connect(ipAddress: String, port: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AdbConnection", "Attempting to connect to $ipAddress:$port")
            this@AdbConnectionImpl.ipAddress = ipAddress
            this@AdbConnectionImpl.port = port
            
            // Close existing connection
            disconnect()
            
            // Create socket connection that supports both IPv4 and IPv6
            socket = Socket()
            android.util.Log.d("AdbConnection", "Creating socket connection...")
            socket!!.connect(InetSocketAddress(ipAddress, port), 5000)
            android.util.Log.d("AdbConnection", "Socket connected successfully")
            writer = PrintWriter(socket!!.getOutputStream(), true)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            
            // Send initial ADB command
            val response = sendCommandInternal("host:transport-any")
            
            if (response.isSuccess) {
                // Check if device is authorized
                val authStatus = checkAuthorizationStatus()
                _connectionStatus.value = ConnectionStatus(
                    isConnected = true,
                    isAuthorized = authStatus
                )
                Result.success(authStatus)
            } else {
                _connectionStatus.value = ConnectionStatus(
                    isConnected = false,
                    isAuthorized = false,
                    errorMessage = "Failed to establish ADB connection"
                )
                Result.failure(Exception("Failed to establish ADB connection"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AdbConnection", "Connection failed: ${e.message}", e)
            _connectionStatus.value = ConnectionStatus(
                isConnected = false,
                isAuthorized = false,
                errorMessage = e.message
            )
            Result.failure(e)
        }
    }
    
    override suspend fun disconnect(): Unit = withContext(Dispatchers.IO) {
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
                isAuthorized = false
            )
        }
    }
    
    override suspend fun isConnected(): Boolean {
        return socket?.isConnected == true && !socket!!.isClosed
    }
    
    override suspend fun isAuthorized(): Boolean {
        return checkAuthorizationStatus()
    }
    
    override suspend fun sendCommand(command: String): Result<String> {
        return sendCommandInternal(command)
    }
    
    override suspend fun sendKeyEvent(keyCode: Int): Result<Unit> {
        val command = "input keyevent $keyCode"
        return sendCommandInternal(command).map { Unit }
    }
    
    override suspend fun sendText(text: String): Result<Unit> {
        val escapedText = text.replace(" ", "%s").replace("&", "\\&")
        val command = "input text $escapedText"
        return sendCommandInternal(command).map { Unit }
    }
    
    override suspend fun launchApp(packageName: String): Result<Unit> {
        val command = "monkey -p $packageName 1"
        return sendCommandInternal(command).map { Unit }
    }
    
    override suspend fun forceStopApp(packageName: String): Result<Unit> {
        val command = "am force-stop $packageName"
        return sendCommandInternal(command).map { Unit }
    }
    
    override suspend fun getInstalledApps(): Result<List<String>> {
        val command = "pm list packages"
        return sendCommandInternal(command).map { result ->
            result.lines()
                .filter { it.startsWith("package:") }
                .map { it.substring(9) }
        }
    }
    
    private suspend fun sendCommandInternal(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isConnected()) {
                return@withContext Result.failure(Exception("Not connected to device"))
            }
            
            writer?.println(command)
            val response = reader?.readLine()
            
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("No response from device"))
            }
        } catch (e: Exception) {
            _connectionStatus.value = _connectionStatus.value.copy(
                isConnected = false,
                errorMessage = e.message
            )
            Result.failure(e)
        }
    }
    
    private suspend fun checkAuthorizationStatus(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = sendCommandInternal("getprop ro.product.model")
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun captureScreen(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val currentIp = ipAddress ?: return@withContext Result.failure(Exception("Not connected to device"))
            
            val captureSocket = Socket()
            // Using a new socket for binary data to avoid interfering with the command session
            captureSocket.connect(java.net.InetSocketAddress(currentIp, port), 2000)
            
            val out = captureSocket.getOutputStream()
            val inp = captureSocket.getInputStream()
            
            // ADB 'screencap -p' returns a raw PNG stream
            out.write("screencap -p\n".toByteArray())
            out.flush()
            
            val outputStream = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(16384)
            var bytesRead: Int
            
            while (inp.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            
            captureSocket.close()
            val data = outputStream.toByteArray()
            if (data.isEmpty()) {
                Result.failure(Exception("Empty screenshot received"))
            } else {
                Result.success(data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
