package com.example.codevaultide.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codevaultide.ai.GeminiAgentManager
import com.example.codevaultide.database.FileEntity
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.editor.FileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    fileViewModel: FileViewModel,
    onNewFileClick: (String) -> Unit = {},
    onOpenFileClick: () -> Unit = {},
    onRunClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRecentFileClick: (FileEntity) -> Unit = {}
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
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
                    IconButton(onClick = { showAiSheet = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
                    }
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                        description = "Start a fresh code project with standard compilers",
                        icon = Icons.Default.Add,
                        onClick = { showNewFileDialog = true }
                    )
                    ActionCard(
                        title = "Open Existing File",
                        description = "Browse and edit your local workspace",
                        icon = Icons.Default.FolderOpen,
                        onClick = onOpenFileClick
                    )
                }
            }

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
                    )
                }
            }

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
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AiAssistantSheetContent(
                onClose = { showAiSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAssistantSheetContent(
    editorViewModel: EditorViewModel? = null,
    fileViewModel: FileViewModel? = null,
    onClose: () -> Unit = {},
    onOpenInEditor: (fileName: String, code: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val currentCode = editorViewModel?.code?.collectAsState()?.value ?: ""
    val activeFileName = editorViewModel?.fileName?.collectAsState()?.value ?: "Main.kt"

    var queryText by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("How can I help you fix or generate code today?") }

    var isLoading by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf("Idle") }

    val coroutineScope = rememberCoroutineScope()
    var activeJob by remember { mutableStateOf<Job?>(null) }
    val responseScrollState = rememberScrollState()

    // Auto-scroll response container as AI streams tokens
    LaunchedEffect(aiResponse) {
        responseScrollState.animateScrollTo(responseScrollState.maxValue)
    }

    val executeAiTask: (String, String) -> Unit = { actionType, prompt ->
        queryText = ""
        isLoading = true
        aiResponse = ""
        currentStatus = "Connecting to Gemini..."

        activeJob = coroutineScope.launch {
            try {
                if (GeminiAgentManager.apiKey.isBlank()) {
                    aiResponse = "Gemini API key is not configured. Please add your key in Settings."
                } else {
                    currentStatus = "Streaming response..."
                    val fullPrompt = if (actionType == "FIX") "Fix all bugs and compile errors in $activeFileName" else prompt

                    GeminiAgentManager.generateCodeResponseStream(fullPrompt, currentCode)
                        .collect { chunk ->
                            aiResponse += chunk.text ?: ""
                        }
                }
            } catch (e: Exception) {
                aiResponse = "Error: ${e.localizedMessage ?: "Failed to generate AI response."}"
            } finally {
                isLoading = false
                currentStatus = "Idle"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        // Header
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
                    text = "Code Fix & Generator Agent",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Middle Scrollable Section
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { executeAiTask("FIX", "Fix current file bugs") },
                    label = { Text("Fix Active File") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    enabled = !isLoading
                )
                AssistChip(
                    onClick = { executeAiTask("GENERATE", "generateFunction") },
                    label = { Text("Generate Code") },
                    leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(bottom = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 160.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = aiResponse,
                        modifier = Modifier
                            .padding(12.dp)
                            .padding(end = 40.dp)
                            .verticalScroll(responseScrollState),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val cleanCode = GeminiAgentManager.cleanCodeOutput(aiResponse)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("CodeVault AI Response", cleanCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied clean code to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && aiResponse.isNotBlank() && editorViewModel != null,
                    onClick = {
                        val codeToApply = GeminiAgentManager.cleanCodeOutput(aiResponse)
                        editorViewModel?.applyAiGeneratedCode(codeToApply)
                        Toast.makeText(context, "Applied Code to Editor!", Toast.LENGTH_SHORT).show()
                        onClose()
                    }
                ) {
                    Icon(Icons.Default.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Apply Code")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && aiResponse.isNotBlank() && editorViewModel != null,
                    onClick = {
                        val codeToInsert = GeminiAgentManager.cleanCodeOutput(aiResponse)
                        editorViewModel?.insertAiCodeAtCursor(codeToInsert)
                        Toast.makeText(context, "Inserted at Cursor!", Toast.LENGTH_SHORT).show()
                        onClose()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("At Cursor")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fixed Input Row pinned at bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("Describe code to fix or generate...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (queryText.isNotBlank()) {
                        val input = queryText
                        val type = if (input.contains("fix", ignoreCase = true)) "FIX" else "GENERATE"
                        executeAiTask(type, input)
                    }
                },
                enabled = !isLoading && queryText.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (!isLoading && queryText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
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
                    "$fileType • $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
