package com.controlsphere.tvremote.presentation.screens.devicemanagement.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.DeviceProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDeviceSwitcher(
    currentDevice: DeviceProfile?,
    recentDevices: List<DeviceProfile>,
    allDevices: List<DeviceProfile>,
    onSwitchToDevice: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredDevices = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allDevices
        } else {
            allDevices.filter { device ->
                device.name.contains(searchQuery, ignoreCase = true) ||
                device.nickname?.contains(searchQuery, ignoreCase = true) == true ||
                (device.room?.isNotBlank() == true && device.room.contains(searchQuery, ignoreCase = true))
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Device Switch")
            }
        },
        text = {
            Column(
                modifier = Modifier.height(400.dp)
            ) {
                // Current device indicator
                currentDevice?.let { current ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Device",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = current.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                current.room?.let { room ->
                                    if (room.isNotBlank()) {
                                        Text(
                                            text = room,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search devices...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recent devices section
                if (recentDevices.isNotEmpty() && searchQuery.isBlank()) {
                    Text(
                        text = "Recently Used",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(recentDevices.take(3)) { device ->
                            DeviceSwitchItem(
                                device = device,
                                isCurrentDevice = device.id == currentDevice?.id,
                                onSwitch = { onSwitchToDevice(device.id) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // All devices section
                Text(
                    text = "All Devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(filteredDevices) { device ->
                        DeviceSwitchItem(
                            device = device,
                            isCurrentDevice = device.id == currentDevice?.id,
                            onSwitch = { onSwitchToDevice(device.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DeviceSwitchItem(
    device: DeviceProfile,
    isCurrentDevice: Boolean,
    onSwitch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentDevice) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Device status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(end = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (device.isOnline) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    ) {}
                }
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isCurrentDevice) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isCurrentDevice) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                modifier = Modifier.size(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "CURRENT",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    device.room?.let { room ->
                        if (room.isNotBlank()) {
                            Text(
                                text = room,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Text(
                        text = "${device.ipAddress}:${device.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (!isCurrentDevice && device.isOnline) {
                IconButton(onClick = onSwitch) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Switch to device",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (!device.isOnline) {
                Icon(
                    Icons.Default.OfflineBolt,
                    contentDescription = "Device offline",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
