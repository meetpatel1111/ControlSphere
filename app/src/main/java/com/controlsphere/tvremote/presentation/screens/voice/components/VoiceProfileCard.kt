package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.VoiceProfile

@Composable
fun VoiceProfileCard(
    profile: VoiceProfile,
    isCurrentProfile: Boolean,
    onSwitchTo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTrain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentProfile) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCurrentProfile) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Current Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrentProfile) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "Language: ${profile.preferredLanguage.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        profile.accent?.let { accent ->
                            Text(
                                text = "Accent: $accent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = "Samples: ${profile.voiceSamples.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onTrain) {
                        Icon(Icons.Default.Mic, contentDescription = "Train")
                    }
                    if (!isCurrentProfile) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
            
            if (!isCurrentProfile) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSwitchTo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Switch to Profile")
                }
            }
        }
    }
}
