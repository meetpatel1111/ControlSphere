package com.controlsphere.tvremote.presentation.tvreceiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.connection.TVReceiverManager
import com.controlsphere.tvremote.data.connection.TVReceiverManager.CommandType
import com.controlsphere.tvremote.data.connection.TVReceiverManager.ReceivedCommand
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TVReceiverScreen(
    navController: NavController,
    viewModel: TVReceiverViewModel = hiltViewModel()
) {
    val uiState by viewModel.receiverStatus.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            // Only start if not already running
            if (!uiState.isReceiving) {
                viewModel.startReceiver()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ControlSphere TV") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "TV Receiver Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (uiState.isReceiving) "🟢 Receiving Commands" else "🔴 Waiting for Connection",
                            color = if (uiState.isReceiving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    uiState.connectedDevice?.let { device ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📱 Connected to: ${device.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "🌐 IP: ${device.ip}:${device.port}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    uiState.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "❌ Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Device info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📺 Device Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "Device: ${uiState.deviceInfo.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "🌐 IP Address: ${uiState.deviceInfo.ip}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "🔌 Port: ${uiState.deviceInfo.port}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "📋 Instructions:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "1. Ensure phone and TV are on same WiFi",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                    Text(
                        text = "2. Open ControlSphere on phone",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                    Text(
                        text = "3. Scan for devices or enter TV IP manually",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                    Text(
                        text = "4. Connect to control your TV",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Command history
            if (uiState.commandHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📜 Command History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.commandHistory.takeLast(10)) { command ->
                                CommandItem(command)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { 
                        scope.launch {
                            viewModel.testConnection()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🧪 Test")
                }
                
                OutlinedButton(
                    onClick = { 
                        scope.launch {
                            viewModel.restartReceiver()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔄 Restart")
                }
            }
        }
    }
}

@Composable
private fun CommandItem(command: ReceivedCommand) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "⏰ ${command.timestamp}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = getCommandIcon(command.type) + " " + command.command,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun getCommandIcon(type: CommandType): String {
    return when (type) {
        CommandType.KEY_EVENT -> "⌨️"
        CommandType.TEXT_INPUT -> "📝"
        CommandType.APP_LAUNCH -> "🚀"
        CommandType.APP_STOP -> "🛑"
        CommandType.VOICE_COMMAND -> "🎤"
        CommandType.UNKNOWN -> "❓"
    }
}
