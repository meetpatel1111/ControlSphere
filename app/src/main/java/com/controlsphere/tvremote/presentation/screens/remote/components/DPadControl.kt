package com.controlsphere.tvremote.presentation.screens.remote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DPadControl(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit
) {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Up button
        DPadButton(
            onClick = onUp,
            text = "↑",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        )

        // Down button
        DPadButton(
            onClick = onDown,
            text = "↓",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (20).dp)
        )

        // Left button
        DPadButton(
            onClick = onLeft,
            text = "←",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-20).dp)
        )

        // Right button
        DPadButton(
            onClick = onRight,
            text = "→",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (20).dp)
        )

        // Center button
        DPadButton(
            onClick = onCenter,
            text = "OK",
            modifier = Modifier.size(60.dp),
            backgroundColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DPadButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = if (backgroundColor == MaterialTheme.colorScheme.surfaceVariant) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
