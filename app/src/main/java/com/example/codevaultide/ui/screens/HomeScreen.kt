package com.example.codevaultide.ui.screens

<<<<<<< HEAD
import androidx.compose.foundation.background
=======
>>>>>>> origin/main
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
<<<<<<< HEAD
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class RecentFile(
    val name: String,
    val type: String,
    val time: String,
    val versions: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewFileClick: () -> Unit = {},
    onOpenFileClick: () -> Unit = {},
    onRunClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRecentFileClick: (RecentFile) -> Unit = {}
) {
    val recentFiles = listOf(
        RecentFile("Main.kt", "Kotlin Source", "2 mins ago", 12),
        RecentFile("build.gradle.kts", "Gradle Script", "1 hour ago", 5),
        RecentFile("FileManager.kt", "Kotlin Source", "Yesterday", 8)
    )
=======
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.codevaultide.R
import com.example.codevaultide.database.FileEntity
import com.example.codevaultide.editor.FileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    fileViewModel: FileViewModel,
    onNewFileClick: (String) -> Unit = {},
    onOpenFileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRecentFileClick: (FileEntity) -> Unit = {}
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    // List of supported file extensions
    val supportedExtensions = remember {
        listOf(
            ".c", ".cpp", ".java", ".py", ".js", ".ts",
            ".rs", ".kt", ".cs", ".html", ".txt"
        )
    }
    var selectedExtension by remember { mutableStateOf(supportedExtensions.first()) }

    val recentFiles by fileViewModel.allFiles.collectAsState(initial = emptyList())

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewFileDialog = false
                newFileName = ""
            },
            title = { Text("Create New File") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name") },
                        placeholder = { Text("e.g. main") },
                        trailingIcon = {
                            Text(
                                text = selectedExtension,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select File Extension:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ContextualFlowRow(
                        itemCount = supportedExtensions.size,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { index ->
                        val ext = supportedExtensions[index]
                        FilterChip(
                            selected = (selectedExtension == ext),
                            onClick = { selectedExtension = ext },
                            label = {
                                Text(
                                    text = ext,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            val baseName = newFileName.trim().substringBeforeLast(".")
                            val finalFileName = "$baseName$selectedExtension"
                            onNewFileClick(finalFileName)
                            showNewFileDialog = false
                            newFileName = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewFileDialog = false
                        newFileName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
>>>>>>> origin/main

    Scaffold(
        topBar = {
            TopAppBar(
<<<<<<< HEAD
=======
                windowInsets = WindowInsets(0, 0, 0, 0),
>>>>>>> origin/main
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CodeVault IDE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
<<<<<<< HEAD
                Spacer(modifier = Modifier.height(8.dp))
=======
                Spacer(modifier = Modifier.height(16.dp))
>>>>>>> origin/main
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
<<<<<<< HEAD
=======
                Spacer(modifier = Modifier.height(4.dp))
>>>>>>> origin/main
                Text(
                    text = "Local version control & professional editing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "Create New File",
<<<<<<< HEAD
                        description = "Start a fresh Kotlin or Markdown project",
                        icon = Icons.Default.Add,
                        onClick = onNewFileClick
=======
                        description = "Start a fresh code project with standard compilers",
                        icon = Icons.Default.Add,
                        onClick = { showNewFileDialog = true }
>>>>>>> origin/main
                    )
                    ActionCard(
                        title = "Open Existing File",
                        description = "Browse and edit your local workspace",
                        icon = Icons.Default.FolderOpen,
                        onClick = onOpenFileClick
                    )
                }
            }

<<<<<<< HEAD
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickButton(
                        modifier = Modifier.weight(1f),
                        text = "Run Code",
                        icon = Icons.Default.PlayArrow,
                        click = onRunClick
                    )
                    QuickButton(
                        modifier = Modifier.weight(1f),
                        text = "History",
                        icon = Icons.Default.History,
                        click = onHistoryClick
=======
            if (recentFiles.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Files",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(recentFiles) { file ->
                    RecentFileCard(
                        file = file,
                        onClick = { onRecentFileClick(file) }
>>>>>>> origin/main
                    )
                }
            }

            item {
<<<<<<< HEAD
                Text(
                    text = "Recent Files",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(recentFiles) { file ->
                RecentFileCard(
                    file = file,
                    onClick = { onRecentFileClick(file) }
                )
            }
            
            item {
=======
>>>>>>> origin/main
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
<<<<<<< HEAD
=======

    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            AiAssistantSheetContent(onClose = { showAiSheet = false })
        }
    }
}

@Composable
fun AiAssistantSheetContent(onClose: () -> Unit) {
    var queryText by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("How can I help you with your code today?") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CodeVault AI Assistant",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = aiResponse,
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("Ask AI to generate or fix code...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (queryText.isNotBlank()) {
                        aiResponse = "Analyzing code request for: \"$queryText\"..."
                        queryText = ""
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
>>>>>>> origin/main
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp).size(28.dp)
                )
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
<<<<<<< HEAD
fun QuickButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    click: () -> Unit
) {
    ElevatedButton(
        onClick = click,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun RecentFileCard(
    file: RecentFile,
    onClick: () -> Unit = {}
) {
=======
fun RecentFileCard(
    file: FileEntity,
    onClick: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val formattedDate = sdf.format(Date(file.lastModified))
    val fileType = when {
        file.name.endsWith(".c") -> "C Source"
        file.name.endsWith(".cpp") -> "C++ Source"
        file.name.endsWith(".java") -> "Java Source"
        file.name.endsWith(".py") -> "Python Source"
        file.name.endsWith(".js") -> "JavaScript"
        file.name.endsWith(".ts") -> "TypeScript"
        file.name.endsWith(".rs") -> "Rust Source"
        file.name.endsWith(".kt") -> "Kotlin Source"
        file.name.endsWith(".cs") -> "C# Source"
        file.name.endsWith(".html") -> "HTML Document"
        file.name.endsWith(".txt") -> "Text Document"
        else -> "Source File"
    }

>>>>>>> origin/main
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
<<<<<<< HEAD
            1.dp, 
=======
            1.dp,
>>>>>>> origin/main
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
<<<<<<< HEAD
                    "${file.type} • ${file.time}",
=======
                    "$fileType • $formattedDate",
>>>>>>> origin/main
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
<<<<<<< HEAD
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "v${file.versions}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
=======
        }
    }
}
>>>>>>> origin/main
