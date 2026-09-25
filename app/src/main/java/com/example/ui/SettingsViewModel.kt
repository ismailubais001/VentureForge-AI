package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.UserPreferencesRepository
import com.example.domain.repository.VentureRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val ventureRepository: VentureRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val language: StateFlow<String> = preferencesRepository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val onboardingCompleted: StateFlow<Boolean> = preferencesRepository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val backendUrl: StateFlow<String> = preferencesRepository.backendUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(lang)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(completed)
        }
    }

    fun setBackendUrl(url: String) {
        viewModelScope.launch {
            preferencesRepository.setBackendUrl(url)
        }
    }

    fun deleteAllLocalData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            ventureRepository.deleteAllVentures()
            preferencesRepository.clearAll()
            onDeleted()
        }
    }
}
