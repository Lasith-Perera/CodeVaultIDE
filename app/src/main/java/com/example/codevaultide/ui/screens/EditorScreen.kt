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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.painterResource
import com.example.codevaultide.R
import com.example.codevaultide.compiler.CompilerManager
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.editor.FileViewModel
import com.example.codevaultide.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    fileViewModel: FileViewModel,
    settingsViewModel: SettingsViewModel,
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
            delay(2000) // Debounce for 2 seconds
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
        performSave() // Auto-save before run
        showTerminalSheet = true
        isExecuting = true
        terminalOutput = ""
        val startTime = System.currentTimeMillis()

        scope.launch {
            val result = compilerManager.compileAndRun(
                language = if (activeFileName.endsWith(".py")) "python" else "cpp",
                code = editorValue.text,
                stdin = stdinInput
            )
            terminalOutput = formatInteractiveTerminalOutput(result.second, stdinInput)
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
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "CodeVault Logo",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
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
                        if (newValue.text != code) {
                            viewModel.updateCode(newValue.text)
                        }
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
                    .fillMaxHeight(0.45f) // Reduced height from 0.75f to 0.45f
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
                            
                            // Create/Update the new file in DB
                            fileViewModel.updateFile(newId, newFileNameInput, currentContent)
                            
                            // Update ViewModel state to the new file
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
