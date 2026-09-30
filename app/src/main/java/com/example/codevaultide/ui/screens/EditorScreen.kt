<<<<<<< HEAD
package com.example.codevaultide.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codevaultide.compiler.CompilerManager
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.editor.FileViewModel
import com.example.codevaultide.editor.HtmlPreview
import com.example.codevaultide.editor.MarkdownPreview
import com.example.codevaultide.editor.SyntaxRules
import com.example.codevaultide.ui.settings.SettingsViewModel
import com.example.codevaultide.ui.components.EdgePanelHandle
import com.example.codevaultide.ui.components.RecentFilesEdgePanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import com.example.codevaultide.editor.Snapshot
import com.example.codevaultide.editor.SnapshotManager

private fun languageForFile(name: String): String =
    when (name.substringAfterLast('.', "").lowercase()) {

        "kt", "kts" -> "kotlin"
        "py" -> "python"
        "cpp", "cc", "cxx" -> "cpp"
        "c" -> "c"
        "java" -> "java"
        "js" -> "javascript"
        "ts" -> "typescript"
        "go" -> "go"
        "rs" -> "rust"
        "swift" -> "swift"
        "php" -> "php"
        "rb" -> "ruby"
        "cs" -> "c#"

        else -> "cpp"
    }

private fun isHtmlFile(fileName: String): Boolean {
    return fileName
        .substringAfterLast('.', "")
        .equals("html", ignoreCase = true)
}

private fun isMarkdownFile(fileName: String): Boolean {
    return fileName.endsWith(".md", true) ||
            fileName.endsWith(".markdown", true)
}

private fun isRunnableCodeFile(fileName: String): Boolean {
    val extension = fileName
        .substringAfterLast('.', "")
        .lowercase()

    return extension in setOf(
        "py",
        "cpp", "cc", "cxx", "c",
        "java",
        "kt", "kts",
        "js", "ts",
        "go",
        "rs",
        "swift",
        "php",
        "rb",
        "cs",
        "sh", "bash",
        "lua",
        "pl",
        "scala",
        "dart",
        "groovy"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    fileViewModel: FileViewModel,
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {

    val context = LocalContext.current

    val activeFileName by viewModel.fileName.collectAsState()
    val activeFileId by viewModel.fileId.collectAsState()
    val code by viewModel.code.collectAsState()
    val recentFiles by fileViewModel.allFiles.collectAsState()

    var showRecentFilesPanel by remember {
        mutableStateOf(false)
    }

    var showRecoveryDialog by remember { mutableStateOf(false) }
    var cachedContent by remember { mutableStateOf("") }

    LaunchedEffect(activeFileId) {
        val cacheFile = java.io.File(
            context.cacheDir,
            "editor_cache_${activeFileId ?: "new"}.tmp"
        )

        if (cacheFile.exists()) {
            val content = runCatching { cacheFile.readText() }.getOrDefault("")

            if (content.isNotEmpty() && content != viewModel.code.value) {
                cachedContent = content
                showRecoveryDialog = true
            }
        }
    }

    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text("Unsaved Changes Found") },
            text = { Text("It looks like the app closed unexpectedly. Would you like to restore your unsaved work?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.setCode(cachedContent)

                    // Remove the old crash marker after it has been restored.
                    // The periodic cache task will create a fresh cache again
                    // if the restored text remains unsaved.
                    val cacheFile = java.io.File(
                        context.cacheDir,
                        "editor_cache_${activeFileId ?: "new"}.tmp"
                    )
                    runCatching { cacheFile.delete() }

                    showRecoveryDialog = false
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val cacheFile = java.io.File(context.cacheDir, "editor_cache_${activeFileId ?: "new"}.tmp")
                    cacheFile.delete()
                    showRecoveryDialog = false
                }) { Text("Discard") }
            }
        )
    }

    val fontSize by settingsViewModel.fontSize.collectAsState()
    val isAutoSaveEnabled by settingsViewModel.isAutoSaveEnabled.collectAsState()
    val isVersionBackupEnabled by settingsViewModel.isVersionBackupEnabled.collectAsState()
    val encoding by settingsViewModel.encoding.collectAsState()
    val isReadOnly by viewModel.isReadOnly.collectAsState()

    val compilerManager = remember {
        CompilerManager()
    }

    val scope = rememberCoroutineScope()

    val clipboardManager = remember {
        context.getSystemService(
            Context.CLIPBOARD_SERVICE
        ) as ClipboardManager
    }

    val languageName = remember(activeFileName) {

        when {

            activeFileName.endsWith(".py", true) ->
                "Python 3"

            activeFileName.endsWith(".cpp", true) ||
                    activeFileName.endsWith(".c", true) ->
                "C++"

            activeFileName.endsWith(".java", true) ->
                "Java"

            activeFileName.endsWith(".kt", true) ->
                "Kotlin"

            activeFileName.endsWith(".js", true) ->
                "JavaScript"

            activeFileName.endsWith(".html", true) ->
                "HTML"

            activeFileName.endsWith(".md", true) ||
                    activeFileName.endsWith(".markdown", true) ->
                "Markdown"

            else ->
                "Plain Text"
        }
    }

    val htmlFile = remember(activeFileName) {
        isHtmlFile(activeFileName)
    }

    val markdownFile = remember(activeFileName) {
        isMarkdownFile(activeFileName)
    }

    val runnableCodeFile = remember(activeFileName) {
        isRunnableCodeFile(activeFileName)
    }

    var editorValue by remember(activeFileId) {

        val initialCode = viewModel.code.value

        mutableStateOf(
            TextFieldValue(
                text = initialCode,
                selection = TextRange(initialCode.length)
            )
        )
    }

    var showSearchDialog by remember {
        mutableStateOf(false)
    }

    var showSaveAsDialog by remember {
        mutableStateOf(false)
    }

    var showTerminalSheet by remember {
        mutableStateOf(false)
    }

    var showMenuDropdown by remember {
        mutableStateOf(false)
    }

    /*
     * Preview states
     */
    var isMarkdownPreviewEnabled by remember {
        mutableStateOf(false)
    }

    var isHtmlPreviewEnabled by remember {
        mutableStateOf(false)
    }

    var isSplitViewEnabled by remember {
        mutableStateOf(false)
    }

    var isWordWrapEnabled by remember {
        mutableStateOf(false)
    }

    var terminalOutput by remember {
        mutableStateOf("")
    }

    var stdinInput by remember {
        mutableStateOf("")
    }

    var isExecuting by remember {
        mutableStateOf(false)
    }

    var executionTimeMs by remember {
        mutableStateOf<Long?>(null)
    }

    /*
     * Automatically close previews
     * when changing file type.
     */
    LaunchedEffect(
        htmlFile,
        markdownFile,
        runnableCodeFile
    ) {

        if (!htmlFile) {
            isHtmlPreviewEnabled = false
        }

        if (!markdownFile) {
            isMarkdownPreviewEnabled = false
        }

        if (!runnableCodeFile) {
            showTerminalSheet = false
            terminalOutput = ""
            stdinInput = ""
        }
    }

    /*
     * Import
     */
    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            uri?.let {

                try {

                    val charset =
                        try {
                            java.nio.charset.Charset.forName(encoding)
                        } catch (e: Exception) {
                            Charsets.UTF_8
                        }

                    context.contentResolver
                        .openInputStream(it)
                        ?.use { inputStream ->

                            val importedText =
                                inputStream
                                    .bufferedReader(charset)
                                    .use { reader ->
                                        reader.readText()
                                    }

                            editorValue =
                                TextFieldValue(
                                    importedText,
                                    selection =
                                        TextRange(
                                            importedText.length
                                        )
                                )

                            viewModel.updateCode(
                                importedText
                            )

                            Toast.makeText(
                                context,
                                "File imported successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } catch (e: Exception) {

                    Toast.makeText(
                        context,
                        "Failed to import file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    /*
     * Export
     */
    val exportLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.CreateDocument(
                    "text/plain"
                )
        ) { uri ->

            uri?.let {

                try {

                    val charset =
                        try {
                            java.nio.charset.Charset.forName(encoding)
                        } catch (e: Exception) {
                            Charsets.UTF_8
                        }

                    context.contentResolver
                        .openOutputStream(it)
                        ?.use { outputStream ->

                            outputStream.write(
                                editorValue.text.toByteArray(charset)
                            )

                            Toast.makeText(
                                context,
                                "File exported successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } catch (e: Exception) {

                    Toast.makeText(
                        context,
                        "Failed to export file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    /*
     * Keep editor synchronized with ViewModel - but ONLY for changes
     * that originate OUTSIDE the text field itself (undo, redo,
     * rollback, loading a file). Those bump externalRevision.
     */
    val externalRevision by viewModel.externalRevision.collectAsState()
    var lastSyncedRevision by remember { mutableStateOf(-1) }

    if (lastSyncedRevision != externalRevision) {
        editorValue = TextFieldValue(
            text = code,
            selection = TextRange(code.length)
        )
        lastSyncedRevision = externalRevision
    }

    val lineCount =
        editorValue.text
            .lines()
            .size
            .coerceAtLeast(1)

    val wordCount =
        editorValue.text
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .size

    val characterCount =
        editorValue.text.length

    fun handleCopy() {

        val selectedText =
            if (editorValue.selection.collapsed) {
                editorValue.text
            } else {
                editorValue.text.substring(
                    editorValue.selection.min,
                    editorValue.selection.max
                )
            }

        if (selectedText.isNotEmpty()) {

            val clip =
                ClipData.newPlainText(
                    "Copied Code",
                    selectedText
                )

            clipboardManager.setPrimaryClip(clip)

            Toast.makeText(
                context,
                "Copied to clipboard",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun handleCut() {

        if (!editorValue.selection.collapsed) {

            viewModel.takeSnapshot() // Checkpoint before cut

            val start =
                editorValue.selection.min

            val end =
                editorValue.selection.max

            val clip =
                ClipData.newPlainText(
                    "Cut Code",
                    editorValue.text.substring(
                        start,
                        end
                    )
                )

            clipboardManager.setPrimaryClip(clip)

            val newText =
                editorValue.text.removeRange(
                    start,
                    end
                )

            editorValue =
                TextFieldValue(
                    newText,
                    selection = TextRange(start)
                )

            viewModel.updateCode(newText)
        }
    }

    fun handlePaste() {

        val clipData =
            clipboardManager.primaryClip

        if (
            clipData != null &&
            clipData.itemCount > 0
        ) {

            val pasteText =
                clipData
                    .getItemAt(0)
                    .text
                    ?.toString()
                    ?: ""

            if (pasteText.isNotEmpty()) {

                viewModel.takeSnapshot() // Checkpoint before paste

                val start =
                    editorValue.selection.min

                val end =
                    editorValue.selection.max

                val newText =
                    editorValue.text.replaceRange(
                        start,
                        end,
                        pasteText
                    )

                val newCursorPos =
                    start + pasteText.length

                editorValue =
                    TextFieldValue(
                        newText,
                        selection =
                            TextRange(
                                newCursorPos
                            )
                    )

                viewModel.updateCode(newText)
            }
        }
    }

    fun performSave(
        showToast: Boolean = true
    ) {

        val currentText =
            editorValue.text

        viewModel.takeSnapshot() // Ensure current state is captured
        viewModel.updateCode(currentText)

        val idToSave =
            activeFileId
                ?: System.currentTimeMillis()

        if (activeFileId == null) {
            viewModel.setFileId(idToSave)
        }

        fileViewModel.updateFile(
            idToSave,
            activeFileName,
            currentText
        )

        if (isVersionBackupEnabled) {

            val summary =
                if (showToast)
                    "Manual Save"
                else
                    "Auto Save"

            viewModel.saveVersionSnapshot(
                summary = summary
            )

            SnapshotManager.add(
                Snapshot(
                    id = System.currentTimeMillis(),
                    fileId = idToSave,
                    fileName = activeFileName,
                    content = currentText,
                    message = summary,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        if (showToast) {

            Toast.makeText(
                context,
                "$activeFileName saved successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(
        editorValue.text,
        isAutoSaveEnabled
    ) {

        if (isAutoSaveEnabled) {

            delay(2000)

            performSave(
                showToast = false
            )
        }
    }

    // Crash prevention: periodically cache the in-progress buffer to disk,
    // independent of manual/auto save, so an unsaved edit survives a crash.
    LaunchedEffect(activeFileId) {
        while (true) {
            delay(10000) // 10 seconds
            if (editorValue.text.isNotEmpty()) {
                try {
                    val cacheFile = java.io.File(
                        context.cacheDir,
                        "editor_cache_${activeFileId ?: "new"}.tmp"
                    )
                    cacheFile.writeText(editorValue.text)
                } catch (e: Exception) {
                    // Ignore cache errors - this is a best-effort safety net.
                }
            }
        }
    }

    fun formatInteractiveTerminalOutput(
        rawOutput: String,
        stdin: String
    ): String {

        if (
            stdin.isBlank() ||
            rawOutput.contains("ERROR")
        ) {
            return rawOutput
        }

        val inputLines =
            stdin.lines()
                .filter {
                    it.isNotBlank()
                }

        if (inputLines.isEmpty()) {
            return rawOutput
        }

        val promptRegex =
            Regex("""([a-zA-Z0-9_\s]*[:?]\s*)""")

        val matches =
            promptRegex
                .findAll(rawOutput)
                .toList()

        if (matches.isNotEmpty()) {

            val sb =
                StringBuilder()

            var lastIndex = 0

            matches.forEachIndexed { index, match ->

                if (index < inputLines.size) {

                    sb.append(
                        rawOutput.substring(
                            lastIndex,
                            match.range.last + 1
                        )
                    )

                    sb.append(
                        inputLines[index]
                    )

                    sb.append("\n")

                    lastIndex =
                        match.range.last + 1
                }
            }

            if (lastIndex < rawOutput.length) {

                sb.append(
                    rawOutput.substring(
                        lastIndex
                    )
                )
            }

            return sb.toString().trim()
        }

        return rawOutput
    }

    fun formatCode() {

        val currentTextToFormat = editorValue.text

        if (currentTextToFormat.isBlank()) {
            return
        }

        viewModel.takeSnapshot() // Checkpoint before reformatting

        // Simple indentation-based formatter.
        val lines = currentTextToFormat.lines()
        val formatted = StringBuilder()
        var indentLevel = 0
        val indentSize = 4

        lines.forEach { line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("}")) {
                indentLevel = (indentLevel - 1).coerceAtLeast(0)
            }

            formatted
                .append(" ".repeat(indentLevel * indentSize))
                .append(trimmed)
                .append("\n")

            if (trimmed.endsWith("{") || trimmed.endsWith("->")) {
                indentLevel++
            }
        }

        val newText = formatted.toString().trimEnd()

        editorValue = editorValue.copy(text = newText)
        viewModel.updateCode(newText)

        Toast.makeText(context, "Code formatted", Toast.LENGTH_SHORT).show()
    }

    fun runCodeExecution() {

        if (!runnableCodeFile) {
            return
        }

        performSave(false)

        showTerminalSheet = true
        isExecuting = true
        terminalOutput = ""

        val startTime =
            System.currentTimeMillis()

        scope.launch {

            val language =
                languageForFile(
                    activeFileName
                )

            val rawResult =
                compilerManager.compileAndRun(
                    language = language,
                    code = editorValue.text,
                    stdin = stdinInput
                )

            terminalOutput =
                formatInteractiveTerminalOutput(
                    rawResult,
                    stdinInput
                )

            executionTimeMs =
                System.currentTimeMillis() -
                        startTime

            isExecuting = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    windowInsets =
                        WindowInsets(
                            0,
                            0,
                            0,
                            0
                        ),

                    navigationIcon = {

                        IconButton(
                            onClick = onBackClick
                        ) {

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },

                    title = {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Code,
                                contentDescription = "Language",
                                modifier =
                                    Modifier.size(16.dp),
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )

                            Spacer(
                                Modifier.width(6.dp)
                            )

                            Column {

                                Text(
                                    text = activeFileName,
                                    fontSize = 15.sp,
                                    fontWeight =
                                        FontWeight.Bold,
                                    maxLines = 1,
                                    overflow =
                                        TextOverflow.Ellipsis
                                )

                                Text(
                                    text = languageName,
                                    fontSize = 10.sp,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
                        }
                    },

                    actions = {

                        /*
                         * ==========================================
                         * MARKDOWN PREVIEW TOP BAR BUTTON
                         * ==========================================
                         *
                         * Visible only for:
                         * .md
                         * .markdown
                         */
                        if (markdownFile) {

                            IconButton(
                                onClick = {

                                    isMarkdownPreviewEnabled =
                                        !isMarkdownPreviewEnabled

                                    if (isMarkdownPreviewEnabled) {

                                        isHtmlPreviewEnabled =
                                            false

                                        isSplitViewEnabled =
                                            false
                                    }
                                }
                            ) {

                                Icon(

                                    imageVector =
                                        if (
                                            isMarkdownPreviewEnabled
                                        )
                                            Icons.Default.Code
                                        else
                                            Icons.Default.Visibility,

                                    contentDescription =
                                        if (
                                            isMarkdownPreviewEnabled
                                        )
                                            "Markdown Editor"
                                        else
                                            "Markdown Preview",

                                    tint =
                                        if (
                                            isMarkdownPreviewEnabled
                                        )
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                        else
                                            MaterialTheme
                                                .colorScheme
                                                .onSurface
                                )
                            }
                        }

                        /*
                         * ==========================================
                         * HTML PREVIEW TOP BAR BUTTON
                         * ==========================================
                         *
                         * Visible only for .html
                         */
                        if (htmlFile) {

                            IconButton(
                                onClick = {

                                    isHtmlPreviewEnabled =
                                        !isHtmlPreviewEnabled

                                    if (isHtmlPreviewEnabled) {

                                        isMarkdownPreviewEnabled =
                                            false

                                        isSplitViewEnabled =
                                            false
                                    }
                                }
                            ) {

                                Icon(

                                    imageVector =
                                        if (
                                            isHtmlPreviewEnabled
                                        )
                                            Icons.Default.Code
                                        else
                                            Icons.Default.Web,

                                    contentDescription =
                                        if (
                                            isHtmlPreviewEnabled
                                        )
                                            "HTML Editor"
                                        else
                                            "HTML Preview",

                                    tint =
                                        if (
                                            isHtmlPreviewEnabled
                                        )
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                        else
                                            MaterialTheme
                                                .colorScheme
                                                .onSurface
                                )
                            }
                        }

                        /*
                         * Search
                         */
                        IconButton(
                            onClick = {
                                showSearchDialog = true
                            }
                        ) {

                            Icon(
                                Icons.Default.Search,
                                contentDescription =
                                    "Find & Replace"
                            )
                        }

                        /*
                         * More menu
                         */
                        Box {

                            IconButton(
                                onClick = {
                                    showMenuDropdown = true
                                }
                            ) {

                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription =
                                        "Menu"
                                )
                            }

                            DropdownMenu(

                                expanded =
                                    showMenuDropdown,

                                onDismissRequest = {
                                    showMenuDropdown = false
                                }

                            ) {

                                DropdownMenuItem(

                                    text = {
                                        Text("Import File")
                                    },

                                    leadingIcon = {

                                        Icon(
                                            Icons.Default.FileUpload,
                                            contentDescription = null
                                        )
                                    },

                                    onClick = {

                                        showMenuDropdown = false

                                        importLauncher.launch(
                                            arrayOf(
                                                "text/*",
                                                "text/html",
                                                "application/json",
                                                "application/javascript",
                                                "application/xml"
                                            )
                                        )
                                    }
                                )

                                DropdownMenuItem(

                                    text = {
                                        Text("Export File")
                                    },

                                    leadingIcon = {

                                        Icon(
                                            Icons.Default.FileDownload,
                                            contentDescription = null
                                        )
                                    },

                                    onClick = {

                                        showMenuDropdown = false

                                        exportLauncher.launch(
                                            activeFileName
                                        )
                                    }
                                )

                                HorizontalDivider()

                                DropdownMenuItem(

                                    text = {

                                        Text(
                                            if (isWordWrapEnabled)
                                                "Disable Word Wrap"
                                            else
                                                "Word Wrap"
                                        )
                                    },

                                    onClick = {

                                        isWordWrapEnabled =
                                            !isWordWrapEnabled

                                        showMenuDropdown = false
                                    }
                                )

                                DropdownMenuItem(

                                    text = {

                                        Text(
                                            if (isReadOnly)
                                                "Disable Read Only"
                                            else
                                                "Read Only"
                                        )
                                    },

                                    onClick = {

                                        viewModel.toggleReadOnly()

                                        showMenuDropdown = false
                                    }
                                )

                                /*
                                 * Markdown Preview is intentionally
                                 * NOT here anymore.
                                 *
                                 * It is now directly in the top bar.
                                 */

                                DropdownMenuItem(

                                    text = {

                                        Text(
                                            if (isSplitViewEnabled)
                                                "Disable Split View"
                                            else
                                                "Split View"
                                        )
                                    },

                                    onClick = {

                                        isSplitViewEnabled =
                                            !isSplitViewEnabled

                                        isMarkdownPreviewEnabled =
                                            false

                                        isHtmlPreviewEnabled =
                                            false

                                        showMenuDropdown = false
                                    }
                                )

                                DropdownMenuItem(

                                    text = {
                                        Text("Format")
                                    },

                                    onClick = {

                                        showMenuDropdown = false

                                        formatCode()
                                    }
                                )

                                HorizontalDivider()

                                DropdownMenuItem(

                                    text = {
                                        Text("Version History")
                                    },

                                    leadingIcon = {

                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null
                                        )
                                    },

                                    onClick = {

                                        showMenuDropdown = false

                                        onHistoryClick()
                                    }
                                )
                            }
                        }
                    },

                    colors =
                        TopAppBarDefaults
                            .topAppBarColors(
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surface
                            )
                )
            },

            bottomBar = {

                EditorStatusBar(
                    lineCount = lineCount,
                    wordCount = wordCount,
                    characterCount = characterCount
                )
            },

            floatingActionButton = {

                if (
                    !showTerminalSheet &&
                    runnableCodeFile &&
                    !htmlFile &&
                    !markdownFile
                ) {

                    Row(

                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp),

                        verticalAlignment =
                            Alignment.CenterVertically,

                        modifier =
                            Modifier.padding(
                                bottom = 8.dp
                            )
                    ) {

                        SmallFloatingActionButton(

                            onClick = {
                                showTerminalSheet = true
                            },

                            shape = CircleShape,

                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant,

                            contentColor =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        ) {

                            Icon(
                                Icons.Default.Terminal,
                                contentDescription =
                                    "Terminal"
                            )
                        }

                        FloatingActionButton(

                            onClick = {
                                runCodeExecution()
                            },

                            shape = CircleShape,

                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primary,

                            contentColor =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimary
                        ) {

                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription =
                                    "Run Code"
                            )
                        }
                    }
                }
            }

        ) { innerPadding ->

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
            ) {

                /*
                 * Editor toolbar
                 */
                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surfaceContainer
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            ),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            handleCut()
                        },
                        enabled = !isReadOnly
                    ) {

                        Icon(
                            Icons.Default.ContentCut,
                            contentDescription = "Cut",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            handleCopy()
                        }
                    ) {

                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            handlePaste()
                        },
                        enabled = !isReadOnly
                    ) {

                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier =
                            Modifier
                                .height(16.dp)
                                .width(1.dp)
                    )

                    IconButton(
                        onClick = {
                            viewModel.undo()
                        },
                        enabled = !isReadOnly
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.redo()
                        },
                        enabled = !isReadOnly
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier =
                            Modifier
                                .height(16.dp)
                                .width(1.dp)
                    )

                    IconButton(
                        onClick = {
                            performSave()
                        }
                    ) {

                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            showSaveAsDialog = true
                        }
                    ) {

                        Icon(
                            Icons.Default.SaveAs,
                            contentDescription = "Save As",
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }
                }

                /*
                 * Main editor / preview
                 */
                key(activeFileId) {

                    when {

                        /*
                         * HTML Preview
                         */
                        isHtmlPreviewEnabled -> {

                            HtmlPreview(
                                content = editorValue.text,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                            )
                        }

                        /*
                         * Markdown Preview
                         */
                        isMarkdownPreviewEnabled -> {

                            MarkdownPreviewArea(
                                text = editorValue.text,
                                modifier =
                                    Modifier.weight(1f)
                            )
                        }

                        /*
                         * Split View
                         */
                        isSplitViewEnabled -> {

                            Row(

                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                            ) {

                                CodeEditorArea(

                                    value = editorValue,

                                    onValueChange = { newValue ->

                                        if (!isReadOnly) {

                                            val oldText =
                                                editorValue.text
                                            val newText =
                                                newValue.text

                                            editorValue =
                                                newValue

                                            viewModel.updateCode(
                                                newText
                                            )

                                            // Checkpoint on word/line breaks
                                            // instead of every keystroke.
                                            if (newText.length > oldText.length) {
                                                val addedChar =
                                                    newText.getOrNull(
                                                        newValue.selection.start - 1
                                                    )
                                                if (
                                                    addedChar == ' ' ||
                                                    addedChar == '\n' ||
                                                    addedChar == '\t'
                                                ) {
                                                    viewModel.takeSnapshot()
                                                }
                                            }
                                        }
                                    },

                                    fontSize = fontSize,

                                    fileName = activeFileName,

                                    isWordWrap =
                                        isWordWrapEnabled,

                                    isReadOnly =
                                        isReadOnly,

                                    modifier =
                                        Modifier.weight(1f)
                                )

                                VerticalDivider()

                                when {

                                    markdownFile -> {

                                        MarkdownPreviewArea(

                                            text =
                                                editorValue.text,

                                            modifier =
                                                Modifier.weight(1f)
                                        )
                                    }

                                    htmlFile -> {

                                        HtmlPreview(
                                            content = editorValue.text,
                                            modifier =
                                                Modifier.weight(1f)
                                        )
                                    }

                                    else -> {

                                        CodeEditorArea(

                                            value = editorValue,

                                            onValueChange = {},

                                            fontSize = fontSize,

                                            fileName = activeFileName,

                                            isWordWrap =
                                                isWordWrapEnabled,

                                            isReadOnly = true,

                                            showLineNumbers = false,

                                            modifier =
                                                Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        /*
                         * Normal Editor
                         */
                        else -> {

                            CodeEditorArea(

                                value = editorValue,

                                onValueChange = { newValue ->

                                    if (!isReadOnly) {

                                        val oldText =
                                            editorValue.text
                                        val newText =
                                            newValue.text

                                        editorValue =
                                            newValue

                                        viewModel.updateCode(
                                            newText
                                        )

                                        // Checkpoint on word/line breaks
                                        // instead of every keystroke.
                                        if (newText.length > oldText.length) {
                                            val addedChar =
                                                newText.getOrNull(
                                                    newValue.selection.start - 1
                                                )
                                            if (
                                                addedChar == ' ' ||
                                                addedChar == '\n' ||
                                                addedChar == '\t'
                                            ) {
                                                viewModel.takeSnapshot()
                                            }
                                        }
                                    }
                                },

                                fontSize = fontSize,

                                fileName = activeFileName,

                                isWordWrap =
                                    isWordWrapEnabled,

                                isReadOnly =
                                    isReadOnly,

                                modifier =
                                    Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        /*
         * Terminal
         */
        if (showTerminalSheet && runnableCodeFile) {

            ModalBottomSheet(

                onDismissRequest = {
                    showTerminalSheet = false
                },

                sheetState =
                    rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    ),

                containerColor =
                    Color(0xFF0D1117)
            ) {

                Column(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                ) {

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint =
                                    Color(0xFF58A6FF),
                                modifier =
                                    Modifier.size(20.dp)
                            )

                            Spacer(
                                Modifier.width(8.dp)
                            )

                            Text(
                                text = "TERMINAL",
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    Color(0xFFC9D1D9),
                                fontSize = 14.sp,
                                fontFamily =
                                    FontFamily.Monospace
                            )

                            Spacer(
                                Modifier.width(12.dp)
                            )

                            if (isExecuting) {

                                Surface(
                                    color =
                                        Color(0xFF388BFD)
                                            .copy(alpha = 0.2f),
                                    shape =
                                        RoundedCornerShape(4.dp)
                                ) {

                                    Text(
                                        text = "RUNNING",
                                        color =
                                            Color(0xFF58A6FF),
                                        fontSize = 10.sp,
                                        fontWeight =
                                            FontWeight.Bold,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 2.dp
                                            )
                                    )
                                }

                            } else if (
                                terminalOutput.contains("ERROR") ||
                                terminalOutput.contains("EOFError")
                            ) {

                                Surface(
                                    color =
                                        Color(0xFFF85149)
                                            .copy(alpha = 0.2f),
                                    shape =
                                        RoundedCornerShape(4.dp)
                                ) {

                                    Text(
                                        text = "FAILED",
                                        color =
                                            Color(0xFFF85149),
                                        fontSize = 10.sp,
                                        fontWeight =
                                            FontWeight.Bold,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 2.dp
                                            )
                                    )
                                }

                            } else if (
                                terminalOutput.isNotEmpty()
                            ) {

                                Surface(
                                    color =
                                        Color(0xFF3FB950)
                                            .copy(alpha = 0.2f),
                                    shape =
                                        RoundedCornerShape(4.dp)
                                ) {

                                    Text(
                                        text = "SUCCESS",
                                        color =
                                            Color(0xFF3FB950),
                                        fontSize = 10.sp,
                                        fontWeight =
                                            FontWeight.Bold,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 2.dp
                                            )
                                    )
                                }
                            }
                        }

                        Row {

                            IconButton(
                                onClick = {

                                    if (
                                        terminalOutput.isNotEmpty()
                                    ) {

                                        val clip =
                                            ClipData.newPlainText(
                                                "Terminal Output",
                                                terminalOutput
                                            )

                                        clipboardManager
                                            .setPrimaryClip(clip)

                                        Toast.makeText(
                                            context,
                                            "Copied Output",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            ) {

                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription =
                                        "Copy Output",
                                    tint =
                                        Color(0xFF8B949E),
                                    modifier =
                                        Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    terminalOutput = ""
                                }
                            ) {

                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription =
                                        "Clear Terminal",
                                    tint =
                                        Color(0xFF8B949E),
                                    modifier =
                                        Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    showTerminalSheet = false
                                }
                            ) {

                                Icon(
                                    Icons.Default.Close,
                                    contentDescription =
                                        "Close",
                                    tint =
                                        Color(0xFF8B949E),
                                    modifier =
                                        Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF21262D),
                        modifier =
                            Modifier.padding(
                                vertical = 6.dp
                            )
                    )

                    Box(

                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF161B22),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                    ) {

                        val scrollState =
                            rememberScrollState()

                        if (isExecuting) {

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color =
                                        Color(0xFF58A6FF)
                                )

                                Spacer(
                                    Modifier.width(10.dp)
                                )

                                Text(
                                    text =
                                        "Compiling and running $activeFileName...",
                                    color =
                                        Color(0xFF8B949E),
                                    fontFamily =
                                        FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }

                        } else if (
                            terminalOutput.isEmpty()
                        ) {

                            Text(
                                text =
                                    "$ $activeFileName\nProcess started...\nEnter inputs below and tap send.",
                                color =
                                    Color(0xFF8B949E),
                                fontFamily =
                                    FontFamily.Monospace,
                                fontSize = 12.sp
                            )

                        } else {

                            Column(
                                modifier =
                                    Modifier.verticalScroll(
                                        scrollState
                                    )
                            ) {

                                Text(
                                    text =
                                        "$ run $activeFileName",
                                    color =
                                        Color(0xFF58A6FF),
                                    fontFamily =
                                        FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        terminalOutput,
                                    color =
                                        if (
                                            terminalOutput.contains(
                                                "ERROR"
                                            ) ||
                                            terminalOutput.contains(
                                                "EOFError"
                                            )
                                        )
                                            Color(0xFFFFA6A1)
                                        else
                                            Color(0xFFE6EDF3),
                                    fontFamily =
                                        FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )

                                if (
                                    executionTimeMs != null
                                ) {

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    Text(
                                        text =
                                            "\n[Process finished in ${executionTimeMs}ms]",
                                        color =
                                            Color(0xFF8B949E),
                                        fontFamily =
                                            FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF161B22),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = "stdin >",
                            color =
                                Color(0xFF58A6FF),
                            fontFamily =
                                FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        BasicTextField(

                            value = stdinInput,

                            onValueChange = {
                                stdinInput = it
                            },

                            modifier =
                                Modifier.weight(1f),

                            textStyle =
                                TextStyle(
                                    color =
                                        Color(0xFFE6EDF3),
                                    fontFamily =
                                        FontFamily.Monospace,
                                    fontSize = 12.sp
                                ),

                            singleLine = false,

                            maxLines = 2,

                            decorationBox = {
                                    innerTextField ->

                                if (
                                    stdinInput.isEmpty()
                                ) {

                                    Text(
                                        text =
                                            "Enter inputs (e.g. 21)...",
                                        color =
                                            Color(0xFF484F58),
                                        fontFamily =
                                            FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }

                                innerTextField()
                            }
                        )

                        IconButton(

                            onClick = {
                                runCodeExecution()
                            },

                            modifier =
                                Modifier.size(28.dp)
                        ) {

                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription =
                                    "Send",
                                tint =
                                    Color(0xFF58A6FF),
                                modifier =
                                    Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        EdgePanelHandle(
            visible = !showRecentFilesPanel,
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = {
                showRecentFilesPanel = true
            }
        )

        RecentFilesEdgePanel(
            visible = showRecentFilesPanel,
            recentFiles = recentFiles,
            activeFileId = activeFileId,
            onDismiss = {
                showRecentFilesPanel = false
            },
            onFileClick = { file ->
                viewModel.loadFile(
                    file.id,
                    file.name,
                    file.content,
                    isVersionBackupEnabled
                )

                showRecentFilesPanel = false
            }
        )
    }

    /*
     * Save As dialog
     */
    if (showSaveAsDialog) {

        var newFileNameInput by remember {
            mutableStateOf(activeFileName)
        }

        AlertDialog(

            onDismissRequest = {
                showSaveAsDialog = false
            },

            title = {
                Text("Save File As")
            },

            text = {

                OutlinedTextField(

                    value =
                        newFileNameInput,

                    onValueChange = {
                        newFileNameInput = it
                    },

                    label = {
                        Text("File Name")
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        if (
                            newFileNameInput.isNotBlank()
                        ) {

                            val newId =
                                System.currentTimeMillis()

                            val currentContent =
                                editorValue.text

                            fileViewModel.updateFile(
                                newId,
                                newFileNameInput,
                                currentContent
                            )

                            viewModel.loadFile(
                                newId,
                                newFileNameInput,
                                currentContent
                            )

                            showSaveAsDialog = false

                            Toast.makeText(
                                context,
                                "Saved as $newFileNameInput",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {

                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showSaveAsDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    /*
     * Find / Replace
     */
    if (showSearchDialog) {

        FindReplaceDialog(

            editorValue = editorValue,

            onEditorValueChange = { newValue ->

                editorValue = newValue

                viewModel.updateCode(
                    newValue.text
                )
            },

            onDismiss = {
                showSearchDialog = false
            }
        )
    }
}

@Composable
private fun MarkdownPreviewArea(
    text: String,
    modifier: Modifier = Modifier
) {

    Surface(

        modifier =
            modifier.fillMaxSize(),

        color =
            Color(0xFF0D1117)
    ) {

        MarkdownPreview(

            content = text,

            modifier =
                Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CodeEditorArea(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    fontSize: Float,
    fileName: String,
    isWordWrap: Boolean = false,
    isReadOnly: Boolean = false,
    showLineNumbers: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val language = remember(fileName) {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        when (ext) {
            "kt", "kts" -> "kotlin"
            "py" -> "python"
            "c" -> "c"
            "cpp", "cc", "cxx" -> "cpp"
            "js" -> "javascript"
            "ts" -> "typescript"
            "java" -> "java"
            else -> "kotlin"
        }
    }

    val syntaxRules = remember(language) { SyntaxRules.load(context, language) }
    val syntaxTransformation = remember(syntaxRules) { CodeSyntaxVisualTransformation(syntaxRules) }

    val verticalScrollState =
        rememberScrollState()

    val horizontalScrollState =
        rememberScrollState()

    val density = LocalDensity.current

    // Real layout info from the BasicTextField, refreshed on every text
    // layout pass. Used to position line numbers at the correct pixel
    // offset for each logical line's FIRST visual line - this is what
    // keeps the gutter in sync when a line wraps into several rows
    // instead of drifting the way a naive "one number per \n" column
    // would once word wrap is on.
    var textLayoutResult by remember {
        mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null)
    }

    // Character offset where each logical (newline-delimited) line starts.
    val lineStartOffsets = remember(value.text) {
        val starts = mutableListOf(0)
        value.text.forEachIndexed { index, c ->
            if (c == '\n') starts.add(index + 1)
        }
        starts
    }

    val editorFontSize =
        fontSize.sp

    val editorLineHeight =
        (fontSize * 1.5f).sp

    Surface(

        modifier = modifier,

        color =
            Color(0xFF10131A)
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        verticalScrollState
                    )
                    .then(
                        if (!isWordWrap)
                            Modifier.horizontalScroll(
                                horizontalScrollState
                            )
                        else
                            Modifier
                    )
                    .padding(
                        top = 12.dp,
                        bottom = 88.dp
                    )
        ) {

            if (showLineNumbers) {

                val layout = textLayoutResult

                // Height must be set explicitly: Modifier.offset() below
                // repositions each number but does NOT contribute to the
                // parent's measured size, so without this the gutter
                // would collapse to a single line's height.
                val contentHeightDp =
                    layout?.let {
                        with(density) { it.size.height.toDp() }
                    } ?: (editorLineHeight.value.dp * lineStartOffsets.size)

                Box(

                    modifier =
                        Modifier
                            .background(
                                Color(0xFF171B24)
                            )
                            .padding(
                                horizontal = 10.dp
                            )
                            .height(contentHeightDp)
                ) {

                    lineStartOffsets.forEachIndexed { logicalIndex, charOffset ->

                        val topDp =
                            if (layout != null) {
                                val safeOffset =
                                    charOffset.coerceAtMost(value.text.length)
                                val visualLine =
                                    layout.getLineForOffset(safeOffset)
                                with(density) {
                                    layout.getLineTop(visualLine).toDp()
                                }
                            } else {
                                // Before the first layout pass completes,
                                // fall back to an even-spacing estimate.
                                editorLineHeight.value.dp * logicalIndex
                            }

                        Text(

                            text =
                                (logicalIndex + 1).toString(),

                            color =
                                Color(0xFF6F7785),

                            fontFamily =
                                FontFamily.Monospace,

                            fontSize =
                                editorFontSize,

                            lineHeight =
                                editorLineHeight,

                            modifier =
                                Modifier.offset(y = topDp)
                        )
                    }
                }

                Spacer(
                    Modifier.width(12.dp)
                )

            } else {

                Spacer(
                    Modifier.width(8.dp)
                )
            }

            Box(

                modifier =
                    Modifier
                        .padding(end = 24.dp)
                        .then(
                            if (!isWordWrap)
                                Modifier.width(1000.dp)
                            else
                                Modifier.fillMaxWidth()
                        )
            ) {

                BasicTextField(

                    value = value,

                    onValueChange =
                        onValueChange,

                    readOnly =
                        isReadOnly,

                    modifier =
                        Modifier.fillMaxWidth(),

                    visualTransformation =
                        syntaxTransformation,

                    // A code editor should never autocorrect identifiers -
                    // "int" becoming a dictionary word mid-keystroke was a
                    // real source of corrupted code.
                    keyboardOptions =
                        KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii
                        ),

                    onTextLayout = { result ->
                        textLayoutResult = result
                    },

                    textStyle =
                        TextStyle(
                            color =
                                Color(0xFFE6EDF3),
                            fontFamily =
                                FontFamily.Monospace,
                            fontSize =
                                editorFontSize,
                            lineHeight =
                                editorLineHeight
                        ),

                    cursorBrush =
                        androidx.compose.ui.graphics
                            .SolidColor(
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            ),

                    decorationBox = {
                            innerTextField ->

                        Box {

                            if (
                                value.text.isEmpty()
                            ) {

                                Text(
                                    text =
                                        "Start writing code here...",
                                    color =
                                        Color(0xFF687080),
                                    fontFamily =
                                        FontFamily.Monospace,
                                    fontSize =
                                        editorFontSize
                                )
                            }

                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

class CodeSyntaxVisualTransformation(private val rules: SyntaxRules? = null) :
    VisualTransformation {

    private val keywordPattern =
        if (rules != null) {
            Pattern.compile("\\b(${rules.keywords.joinToString("|")})\\b")
        } else {
            Pattern.compile(
                "\\b(def|class|if|else|elif|for|while|return|import|from|as|try|except|finally|raise|with|lambda|yield|async|await|pass|break|continue|global|nonlocal|assert|del|fun|val|var|public|private|protected|package|include|using|namespace|int|float|double|char|void|boolean|bool|true|false|null|None|True|False)\\b"
            )
        }

    private val stringPattern =
        Pattern.compile(
            "\".*?\"|'.*?'"
        )

    private val commentPattern =
        Pattern.compile(
            "#.*|//.*"
        )

    private val numberPattern =
        Pattern.compile(
            "\\b\\d+\\b"
        )

    private val functionPattern =
        Pattern.compile(
            "\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\()"
        )

    private val preprocessorPattern =
        Pattern.compile(
            "^\\s*#\\s*(include|define|if|else|endif|ifdef|ifndef|pragma|error|warning)\\b",
            Pattern.MULTILINE
        )

    private val headerPattern =
        Pattern.compile(
            "<[a-zA-Z0-9_./]+>"
        )

    override fun filter(
        text: AnnotatedString
    ): TransformedText {

        val highlighted =
            buildAnnotatedString {

                append(text.text)

                highlightPattern(
                    text.text,
                    stringPattern,
                    rules?.colors?.get("strings")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFF98C379)
                )

                highlightPattern(
                    text.text,
                    commentPattern,
                    rules?.colors?.get("comments")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFF5C6370)
                )

                highlightPattern(
                    text.text,
                    keywordPattern,
                    rules?.colors?.get("keywords")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFFC678DD),
                    FontWeight.Bold
                )

                highlightPattern(
                    text.text,
                    functionPattern,
                    rules?.colors?.get("functions")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFF61AFEF)
                )

                highlightPattern(
                    text.text,
                    numberPattern,
                    rules?.colors?.get("numbers")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFFD19A66)
                )

                // C/C++ specific: Preprocessor and Headers
                highlightPattern(
                    text.text,
                    preprocessorPattern,
                    rules?.colors?.get("keywords")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFFC678DD),
                    FontWeight.Bold
                )

                highlightPattern(
                    text.text,
                    headerPattern,
                    rules?.colors?.get("strings")?.let { hex -> Color(android.graphics.Color.parseColor(hex)) } ?: Color(0xFF98C379)
                )
            }

        return TransformedText(
            highlighted,
            OffsetMapping.Identity
        )
    }

    private fun AnnotatedString.Builder.highlightPattern(
        text: String,
        pattern: Pattern,
        color: Color,
        fontWeight: FontWeight =
            FontWeight.Normal
    ) {

        val matcher =
            pattern.matcher(text)

        while (matcher.find()) {

            addStyle(

                style =
                    SpanStyle(
                        color = color,
                        fontWeight =
                            fontWeight
                    ),

                start =
                    matcher.start(),

                end =
                    matcher.end()
            )
        }
    }
}

@Composable
private fun EditorStatusBar(
    lineCount: Int,
    wordCount: Int,
    characterCount: Int
) {

    Column {

        HorizontalDivider()

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surface
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

            horizontalArrangement =
                Arrangement.Center
        ) {

            Text(
                text =
                    "$lineCount lines | $wordCount words | $characterCount chars",
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FindReplaceDialog(
    editorValue: TextFieldValue,
    onEditorValueChange:
        (TextFieldValue) -> Unit,
    onDismiss: () -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var replacementText by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    fun findNext() {

        if (searchText.isBlank()) {

            message =
                "Enter text to search"

            return
        }

        val code =
            editorValue.text

        val startPosition =
            editorValue.selection.end
                .coerceIn(
                    0,
                    code.length
                )

        var matchIndex =
            code.indexOf(
                string = searchText,
                startIndex = startPosition,
                ignoreCase = true
            )

        if (matchIndex == -1) {

            matchIndex =
                code.indexOf(
                    string = searchText,
                    startIndex = 0,
                    ignoreCase = true
                )
        }

        if (matchIndex >= 0) {

            onEditorValueChange(

                editorValue.copy(

                    selection =
                        TextRange(
                            start = matchIndex,
                            end =
                                matchIndex +
                                        searchText.length
                        )
                )
            )

            message =
                "Text found"

        } else {

            message =
                "Text not found"
        }
    }

    fun replaceCurrent() {

        if (searchText.isBlank()) {

            message =
                "Enter text to replace"

            return
        }

        val selectionStart =
            editorValue.selection.min

        val selectionEnd =
            editorValue.selection.max

        val selectedText =
            editorValue.text.substring(
                selectionStart,
                selectionEnd
            )

        if (
            !selectedText.equals(
                searchText,
                ignoreCase = true
            )
        ) {

            findNext()

            message =
                "Find the text first"

            return
        }

        val newCode =
            editorValue.text.replaceRange(
                selectionStart until selectionEnd,
                replacementText
            )

        val newCursorPosition =
            selectionStart +
                    replacementText.length

        onEditorValueChange(

            TextFieldValue(

                text = newCode,

                selection =
                    TextRange(
                        newCursorPosition
                    )
            )
        )

        message =
            "Text replaced"
    }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text("Find and Replace")
        },

        text = {

            Column {

                OutlinedTextField(

                    value =
                        searchText,

                    onValueChange = {

                        searchText = it
                        message = ""
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text("Find")
                    },

                    singleLine = true
                )

                Spacer(
                    Modifier.height(10.dp)
                )

                OutlinedTextField(

                    value =
                        replacementText,

                    onValueChange = {

                        replacementText = it
                        message = ""
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text("Replace with")
                    },

                    singleLine = true
                )

                if (message.isNotBlank()) {

                    Spacer(
                        Modifier.height(10.dp)
                    )

                    Text(
                        text = message,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        fontSize = 13.sp
                    )
                }

                Spacer(
                    Modifier.height(16.dp)
                )

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            findNext()
                        }
                    ) {

                        Text("Find Next")
                    }

                    Button(
                        onClick = {
                            replaceCurrent()
                        }
                    ) {

                        Text("Replace")
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Close")
            }
        }
    )
}
=======
package com.example.codevaultide.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codevaultide.compiler.CompilerManager
import com.example.codevaultide.editor.EditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    fileViewModel: com.example.codevaultide.editor.FileViewModel,
    settingsViewModel: com.example.codevaultide.ui.settings.SettingsViewModel,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit = {},
) {
    val code by viewModel.code.collectAsState()
    val activeFileName by viewModel.fileName.collectAsState()
    val activeFileId by viewModel.fileId.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val isAutoSaveEnabled by settingsViewModel.isAutoSaveEnabled.collectAsState()

    val compilerManager = remember { CompilerManager() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    val languageName = remember(activeFileName) {
        when {
            activeFileName.endsWith(".py") -> "Python 3"
            activeFileName.endsWith(".cpp") || activeFileName.endsWith(".c") -> "C++"
            activeFileName.endsWith(".java") -> "Java"
            activeFileName.endsWith(".kt") -> "Kotlin"
            activeFileName.endsWith(".js") -> "JavaScript"
            else -> "Plain Text"
        }
    }

    var editorValue by remember(activeFileId) {
        val initialCode = viewModel.code.value
        mutableStateOf(
            TextFieldValue(
                text = initialCode,
                selection = TextRange(initialCode.length)
            )
        )
    }

    var showSearchDialog by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var showTerminalSheet by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var terminalOutput by remember { mutableStateOf("") }
    var stdinInput by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }
    var executionTimeMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(code) {
        if (code != editorValue.text) {
            editorValue = editorValue.copy(text = code)
        }
    }

    val lineCount = editorValue.text.lines().size.coerceAtLeast(1)
    val cursorPosition = editorValue.selection.start.coerceIn(0, editorValue.text.length)
    val textBeforeCursor = editorValue.text.take(cursorPosition)
    val cursorLine = textBeforeCursor.count { it == '\n' } + 1
    val cursorColumn = textBeforeCursor.substringAfterLast('\n').length + 1

    fun handleCopy() {
        val selectedText = if (editorValue.selection.collapsed) {
            editorValue.text
        } else {
            editorValue.text.substring(editorValue.selection.min, editorValue.selection.max)
        }
        if (selectedText.isNotEmpty()) {
            val clip = ClipData.newPlainText("Copied Code", selectedText)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleCut() {
        if (!editorValue.selection.collapsed) {
            val start = editorValue.selection.min
            val end = editorValue.selection.max
            val selectedText = editorValue.text.substring(start, end)

            val clip = ClipData.newPlainText("Cut Code", selectedText)
            clipboardManager.setPrimaryClip(clip)

            val newText = editorValue.text.removeRange(start, end)
            editorValue = TextFieldValue(newText, selection = TextRange(start))
            viewModel.updateCode(newText)
        }
    }

    fun handlePaste() {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && (clipData.itemCount > 0)) {
            val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
            if (pasteText.isNotEmpty()) {
                val start = editorValue.selection.min
                val end = editorValue.selection.max
                val newText = editorValue.text.replaceRange(start, end, pasteText)
                val newCursorPos = start + pasteText.length

                editorValue = TextFieldValue(newText, selection = TextRange(newCursorPos))
                viewModel.updateCode(newText)
            }
        }
    }

    fun performSave(showToast: Boolean = true) {
        val currentText = editorValue.text
        viewModel.updateCode(currentText)
        val idToSave = activeFileId ?: System.currentTimeMillis()
        if (activeFileId == null) {
            viewModel.setFileId(idToSave)
        }
        fileViewModel.updateFile(idToSave, activeFileName, currentText)
        if (showToast) {
            Toast.makeText(context, "$activeFileName saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(editorValue.text, isAutoSaveEnabled) {
        if (isAutoSaveEnabled) {
            delay(2000)
            performSave(showToast = false)
        }
    }

    fun formatInteractiveTerminalOutput(rawOutput: String, stdin: String): String {
        if (stdin.isBlank() || rawOutput.contains("ERROR")) return rawOutput

        val inputLines = stdin.lines().filter { it.isNotBlank() }
        if (inputLines.isEmpty()) return rawOutput

        val promptRegex = Regex("""([a-zA-Z0-9_\s]*[:?]\s*)""")
        val matches = promptRegex.findAll(rawOutput).toList()

        if (matches.isNotEmpty()) {
            val sb = StringBuilder()
            var lastIndex = 0
            matches.forEachIndexed { index, match ->
                if (index < inputLines.size) {
                    sb.append(rawOutput.substring(lastIndex, match.range.last + 1))
                    sb.append(inputLines[index]).append("\n")
                    lastIndex = match.range.last + 1
                }
            }
            if (lastIndex < rawOutput.length) {
                sb.append(rawOutput.substring(lastIndex))
            }
            return sb.toString().trim()
        }

        return rawOutput
    }

    fun runCodeExecution() {
        performSave(showToast = false)
        showTerminalSheet = true
        isExecuting = true
        terminalOutput = ""
        val startTime = System.currentTimeMillis()

        scope.launch {
            val rawResult = compilerManager.compileAndRun(
                language = if (activeFileName.endsWith(".py")) "python" else "cpp",
                code = editorValue.text,
                stdin = stdinInput
            )
            terminalOutput = formatInteractiveTerminalOutput(rawResult, stdinInput)
            executionTimeMs = System.currentTimeMillis() - startTime
            isExecuting = false
        }
    }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Lang",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = activeFileName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = languageName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Find & Replace")
                    }
                    IconButton(onClick = { performSave() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                    IconButton(onClick = { showSaveAsDialog = true }) {
                        Icon(Icons.Default.SaveAs, contentDescription = "Save As")
                    }
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "Version History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            EditorStatusBar(
                languageName = languageName,
                cursorLine = cursorLine,
                cursorColumn = cursorColumn,
                lineCount = lineCount
            )
        },
        floatingActionButton = {
            if (!showTerminalSheet) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { showAiSheet = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant"
                        )
                    }

                    SmallFloatingActionButton(
                        onClick = { showTerminalSheet = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Open Terminal & Inputs"
                        )
                    }

                    FloatingActionButton(
                        onClick = { runCodeExecution() },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run Code"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { handleCut() }) {
                    Icon(Icons.Default.ContentCut, contentDescription = "Cut", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { handleCopy() }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { handlePaste() }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(18.dp))
                }
                HorizontalDivider(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp)
                )
                IconButton(onClick = { viewModel.undo() }) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { viewModel.redo() }) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", modifier = Modifier.size(18.dp))
                }
            }

            key(activeFileId) {
                CodeEditorArea(
                    value = editorValue,
                    onValueChange = { newValue ->
                        editorValue = newValue
                        viewModel.updateCode(newValue.text)
                        viewModel.updateCursorPosition(newValue.selection.start)
                    },
                    fontSize = fontSize,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showTerminalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTerminalSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF0D1117)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal",
                            tint = Color(0xFF58A6FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINAL",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC9D1D9),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        if (isExecuting) {
                            Surface(
                                color = Color(0xFF388BFD).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "RUNNING",
                                    color = Color(0xFF58A6FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (terminalOutput.contains("ERROR") || terminalOutput.contains("EOFError")) {
                            Surface(
                                color = Color(0xFFF85149).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FAILED",
                                    color = Color(0xFFF85149),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (terminalOutput.isNotEmpty()) {
                            Surface(
                                color = Color(0xFF3FB950).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SUCCESS",
                                    color = Color(0xFF3FB950),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = {
                            if (terminalOutput.isNotEmpty()) {
                                val clip = ClipData.newPlainText("Terminal Output", terminalOutput)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied Output", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Output",
                                tint = Color(0xFF8B949E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { terminalOutput = "" }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Terminal",
                                tint = Color(0xFF8B949E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { showTerminalSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Sheet",
                                tint = Color(0xFF8B949E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF21262D), modifier = Modifier.padding(vertical = 6.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF161B22), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val scrollState = rememberScrollState()

                    if (isExecuting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF58A6FF)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Compiling and running $activeFileName...",
                                color = Color(0xFF8B949E),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    } else if (terminalOutput.isEmpty()) {
                        Text(
                            text = "$ $activeFileName\nProcess started...\nEnter inputs below and tap send.",
                            color = Color(0xFF8B949E),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    } else {
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            Text(
                                text = "$ python $activeFileName",
                                color = Color(0xFF58A6FF),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = terminalOutput,
                                color = if (terminalOutput.contains("ERROR") || terminalOutput.contains("EOFError")) Color(0xFFFFA6A1) else Color(0xFFE6EDF3),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            if (executionTimeMs != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\n[Process finished in ${executionTimeMs}ms]",
                                    color = Color(0xFF8B949E),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161B22), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "stdin >",
                        color = Color(0xFF58A6FF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = stdinInput,
                        onValueChange = { stdinInput = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            color = Color(0xFFE6EDF3),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        singleLine = false,
                        maxLines = 2,
                        decorationBox = { innerTextField ->
                            if (stdinInput.isEmpty()) {
                                Text(
                                    text = "Enter inputs (e.g. 21)...",
                                    color = Color(0xFF484F58),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    IconButton(
                        onClick = { runCodeExecution() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Stdin",
                            tint = Color(0xFF58A6FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            AiAssistantSheetContent(
                editorViewModel = viewModel,
                fileViewModel = fileViewModel,
                onClose = { showAiSheet = false },
                onOpenInEditor = { newName, code ->
                    fileViewModel.createNewFile(newName, code) { newId ->
                        viewModel.loadFile(newId, newName, code)
                    }
                }
            )
        }
    }

    if (showSaveAsDialog) {
        var newFileNameInput by remember { mutableStateOf(activeFileName) }

        AlertDialog(
            onDismissRequest = { showSaveAsDialog = false },
            title = { Text("Save File As") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.isNotBlank()) {
                            val newId = System.currentTimeMillis()
                            val currentContent = editorValue.text

                            fileViewModel.updateFile(newId, newFileNameInput, currentContent)
                            viewModel.loadFile(newId, newFileNameInput, currentContent)

                            showSaveAsDialog = false
                            Toast.makeText(context, "Saved as $newFileNameInput", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSearchDialog) {
        FindReplaceDialog(
            editorValue = editorValue,
            onEditorValueChange = { newValue ->
                editorValue = newValue
                viewModel.updateCode(newValue.text)
            },
            onDismiss = {
                showSearchDialog = false
            }
        )
    }
}

@Composable
private fun CodeEditorArea(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    fontSize: Float,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val lines = value.text.lines()
    val lineCount = lines.size.coerceAtLeast(1)

    val syntaxTransformation = remember { CodeSyntaxVisualTransformation() }
    val editorFontSize = fontSize.sp
    val editorLineHeight = (fontSize * 1.5f).sp

    Surface(
        modifier = modifier,
        color = Color(0xFF10131A)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF171B24))
                    .padding(horizontal = 10.dp)
            ) {
                repeat(lineCount) { index ->
                    Text(
                        text = (index + 1).toString(),
                        color = Color(0xFF6F7785),
                        fontFamily = FontFamily.Monospace,
                        fontSize = editorFontSize,
                        lineHeight = editorLineHeight
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .width(1000.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = syntaxTransformation,
                    textStyle = TextStyle(
                        color = Color(0xFFE6EDF3),
                        fontFamily = FontFamily.Monospace,
                        fontSize = editorFontSize,
                        lineHeight = editorLineHeight
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.primary
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.text.isEmpty()) {
                                Text(
                                    text = "Start writing code here...",
                                    color = Color(0xFF687080),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = editorFontSize
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

class CodeSyntaxVisualTransformation : VisualTransformation {

    private val keywordPattern = Pattern.compile(
        "\\b(def|class|if|else|elif|for|while|return|import|from|as|try|except|finally|raise|with|lambda|yield|async|await|pass|break|continue|global|nonlocal|assert|del|fun|val|var|public|private|protected|package|include|using|namespace|int|float|double|char|void|boolean|bool|true|false|null|None|True|False)\\b"
    )

    private val stringPattern = Pattern.compile("\".*?\"|'.*?'")
    private val commentPattern = Pattern.compile("#.*|//.*")
    private val numberPattern = Pattern.compile("\\b\\d+\\b")
    private val functionPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\()")

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = buildAnnotatedString {
            append(text.text)

            highlightPattern(text.text, stringPattern, Color(0xFF98C379))
            highlightPattern(text.text, commentPattern, Color(0xFF5C6370))
            highlightPattern(text.text, keywordPattern, Color(0xFFC678DD), FontWeight.Bold)
            highlightPattern(text.text, functionPattern, Color(0xFF61AFEF))
            highlightPattern(text.text, numberPattern, Color(0xFFD19A66))
        }

        return TransformedText(highlighted, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.highlightPattern(
        text: String,
        pattern: Pattern,
        color: Color,
        fontWeight: FontWeight = FontWeight.Normal
    ) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            addStyle(
                style = SpanStyle(color = color, fontWeight = fontWeight),
                start = matcher.start(),
                end = matcher.end()
            )
        }
    }
}

@Composable
private fun EditorStatusBar(
    languageName: String,
    cursorLine: Int,
    cursorColumn: Int,
    lineCount: Int
) {
    Column {
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = languageName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Line $cursorLine, Column $cursorColumn",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$lineCount lines",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FindReplaceDialog(
    editorValue: TextFieldValue,
    onEditorValueChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var replacementText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    fun findNext() {
        if (searchText.isBlank()) {
            message = "Enter text to search"
            return
        }

        val code = editorValue.text
        val startPosition = editorValue.selection.end.coerceIn(0, code.length)

        var matchIndex = code.indexOf(
            string = searchText,
            startIndex = startPosition,
            ignoreCase = true
        )

        if (matchIndex == -1) {
            matchIndex = code.indexOf(
                string = searchText,
                startIndex = 0,
                ignoreCase = true
            )
        }

        if (matchIndex >= 0) {
            onEditorValueChange(
                editorValue.copy(
                    selection = TextRange(
                        start = matchIndex,
                        end = matchIndex + searchText.length
                    )
                )
            )
            message = "Text found"
        } else {
            message = "Text not found"
        }
    }

    fun replaceCurrent() {
        if (searchText.isBlank()) {
            message = "Enter text to replace"
            return
        }

        val selectionStart = editorValue.selection.min
        val selectionEnd = editorValue.selection.max

        val selectedText = editorValue.text.substring(
            startIndex = selectionStart,
            endIndex = selectionEnd
        )

        if (!selectedText.equals(searchText, ignoreCase = true)) {
            findNext()
            message = "Find the text first"
            return
        }

        val newCode = editorValue.text.replaceRange(
            range = selectionStart until selectionEnd,
            replacement = replacementText
        )

        val newCursorPosition = selectionStart + replacementText.length

        onEditorValueChange(
            TextFieldValue(
                text = newCode,
                selection = TextRange(newCursorPosition)
            )
        )
        message = "Text replaced"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Find and Replace") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        message = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Find") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = replacementText,
                    onValueChange = {
                        replacementText = it
                        message = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Replace with") },
                    singleLine = true
                )

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { findNext() }) {
                        Text("Find Next")
                    }
                    Button(onClick = { replaceCurrent() }) {
                        Text("Replace")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
>>>>>>> origin/main
