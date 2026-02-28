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
import com.controlsphere.tvremote.data.voice.MultiLanguageCommand

@Composable
fun MultiLanguageCard(
    command: MultiLanguageCommand,
    onReplay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                        text = command.basePhrase,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Detected: ${command.detectedLanguage.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Confidence: ${(command.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (command.confidence > 0.8f) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onReplay) {
                    Icon(Icons.Default.Replay, contentDescription = "Replay")
                }
            }
            
            // Show translations
            if (command.translations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Translations:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                command.translations.entries.take(3).forEach { (language, translation) ->
                    Text(
                        text = "$language: $translation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (command.translations.size > 3) {
                    Text(
                        text = "... and ${command.translations.size - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
