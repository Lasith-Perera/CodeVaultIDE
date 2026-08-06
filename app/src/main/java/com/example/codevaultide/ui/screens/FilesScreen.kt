package com.example.codevaultide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codevaultide.database.FileEntity
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.editor.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onBackClick: () -> Unit = {},
    onFileClick: () -> Unit = {},
    editorViewModel: EditorViewModel,
    fileViewModel: FileViewModel
) {
    // Database එකෙන් සියලුම Files auto-load කරගැනීම
    val filesList by fileViewModel.allFiles.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Workspace Files", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (filesList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filesList) { file ->
                    FileItem(name = file.name) {
                        editorViewModel.setCode(file.content)
                        onFileClick()
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(
    name: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}