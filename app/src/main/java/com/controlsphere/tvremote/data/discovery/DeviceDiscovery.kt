package com.controlsphere.tvremote.data.discovery

import kotlinx.coroutines.flow.Flow

interface DeviceDiscovery {
    suspend fun scanForDevices(): Flow<DiscoveredDevice>
    suspend fun testConnection(ipAddress: String, port: Int = 5556): Result<Boolean>
    fun stopScanning()
}

data class DiscoveredDevice(
    val ipAddress: String,
    val port: Int = 5556,
    val deviceName: String? = null,
    val manufacturer: String? = null,
    val model: String? = null
)
