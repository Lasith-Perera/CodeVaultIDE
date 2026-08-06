package com.example.codevaultide.ui.navigation

<<<<<<< HEAD

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
=======
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
>>>>>>> origin/main
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.codevaultide.editor.EditorViewModel
<<<<<<< HEAD
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
fun AppNavigation(settingsViewModel: SettingsViewModel = viewModel()){


    val navController =
        rememberNavController()



    // Shared editor state
    val editorViewModel: EditorViewModel =
        viewModel()

=======
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
>>>>>>> origin/main

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

<<<<<<< HEAD

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Routes.HOME, Routes.FILES, Routes.COMPILER, Routes.SETTINGS)) {
=======
    var showAiSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Routes.HOME, Routes.FILES, Routes.HISTORY)) {
>>>>>>> origin/main
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Routes.HOME,
                        onClick = {
<<<<<<< HEAD
=======
                            editorViewModel.loadFile(null, "Main.kt", "")
>>>>>>> origin/main
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
<<<<<<< HEAD
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
=======
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
>>>>>>> origin/main
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
<<<<<<< HEAD

            navController = navController,

            startDestination = Routes.HOME,

            modifier = Modifier.padding(innerPadding)

        ){



        composable(Routes.HOME){


            HomeScreen(

                onNewFileClick = {

                    navController.navigate(
                        Routes.EDITOR
                    )

                },


                onOpenFileClick = {

                    navController.navigate(
                        Routes.FILES
                    )

                },


                onRunClick = {

                    navController.navigate(
                        Routes.COMPILER
                    )

                },


                onHistoryClick = {

                    navController.navigate(
                        Routes.HISTORY
                    )

                },

                onSettingsClick = {
                    navController.navigate(
                        Routes.SETTINGS
                    )
                },

                onRecentFileClick = { file ->
                    // Set dummy content based on filename for demonstration
                    editorViewModel.setCode("// Content for ${file.name}\n\n// TODO: Implement actual file loading\n\nfun main() {\n    println(\"Opening ${file.name}\")\n}")
                    navController.navigate(Routes.EDITOR)
                }

            )


        }





        composable(Routes.EDITOR){


            EditorScreen(

                viewModel = editorViewModel,
                settingsViewModel = settingsViewModel,


                onBackClick = {

                    navController.popBackStack()

                },


                onRunClick = {

                    navController.navigate(
                        Routes.COMPILER
                    )

                },


                onHistoryClick = {

                    navController.navigate(
                        Routes.HISTORY
                    )

                }

            )


        }





        composable(Routes.HISTORY){


            HistoryScreen(

                editorViewModel = editorViewModel,


                onBackClick = {

                    navController.popBackStack()

                },


                onCompareClick = {

                    // Diff screen later

                }

            )


        }




        composable(Routes.FILES){


            FilesScreen(

                onBackClick = {

                    navController.popBackStack()

                },
                onFileClick = {
                    navController.navigate(Routes.EDITOR)
                },
                editorViewModel = editorViewModel

            )

        }




        composable(Routes.COMPILER){


            CompilerScreen(

                onBackClick = {

                    navController.popBackStack()

                },
                editorViewModel = editorViewModel

            )

        }




        composable(Routes.SETTINGS){


            SettingsScreen(
                settingsViewModel = settingsViewModel,

                onBackClick = {

                    navController.popBackStack()

                }

            )

        }


    }
    }


}
=======
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
>>>>>>> origin/main
