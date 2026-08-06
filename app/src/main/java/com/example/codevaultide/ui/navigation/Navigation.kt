package com.example.codevaultide.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.editor.FileViewModel
import com.example.codevaultide.ui.screens.*
import com.example.codevaultide.ui.settings.SettingsViewModel
import com.example.codevaultide.util.CodeTemplates

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val FILES = "files"
    const val AI = "ai"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    settingsViewModel: SettingsViewModel = viewModel(),
    fileViewModel: FileViewModel = viewModel()
) {
    val navController = rememberNavController()
    val editorViewModel: EditorViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showAiSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Routes.HOME, Routes.FILES, Routes.HISTORY)) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            editorViewModel.loadFile(null, "Main.kt", "")
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
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant") },
                        label = { Text("AI") },
                        selected = showAiSheet,
                        onClick = {
                            showAiSheet = true
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentRoute == Routes.HISTORY,
                        onClick = {
                            navController.navigate(Routes.HISTORY) {
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
                    fileViewModel = fileViewModel,
                    onNewFileClick = { fileName ->
                        val template = CodeTemplates.getInitialCode(fileName)
                        fileViewModel.createNewFile(fileName, template) { newId ->
                            editorViewModel.loadFile(newId, fileName, template)
                            navController.navigate(Routes.EDITOR)
                        }
                    },
                    onOpenFileClick = { navController.navigate(Routes.FILES) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onRecentFileClick = { file ->
                        editorViewModel.loadFile(file.id, file.name, file.content)
                        navController.navigate(Routes.EDITOR)
                    }
                )
            }

            composable(Routes.EDITOR) {
                EditorScreen(
                    viewModel = editorViewModel,
                    fileViewModel = fileViewModel,
                    settingsViewModel = settingsViewModel,
                    onBackClick = {
                        editorViewModel.loadFile(null, "Main.kt", "")
                        navController.popBackStack()
                    },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    editorViewModel = editorViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCompareClick = { /* Diff view */ }
                )
            }

            composable(Routes.FILES) {
                FilesScreen(
                    onBackClick = { navController.popBackStack() },
                    onFileClick = { file ->
                        editorViewModel.loadFile(file.id, file.name, file.content)
                        navController.navigate(Routes.EDITOR)
                    },
                    editorViewModel = editorViewModel,
                    fileViewModel = fileViewModel
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        if (showAiSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAiSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                AiAssistantSheetContent(onClose = { showAiSheet = false })
            }
        }
    }
}