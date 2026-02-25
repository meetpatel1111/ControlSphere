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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.controlsphere.tvremote.data.voice.RecordingState

@Composable
fun VoiceControlButton(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = recordingState == RecordingState.RECORDING
    val isProcessing = recordingState == RecordingState.PROCESSING
    
    // Pulsing animation for recording state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Rotation animation for processing state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow effect when recording
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color.Red.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .scale(scale)
            )
        }

        // Main button
        Button(
            onClick = if (isRecording) onStopRecording else onStartRecording,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = when (recordingState) {
                    RecordingState.RECORDING -> Color.Red
                    RecordingState.PROCESSING -> MaterialTheme.colorScheme.primary
                    RecordingState.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            enabled = recordingState != RecordingState.PROCESSING
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop recording" else "Start recording",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Sound waves animation when recording
        if (isRecording) {
            SoundWavesAnimation()
        }
    }
}

@Composable
private fun SoundWavesAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    
    (1..3).forEach { index ->
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, delayMillis = index * 200, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_alpha_$index"
        )
        
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.5f + (index * 0.3f),
            animationSpec = infiniteRepeatable(
                animation = tween(1500, delayMillis = index * 200, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_scale_$index"
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.3f),
                    shape = CircleShape
                )
                .scale(scale)
        )
    }
}
