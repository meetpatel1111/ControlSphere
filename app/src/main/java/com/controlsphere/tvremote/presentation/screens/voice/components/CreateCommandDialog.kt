package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.*
import com.controlsphere.tvremote.data.voice.ActionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommandDialog(
    command: CustomVoiceCommand?,
    availableDevices: List<DeviceProfile>,
    onDismiss: () -> Unit,
    onSave: (CustomVoiceCommand) -> Unit
) {
    var name by remember { mutableStateOf(command?.name ?: "") }
    var description by remember { mutableStateOf(command?.description ?: "") }
    var triggerPhrases by remember { mutableStateOf(command?.triggerPhrases?.joinToString(", ") ?: "") }
    var selectedCategory by remember { mutableStateOf(command?.category ?: CommandCategory.NAVIGATION) }
    var selectedDevice by remember { mutableStateOf(command?.deviceId) }
    var isEnabled by remember { mutableStateOf(command?.isEnabled ?: true) }
    
    // Action sequence management
    var actionSequence by remember { mutableStateOf(command?.actionSequence?.toMutableList() ?: mutableListOf()) }
    var showAddActionDialog by remember { mutableStateOf(false) }
    var editingActionIndex by remember { mutableStateOf<Int?>(null) }

    val isFormValid = name.isNotBlank() && description.isNotBlank() && 
                     triggerPhrases.isNotBlank() && actionSequence.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (command == null) "Create Command" else "Edit Command") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Basic Information
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Command Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    item {
                        OutlinedTextField(
                            value = triggerPhrases,
                            onValueChange = { triggerPhrases = it },
                            label = { Text("Trigger Phrases (comma separated) *") },
                            placeholder = { Text("e.g., turn on tv, power on, start tv") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Category Selection
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = getCategoryDisplayName(selectedCategory),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                CommandCategory.values().forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(getCategoryDisplayName(category)) },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Device Selection
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedDevice?.let { deviceId ->
                                    availableDevices.find { it.id == deviceId }?.name
                                } ?: "Global Command",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Device") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Global Command") },
                                    onClick = {
                                        selectedDevice = null
                                        expanded = false
                                    }
                                )
                                
                                availableDevices.forEach { device ->
                                    DropdownMenuItem(
                                        text = { Text(device.name) },
                                        onClick = {
                                            selectedDevice = device.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Action Sequence
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Action Sequence",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            TextButton(onClick = { showAddActionDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Action")
                            }
                        }
                    }
                    
                    actionSequence.forEachIndexed { index, action ->
                        item {
                            ActionItem(
                                action = action,
                                index = index,
                                onEdit = { editIndex -> 
                                    editingActionIndex = editIndex
                                    showAddActionDialog = true
                                },
                                onDelete = { deleteIndex ->
                                    actionSequence.removeAt(deleteIndex)
                                }
                            )
                        }
                    }
                    
                    // Enable toggle
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isEnabled,
                                onCheckedChange = { isEnabled = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable this command")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFormValid) {
                        val newCommand = CustomVoiceCommand(
                            id = command?.id ?: "",
                            name = name.trim(),
                            triggerPhrases = triggerPhrases.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() },
                            actionSequence = actionSequence,
                            description = description.trim(),
                            isEnabled = isEnabled,
                            deviceId = selectedDevice,
                            category = selectedCategory,
                            usageCount = command?.usageCount ?: 0,
                            createdTime = command?.createdTime ?: System.currentTimeMillis()
                        )
                        onSave(newCommand)
                    }
                },
                enabled = isFormValid
            ) {
                Text(if (command == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Add/Edit Action Dialog
    if (showAddActionDialog) {
        AddActionDialog(
            action = editingActionIndex?.let { actionSequence.getOrNull(it) },
            onDismiss = { 
                showAddActionDialog = false
                editingActionIndex = null
            },
            onSave = { action ->
                val currentIndex = editingActionIndex
                if (currentIndex != null) {
                    actionSequence[currentIndex] = action
                } else {
                    actionSequence.add(action)
                }
                showAddActionDialog = false
                editingActionIndex = null
            }
        )
    }
}

@Composable
private fun ActionItem(
    action: CommandAction,
    index: Int,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getActionTypeDisplayName(action.type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (action.parameters.isNotEmpty()) {
                    action.parameters.forEach { (key, value) ->
                        Text(
                            text = "$key: $value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (action.delay > 0) {
                    Text(
                        text = "Delay: ${action.delay}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Row {
                IconButton(onClick = { onEdit(index) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddActionDialog(
    action: CommandAction?,
    onDismiss: () -> Unit,
    onSave: (CommandAction) -> Unit
) {
    var selectedActionType by remember { mutableStateOf(action?.type ?: ActionType.KEY_EVENT) }
    var parameters by remember { mutableStateOf(action?.parameters?.toMutableMap() ?: mutableMapOf()) }
    var delay by remember { mutableStateOf(action?.delay?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (action == null) "Add Action" else "Edit Action") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action Type Selection
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = getActionTypeDisplayName(selectedActionType),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Action Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ActionType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(getActionTypeDisplayName(type)) },
                                onClick = {
                                    selectedActionType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Parameters based on action type
                when (selectedActionType) {
                    ActionType.KEY_EVENT -> {
                        var keyName by remember { mutableStateOf(parameters["key"] ?: "") }
                        OutlinedTextField(
                            value = keyName,
                            onValueChange = { keyName = it },
                            label = { Text("Key Name") },
                            placeholder = { Text("e.g., HOME, BACK, VOLUME_UP") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(keyName) {
                            if (keyName.isNotBlank()) {
                                parameters["key"] = keyName
                            } else {
                                parameters.remove("key")
                            }
                        }
                    }
                    
                    ActionType.TEXT_INPUT -> {
                        var text by remember { mutableStateOf(parameters["text"] ?: "") }
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Text to Input") },
                            placeholder = { Text("e.g., Hello World") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(text) {
                            if (text.isNotBlank()) {
                                parameters["text"] = text
                            } else {
                                parameters.remove("text")
                            }
                        }
                    }
                    
                    ActionType.APP_LAUNCH -> {
                        var packageName by remember { mutableStateOf(parameters["package"] ?: "") }
                        OutlinedTextField(
                            value = packageName,
                            onValueChange = { packageName = it },
                            label = { Text("Package Name") },
                            placeholder = { Text("e.g., com.netflix.mediaclient") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(packageName) {
                            if (packageName.isNotBlank()) {
                                parameters["package"] = packageName
                            } else {
                                parameters.remove("package")
                            }
                        }
                    }
                    
                    ActionType.DELAY -> {
                        OutlinedTextField(
                            value = delay,
                            onValueChange = { delay = it },
                            label = { Text("Delay (ms)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    ActionType.VOICE_COMMAND -> {
                        var command by remember { mutableStateOf(parameters["command"] ?: "") }
                        OutlinedTextField(
                            value = command,
                            onValueChange = { command = it },
                            label = { Text("Voice Command") },
                            placeholder = { Text("e.g., open netflix") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(command) {
                            if (command.isNotBlank()) {
                                parameters["command"] = command
                            } else {
                                parameters.remove("command")
                            }
                        }
                    }
                    
                    ActionType.VISUAL_SEARCH -> {
                        var query by remember { mutableStateOf(parameters["query"] ?: "") }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Search Query") },
                            placeholder = { Text("e.g., movies") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(query) {
                            if (query.isNotBlank()) {
                                parameters["query"] = query
                            } else {
                                parameters.remove("query")
                            }
                        }
                    }
                    
                    ActionType.COMPUTER_USE -> {
                        var task by remember { mutableStateOf(parameters["task"] ?: "") }
                        OutlinedTextField(
                            value = task,
                            onValueChange = { task = it },
                            label = { Text("Computer Use Task") },
                            placeholder = { Text("e.g., navigate to settings") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(task) {
                            if (task.isNotBlank()) {
                                parameters["task"] = task
                            } else {
                                parameters.remove("task")
                            }
                        }
                    }
                    
                    ActionType.CONDITIONAL -> {
                        var condition by remember { mutableStateOf(parameters["condition"] ?: "") }
                        OutlinedTextField(
                            value = condition,
                            onValueChange = { condition = it },
                            label = { Text("Condition") },
                            placeholder = { Text("e.g., screen_on") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        LaunchedEffect(condition) {
                            if (condition.isNotBlank()) {
                                parameters["condition"] = condition
                            } else {
                                parameters.remove("condition")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalDelay = delay.toLongOrNull() ?: 0L
                    val newAction = CommandAction(
                        type = selectedActionType,
                        parameters = parameters,
                        delay = finalDelay
                    )
                    onSave(newAction)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCategoryDisplayName(category: CommandCategory): String {
    return when (category) {
        CommandCategory.NAVIGATION -> "Navigation"
        CommandCategory.MEDIA -> "Media"
        CommandCategory.SEARCH -> "Search"
        CommandCategory.APP_CONTROL -> "App Control"
        CommandCategory.AUTOMATION -> "Automation"
        CommandCategory.SYSTEM -> "System"
        CommandCategory.CUSTOM -> "Custom"
    }
}

private fun getActionTypeDisplayName(type: ActionType): String {
    return when (type) {
        ActionType.KEY_EVENT -> "Key Event"
        ActionType.TEXT_INPUT -> "Text Input"
        ActionType.APP_LAUNCH -> "Launch App"
        ActionType.VOICE_COMMAND -> "Voice Command"
        ActionType.VISUAL_SEARCH -> "Visual Search"
        ActionType.COMPUTER_USE -> "Computer Use"
        ActionType.DELAY -> "Delay"
        ActionType.CONDITIONAL -> "Conditional"
    }
}
