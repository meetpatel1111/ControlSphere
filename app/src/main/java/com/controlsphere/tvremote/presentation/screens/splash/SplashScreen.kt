package com.controlsphere.tvremote.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.controlsphere.tvremote.R
import com.controlsphere.tvremote.presentation.navigation.Screen
import kotlinx.coroutines.delay
import android.util.Log

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "Splash screen started, will navigate in 2 seconds")
        delay(2000) // Show splash for 2 seconds
        Log.d("SplashScreen", "Navigating to DevicePairing")
        navController.navigate(Screen.DevicePairing.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo/icon placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Add actual app icon
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF6200EE),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "CS",
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ControlSphere",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart TV Commander",
                color = Color(0xFF03DAC6),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF03DAC6),
                strokeWidth = 2.dp
            )
        }
    }
}
