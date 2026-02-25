package com.controlsphere.tvremote.data.adb

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdbConnectionService : Service() {
    
    @Inject
    lateinit var adbConnection: AdbConnectionImpl
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val ipAddress = intent.getStringExtra(EXTRA_IP_ADDRESS)
                val port = intent.getIntExtra(EXTRA_PORT, 5555)
                if (ipAddress != null) {
                    connectToDevice(ipAddress, port)
                }
            }
            ACTION_DISCONNECT -> {
                disconnectFromDevice()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    fun getConnectionStatus(): StateFlow<ConnectionStatus> {
        return adbConnection.getConnectionStatus()
    }
    
    private fun connectToDevice(ipAddress: String, port: Int) {
        serviceScope.launch {
            adbConnection.connect(ipAddress, port)
        }
    }
    
    private fun disconnectFromDevice() {
        serviceScope.launch {
            adbConnection.disconnect()
        }
    }
    
    companion object {
        const val ACTION_CONNECT = "com.controlsphere.tvremote.CONNECT"
        const val ACTION_DISCONNECT = "com.controlsphere.tvremote.DISCONNECT"
        
        const val EXTRA_IP_ADDRESS = "ip_address"
        const val EXTRA_PORT = "port"
    }
}
