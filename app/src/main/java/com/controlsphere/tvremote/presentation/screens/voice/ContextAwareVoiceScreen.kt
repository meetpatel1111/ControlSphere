package com.controlsphere.tvremote.presentation.screens.voice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.controlsphere.tvremote.data.voice.*
import com.controlsphere.tvremote.presentation.screens.voice.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextAwareVoiceScreen(
    navController: NavController,
    viewModel: ContextAwareVoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversationContext by viewModel.conversationContext.collectAsState()
    val sessionStats by viewModel.sessionStatistics.collectAsState()
    
    var showShortcutDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSessionStats by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Voice Control") },
                actions = {
                    IconButton(onClick = { showSessionStats = true }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Session Stats")
                    }
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Voice Profile")
                    }
                    IconButton(onClick = { showShortcutDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Shortcut")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Session Stats Dialog
        if (showSessionStats) {
            SessionStatsDialog(
                stats = sessionStats,
                onDismiss = { showSessionStats = false }
            )
        }
        
        // Voice Profile Dialog
        if (showProfileDialog) {
            VoiceProfileDialog(
                currentProfile = uiState.currentProfile,
                onDismiss = { showProfileDialog = false },
                onSave = { profile ->
                    viewModel.updateVoiceProfile(profile)
                    showProfileDialog = false
                }
            )
        }
        
        // Create Shortcut Dialog
        if (showShortcutDialog) {
            CreateShortcutDialog(
                onDismiss = { showShortcutDialog = false },
                onSave = { shortcut ->
                    viewModel.createVoiceShortcut(shortcut)
                    showShortcutDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Context Awareness Status
            item {
                ContextAwareStatusCard(
                    isActive = uiState.isContextAwareEnabled,
                    sessionDuration = conversationContext.sessionDuration,
                    commandCount = conversationContext.commandHistory.size,
                    onToggleContext = { viewModel.toggleContextAwareness() }
                )
            }

            // Conversation Context
            if (conversationContext.commandHistory.isNotEmpty()) {
                item {
                    ConversationContextCard(
                        context = conversationContext,
                        onClearContext = { viewModel.clearConversationContext() }
                    )
                }
            }

            // Multi-Intent Examples
            item {
                MultiIntentExamplesCard(
                    onExampleClick = { example ->
                        viewModel.processExampleCommand(example)
                    }
                )
            }

            // Voice Shortcuts
            item {
                VoiceShortcutsCard(
                    shortcuts = uiState.voiceShortcuts,
                    onShortcutClick = { shortcut ->
                        viewModel.executeShortcut(shortcut)
                    },
                    onDeleteShortcut = { shortcutId ->
                        viewModel.deleteShortcut(shortcutId)
                    }
                )
            }

            // Enhanced Voice Control
            item {
                EnhancedVoiceControlCard(
                    recordingState = uiState.recordingState,
                    lastResult = uiState.lastCommandResult,
                    onStartRecording = { viewModel.startContextAwareRecording() },
                    onStopRecording = { viewModel.stopContextAwareRecording() }
                )
            }

            // Contextual Suggestions
            if (uiState.contextualSuggestions.isNotEmpty()) {
                item {
                    ContextualSuggestionsCard(
                        suggestions = uiState.contextualSuggestions,
                        onSuggestionClick = { suggestion ->
                            viewModel.processSuggestion(suggestion)
                        }
                    )
                }
            }

            // Voice Profile Status
            uiState.currentProfile?.let { profile ->
                item {
                    VoiceProfileStatusCard(
                        profile = profile,
                        onEditProfile = { showProfileDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextAwareStatusCard(
    isActive: Boolean,
    sessionDuration: Long,
    commandCount: Int,
    onToggleContext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Context-Aware Voice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggleContext() }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatDuration(sessionDuration),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Session Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = commandCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Commands",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationContextCard(
    context: ConversationContext,
    onClearContext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conversation Context",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClearContext) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Context")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (context.lastCommand.isNotEmpty()) {
                Text(
                    text = "Last: ${context.lastCommand}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (context.commandHistory.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent commands:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                
                context.commandHistory.takeLast(3).reversed().forEach { command ->
                    Text(
                        text = "• $command",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiIntentExamplesCard(
    onExampleClick: (String) -> Unit
) {
    val examples = listOf(
        "Open Netflix and play the latest episode",
        "Search for action movies and play the first one",
        "Set volume to 50% and launch YouTube",
        "Go to settings and find display options"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Multi-Intent Commands",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            examples.forEach { example ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onExampleClick(example) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Try this",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceShortcutsCard(
    shortcuts: List<VoiceShortcut>,
    onShortcutClick: (VoiceShortcut) -> Unit,
    onDeleteShortcut: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Voice Shortcuts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (shortcuts.isEmpty()) {
                Text(
                    text = "No shortcuts created. Tap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                shortcuts.forEach { shortcut ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shortcut.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = shortcut.triggerPhrases.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row {
                            IconButton(
                                onClick = { onShortcutClick(shortcut) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Execute",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteShortcut(shortcut.id) },
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
            }
        }
    }
}

@Composable
private fun EnhancedVoiceControlCard(
    recordingState: RecordingState,
    lastResult: ContextAwareCommandResult?,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Voice Control Button
            EnhancedVoiceControlButton(
                recordingState = recordingState,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Text
            when (recordingState) {
                RecordingState.IDLE -> {
                    Text(
                        text = "Tap for context-aware voice control",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                RecordingState.RECORDING -> {
                    Text(
                        text = "Listening with context awareness...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                RecordingState.PROCESSING -> {
                    Text(
                        text = "Processing with AI enhancement...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                RecordingState.ERROR -> {
                    Text(
                        text = "Error occurred. Try again.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Last Result Display
            lastResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                LastResultDisplay(result = result)
            }
        }
    }
}

@Composable
private fun EnhancedVoiceControlButton(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val isEnabled = recordingState != RecordingState.RECORDING
    val buttonColor = when (recordingState) {
        RecordingState.RECORDING -> MaterialTheme.colorScheme.error
        RecordingState.PROCESSING -> MaterialTheme.colorScheme.secondary
        RecordingState.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        if (recordingState == RecordingState.RECORDING) {
                // Pulsing effect when recording would go here
                // For now, just show the recording state
            }      
        IconButton(
            onClick = if (isEnabled) onStartRecording else onStopRecording,
            modifier = Modifier.size(120.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = buttonColor,
                shape = RoundedCornerShape(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when (recordingState) {
                        RecordingState.RECORDING -> {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop Recording",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                        RecordingState.PROCESSING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 4.dp
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Start Recording",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastResultDisplay(result: ContextAwareCommandResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Command Processed",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = result.originalCommand,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (result.intents.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Multi-intent: ${result.intents.size} actions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (result.isShortcut) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚡ Voice Shortcut",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = result.confidence,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Confidence: ${(result.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContextualSuggestionsCard(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Contextual Suggestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onSuggestionClick(suggestion) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Use suggestion",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceProfileStatusCard(
    profile: VoiceProfile,
    onEditProfile: () -> Unit
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
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Voice Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Language: ${profile.preferredLanguage.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onEditProfile) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
