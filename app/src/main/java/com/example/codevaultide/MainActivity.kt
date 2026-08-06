package com.example.codevaultide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codevaultide.ui.navigation.AppNavigation
import com.example.codevaultide.ui.theme.CodeVaultTheme
import com.example.codevaultide.ui.settings.SettingsViewModel
<<<<<<< HEAD
=======
import com.example.codevaultide.util.CodeTemplates
>>>>>>> origin/main

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

            CodeVaultTheme(darkTheme = isDarkTheme) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}