package com.controlsphere.tvremote.presentation.screens.voice.components

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
import androidx.compose.ui.window.Dialog
import com.controlsphere.tvremote.data.voice.*
import com.controlsphere.tvremote.presentation.screens.voice.VoiceShortcutData
import com.controlsphere.tvremote.presentation.screens.voice.ShortcutCommand

@Composable
fun SessionStatsDialog(
    stats: SessionStatistics,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session Statistics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Session Duration
                StatItem(
                    icon = Icons.Default.AccessTime,
                    label = "Session Duration",
                    value = formatDuration(stats.sessionDuration)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Command Count
                StatItem(
                    icon = Icons.Default.Mic,
                    label = "Commands Processed",
                    value = stats.commandCount.toString()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Average Confidence
                StatItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Average Confidence",
                    value = "${(stats.averageConfidence * 100).toInt()}%"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Most Used Intents
                if (stats.mostUsedIntents.isNotEmpty()) {
                    Text(
                        text = "Most Used Intents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    stats.mostUsedIntents.forEach { (intent, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = intent,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun VoiceProfileDialog(
    currentProfile: VoiceProfile?,
    onDismiss: () -> Unit,
    onSave: (VoiceProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile?.name ?: "") }
    var selectedLanguage by remember { mutableStateOf(currentProfile?.preferredLanguage ?: VoiceLanguage.ENGLISH) }
    var pitch by remember { mutableStateOf(currentProfile?.pitch ?: 1.0f) }
    var speed by remember { mutableStateOf(currentProfile?.speed ?: 1.0f) }
    var accent by remember { mutableStateOf(currentProfile?.accent ?: "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Profile Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Language Selection
                Text(
                    text = "Language",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(120.dp)
                ) {
                    items(VoiceLanguage.values()) { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = { selectedLanguage = language }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${language.flag} ${language.displayName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pitch Slider
                Text(
                    text = "Pitch: ${pitch.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Slider(
                    value = pitch,
                    onValueChange = { pitch = it },
                    valueRange = 0.5f..1.5f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Speed Slider
                Text(
                    text = "Speed: ${speed.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 0.5f..1.5f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Accent Field
                OutlinedTextField(
                    value = accent,
                    onValueChange = { accent = it },
                    label = { Text("Accent (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val profile = VoiceProfile(
                                id = currentProfile?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name,
                                userId = "current_user", // In real app, get from auth
                                voiceSamples = currentProfile?.voiceSamples ?: emptyList(),
                                preferredLanguage = selectedLanguage,
                                accent = accent.takeIf { it.isNotBlank() },
                                pitch = pitch,
                                speed = speed,
                                personalizedResponses = currentProfile?.personalizedResponses ?: true,
                                wakeWordSensitivity = 0.7f,
                                createdTime = currentProfile?.createdTime ?: System.currentTimeMillis(),
                                lastUsed = System.currentTimeMillis()
                            )
                            onSave(profile)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateShortcutDialog(
    onDismiss: () -> Unit,
    onSave: (VoiceShortcutData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var triggerPhrases by remember { mutableStateOf("") }
    var commands by remember { mutableStateOf(listOf<ShortcutCommand>()) }
    var showAddCommandDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Voice Shortcut",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Shortcut Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Shortcut Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Trigger Phrases
                OutlinedTextField(
                    value = triggerPhrases,
                    onValueChange = { triggerPhrases = it },
                    label = { Text("Trigger Phrases (comma-separated)") },
                    placeholder = { Text("e.g., movie night, let's watch, netflix time") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Commands Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commands",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(onClick = { showAddCommandDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Command")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (commands.isEmpty()) {
                    Text(
                        text = "No commands added. Tap + to add commands.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(commands) { command ->
                            CommandItem(
                                command = command,
                                onDelete = { 
                                    commands = commands - command
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val shortcutData = VoiceShortcutData(
                                name = name,
                                triggerPhrases = triggerPhrases.split(",").map { it.trim() },
                                commands = commands
                            )
                            onSave(shortcutData)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && triggerPhrases.isNotBlank() && commands.isNotEmpty()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
    
    // Add Command Dialog
    if (showAddCommandDialog) {
        AddCommandDialog(
            onDismiss = { showAddCommandDialog = false },
            onAdd = { command ->
                commands = commands + command
                showAddCommandDialog = false
            }
        )
    }
}

@Composable
private fun AddCommandDialog(
    onDismiss: () -> Unit,
    onAdd: (ShortcutCommand) -> Unit
) {
    var selectedCommandType by remember { mutableStateOf("launch_app") }
    var parameters by remember { mutableStateOf(mapOf("app" to "")) }
    
    val commandTypes = listOf(
        "launch_app" to "Launch App",
        "search" to "Search",
        "media_control" to "Media Control",
        "volume_control" to "Volume Control",
        "navigation" to "Navigation",
        "text_input" to "Text Input"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Add Command",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Command Type Selection
                Text(
                    text = "Command Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                commandTypes.forEach { (type, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCommandType == type,
                            onClick = { 
                                selectedCommandType = type
                                parameters = getDefaultParameters(type)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Parameters
                Text(
                    text = "Parameters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                when (selectedCommandType) {
                    "launch_app" -> {
                        OutlinedTextField(
                            value = parameters["app"] ?: "",
                            onValueChange = { parameters = parameters + ("app" to it) },
                            label = { Text("App Package Name") },
                            placeholder = { Text("e.g., com.netflix.mediaclient") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "search" -> {
                        OutlinedTextField(
                            value = parameters["query"] ?: "",
                            onValueChange = { parameters = parameters + ("query" to it) },
                            label = { Text("Search Query") },
                            placeholder = { Text("e.g., action movies") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "media_control" -> {
                        val actions = listOf("play", "pause", "next", "previous", "rewind", "fast_forward")
                        var selectedAction by remember { mutableStateOf(parameters["action"] ?: "play") }
                        
                        Text(
                            text = "Action",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        actions.forEach { action ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedAction == action,
                                    onClick = { 
                                        selectedAction = action
                                        parameters = parameters + ("action" to action)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = action.replace("_", " ").capitalize(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    "volume_control" -> {
                        var volumeLevel by remember { mutableStateOf(parameters["level"] ?: "50%") }
                        
                        Text(
                            text = "Volume Level: $volumeLevel",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Slider(
                            value = volumeLevel.removeSuffix("%").toFloatOrNull() ?: 50f,
                            onValueChange = { 
                                volumeLevel = "${it.toInt()}%"
                                parameters = parameters + ("level" to volumeLevel)
                            },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "navigation" -> {
                        val destinations = listOf("home", "settings", "apps", "search", "back")
                        var selectedDestination by remember { mutableStateOf(parameters["destination"] ?: "home") }
                        
                        Text(
                            text = "Destination",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        destinations.forEach { destination ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDestination == destination,
                                    onClick = { 
                                        selectedDestination = destination
                                        parameters = parameters + ("destination" to destination)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = destination.replace("_", " ").capitalize(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    "text_input" -> {
                        OutlinedTextField(
                            value = parameters["text"] ?: "",
                            onValueChange = { parameters = parameters + ("text" to it) },
                            label = { Text("Text to Input") },
                            placeholder = { Text("e.g., Hello World") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val command = ShortcutCommand(
                                type = selectedCommandType,
                                parameters = parameters
                            )
                            onAdd(command)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = areParametersValid(selectedCommandType, parameters)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CommandItem(
    command: ShortcutCommand,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
                    text = getCommandDisplayName(command.type),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = getCommandDescription(command),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

private fun getDefaultParameters(type: String): Map<String, String> {
    return when (type) {
        "launch_app" -> mapOf("app" to "")
        "search" -> mapOf("query" to "")
        "media_control" -> mapOf("action" to "play")
        "volume_control" -> mapOf("level" to "50%")
        "navigation" -> mapOf("destination" to "home")
        "text_input" -> mapOf("text" to "")
        else -> emptyMap()
    }
}

private fun areParametersValid(type: String, parameters: Map<String, String>): Boolean {
    return when (type) {
        "launch_app" -> parameters["app"]?.isNotBlank() == true
        "search" -> parameters["query"]?.isNotBlank() == true
        "media_control" -> parameters["action"]?.isNotBlank() == true
        "volume_control" -> parameters["level"]?.isNotBlank() == true
        "navigation" -> parameters["destination"]?.isNotBlank() == true
        "text_input" -> parameters["text"]?.isNotBlank() == true
        else -> false
    }
}

private fun getCommandDisplayName(type: String): String {
    return when (type) {
        "launch_app" -> "Launch App"
        "search" -> "Search"
        "media_control" -> "Media Control"
        "volume_control" -> "Volume Control"
        "navigation" -> "Navigation"
        "text_input" -> "Text Input"
        else -> type.replace("_", " ").capitalize()
    }
}

private fun getCommandDescription(command: ShortcutCommand): String {
    return when (command.type) {
        "launch_app" -> "App: ${command.parameters["app"]}"
        "search" -> "Query: ${command.parameters["query"]}"
        "media_control" -> "Action: ${command.parameters["action"]}"
        "volume_control" -> "Level: ${command.parameters["level"]}"
        "navigation" -> "Destination: ${command.parameters["destination"]}"
        "text_input" -> "Text: ${command.parameters["text"]}"
        else -> command.parameters.entries.joinToString(", ") { "${it.key}: ${it.value}" }
    }
}
