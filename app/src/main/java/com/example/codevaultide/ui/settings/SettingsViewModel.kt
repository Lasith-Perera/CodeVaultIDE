package com.example.codevaultide.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(14f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _isAutoSaveEnabled = MutableStateFlow(false)
    val isAutoSaveEnabled: StateFlow<Boolean> = _isAutoSaveEnabled.asStateFlow()

    private val _isVersionBackupEnabled = MutableStateFlow(true)
    val isVersionBackupEnabled: StateFlow<Boolean> = _isVersionBackupEnabled.asStateFlow()

    private val _encoding = MutableStateFlow("UTF-8")
    val encoding: StateFlow<String> = _encoding.asStateFlow()

    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
    }

    fun setAutoSaveEnabled(enabled: Boolean) {
        _isAutoSaveEnabled.value = enabled
    }

    fun setVersionBackupEnabled(enabled: Boolean) {
        _isVersionBackupEnabled.value = enabled
    }

    fun setEncoding(encoding: String) {
        _encoding.value = encoding
    }
}
