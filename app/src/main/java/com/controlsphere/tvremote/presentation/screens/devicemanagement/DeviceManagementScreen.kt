package com.controlsphere.tvremote.presentation.screens.devicemanagement

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.controlsphere.tvremote.data.voice.DeviceProfile
import com.controlsphere.tvremote.presentation.screens.devicemanagement.components.DeviceCard
import com.controlsphere.tvremote.presentation.screens.devicemanagement.components.AddDeviceDialog
import com.controlsphere.tvremote.presentation.screens.devicemanagement.components.DeviceGroupCard
import com.controlsphere.tvremote.presentation.screens.devicemanagement.components.QuickDeviceSwitcher
import com.controlsphere.tvremote.presentation.screens.devicemanagement.components.GroupControlPanel
import com.controlsphere.tvremote.presentation.screens.devicemanagement.components.RoomOrganizationPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    navController: NavController,
    viewModel: DeviceManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showQuickSwitcher by remember { mutableStateOf(false) }
    var showGroupControl by remember { mutableStateOf(false) }
    var showRoomControl by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAllDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showQuickSwitcher = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Quick Switch")
                    }
                    IconButton(onClick = { showGroupControl = true }) {
                        Icon(Icons.Default.GroupWork, contentDescription = "Group Control")
                    }
                    IconButton(onClick = { showRoomControl = true }) {
                        Icon(Icons.Default.Room, contentDescription = "Room Control")
                    }
                    IconButton(onClick = { showAddDeviceDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Device")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateGroupDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = "Create Group")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Device Section
            item {
                uiState.currentDevice?.let { currentDevice ->
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
                                text = "Current Device",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentDevice.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "${currentDevice.ipAddress}:${currentDevice.port}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentDevice.isOnline) "Connected" else "Offline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (currentDevice.isOnline) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = currentDevice.deviceType.name.replace("_", " "),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Device Groups Section
            item {
                if (uiState.deviceGroups.isNotEmpty()) {
                    Text(
                        text = "Device Groups",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            items(uiState.deviceGroups) { group ->
                DeviceGroupCard(
                    group = group,
                    devices = uiState.deviceProfiles.filter { it.id in group.deviceIds },
                    onDeleteGroup = { viewModel.deleteDeviceGroup(group.id) },
                    onEditGroup = { /* Navigate to edit group */ }
                )
            }

            // All Devices Section
            item {
                Text(
                    text = "All Devices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Filter tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedFilterIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.filterOptions.forEachIndexed { index, filter ->
                        Tab(
                            selected = index == uiState.selectedFilterIndex,
                            onClick = { viewModel.setFilter(index) },
                            text = { Text(filter) }
                        )
                    }
                }
            }

            val filteredDevices = when (uiState.selectedFilterIndex) {
                0 -> uiState.deviceProfiles // All
                1 -> uiState.deviceProfiles.filter { it.isOnline } // Online
                2 -> uiState.deviceProfiles.filter { !it.isOnline } // Offline
                3 -> uiState.deviceProfiles.filter { it.room != null } // By Room
                else -> uiState.deviceProfiles
            }

            if (filteredDevices.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DevicesOther,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No devices found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add a device to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredDevices) { device ->
                    DeviceCard(
                        device = device,
                        isCurrentDevice = device.id == uiState.currentDevice?.id,
                        onSwitchTo = { viewModel.switchToDevice(device.id) },
                        onEdit = { /* Navigate to edit device */ },
                        onDelete = { viewModel.deleteDevice(device.id) },
                        onRefresh = { viewModel.refreshDeviceStatus(device.id) }
                    )
                }
            }
        }
    }

    // Add Device Dialog
    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onAddDevice = { deviceProfile ->
                viewModel.addDevice(deviceProfile)
                showAddDeviceDialog = false
            }
        )
    }

    // Create Group Dialog
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            availableDevices = uiState.deviceProfiles,
            onDismiss = { showCreateGroupDialog = false },
            onCreateGroup = { name, deviceIds, room ->
                viewModel.createDeviceGroup(name, deviceIds, room)
                showCreateGroupDialog = false
            }
        )
    }

    // Quick Device Switcher Dialog
    if (showQuickSwitcher) {
        QuickDeviceSwitcher(
            currentDevice = uiState.currentDevice,
            recentDevices = uiState.recentDevices,
            allDevices = uiState.deviceProfiles,
            onSwitchToDevice = { deviceId ->
                viewModel.switchToDevice(deviceId)
                showQuickSwitcher = false
            },
            onDismiss = { showQuickSwitcher = false }
        )
    }

    // Group Control Panel Dialog
    if (showGroupControl) {
        GroupControlPanel(
            groups = uiState.deviceGroups,
            devices = uiState.deviceProfiles,
            activeGroup = uiState.activeGroup,
            onGroupSelected = { groupId ->
                // Handle group activation
                viewModel.clearOperationResults()
            },
            onSendKeyEvent = { keyEvent ->
                viewModel.sendKeyEventToGroup(
                    uiState.activeGroup?.id ?: return@GroupControlPanel,
                    keyEvent
                )
            },
            onDismiss = { showGroupControl = false }
        )
    }

    // Room Organization Panel Dialog
    if (showRoomControl) {
        RoomOrganizationPanel(
            roomDevices = uiState.roomDevices,
            onExecuteOnRoom = { room ->
                viewModel.executeOnRoom(room) { device ->
                    // Execute room-specific actions
                }
                showRoomControl = false
            },
            onDismiss = { showRoomControl = false }
        )
    }
}

@Composable
private fun CreateGroupDialog(
    availableDevices: List<DeviceProfile>,
    onDismiss: () -> Unit,
    onCreateGroup: (String, List<String>, String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf("") }
    val selectedDevices = remember { mutableStateOf<List<String>>(emptyList()) }
    val rooms = remember { availableDevices.mapNotNull { it.room }.distinct() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Device Group") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (rooms.isNotEmpty()) {
                    OutlinedTextField(
                        value = selectedRoom,
                        onValueChange = { selectedRoom = it },
                        label = { Text("Room") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = "Select Devices",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableDevices) { device ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = device.id in selectedDevices.value,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedDevices.value = selectedDevices.value + device.id
                                    } else {
                                        selectedDevices.value = selectedDevices.value - device.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(device.name)
                                Text(
                                    text = device.ipAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (groupName.isNotBlank() && selectedDevices.value.isNotEmpty()) {
                        onCreateGroup(groupName, selectedDevices.value, selectedRoom)
                    }
                },
                enabled = groupName.isNotBlank() && selectedDevices.value.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
