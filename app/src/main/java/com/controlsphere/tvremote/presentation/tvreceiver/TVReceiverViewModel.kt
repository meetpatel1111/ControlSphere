package com.controlsphere.tvremote.presentation.tvreceiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.connection.TVReceiverManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TVReceiverViewModel @Inject constructor(
    private val tvReceiverManager: TVReceiverManager
) : ViewModel() {
    
    val receiverStatus: StateFlow<TVReceiverManager.ReceiverStatus> = tvReceiverManager.receiverStatus
    
    fun startReceiver() {
        viewModelScope.launch {
            tvReceiverManager.startReceiver()
        }
    }
    
    fun stopReceiver() {
        viewModelScope.launch {
            tvReceiverManager.stopReceiver()
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            tvReceiverManager.testConnection()
        }
    }
    
    fun restartReceiver() {
        viewModelScope.launch {
            tvReceiverManager.stopReceiver()
            kotlinx.coroutines.delay(1000)
            tvReceiverManager.startReceiver()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopReceiver()
    }
}
