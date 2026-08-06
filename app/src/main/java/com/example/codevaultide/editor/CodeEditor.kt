package com.example.codevaultide.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val lines = code.lines()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .verticalScroll(scrollState)
    ) {
        // Line Numbers
        Column(
            modifier = Modifier
                .width(40.dp)
                .background(Color(0xFF252526))
                .padding(vertical = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = (index + 1).toString(),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Code Area
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
        )
    }
}
