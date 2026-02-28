package com.controlsphere.tvremote.presentation.screens.remote.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtons(
    onAppsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPowerClick: () -> Unit,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    onDeviceManagementClick: () -> Unit = {},
    onCustomCommandsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        ActionButton(
            onClick = onBackClick,
            icon = Icons.Default.ArrowBack,
            contentDescription = "Back"
        )

        // Home button
        ActionButton(
            onClick = onHomeClick,
            icon = Icons.Default.Home,
            contentDescription = "Home"
        )

        // Apps button
        ActionButton(
            onClick = onAppsClick,
            icon = Icons.Default.Apps,
            contentDescription = "Apps"
        )

        // Settings button (now Device Management)
        ActionButton(
            onClick = onDeviceManagementClick,
            icon = Icons.Default.DevicesOther,
            contentDescription = "Device Management"
        )

        // Custom Commands button
        ActionButton(
            onClick = onCustomCommandsClick,
            icon = Icons.Default.RecordVoiceOver,
            contentDescription = "Custom Commands"
        )

        // Power button (highlighted)
        ActionButton(
            onClick = onPowerClick,
            icon = Icons.Default.PowerSettingsNew,
            contentDescription = "Power",
            isCritical = true
        )
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    isCritical: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = if (isCritical) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
