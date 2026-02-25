package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTTSControlPanel(
    apiKey: String,
    onGenerateSpeech: (String, String, String) -> Unit,
    onGenerateMultiSpeaker: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var textToSpeak by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf(GeminiVoice.KORE) }
    var stylePrompt by remember { mutableStateOf("") }
    var showMultiSpeaker by remember { mutableStateOf(false) }
    var conversation by remember { mutableStateOf("") }
    var speakerConfigs by remember { mutableStateOf(mapOf("Speaker1" to "Kore", "Speaker2" to "Puck")) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎤 Advanced TTS Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Mode selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { showMultiSpeaker = false },
                    label = { Text("Single Speaker") },
                    selected = !showMultiSpeaker
                )
                FilterChip(
                    onClick = { showMultiSpeaker = true },
                    label = { Text("Multi-Speaker") },
                    selected = showMultiSpeaker
                )
            }

            if (!showMultiSpeaker) {
                // Single Speaker Mode
                SingleSpeakerControls(
                    textToSpeak = textToSpeak,
                    onTextChange = { textToSpeak = it },
                    selectedVoice = selectedVoice,
                    onVoiceChange = { selectedVoice = it },
                    stylePrompt = stylePrompt,
                    onStylePromptChange = { stylePrompt = it },
                    onGenerate = { onGenerateSpeech(textToSpeak, selectedVoice.voiceName, stylePrompt) }
                )
            } else {
                // Multi-Speaker Mode
                MultiSpeakerControls(
                    conversation = conversation,
                    onConversationChange = { conversation = it },
                    speakerConfigs = speakerConfigs,
                    onSpeakerConfigChange = { speakerConfigs = it },
                    onGenerate = { onGenerateMultiSpeaker(conversation, speakerConfigs) }
                )
            }
        }
    }
}

@Composable
private fun SingleSpeakerControls(
    textToSpeak: String,
    onTextChange: (String) -> Unit,
    selectedVoice: GeminiVoice,
    onVoiceChange: (GeminiVoice) -> Unit,
    stylePrompt: String,
    onStylePromptChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    // Text input
    OutlinedTextField(
        value = textToSpeak,
        onValueChange = onTextChange,
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
                        onClick = { onVoiceChange(voice) },
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
        onValueChange = onStylePromptChange,
        label = { Text("Style Prompt (optional)") },
        placeholder = { Text("e.g., Say cheerfully:") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2
    )

    // Generate Button
    Button(
        onClick = onGenerate,
        modifier = Modifier.fillMaxWidth(),
        enabled = textToSpeak.isNotBlank()
    ) {
        Text("Generate Speech")
    }
}

@Composable
private fun MultiSpeakerControls(
    conversation: String,
    onConversationChange: (String) -> Unit,
    speakerConfigs: Map<String, String>,
    onSpeakerConfigChange: (Map<String, String>) -> Unit,
    onGenerate: () -> Unit
) {
    // Conversation input
    OutlinedTextField(
        value = conversation,
        onValueChange = onConversationChange,
        label = { Text("Conversation") },
        placeholder = { 
            Text("Speaker1: Hello there!\nSpeaker2: Hi! How are you?") 
        },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 6
    )

    // Speaker Configuration
    Text(
        text = "Speaker Configuration",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        speakerConfigs.forEach { (speaker, voiceName) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = speaker,
                    onValueChange = { newSpeaker ->
                        val newConfigs = speakerConfigs.toMutableMap()
                        newConfigs.remove(speaker)
                        newConfigs[newSpeaker] = voiceName
                        onSpeakerConfigChange(newConfigs)
                    },
                    label = { Text("Speaker") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = voiceName,
                    onValueChange = { newVoiceName ->
                        onSpeakerConfigChange(speakerConfigs + (speaker to newVoiceName))
                    },
                    label = { Text("Voice") },
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = {
                        val newConfigs = speakerConfigs.toMutableMap()
                        newConfigs.remove(speaker)
                        onSpeakerConfigChange(newConfigs)
                    }
                ) {
                    Text("×")
                }
            }
        }
        
        Button(
            onClick = {
                val newSpeaker = "Speaker${speakerConfigs.size + 1}"
                onSpeakerConfigChange(speakerConfigs + (newSpeaker to "Kore"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Speaker")
        }
    }

    // Generate Button
    Button(
        onClick = onGenerate,
        modifier = Modifier.fillMaxWidth(),
        enabled = conversation.isNotBlank() && speakerConfigs.isNotEmpty()
    ) {
        Text("Generate Multi-Speaker Speech")
    }
}

@Composable
fun VoiceStyleGuide() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎭 Voice Style Guide",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Use natural language prompts to control speech style:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val examples = listOf(
                "Say cheerfully:",
                "Say professionally:",
                "Say with excitement:",
                "Say calmly:",
                "Say as a news anchor:",
                "Say as a friendly assistant:"
            )
            
            examples.forEach { example ->
                Text(
                    text = "• $example",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
