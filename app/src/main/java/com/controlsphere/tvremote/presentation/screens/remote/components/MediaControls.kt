package com.controlsphere.tvremote.presentation.screens.remote.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MediaControls(
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRewind: () -> Unit,
    onFastForward: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous
            MediaButton(
                onClick = onPrevious,
                icon = Icons.Default.SkipPrevious,
                contentDescription = "Previous"
            )

            // Rewind
            MediaButton(
                onClick = onRewind,
                icon = Icons.Default.FastRewind,
                contentDescription = "Rewind"
            )

            // Play/Pause (larger, central button)
            MediaButton(
                onClick = onPlayPause,
                icon = Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                size = 56.dp
            )

            // Fast Forward
            MediaButton(
                onClick = onFastForward,
                icon = Icons.Default.FastForward,
                contentDescription = "Fast Forward"
            )

            // Next
            MediaButton(
                onClick = onNext,
                icon = Icons.Default.SkipNext,
                contentDescription = "Next"
            )
        }
    }
}

@Composable
private fun MediaButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(if (size == 56.dp) 28.dp else 24.dp)
        )
    }
}
