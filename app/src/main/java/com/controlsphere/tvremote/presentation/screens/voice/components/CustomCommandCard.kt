package com.controlsphere.tvremote.presentation.screens.voice.components

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
import com.controlsphere.tvremote.data.voice.CustomVoiceCommand
import com.controlsphere.tvremote.data.voice.CommandCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCommandCard(
    command: CustomVoiceCommand,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onExecute: () -> Unit,
    compact: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (command.isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp)
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
                            text = command.name,
                            style = if (compact) 
                                MaterialTheme.typography.titleMedium 
                            else 
                                MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (!command.isEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "DISABLED",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (!compact) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = command.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Trigger phrases
                        Text(
                            text = "Triggers:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        command.triggerPhrases.take(3).forEach { phrase ->
                            Text(
                                text = "• $phrase",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (command.triggerPhrases.size > 3) {
                            Text(
                                text = "... and ${command.triggerPhrases.size - 3} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Action sequence info
                        Text(
                            text = "${command.actionSequence.size} actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (command.usageCount > 0) {
                            Text(
                                text = "Used ${command.usageCount} times",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        if (command.isEnabled) {
                            DropdownMenuItem(
                                text = { Text("Execute") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                onClick = {
                                    onExecute()
                                    showMenu = false
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text(if (command.isEnabled) "Disable" else "Enable") },
                            leadingIcon = { 
                                Icon(
                                    if (command.isEnabled) Icons.Default.Block 
                                    else Icons.Default.Check, 
                                    contentDescription = null
                                ) 
                            },
                            onClick = {
                                onToggle()
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
                        
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                onDuplicate()
                                showMenu = false
                            }
                        )
                        
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
            
            if (!compact) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category and device info
                    Column {
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                            color = getCategoryColor(command.category).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = getCategoryDisplayName(command.category),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = getCategoryColor(command.category)
                            )
                        }
                        
                        command.deviceId?.let { deviceId ->
                            Text(
                                text = "Device specific",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } ?: Text(
                            text = "Global command",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Quick actions
                    Row {
                        if (command.isEnabled) {
                            OutlinedButton(
                                onClick = onExecute,
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Text("Execute")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onToggle,
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Text("Enable")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommandTemplateCard(
    template: com.controlsphere.tvremote.data.voice.CommandTemplate,
    onUseTemplate: () -> Unit
) {
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
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                            color = getCategoryColor(template.category).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = getCategoryDisplayName(template.category),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = getCategoryColor(template.category)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "${template.actionSequence.size} actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    if (template.triggerPhrases.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Example: \"${template.triggerPhrases.first()}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                IconButton(onClick = onUseTemplate) {
                    Icon(Icons.Default.Add, contentDescription = "Use Template")
                }
            }
        }
    }
}

private fun getCategoryColor(category: CommandCategory): Color {
    return when (category) {
        CommandCategory.NAVIGATION -> Color(0xFF2196F3) // Blue
        CommandCategory.MEDIA -> Color(0xFF4CAF50) // Green
        CommandCategory.SEARCH -> Color(0xFFFF9800) // Orange
        CommandCategory.APP_CONTROL -> Color(0xFF9C27B0) // Purple
        CommandCategory.AUTOMATION -> Color(0xFFF44336) // Red
        CommandCategory.SYSTEM -> Color(0xFF607D8B) // Blue Grey
        CommandCategory.CUSTOM -> Color(0xFF795548) // Brown
    }
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
