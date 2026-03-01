package com.controlsphere.tvremote.presentation.screens.devicepairing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.util.Log
import com.controlsphere.tvremote.R
import com.controlsphere.tvremote.presentation.navigation.Screen
import com.controlsphere.tvremote.presentation.screens.devicepairing.components.DeviceItem
import com.controlsphere.tvremote.presentation.screens.devicepairing.components.ManualConnectionDialog
import com.controlsphere.tvremote.presentation.screens.devicepairing.components.ScanningIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePairingScreen(
    navController: NavController,
    viewModel: DevicePairingViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showManualDialog by remember { mutableStateOf(false) }

    // Navigate to remote screen when connected
    LaunchedEffect(uiState.isConnected) {
        if (uiState.isConnected && uiState.isAuthorized) {
            Log.d("DevicePairing", "Connected and authorized, navigating to Remote screen")
            navController.navigate(Screen.Remote.route) {
                popUpTo(Screen.DevicePairing.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_pairing_title)) },
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
            // Instructions
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
                        text = stringResource(R.string.device_pairing_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Connection status indicator
                    if (uiState.isConnected) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Connected to TV",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            if (!uiState.isAuthorized) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(Authorizing...)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startScanning() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isScanning
                ) {
                    Text(stringResource(R.string.scan_devices))
                }

                OutlinedButton(
                    onClick = { showManualDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.manual_connection))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scanning indicator
            if (uiState.isScanning) {
                ScanningIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Device list
            if (uiState.discoveredDevices.isNotEmpty()) {
                Text(
                    text = "Found Devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.discoveredDevices) { device ->
                        DeviceItem(
                            device = device,
                            onConnect = { viewModel.connectToDevice(device.ipAddress) },
                            isConnecting = uiState.connectingToDevice == device.ipAddress
                        )
                    }
                }
            } else if (!uiState.isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No devices found. Try scanning or connect manually.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Manual connection dialog
    if (showManualDialog) {
        ManualConnectionDialog(
            onDismiss = { showManualDialog = false },
            onConnect = { ipAddress, port ->
                viewModel.connectToDevice(ipAddress, port)
                showManualDialog = false
            },
            onTestMode = {
                // Navigate directly to voice screen for testing
                navController.navigate(Screen.Voice.route)
                showManualDialog = false
            }
        )
    }
}
