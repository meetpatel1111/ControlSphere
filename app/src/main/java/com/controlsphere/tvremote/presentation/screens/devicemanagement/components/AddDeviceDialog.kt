package com.controlsphere.tvremote.presentation.screens.devicemanagement.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.DeviceProfile
import com.controlsphere.tvremote.data.voice.DeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAddDevice: (DeviceProfile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("5556") }
    var deviceType by remember { mutableStateOf(DeviceType.ANDROID_TV) }
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var osVersion by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    
    val isFormValid = name.isNotBlank() && ipAddress.isNotBlank() && 
                     port.isNotBlank() && manufacturer.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Device") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Basic Information Section
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Connection Section
                Text(
                    text = "Connection Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Address *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Device Type Selection
                Text(
                    text = "Device Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = getDeviceTypeDisplayName(deviceType),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Device Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DeviceType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(getDeviceTypeDisplayName(type)) },
                                onClick = {
                                    deviceType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Device Details Section
                Text(
                    text = "Device Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("Manufacturer *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = osVersion,
                    onValueChange = { osVersion = it },
                    label = { Text("OS Version") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Location Section
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFormValid) {
                        val deviceProfile = DeviceProfile(
                            id = java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            ipAddress = ipAddress.trim(),
                            port = port.toIntOrNull() ?: 5556,
                            deviceType = deviceType,
                            model = model.trim(),
                            manufacturer = manufacturer.trim(),
                            osVersion = osVersion.trim(),
                            room = room.trim().takeIf { it.isNotBlank() },
                            nickname = nickname.trim().takeIf { it.isNotBlank() }
                        )
                        onAddDevice(deviceProfile)
                    }
                },
                enabled = isFormValid
            ) {
                Text("Add Device")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getDeviceTypeDisplayName(deviceType: DeviceType): String {
    return when (deviceType) {
        DeviceType.ANDROID_TV -> "Android TV"
        DeviceType.GOOGLE_TV -> "Google TV"
        DeviceType.CHROMECAST -> "Chromecast with Google TV"
        DeviceType.NVIDIA_SHIELD -> "NVIDIA Shield"
        DeviceType.SONY_TV -> "Sony Android TV"
        DeviceType.TCL_TV -> "TCL Android TV"
        DeviceType.UNKNOWN -> "Unknown Device"
    }
}
