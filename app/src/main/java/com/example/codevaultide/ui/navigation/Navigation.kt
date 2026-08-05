package com.example.codevaultide.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.ui.screens.*
import com.example.codevaultide.ui.settings.SettingsViewModel

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val FILES = "files"
    const val COMPILER = "compiler"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel = viewModel()) {
    val navController = rememberNavController()
    val editorViewModel: EditorViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Routes.HOME, Routes.FILES, Routes.COMPILER, Routes.SETTINGS)) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                        label = { Text("Files") },
                        selected = currentRoute == Routes.FILES,
                        onClick = {
                            navController.navigate(Routes.FILES) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Compiler") },
                        label = { Text("Run") },
                        selected = currentRoute == Routes.COMPILER,
                        onClick = {
                            navController.navigate(Routes.COMPILER) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = {
                            navController.navigate(Routes.SETTINGS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNewFileClick = { navController.navigate(Routes.EDITOR) },
                    onOpenFileClick = { navController.navigate(Routes.FILES) },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onRecentFileClick = { file ->
                        editorViewModel.setCode("// Content for ${file.name}\n\nfun main() {\n    println(\"Opening ${file.name}\")\n}")
                        navController.navigate(Routes.EDITOR)
                    }
                )
            }

            composable(Routes.EDITOR) {
                EditorScreen(
                    viewModel = editorViewModel,
                    onBackClick = { navController.popBackStack() },
                    onRunClick = { navController.navigate(Routes.COMPILER) },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    editorViewModel = editorViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCompareClick = { /* Diff screen implementation */ }
                )
            }

            composable(Routes.FILES) {
                FilesScreen(
                    onBackClick = { navController.popBackStack() },
                    onFileClick = { navController.navigate(Routes.EDITOR) },
                    editorViewModel = editorViewModel
                )
            }

            composable(Routes.COMPILER) {
                CompilerScreen(
                    onBackClick = { navController.popBackStack() },
                    editorViewModel = editorViewModel
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}