package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.AmbientModeConfig
import com.controlsphere.tvremote.data.voice.WakeWordState

@Composable
fun AmbientModeCard(
    config: AmbientModeConfig,
    isActive: Boolean,
    wakeWordState: WakeWordState,
    onEnable: (String, Float, Boolean) -> Unit,
    onDisable: () -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ambient Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onConfigure) {
                        Icon(Icons.Default.Settings, contentDescription = "Configure")
                    }
                    
                    Switch(
                        checked = isActive,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                onEnable(config.wakeWord, config.sensitivity, config.continuousListening)
                            } else {
                                onDisable()
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isActive) {
                // Wake word status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (statusColor, statusText) = when (wakeWordState) {
                        WakeWordState.INACTIVE -> MaterialTheme.colorScheme.error to "Inactive"
                        WakeWordState.LISTENING -> MaterialTheme.colorScheme.primary to "Listening"
                        WakeWordState.PROCESSING -> MaterialTheme.colorScheme.tertiary to "Processing"
                        WakeWordState.RESPONDING -> MaterialTheme.colorScheme.secondary to "Responding"
                        WakeWordState.ERROR -> MaterialTheme.colorScheme.error to "Error"
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(end = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = statusColor,
                            shape = MaterialTheme.shapes.small
                        ) {}
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Configuration info
            Column {
                Text(
                    text = "Wake Word: \"${config.wakeWord}\"",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Sensitivity: ${(config.sensitivity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Response Delay: ${config.responseDelay}ms",
                    style = MaterialTheme.typography.bodySmall
                )
                
                if (config.continuousListening) {
                    Text(
                        text = "Continuous Listening: Enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (config.privacyMode) {
                    Text(
                        text = "Privacy Mode: Enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (config.batteryOptimization) {
                    Text(
                        text = "Battery Optimization: Enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
