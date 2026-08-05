package com.example.codevaultide.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codevaultide.ui.settings.SettingsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit = {}
) {
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val isAutoSaveEnabled by settingsViewModel.isAutoSaveEnabled.collectAsState()
    val isVersionBackupEnabled by settingsViewModel.isVersionBackupEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text("Settings")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Theme Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (isDarkTheme) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { settingsViewModel.setTheme(it) }
                )
            }

            HorizontalDivider()

            // Font Size Setting
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Font Size", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${fontSize.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = fontSize,
                    onValueChange = { settingsViewModel.setFontSize(it) },
                    valueRange = 12f..30f,
                    steps = 17
                )
                Text(
                    text = "Preview Text",
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Auto Save Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Auto Save", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (isAutoSaveEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = isAutoSaveEnabled,
                    onCheckedChange = { settingsViewModel.setAutoSaveEnabled(it) }
                )
            }

            HorizontalDivider()

            // Version Backup Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Version Backup", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (isVersionBackupEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = isVersionBackupEnabled,
                    onCheckedChange = { settingsViewModel.setVersionBackupEnabled(it) }
                )
            }
        }
    }
}