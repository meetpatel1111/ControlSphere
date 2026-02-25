package com.controlsphere.tvremote.presentation.screens.devicepairing.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.controlsphere.tvremote.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualConnectionDialog(
    onDismiss: () -> Unit,
    onConnect: (ipAddress: String, port: Int) -> Unit,
    onTestMode: () -> Unit = {}
) {
    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("5555") }
    var ipAddressError by remember { mutableStateOf<String?>(null) }

    fun validateAndConnect() {
        ipAddressError = null
        
        if (ipAddress.isBlank()) {
            ipAddressError = "IP address is required"
            return
        }
        
        val ipRegex = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        if (!ipRegex.matches(ipAddress)) {
            ipAddressError = "Invalid IP address format"
            return
        }
        
        val portNum = port.toIntOrNull()
        if (portNum == null || portNum !in 1..65535) {
            ipAddressError = "Invalid port number"
            return
        }
        
        onConnect(ipAddress, portNum)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.manual_connection),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // IP Address input
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { 
                        ipAddress = it
                        ipAddressError = null
                    },
                    label = { Text(stringResource(R.string.enter_ip_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = ipAddressError != null,
                    supportingText = ipAddressError?.let { 
                        { Text(it, color = MaterialTheme.colorScheme.error) } 
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Port input
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dialog buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = ::validateAndConnect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.connect))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Test Mode button
                Button(
                    onClick = onTestMode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Test Mode (No TV Required)")
                }
            }
        }
    }
}
