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
import com.controlsphere.tvremote.data.voice.CustomVoiceCommand
import com.controlsphere.tvremote.data.voice.CommandCategory
import com.controlsphere.tvremote.presentation.screens.voice.components.CustomCommandCard
import com.controlsphere.tvremote.presentation.screens.voice.components.CreateCommandDialog
import com.controlsphere.tvremote.presentation.screens.voice.components.CommandTemplateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCommandsScreen(
    navController: NavController,
    viewModel: CustomCommandsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Voice Commands") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshCommands() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Command")
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
            // Statistics Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${uiState.allCommands.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total Commands",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${uiState.enabledCommands.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${uiState.globalCommands.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Global",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Category Filter
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedCategoryIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.categoryOptions.forEachIndexed { index, category ->
                        Tab(
                            selected = index == uiState.selectedCategoryIndex,
                            onClick = { 
                                viewModel.setCategoryFilter(index)
                            },
                            text = { Text(category) }
                        )
                    }
                }
            }

            // Command Templates Section
            if (uiState.selectedCategoryIndex == 0) {
                item {
                    Text(
                        text = "Quick Start Templates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.commandTemplates) { template ->
                    CommandTemplateCard(
                        template = template,
                        onUseTemplate = { 
                            viewModel.createFromTemplate(template)
                            showCreateDialog = true
                        }
                    )
                }
            }

            // Custom Commands Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Commands",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (uiState.filteredCommands.isNotEmpty()) {
                        Text(
                            text = "${uiState.filteredCommands.size} commands",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.filteredCommands.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No custom commands found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Create your first voice command or use a template",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.filteredCommands) { command ->
                    CustomCommandCard(
                        command = command,
                        onEdit = { viewModel.editCommand(command) },
                        onToggle = { viewModel.toggleCommand(command.id) },
                        onDelete = { viewModel.deleteCommand(command.id) },
                        onDuplicate = { viewModel.duplicateCommand(command) },
                        onExecute = { viewModel.executeCommand(command.id) }
                    )
                }
            }

            // Most Used Commands Section
            if (uiState.mostUsedCommands.isNotEmpty()) {
                item {
                    Text(
                        text = "Most Used",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.mostUsedCommands.take(5)) { command ->
                    CustomCommandCard(
                        command = command,
                        onEdit = { viewModel.editCommand(command) },
                        onToggle = { viewModel.toggleCommand(command.id) },
                        onDelete = { viewModel.deleteCommand(command.id) },
                        onDuplicate = { viewModel.duplicateCommand(command) },
                        onExecute = { viewModel.executeCommand(command.id) },
                        compact = true
                    )
                }
            }
        }
    }

    // Create Command Dialog
    if (showCreateDialog) {
        CreateCommandDialog(
            command = uiState.editingCommand,
            availableDevices = uiState.availableDevices,
            onDismiss = { 
                showCreateDialog = false
                viewModel.clearEditingCommand()
            },
            onSave = { command ->
                if (command.id.isEmpty()) {
                    viewModel.createCommand(command)
                } else {
                    viewModel.updateCommand(command)
                }
                showCreateDialog = false
            }
        )
    }
}
