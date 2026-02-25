package com.controlsphere.tvremote.presentation.screens.remote

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.controlsphere.tvremote.R
import com.controlsphere.tvremote.presentation.navigation.Screen
import com.controlsphere.tvremote.presentation.screens.remote.components.ActionButtons
import com.controlsphere.tvremote.presentation.screens.remote.components.DPadControl
import com.controlsphere.tvremote.presentation.screens.remote.components.MediaControls
import com.controlsphere.tvremote.presentation.screens.remote.components.VolumeControls
import com.controlsphere.tvremote.presentation.screens.remote.components.QuickActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    navController: NavController,
    viewModel: RemoteViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    // Navigate back to pairing if disconnected
    LaunchedEffect(uiState.isConnected) {
        if (!uiState.isConnected) {
            navController.navigate(Screen.DevicePairing.route) {
                popUpTo(Screen.Remote.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.remote_title)) },
                actions = {
                    // Connection status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(end = 8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = if (uiState.isConnected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.small
                            ) {}
                        }
                        Text(
                            text = if (uiState.isConnected) 
                                stringResource(R.string.connected) 
                            else 
                                stringResource(R.string.disconnected),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            // Quick action buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                ActionButtons(
                    onAppsClick = { navController.navigate("apps") },
                    onSettingsClick = { /* Navigate to settings */ },
                    onPowerClick = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.POWER) },
                    onHomeClick = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.HOME) },
                    onBackClick = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.BACK) }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isConnected) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Volume controls at the top
                VolumeControls(
                    onVolumeUp = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.VOLUME_UP) },
                    onVolumeDown = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.VOLUME_DOWN) },
                    onMute = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.MUTE) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // D-Pad controls
                DPadControl(
                    onUp = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_UP) },
                    onDown = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_DOWN) },
                    onLeft = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_LEFT) },
                    onRight = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_RIGHT) },
                    onCenter = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.DPAD_CENTER) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick actions for text input and search
                QuickActions(
                    onTextInput = { navController.navigate("text_input") },
                    onSearch = { navController.navigate("search") },
                    onVoiceControl = { navController.navigate("voice") }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Media controls
                MediaControls(
                    onPlayPause = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.PLAY_PAUSE) },
                    onPrevious = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.MEDIA_PREVIOUS) },
                    onNext = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.MEDIA_NEXT) },
                    onRewind = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.REWIND) },
                    onFastForward = { viewModel.sendKeyEvent(com.controlsphere.tvremote.domain.model.KeyEvent.FAST_FORWARD) }
                )
            }
        } else {
            // Show disconnected state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.connection_lost),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Screen.DevicePairing.route) }
                    ) {
                        Text(stringResource(R.string.reconnect))
                    }
                }
            }
        }
    }
}
