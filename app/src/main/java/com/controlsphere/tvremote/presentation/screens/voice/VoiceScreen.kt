package com.controlsphere.tvremote.presentation.screens.voice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.controlsphere.tvremote.presentation.screens.voice.components.*
import com.controlsphere.tvremote.presentation.navigation.Screen
import com.controlsphere.tvremote.data.voice.RecordingState
import com.controlsphere.tvremote.data.voice.VoiceConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    navController: NavController,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showApiKeyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Control") },
                actions = {
                    IconButton(onClick = { showApiKeyDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { navController.navigate("advanced_voice") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Advanced Voice")
                    }
                }
            )
        }
    ) { paddingValues ->
        // API Key dialog
        if (showApiKeyDialog) {
            ApiKeyDialog(
                currentApiKey = uiState.apiKey,
                onDismiss = { showApiKeyDialog = false },
                onSave = { apiKey ->
                    viewModel.saveApiKey(apiKey)
                    showApiKeyDialog = false
                }
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Tap the microphone button and speak a command. ControlSphere uses Google's latest Gemini AI models for intelligent voice control.\n\n" +
                              "**Basic Commands:**\n" +
                              "• \"Open Netflix\" - Launch apps\n" +
                              "• \"Search for movies\" - Global search\n" +
                              "• \"Volume up\" - Audio control\n" +
                              "• \"Play\" - Media control\n\n" +
                              "**Advanced Features:**\n" +
                              "• Natural language understanding\n" +
                              "• Context-aware commands\n" +
                              "• Real-time processing\n" +
                              "• Multi-step actions",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                // Voice control button with Live API option
                if (VoiceConfig.FeatureFlags.ENABLE_LIVE_API) {
                    LiveVoiceControl(
                        apiKey = uiState.apiKey,
                        onStartLiveSession = { /* Handle live session start */ },
                        onStopLiveSession = { /* Handle live session stop */ }
                    )
                } else {
                    VoiceControlButton(
                        recordingState = uiState.recordingState,
                        onStartRecording = { viewModel.startVoiceCommand() },
                        onStopRecording = { /* Handled automatically */ }
                    )
                }
            }

            item {
                // Status text
                when (uiState.recordingState) {
                    RecordingState.IDLE -> {
                        Text(
                            text = "Tap to start voice command",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RecordingState.RECORDING -> {
                        Text(
                            text = "Listening...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    RecordingState.PROCESSING -> {
                        Text(
                            text = "Processing command...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    RecordingState.ERROR -> {
                        Text(
                            text = "Error occurred. Try again.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
