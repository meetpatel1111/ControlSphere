package com.controlsphere.tvremote.presentation.screens.voice

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
import com.controlsphere.tvremote.data.voice.*
import com.controlsphere.tvremote.presentation.screens.voice.components.VoiceProfileCard
import com.controlsphere.tvremote.presentation.screens.voice.components.AmbientModeCard
import com.controlsphere.tvremote.presentation.screens.voice.components.MultiLanguageCard
import com.controlsphere.tvremote.presentation.screens.voice.components.PersonalizedResponseCard
import com.controlsphere.tvremote.presentation.screens.voice.components.VoiceTrainingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedVoiceScreen(
    navController: NavController,
    viewModel: AdvancedVoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showAmbientConfigDialog by remember { mutableStateOf(false) }
    var showPersonalizedResponseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Voice Features") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateProfileDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Voice Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Profile Section
            item {
                uiState.currentProfile?.let { profile ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Current Voice Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            
                            Text(
                                text = "Language: ${profile.preferredLanguage.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            profile.accent?.let { accent ->
                                Text(
                                    text = "Accent: $accent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Wake Word Sensitivity: ${(profile.wakeWordSensitivity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Button(
                                    onClick = { viewModel.startVoiceTraining(profile.id) },
                                    enabled = !uiState.isTraining
                                ) {
                                    if (uiState.isTraining) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Train")
                                    }
                                }
                            }
                            
                            if (uiState.trainingComplete) {
                                Text(
                                    text = "Training completed successfully!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Ambient Mode Section
            item {
                AmbientModeCard(
                    config = uiState.ambientModeConfig,
                    isActive = uiState.isAmbientModeActive,
                    wakeWordState = uiState.wakeWordState,
                    onEnable = { wakeWord, sensitivity, continuous ->
                        viewModel.enableAmbientMode(wakeWord, sensitivity, continuous)
                    },
                    onDisable = { viewModel.disableAmbientMode() },
                    onConfigure = { showAmbientConfigDialog = true }
                )
            }

            // Voice Recognition Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Voice Recognition Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Language",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.currentLanguage.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // Language switch dropdown
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = uiState.currentLanguage.name,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Switch Language") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    VoiceLanguage.values().forEach { language ->
                                        DropdownMenuItem(
                                            text = { Text(language.name) },
                                            onClick = {
                                                viewModel.switchLanguage(language)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (uiState.isListening) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(end = 8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    ) {}
                                }
                                Text(
                                    text = "Listening for command...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        uiState.recognizedCommand?.let { command ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Recognized Command:",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = command,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        uiState.personalizedResponse?.let { response ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Response:",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = response,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Voice Profiles List
            item {
                Text(
                    text = "Voice Profiles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(uiState.voiceProfiles) { profile ->
                VoiceProfileCard(
                    profile = profile,
                    isCurrentProfile = profile.id == uiState.currentProfile?.id,
                    onSwitchTo = { viewModel.switchToProfile(profile.id) },
                    onEdit = { /* Navigate to edit profile */ },
                    onDelete = { viewModel.deleteVoiceProfile(profile.id) },
                    onTrain = { viewModel.startVoiceTraining(profile.id) }
                )
            }

            // Multi-language Commands History
            if (uiState.multiLanguageCommands.isNotEmpty()) {
                item {
                    Text(
                        text = "Multi-language Commands",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.multiLanguageCommands.takeLast(5)) { command ->
                    MultiLanguageCard(
                        command = command,
                        onReplay = { /* Replay command */ }
                    )
                }
            }

            // Personalized Responses
            if (uiState.personalizedResponses.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personalized Responses",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showPersonalizedResponseDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Response")
                        }
                    }
                }
                
                items(uiState.personalizedResponses.takeLast(3)) { response ->
                    PersonalizedResponseCard(
                        response = response,
                        onEdit = { /* Edit response */ },
                        onDelete = { /* Delete response */ }
                    )
                }
            }

            // Recent Recognition Results
            if (uiState.recognitionResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Recognition Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.recognitionResults.takeLast(5)) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = result.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Confidence: ${(result.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "Language: ${result.language.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Text(
                                text = "Processing time: ${result.processingTime}ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        CreateVoiceProfileDialog(
            onDismiss = { showCreateProfileDialog = false },
            onCreate = { name, userId, language, accent, pitch, speed ->
                viewModel.createVoiceProfile(name, userId, language, accent, pitch, speed)
                showCreateProfileDialog = false
            }
        )
    }

    // Ambient Mode Config Dialog
    if (showAmbientConfigDialog) {
        AmbientModeConfigDialog(
            currentConfig = uiState.ambientModeConfig,
            onDismiss = { showAmbientConfigDialog = false },
            onSave = { config ->
                viewModel.updateAmbientModeConfig(config)
                showAmbientConfigDialog = false
            }
        )
    }

    // Personalized Response Dialog
    if (showPersonalizedResponseDialog) {
        PersonalizedResponseDialog(
            onDismiss = { showPersonalizedResponseDialog = false },
            onCreate = { template, responseType, variables ->
                viewModel.createPersonalizedResponse(template, responseType, variables)
                showPersonalizedResponseDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateVoiceProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, VoiceLanguage, String?, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(VoiceLanguage.ENGLISH) }
    var accent by remember { mutableStateOf("") }
    var pitch by remember { mutableStateOf(1.0f) }
    var speed by remember { mutableStateOf(1.0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Voice Profile") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLanguage.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Preferred Language") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        VoiceLanguage.values().forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language.name) },
                                onClick = {
                                    selectedLanguage = language
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = accent,
                    onValueChange = { accent = it },
                    label = { Text("Accent (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Pitch: ${(pitch * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = pitch,
                    onValueChange = { pitch = it },
                    valueRange = 0.5f..1.5f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Speed: ${(speed * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 0.5f..1.5f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && userId.isNotBlank()) {
                        onCreate(
                            name,
                            userId,
                            selectedLanguage,
                            accent.ifBlank { null },
                            pitch,
                            speed
                        )
                    }
                },
                enabled = name.isNotBlank() && userId.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmbientModeConfigDialog(
    currentConfig: AmbientModeConfig,
    onDismiss: () -> Unit,
    onSave: (AmbientModeConfig) -> Unit
) {
    var wakeWord by remember { mutableStateOf(currentConfig.wakeWord) }
    var sensitivity by remember { mutableStateOf(currentConfig.sensitivity) }
    var responseDelay by remember { mutableStateOf(currentConfig.responseDelay) }
    var continuousListening by remember { mutableStateOf(currentConfig.continuousListening) }
    var batteryOptimization by remember { mutableStateOf(currentConfig.batteryOptimization) }
    var privacyMode by remember { mutableStateOf(currentConfig.privacyMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ambient Mode Configuration") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = wakeWord,
                    onValueChange = { wakeWord = it },
                    label = { Text("Wake Word") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Sensitivity: ${(sensitivity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.1f..1.0f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Response Delay: ${responseDelay}ms",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = responseDelay.toFloat(),
                    onValueChange = { responseDelay = it.toLong() },
                    valueRange = 0f..2000f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Continuous Listening")
                    Switch(
                        checked = continuousListening,
                        onCheckedChange = { continuousListening = it }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Battery Optimization")
                    Switch(
                        checked = batteryOptimization,
                        onCheckedChange = { batteryOptimization = it }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Privacy Mode")
                    Switch(
                        checked = privacyMode,
                        onCheckedChange = { privacyMode = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val config = AmbientModeConfig(
                        isEnabled = currentConfig.isEnabled,
                        wakeWord = wakeWord,
                        sensitivity = sensitivity,
                        responseDelay = responseDelay,
                        continuousListening = continuousListening,
                        batteryOptimization = batteryOptimization,
                        nightMode = currentConfig.nightMode,
                        privacyMode = privacyMode
                    )
                    onSave(config)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalizedResponseDialog(
    onDismiss: () -> Unit,
    onCreate: (String, ResponseType, Map<String, String>) -> Unit
) {
    var template by remember { mutableStateOf("") }
    var selectedResponseType by remember { mutableStateOf(ResponseType.CONFIRMATION) }
    var variables by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Personalized Response") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text("Response Template") },
                    placeholder = { Text("e.g., \"I'll {command} for you right away, {user}!\"") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedResponseType.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Response Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ResponseType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedResponseType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = variables,
                    onValueChange = { variables = it },
                    label = { Text("Variables (key=value, comma separated)") },
                    placeholder = { Text("e.g., user=John, device=TV") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (template.isNotBlank()) {
                        val variablesMap = variables.split(",")
                            .mapNotNull { it.trim().split("=", limit = 2) }
                            .filter { it.size == 2 }
                            .associate { it[0].trim() to it[1].trim() }
                        
                        onCreate(template, selectedResponseType, variablesMap)
                    }
                },
                enabled = template.isNotBlank()
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
