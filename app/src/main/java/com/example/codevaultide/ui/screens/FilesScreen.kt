package com.example.codevaultide.ui.screens

<<<<<<< HEAD

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codevaultide.editor.EditorViewModel


=======
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codevaultide.database.FileEntity
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.editor.FileViewModel
>>>>>>> origin/main

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onBackClick: () -> Unit = {},
<<<<<<< HEAD
    onFileClick: () -> Unit = {},
    editorViewModel: EditorViewModel
){


    Scaffold(

        topBar = {

            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
=======
    onFileClick: (FileEntity) -> Unit = {},
    editorViewModel: EditorViewModel,
    fileViewModel: FileViewModel
) {
    val filesList by fileViewModel.allFiles.collectAsState(initial = emptyList())

    // Selection mode state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFileIds by remember { mutableStateOf(setOf<Long>()) }

    // Dialog state management
    var fileToRename by remember { mutableStateOf<FileEntity?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<FileEntity?>(null) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                isSelectionMode = false
                                selectedFileIds = emptySet()
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isSelectionMode) "Close Selection" else "Back"
>>>>>>> origin/main
                        )
                    }
                },
                title = {
<<<<<<< HEAD
                    Text("Files")
                }

            )

        }

    ){padding ->


        Column(

            modifier =
                Modifier
                    .padding(padding)
                    .padding(20.dp)

        ){


            FileItem("Main.kt") {
                editorViewModel.updateCode("// Opened Main.kt\npackage com.example.codevault\n\nfun main() {\n    println(\"Main file\")\n}")
                onFileClick()
            }

            FileItem("Login.kt") {
                editorViewModel.updateCode("// Opened Login.kt\nclass Login {\n    fun authenticate() { }\n}")
                onFileClick()
            }

            FileItem("README.md") {
                editorViewModel.updateCode("# CodeVault IDE\n\nThis is a local IDE with version control.")
                onFileClick()
            }


        }


    }


}



@Composable
fun FileItem(name:String, onClick: () -> Unit = {}){


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        onClick = onClick

    ){


        Row(

            modifier =
                Modifier.padding(20.dp)

        ){


            Icon(
                Icons.Default.Description,
                null
            )


            Spacer(
                Modifier.width(15.dp)
            )


            Text(name)


        }


    }


=======
                    Text(
                        text = if (isSelectionMode) "${selectedFileIds.size} Selected" else "Workspace Files",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (filesList.isNotEmpty()) {
                        if (isSelectionMode) {
                            IconButton(
                                onClick = {
                                    selectedFileIds = if (selectedFileIds.size == filesList.size) {
                                        emptySet()
                                    } else {
                                        filesList.map { it.id }.toSet()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                            }

                            if (selectedFileIds.isNotEmpty()) {
                                IconButton(onClick = { showBulkDeleteDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Selected",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = { isSelectionMode = true }) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Multi Select")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (filesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No files created yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filesList, key = { it.id }) { file ->
                    val isSelected = selectedFileIds.contains(file.id)

                    FileItem(
                        file = file,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                selectedFileIds = if (isSelected) {
                                    selectedFileIds - file.id
                                } else {
                                    selectedFileIds + file.id
                                }
                            } else {
                                onFileClick(file)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedFileIds = setOf(file.id)
                            }
                        },
                        onRenameClick = {
                            fileToRename = file
                            renameInputText = file.name
                        },
                        onDeleteClick = {
                            fileToDelete = file
                        }
                    )
                }
            }
        }
    }

    // Rename File Dialog
    fileToRename?.let { targetFile ->
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("New File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            fileViewModel.updateFileName(targetFile.id, renameInputText.trim())
                            fileToRename = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Single Delete Confirmation Dialog
    fileToDelete?.let { targetFile ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete '${targetFile.name}'?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        fileViewModel.deleteFile(targetFile)
                        fileToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bulk Delete Confirmation Dialog
    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Delete ${selectedFileIds.size} Files") },
            text = { Text("Are you sure you want to delete all selected files?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        fileViewModel.deleteFilesByIds(selectedFileIds.toList())
                        selectedFileIds = emptySet()
                        isSelectionMode = false
                        showBulkDeleteDialog = false
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    file: FileEntity,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (!isSelectionMode) {
                // Edit / Rename Button
                IconButton(onClick = onRenameClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete Button
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
>>>>>>> origin/main
}