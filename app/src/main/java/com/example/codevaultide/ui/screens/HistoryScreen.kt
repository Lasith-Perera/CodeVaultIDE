<<<<<<< HEAD
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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
import com.example.codevaultide.database.VersionEntity
import com.example.codevaultide.database.VersionWithFile
import com.example.codevaultide.editor.EditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Visibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    editorViewModel: EditorViewModel,
    onBackClick: () -> Unit = {},
    onCompareClick: (selectedContent: String) -> Unit = {}
) {
    val context = LocalContext.current
    val activeFileName by editorViewModel.fileName.collectAsState()
    val activeFileId by editorViewModel.fileId.collectAsState()
    val versionHistory by editorViewModel.versionHistory.collectAsState()
    val allVersionHistory by editorViewModel.allVersionHistory.collectAsState()
    var selectedVersion by remember { mutableStateOf<VersionEntity?>(null) }
    val selectedContent by editorViewModel.selectedVersionContent.collectAsState()

    // Drill-down state: lets you browse the history of a file other than the
    // one currently open in the editor, starting from the "All files" list.
    var drillDownFileId by remember { mutableStateOf<Long?>(null) }
    var drillDownFileName by remember { mutableStateOf<String?>(null) }

    val effectiveFileId = activeFileId ?: drillDownFileId
    val effectiveFileName = if (activeFileId != null) activeFileName else drillDownFileName

    val uniqueFiles = remember(allVersionHistory) {
        allVersionHistory.distinctBy { it.fileId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeFileId == null && drillDownFileId != null) {
                            drillDownFileId = null
                            drillDownFileName = null
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text("Version History", fontWeight = FontWeight.Bold)
                        Text(
                            if (effectiveFileId == null) "All files" else (effectiveFileName ?: "Unknown file"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (effectiveFileId == null) {
            // Top level: list of files that have any version history.
            if (uniqueFiles.isEmpty()) {
                EmptyHistory(Modifier.fillMaxSize().padding(padding))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uniqueFiles, key = { it.fileId }) { version ->
                        FileHistoryCard(
                            fileName = version.fileName
                                ?: version.storedFileName?.let { "Deleted file ($it)" }
                                ?: "Deleted file (${version.fileId})",
                            onClick = {
                                drillDownFileId = version.fileId
                                drillDownFileName = version.fileName
                                    ?: version.storedFileName
                                    ?: "Deleted file"
                            },
                            onDelete = { editorViewModel.deleteVersionHistory(version.fileId) }
                        )
                    }
                }
            }
        } else {
            // Drilled into one file's history. If it's the file currently open
            // in the editor, use the live per-file flow; otherwise fall back to
            // the global flow filtered to that file id.
            val historyToShow: List<VersionEntity> =
                if (activeFileId != null && activeFileId == effectiveFileId) {
                    versionHistory
                } else {
                    allVersionHistory.filter { it.fileId == effectiveFileId }.map { it.toVersionEntity() }
                }

            if (historyToShow.isEmpty()) {
                // Everything for this file was deleted while browsing it - bounce back.
                LaunchedEffect(effectiveFileId) {
                    if (drillDownFileId != null) {
                        drillDownFileId = null
                        drillDownFileName = null
                    }
                }
                EmptyHistory(Modifier.fillMaxSize().padding(padding))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyToShow, key = { it.id }) { version ->
                        VersionCard(
                            version = version,
                            onPreview = {
                                editorViewModel.loadVersionContent(version.id)
                                selectedVersion = version
                            },
                            onCompare = { editorViewModel.getVersionContent(version.id) { it?.let(onCompareClick) } },
                            onDelete = { editorViewModel.deleteVersion(version.id) }
                        )
                    }
                }
            }
        }
    }

    selectedVersion?.let { version ->
        AlertDialog(
            onDismissRequest = { selectedVersion = null },
            title = {
                Column {
                    Text("v${version.versionNumber}  ${version.description}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (version.storageType == VersionEntity.STORAGE_BASE) "BASE SNAPSHOT" else "INCREMENTAL DELTA",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(formatTimestamp(version.timestamp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Box(
                    Modifier.fillMaxWidth().heightIn(max = 300.dp)
                        .background(Color(0xFF10131A), RoundedCornerShape(8.dp)).padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(selectedContent ?: "Reconstructing version…", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFE6EDF3))
                }
            },
            confirmButton = {
                Button(onClick = {
                    editorViewModel.rollbackToFileVersion(version)
                    selectedVersion = null
                    Toast.makeText(context, "Restored v${version.versionNumber}", Toast.LENGTH_SHORT).show()
                    onBackClick()
                }) { Text("Restore This Version") }
            },
            dismissButton = {
                TextButton(onClick = { selectedVersion = null }) { Text("Close") }
            }
        )
    }
}

/** Maps the joined global-history row back into a [VersionEntity] for reuse by [VersionCard]. */
private fun VersionWithFile.toVersionEntity() = VersionEntity(
    id = id,
    fileId = fileId,
    versionNumber = versionNumber,
    parentVersionId = parentVersionId,
    branchName = branchName,
    snapshotContent = snapshotContent,
    content = content,
    deltaPatch = deltaPatch,
    storageType = storageType,
    description = description,
    storedFileName = storedFileName,
    timestamp = timestamp
)

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("No version history available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("Save a file to create its first version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FileHistoryCard(fileName: String, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete History", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun VersionCard(version: VersionEntity, onPreview: () -> Unit, onCompare: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onPreview), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("v${version.versionNumber}  ${version.description}", fontWeight = FontWeight.Bold)
                Text("${formatTimestamp(version.timestamp)} • ${version.branchName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Compare, "Compare Version", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onPreview) { Icon(Icons.Default.Visibility, "View Details", tint = MaterialTheme.colorScheme.secondary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete Version", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
=======
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
>>>>>>> origin/main
