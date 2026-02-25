package com.controlsphere.tvremote.presentation.screens.apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    var uiState by mutableStateOf(AppsUiState())
        private set

    init {
        // Observe connection status
        viewModelScope.launch {
            deviceRepository.getConnectionStatus().collect { status ->
                uiState = uiState.copy(
                    isConnected = status.isConnected,
                    errorMessage = status.errorMessage?.takeIf { !status.isConnected }
                )
            }
        }
    }

    fun loadInstalledApps() {
        if (!uiState.isConnected) return

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = deviceRepository.getInstalledApps()
            
            uiState = uiState.copy(isLoading = false)
            
            result.fold(
                onSuccess = { packages ->
                    val apps = packages.map { packageName ->
                        AppInfo(
                            packageName = packageName,
                            label = getAppLabel(packageName),
                            isFavorite = isFavorite(packageName)
                        )
                    }.sortedBy { it.label }
                    
                    uiState = uiState.copy(apps = apps)
                },
                onFailure = { exception ->
                    uiState = uiState.copy(
                        errorMessage = exception.message ?: "Failed to load apps"
                    )
                }
            )
        }
    }

    fun launchApp(packageName: String) {
        uiState = uiState.copy(launchingApp = packageName, errorMessage = null)

        viewModelScope.launch {
            val result = deviceRepository.launchApp(packageName)
            
            uiState = uiState.copy(launchingApp = null)
            
            if (!result.isSuccess) {
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to launch app"
                )
            }
        }
    }

    fun forceStopApp(packageName: String) {
        viewModelScope.launch {
            val result = deviceRepository.forceStopApp(packageName)
            
            if (!result.isSuccess) {
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to stop app"
                )
            }
        }
    }

    fun toggleFavorite(packageName: String) {
        val currentApps = uiState.apps.toMutableList()
        val appIndex = currentApps.indexOfFirst { it.packageName == packageName }
        
        if (appIndex >= 0) {
            val app = currentApps[appIndex]
            currentApps[appIndex] = app.copy(isFavorite = !app.isFavorite)
            
            uiState = uiState.copy(apps = currentApps)
            
            // Save favorite state (in a real app, this would be persisted)
            saveFavoriteState(packageName, !app.isFavorite)
        }
    }

    fun toggleFavoritesFilter() {
        uiState = uiState.copy(showFavoritesOnly = !uiState.showFavoritesOnly)
    }

    private fun getAppLabel(packageName: String): String {
        // In a real implementation, this would resolve the actual app label
        // For now, return a formatted version of the package name
        return packageName.split(".").lastOrNull()?.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        } ?: packageName
    }

    private fun isFavorite(packageName: String): Boolean {
        // In a real implementation, this would check persisted storage
        return false
    }

    private fun saveFavoriteState(packageName: String, isFavorite: Boolean) {
        // In a real implementation, this would persist to storage
        // For now, this is a no-op
    }
}

data class AppsUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val apps: List<AppInfo> = emptyList(),
    val launchingApp: String? = null,
    val showFavoritesOnly: Boolean = false,
    val errorMessage: String? = null
)
