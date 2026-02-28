package com.controlsphere.tvremote.presentation.screens.devicemanagement.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.DeviceProfile
import com.controlsphere.tvremote.data.voice.DeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: DeviceProfile,
    isCurrentDevice: Boolean,
    onSwitchTo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRefresh: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentDevice) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentDevice) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCurrentDevice) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "CURRENT",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = device.nickname ?: device.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${device.ipAddress}:${device.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    device.room?.let { room ->
                        Text(
                            text = "Room: $room",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh Status") },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {
                                onRefresh()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                onEdit()
                                showMenu = false
                            }
                        )
                        if (!isCurrentDevice) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Delete, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device info
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(end = 4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = if (device.isOnline) 
                                    Color.Green 
                                else 
                                    Color.Red,
                                shape = MaterialTheme.shapes.small
                            ) {}
                        }
                        Text(
                            text = if (device.isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (device.isOnline) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Text(
                        text = getDeviceTypeDisplayName(device.deviceType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${device.manufacturer} ${device.model}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action buttons
                if (!isCurrentDevice && device.isOnline) {
                    Button(
                        onClick = onSwitchTo,
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Text("Switch")
                    }
                } else if (isCurrentDevice) {
                    OutlinedButton(
                        onClick = { /* Show device details */ },
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Text("Connected")
                    }
                }
            }
            
            // Additional info
            if (device.favoriteApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Favorite Apps: ${device.favoriteApps.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (device.customCommands.isNotEmpty()) {
                Text(
                    text = "Custom Commands: ${device.customCommands.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DeviceGroupCard(
    group: com.controlsphere.tvremote.data.voice.DeviceGroup,
    devices: List<DeviceProfile>,
    onDeleteGroup: () -> Unit,
    onEditGroup: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${devices.size} devices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    if (group.room.isNotBlank()) {
                        Text(
                            text = "Room: ${group.room}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Group") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                onEditGroup()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Group") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                ) 
                            },
                            onClick = {
                                onDeleteGroup()
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            if (devices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    devices.take(3).forEach { device ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .padding(end = 6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (device.isOnline) 
                                        Color.Green 
                                    else 
                                        Color.Red,
                                    shape = MaterialTheme.shapes.small
                                ) {}
                            }
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    if (devices.size > 3) {
                        Text(
                            text = "... and ${devices.size - 3} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun getDeviceTypeDisplayName(deviceType: DeviceType): String {
    return when (deviceType) {
        DeviceType.ANDROID_TV -> "Android TV"
        DeviceType.GOOGLE_TV -> "Google TV"
        DeviceType.CHROMECAST -> "Chromecast"
        DeviceType.NVIDIA_SHIELD -> "NVIDIA Shield"
        DeviceType.SONY_TV -> "Sony TV"
        DeviceType.TCL_TV -> "TCL TV"
        DeviceType.UNKNOWN -> "Unknown Device"
    }
}
