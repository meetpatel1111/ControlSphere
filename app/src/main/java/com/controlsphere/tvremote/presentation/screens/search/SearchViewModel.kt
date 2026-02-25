package com.controlsphere.tvremote.presentation.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.controlsphere.tvremote.domain.model.KeyEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
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

    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(searchQuery = query, errorMessage = null)
    }

    fun executeSearch() {
        if (uiState.searchQuery.isBlank() || !uiState.isConnected) return

        uiState = uiState.copy(isSearching = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Step 1: Open search (using search key event)
                val searchResult = deviceRepository.sendKeyEvent(KeyEvent.SETTINGS_SEARCH)
                if (!searchResult.isSuccess) {
                    throw Exception(searchResult.exceptionOrNull()?.message ?: "Failed to open search")
                }

                // Small delay to ensure search opens
                kotlinx.coroutines.delay(500)

                // Step 2: Send the search text
                val textResult = deviceRepository.sendText(uiState.searchQuery)
                if (!textResult.isSuccess) {
                    throw Exception(textResult.exceptionOrNull()?.message ?: "Failed to send search text")
                }

                // Small delay to ensure text is entered
                kotlinx.coroutines.delay(300)

                // Step 3: Press Enter to execute search
                val enterResult = deviceRepository.sendKeyEvent(KeyEvent.ENTER)
                if (!enterResult.isSuccess) {
                    throw Exception(enterResult.exceptionOrNull()?.message ?: "Failed to execute search")
                }

                // Add to recent searches
                val recentSearches = uiState.recentSearches.toMutableList()
                if (!recentSearches.contains(uiState.searchQuery)) {
                    recentSearches.add(0, uiState.searchQuery)
                    // Keep only last 10 searches
                    if (recentSearches.size > 10) {
                        recentSearches.removeAt(recentSearches.size - 1)
                    }
                }

                uiState = uiState.copy(
                    isSearching = false,
                    searchExecuted = true,
                    recentSearches = recentSearches
                )

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSearching = false,
                    errorMessage = e.message ?: "Search failed"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

data class SearchUiState(
    val isConnected: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchExecuted: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val errorMessage: String? = null
)
