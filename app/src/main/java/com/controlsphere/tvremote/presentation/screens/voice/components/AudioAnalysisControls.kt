package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioAnalysisControls(
    apiKey: String,
    onAnalyzeAudio: (ByteArray, String, AudioAnalysisType) -> Unit,
    onTranscribeAudio: (ByteArray, String, Boolean, Boolean, Boolean) -> Unit,
    onDetectSpeakers: (ByteArray, String) -> Unit,
    onGetSegmentTranscript: (ByteArray, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var analysisType by remember { mutableStateOf(AudioAnalysisType.GENERAL) }
    var includeTimestamps by remember { mutableStateOf(true) }
    var includeEmotionDetection by remember { mutableStateOf(true) }
    var includeTranslation by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("01:00") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎵 Audio Analysis Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Analysis Type Selection
            Text(
                text = "Analysis Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                modifier = Modifier.height(120.dp)
            ) {
                items(AudioAnalysisType.values()) { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = analysisType == type,
                                onClick = { analysisType = type },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = analysisType == type,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (type) {
                                AudioAnalysisType.GENERAL -> "General Analysis"
                                AudioAnalysisType.EMOTION -> "Emotion Detection"
                                AudioAnalysisType.CONTENT_SUMMARY -> "Content Summary"
                                AudioAnalysisType.SPEAKER_ANALYSIS -> "Speaker Analysis"
                                AudioAnalysisType.MUSIC_ANALYSIS -> "Music Analysis"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Transcription Options
            Text(
                text = "Transcription Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { includeTimestamps = !includeTimestamps },
                    label = { Text("Timestamps") },
                    selected = includeTimestamps
                )
                
                FilterChip(
                    onClick = { includeEmotionDetection = !includeEmotionDetection },
                    label = { Text("Emotion") },
                    selected = includeEmotionDetection
                )
                
                FilterChip(
                    onClick = { includeTranslation = !includeTranslation },
                    label = { Text("Translation") },
                    selected = includeTranslation
                )
            }
            
            // Segment Transcription
            Text(
                text = "Segment Transcription",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (MM:SS)") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time (MM:SS)") },
                    modifier = Modifier.weight(1f)
                )
                
                Button(
                    onClick = { /* Trigger segment transcription */ },
                    enabled = false // Will be enabled when audio is available
                ) {
                    Text("Get Segment")
                }
            }
            
            // Analysis Button
            Button(
                onClick = { /* Trigger audio analysis */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Will be enabled when audio is available
            ) {
                Text("Analyze Audio")
            }
            
            // Transcription Button
            Button(
                onClick = { /* Trigger transcription */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Will be enabled when audio is available
            ) {
                Text("Transcribe Audio")
            }
            
            // Speaker Detection Button
            Button(
                onClick = { /* Trigger speaker detection */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Will be enabled when audio is available
            ) {
                Text("Detect Speakers")
            }
        }
    }
}

@Composable
fun AudioAnalysisResults(
    transcriptionResult: AudioTranscriptionResult?,
    analysisResult: AudioAnalysisResult?,
    speakerResult: SpeakerDiarizationResult?,
    segmentResult: SegmentTranscriptResult?,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Analysis Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Transcription Results
            transcriptionResult?.let { transcription ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📝 Transcription",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = transcription.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(transcription.segments) { segment ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = segment.speaker,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = segment.timestamp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Text(
                                        text = segment.content,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = segment.language,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        segment.translation?.let { translation ->
                                            Text(
                                                text = "→ $translation",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        Text(
                                            text = segment.emotion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = when (segment.emotion) {
                                                "happy" -> MaterialTheme.colorScheme.primary
                                                "sad" -> MaterialTheme.colorScheme.error
                                                "angry" -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Analysis Results
            analysisResult?.let { analysis ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔍 Analysis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = when (analysis.analysisType) {
                            AudioAnalysisType.GENERAL -> "General Analysis"
                            AudioAnalysisType.EMOTION -> "Emotion Detection"
                            AudioAnalysisType.CONTENT_SUMMARY -> "Content Summary"
                            AudioAnalysisType.SPEAKER_ANALYSIS -> "Speaker Analysis"
                            AudioAnalysisType.MUSIC_ANALYSIS -> "Music Analysis"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = analysis.analysis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Speaker Results
            speakerResult?.let { diarization ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👥 Speaker Diarization",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(diarization.speakers) { speaker ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = speaker.id,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Text(
                                        text = "${speaker.percentage.toInt()}% speaking time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = speaker.characteristics,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    
                                    Text(
                                        text = "Role: ${speaker.role}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Segment Results
            segmentResult?.let { segment ->
                Column(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏱️ Segment Transcript",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Time: ${segment.startTime} - ${segment.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = segment.transcript,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AudioAnalysisGuide() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎵 Audio Analysis Guide",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            val features = listOf(
                "• Transcribe speech to text with timestamps",
                "• Detect and identify different speakers",
                "• Analyze emotions in speech",
                "• Support for 50+ languages",
                "• Automatic translation available",
                "• Segment-specific transcription",
                "• Music and audio content analysis"
            )
            
            features.forEach { feature ->
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Note: Audio analysis requires Gemini 3 Flash Preview model and may consume additional API quota.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
