package com.example.codevaultide.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codevaultide.editor.EditorViewModel

data class FileVersion(
    val id: Long,
    val timestamp: String,
    val summary: String,
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    editorViewModel: EditorViewModel,
    onBackClick: () -> Unit = {},
    onCompareClick: (selectedContent: String) -> Unit = {}
) {
    val context = LocalContext.current
    val activeFileName by editorViewModel.fileName.collectAsState()

    val versionHistory by editorViewModel.versionHistory.collectAsState(initial = emptyList<FileVersion>())
    var selectedVersionForPreview by remember { mutableStateOf<FileVersion?>(null) }

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
                title = {
                    Column {
                        Text("Version History", fontWeight = FontWeight.Bold)
                        Text(
                            text = activeFileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (versionHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No version history available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(versionHistory, key = { version -> version.id }) { version: FileVersion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVersionForPreview = version },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = version.summary, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = version.timestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = { onCompareClick(version.content) }) {
                                    Icon(
                                        imageVector = Icons.Default.Compare,
                                        contentDescription = "Compare Version",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        editorViewModel.rollbackToFileVersion(version.content)
                                        Toast.makeText(
                                            context,
                                            "Restored to version from ${version.timestamp}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onBackClick()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "Restore Version",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedVersionForPreview?.let { version ->
        AlertDialog(
            onDismissRequest = { selectedVersionForPreview = null },
            title = {
                Column {
                    Text(text = version.summary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = version.timestamp,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .background(Color(0xFF10131A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = version.content,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFFE6EDF3)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editorViewModel.rollbackToFileVersion(version.content)
                        selectedVersionForPreview = null
                        Toast.makeText(context, "Restored to ${version.timestamp}", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                ) {
                    Text("Restore This Version")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedVersionForPreview = null }) {
                    Text("Close")
                }
            }
        )
    }
}