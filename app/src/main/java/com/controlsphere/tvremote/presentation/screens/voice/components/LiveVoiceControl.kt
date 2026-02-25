package com.controlsphere.tvremote.presentation.screens.voice.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.LiveAudioResult
import com.controlsphere.tvremote.data.voice.RecordingState
import kotlinx.coroutines.flow.collect

@Composable
fun LiveVoiceControl(
    apiKey: String,
    onStartLiveSession: (String) -> Unit,
    onStopLiveSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiveSessionActive by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf("Tap to start live conversation") }
    var transcription by remember { mutableStateOf("") }
    
    // Live session status would come from the actual Live API implementation
    LaunchedEffect(isLiveSessionActive) {
        if (isLiveSessionActive) {
            // In a real implementation, this would collect from the Live API flow
            currentStatus = "Live session active - listening..."
        } else {
            currentStatus = "Tap to start live conversation"
            transcription = ""
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live status indicator
        if (isLiveSessionActive) {
            LiveStatusIndicator(status = currentStatus)
        }
        
        // Live voice button with enhanced animation
        LiveVoiceButton(
            isActive = isLiveSessionActive,
            onStart = { 
                isLiveSessionActive = true
                onStartLiveSession(apiKey)
            },
            onStop = { 
                isLiveSessionActive = false
                onStopLiveSession()
            }
        )
        
        // Transcription display
        if (transcription.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "\"$transcription\"",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Live features info
        if (!isLiveSessionActive) {
            LiveFeaturesInfo()
        }
    }
}

@Composable
private fun LiveVoiceButton(
    isActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_animation")
    
    // Pulsing effect for active session
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Rotation for processing state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow for live session
        if (isActive) {
            repeat(3) { index ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, delayMillis = index * 300, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow_alpha_$index"
                )
                
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.8f + (index * 0.3f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, delayMillis = index * 300, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow_scale_$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = alpha),
                            shape = CircleShape
                        )
                        .scale(glowScale)
                )
            }
        }

        // Main button
        Button(
            onClick = if (isActive) onStop else onStart,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 12.dp,
                pressedElevation = 16.dp
            )
        ) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop live session",
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start live session",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveStatusIndicator(
    status: String
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    )
            )
            
            Text(
                text = "LIVE: $status",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveFeaturesInfo() {
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
                text = "🎤 Live Voice Features",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "• Real-time conversation with sub-second latency\n" +
                      "• Natural voice interaction\n" +
                      "• Continuous listening mode\n" +
                      "• Spoken responses available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
