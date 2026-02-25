package com.controlsphere.tvremote.data.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDiscoveryImpl @Inject constructor() : DeviceDiscovery {
    
    private var isScanning = false
    
    override suspend fun scanForDevices(): Flow<DiscoveredDevice> = flow {
        isScanning = true
        val subnet = getLocalSubnet()
        
        if (subnet != null) {
            // Scan the local subnet for devices with ADB port open
            val baseIp = subnet.substringBeforeLast(".")
            
            for (i in 1..254) {
                if (!isScanning) break
                
                val ipAddress = "$baseIp.$i"
                val result = testConnection(ipAddress)
                
                if (result.isSuccess && result.getOrNull() == true) {
                    val deviceInfo = getDeviceInfo(ipAddress)
                    emit(
                        DiscoveredDevice(
                            ipAddress = ipAddress,
                            deviceName = deviceInfo?.first,
                            manufacturer = deviceInfo?.second,
                            model = deviceInfo?.third
                        )
                    )
                }
                
                delay(50) // Small delay to avoid overwhelming the network
            }
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun testConnection(ipAddress: String, port: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ipAddress, port), 2000) // 2 second timeout
            
            if (socket.isConnected) {
                socket.close()
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun stopScanning() {
        isScanning = false
    }
    
    private fun getLocalSubnet(): String? {
        return try {
            val localAddress = InetAddress.getLocalHost()
            val hostAddress = localAddress.hostAddress
            hostAddress?.substringBeforeLast(".")
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun getDeviceInfo(ipAddress: String): Triple<String?, String?, String?>? = withContext(Dispatchers.IO) {
        try {
            // Try to get basic device info via ADB
            val socket = Socket(ipAddress, 5555)
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            
            // Get device model
            writer.println("host:transport-any")
            reader.readLine()
            
            writer.println("shell:getprop ro.product.model")
            val model = reader.readLine()
            
            writer.println("shell:getprop ro.product.manufacturer")
            val manufacturer = reader.readLine()
            
            writer.println("shell:getprop ro.product.device")
            val deviceName = reader.readLine()
            
            socket.close()
            
            Triple(deviceName, manufacturer, model)
        } catch (e: Exception) {
            null
        }
    }
}
