package com.controlsphere.tvremote.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.controlsphere.tvremote.data.voice.VoiceLanguage
import com.controlsphere.tvremote.presentation.screens.apps.AppsScreen
import com.controlsphere.tvremote.presentation.screens.devicepairing.DevicePairingScreen
import com.controlsphere.tvremote.presentation.screens.remote.RemoteScreen
import com.controlsphere.tvremote.presentation.screens.search.SearchScreen
import com.controlsphere.tvremote.presentation.screens.settings.LanguageSelectionScreen
import com.controlsphere.tvremote.presentation.screens.settings.SettingsScreen
import com.controlsphere.tvremote.presentation.screens.splash.SplashScreen
import com.controlsphere.tvremote.presentation.screens.textinput.TextInputScreen
import com.controlsphere.tvremote.presentation.screens.voice.VoiceScreen

@Composable
fun ControlSphereNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.DevicePairing.route) {
            DevicePairingScreen(navController = navController)
        }
        composable(Screen.Remote.route) {
            RemoteScreen(navController = navController)
        }
        composable(Screen.Voice.route) {
            VoiceScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                currentLanguage = VoiceLanguage.ENGLISH, // TODO: Get from ViewModel
                onLanguageClick = { navController.navigate(Screen.LanguageSelection.route) },
                onThemeClick = { /* TODO: Implement theme selection */ },
                onSecurityClick = { /* TODO: Implement biometric setup */ },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                currentLanguage = VoiceLanguage.ENGLISH, // TODO: Get from ViewModel
                onLanguageSelected = { language ->
                    // TODO: Save language preference via ViewModel
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("apps") {
            AppsScreen(navController = navController)
        }
        composable("text_input") {
            TextInputScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("voice") {
            VoiceScreen(navController = navController)
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object DevicePairing : Screen("device_pairing")
    object Remote : Screen("remote")
    object Voice : Screen("voice")
    object Settings : Screen("settings")
    object LanguageSelection : Screen("language_selection")
}
