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
import com.controlsphere.tvremote.data.voice.DeviceGroup
import com.controlsphere.tvremote.data.voice.DeviceProfile
import com.controlsphere.tvremote.domain.model.KeyEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupControlPanel(
    groups: List<DeviceGroup>,
    devices: List<DeviceProfile>,
    activeGroup: DeviceGroup?,
    onGroupSelected: (String) -> Unit,
    onSendKeyEvent: (KeyEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGroupId by remember { mutableStateOf(activeGroup?.id ?: "") }
    val selectedGroup = groups.find { it.id == selectedGroupId }
    val groupDevices = selectedGroup?.let { group ->
        devices.filter { it.id in group.deviceIds }
    } ?: emptyList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GroupWork, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Group Control")
            }
        },
        text = {
            Column(
                modifier = Modifier.height(500.dp)
            ) {
                // Group selection
                if (groups.isNotEmpty()) {
                    Text(
                        text = "Select Group",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(groups) { group ->
                            GroupSelectionItem(
                                group = group,
                                devices = devices.filter { it.id in group.deviceIds },
                                isSelected = group.id == selectedGroupId,
                                onSelect = { selectedGroupId = group.id }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Selected group info
                selectedGroup?.let { group ->
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
                                text = group.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${groupDevices.size} devices • ${group.room}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            // Device status
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                val onlineCount = groupDevices.count { it.isOnline }
                                Text(
                                    text = "$onlineCount/${groupDevices.size} online",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (onlineCount == groupDevices.size) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Control buttons
                    if (selectedGroup != null) {
                        Text(
                            text = "Group Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // Media controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GroupControlButton(
                                icon = Icons.Default.PlayArrow,
                                label = "Play",
                                onClick = { onSendKeyEvent(KeyEvent.PLAY_PAUSE) }
                            )
                            GroupControlButton(
                                icon = Icons.Default.Stop,
                                label = "Stop",
                                onClick = { onSendKeyEvent(KeyEvent.PLAY_PAUSE) }
                            )
                            GroupControlButton(
                                icon = Icons.Default.SkipPrevious,
                                label = "Previous",
                                onClick = { onSendKeyEvent(KeyEvent.MEDIA_PREVIOUS) }
                            )
                            GroupControlButton(
                                icon = Icons.Default.SkipNext,
                                label = "Next",
                                onClick = { onSendKeyEvent(KeyEvent.MEDIA_NEXT) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Navigation controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GroupControlButton(
                                icon = Icons.Default.Home,
                                label = "Home",
                                onClick = { onSendKeyEvent(KeyEvent.HOME) }
                            )
                            GroupControlButton(
                                icon = Icons.Default.ArrowBack,
                                label = "Back",
                                onClick = { onSendKeyEvent(KeyEvent.BACK) }
                            )
                            GroupControlButton(
                                icon = Icons.Default.PowerSettingsNew,
                                label = "Power",
                                onClick = { onSendKeyEvent(KeyEvent.POWER) },
                                isCritical = true
                            )
                            GroupControlButton(
                                icon = Icons.Default.VolumeUp,
                                label = "Vol+",
                                onClick = { onSendKeyEvent(KeyEvent.VOLUME_UP) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Device list in group
                        Text(
                            text = "Devices in Group",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.height(120.dp)
                        ) {
                            items(groupDevices) { device ->
                                GroupDeviceItem(device = device)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.GroupAdd,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No groups available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Create a group to control multiple devices",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
                if (selectedGroup != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            onGroupSelected(selectedGroupId)
                            onDismiss()
                        }
                    ) {
                        Text("Activate Group")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupSelectionItem(
    group: DeviceGroup,
    devices: List<DeviceProfile>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect
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
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "${devices.size} devices • ${group.room}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isCritical: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isCritical) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun GroupDeviceItem(device: DeviceProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = device.ipAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
}
