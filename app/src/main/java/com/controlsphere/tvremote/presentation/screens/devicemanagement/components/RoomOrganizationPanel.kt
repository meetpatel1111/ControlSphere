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
fun RoomOrganizationPanel(
    roomDevices: Map<String, List<DeviceProfile>>,
    onExecuteOnRoom: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRoom by remember { mutableStateOf("") }
    val rooms = roomDevices.keys.toList()
    
    if (selectedRoom.isBlank() && rooms.isNotEmpty()) {
        selectedRoom = rooms.first()
    }
    
    val currentRoomDevices = roomDevices[selectedRoom] ?: emptyList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Room, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Room Control")
            }
        },
        text = {
            Column(
                modifier = Modifier.height(500.dp)
            ) {
                if (rooms.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No rooms configured",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Assign devices to rooms to organize them",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Room selection tabs
                    ScrollableTabRow(
                        selectedTabIndex = rooms.indexOf(selectedRoom),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rooms.forEachIndexed { index, room ->
                            Tab(
                                selected = room == selectedRoom,
                                onClick = { selectedRoom = room },
                                text = { Text(room) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Room overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = selectedRoom,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                val onlineCount = currentRoomDevices.count { it.isOnline }
                                Text(
                                    text = "$onlineCount/${currentRoomDevices.size} devices online",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            // Quick room actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onExecuteOnRoom(selectedRoom) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.PowerSettingsNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Power All")
                                }
                                
                                OutlinedButton(
                                    onClick = { onExecuteOnRoom(selectedRoom) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Home All")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device list for selected room
                    Text(
                        text = "Devices in $selectedRoom",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(250.dp)
                    ) {
                        items(currentRoomDevices) { device ->
                            RoomDeviceItem(device = device)
                        }
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
private fun RoomDeviceItem(device: DeviceProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                        .size(12.dp)
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
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (device.nickname?.isNotBlank() == true) {
                        Text(
                            text = device.nickname,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = "${device.ipAddress}:${device.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = device.deviceType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Device type icon
            Icon(
                when (device.deviceType) {
                    com.controlsphere.tvremote.data.voice.DeviceType.ANDROID_TV,
                    com.controlsphere.tvremote.data.voice.DeviceType.GOOGLE_TV,
                    com.controlsphere.tvremote.data.voice.DeviceType.SONY_TV,
                    com.controlsphere.tvremote.data.voice.DeviceType.TCL_TV -> Icons.Default.Tv
                    com.controlsphere.tvremote.data.voice.DeviceType.CHROMECAST,
                    com.controlsphere.tvremote.data.voice.DeviceType.NVIDIA_SHIELD -> Icons.Default.Dvr
                    else -> Icons.Default.DevicesOther
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
