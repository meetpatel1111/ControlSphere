package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.*

@Composable
fun TTSControlPanel(
    apiKey: String,
    onGenerateSpeech: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textToSpeak by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf(GeminiVoice.KORE) }
    var stylePrompt by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎤 Text-to-Speech Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Text input
            OutlinedTextField(
                value = textToSpeak,
                onValueChange = { textToSpeak = it },
                label = { Text("Text to speak") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Voice Selection
            Text(
                text = "Voice Selection",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                items(GeminiVoice.values()) { voice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedVoice == voice,
                                onClick = { selectedVoice = voice },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVoice == voice,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = voice.voiceName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedVoice == voice) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = voice.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Style Prompt
            OutlinedTextField(
                value = stylePrompt,
                onValueChange = { stylePrompt = it },
                label = { Text("Style Prompt (optional)") },
                placeholder = { Text("e.g., Say cheerfully:") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Generate Speech Button
            Button(
                onClick = { onGenerateSpeech(textToSpeak, selectedVoice.voiceName, stylePrompt) },
                modifier = Modifier.fillMaxWidth(),
                enabled = textToSpeak.isNotBlank() && apiKey.isNotBlank()
            ) {
                Text("Generate Speech")
            }
        }
    }
}

@Composable
fun TTSPlaybackControls(
    playbackState: PlaybackState,
    currentAudio: TTSResult?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🔊 Audio Playback",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Current audio info
            currentAudio?.let { audio ->
                Text(
                    text = "Now Playing: \"${audio.text}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Style: ${audio.voiceStyle.description} | Duration: ${audio.duration}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Playback status
            Text(
                text = "Status: ${playbackState.name.lowercase().replace("_", " ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = when (playbackState) {
                    PlaybackState.PLAYING -> MaterialTheme.colorScheme.primary
                    PlaybackState.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (playbackState) {
                    PlaybackState.PLAYING -> {
                        OutlinedButton(
                            onClick = onPause,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pause")
                        }
                    }
                    PlaybackState.PAUSED -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Resume")
                        }
                    }
                    else -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f),
                            enabled = false
                        ) {
                            Text("Resume")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}
