package com.example.codevaultide.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onCompareClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeFileName by editorViewModel.fileName.collectAsState()

    val dummyVersions = listOf(
        FileVersion(1, "10 mins ago", "Added main function", "// Current Version\nfun main() {\n    println(\"Hello World\")\n}"),
        FileVersion(2, "1 hour ago", "Updated logic", "// Version 2\nfun main() {\n    // Initial setup\n}"),
        FileVersion(3, "Yesterday", "Initial Commit", "// Version 1\n")
    )

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
                        Text(activeFileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyVersions) { version ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(version.summary, fontWeight = FontWeight.Bold)
                                Text(version.timestamp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(
                            onClick = {
                                editorViewModel.rollbackToFileVersion(version.content)
                                Toast.makeText(context, "Restored to ${version.timestamp}", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            }
                        ) {
                            Icon(
                                Icons.Default.Restore,
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