package com.example.codevaultide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codevaultide.editor.EditorViewModel

import com.example.codevaultide.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onRunClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val code by viewModel.code.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val isAutoSaveEnabled by settingsViewModel.isAutoSaveEnabled.collectAsState()
    
    var editorValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = code,
                selection = TextRange(code.length)
            )
        )
    }

    var showSearchDialog by remember { mutableStateOf(false) }

    // Sync from ViewModel if needed (e.g. after rollback)
    LaunchedEffect(code) {
        if (code != editorValue.text) {
            editorValue = editorValue.copy(text = code)
        }
    }

    // Auto Save Logic
    LaunchedEffect(editorValue.text) {
        if (isAutoSaveEnabled && editorValue.text != code) {
            // In a real app, we might want to debounce this
            viewModel.updateCode(editorValue.text)
        }
    }

    val lineCount = editorValue.text.lines().size.coerceAtLeast(1)
    val cursorPosition = editorValue.selection.start.coerceIn(0, editorValue.text.length)
    val textBeforeCursor = editorValue.text.take(cursorPosition)
    val cursorLine = textBeforeCursor.count { it == '\n' } + 1
    val cursorColumn = textBeforeCursor.substringAfterLast('\n').length + 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Main.kt", maxLines = 1)
                        Text(
                            text = "Kotlin Source",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.undo() }) {
                        Text("Undo")
                    }

                    TextButton(onClick = { viewModel.redo() }) {
                        Text("Redo")
                    }

                    TextButton(onClick = { showSearchDialog = true }) {
                        Text("Find")
                    }

                    TextButton(onClick = onHistoryClick) {
                        Text("History")
                    }

                    TextButton(onClick = onRunClick) {
                        Text("Run")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            EditorStatusBar(
                languageName = "Kotlin",
                cursorLine = cursorLine,
                cursorColumn = cursorColumn,
                lineCount = lineCount
            )
        }
    ) { innerPadding ->

        CodeEditorArea(
            value = editorValue,
            fontSize = fontSize,
            onValueChange = { newValue ->
                editorValue = newValue
                if (!isAutoSaveEnabled && newValue.text != code) {
                    viewModel.updateCode(newValue.text)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
    fontSize: Float,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val lines = value.text.lines()
    val lineCount = lines.size.coerceAtLeast(1)

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
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.5).sp
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
                    textStyle = TextStyle(
                        color = Color(0xFFE6EDF3),
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.5).sp
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
                                    fontSize = fontSize.sp
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
                androidx.compose.material3.OutlinedTextField(
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

                androidx.compose.material3.OutlinedTextField(
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
