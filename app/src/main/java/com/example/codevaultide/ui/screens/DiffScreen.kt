package com.example.codevaultide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codevaultide.versioncontrol.DiffLine
import com.example.codevaultide.versioncontrol.DiffType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffScreen(
    diffLines: List<DiffLine>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Code Comparison") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0D1117))
        ) {
            items(diffLines) { line ->
                val backgroundColor = when (line.type) {
                    DiffType.ADDED -> Color(0xFF2EA043).copy(alpha = 0.2f)
                    DiffType.REMOVED -> Color(0xFFF85149).copy(alpha = 0.2f)
                    DiffType.UNCHANGED -> Color.Transparent
                }
                
                val prefix = when (line.type) {
                    DiffType.ADDED -> "+"
                    DiffType.REMOVED -> "-"
                    DiffType.UNCHANGED -> " "
                }

                val textColor = when (line.type) {
                    DiffType.ADDED -> Color(0xFF3FB950)
                    DiffType.REMOVED -> Color(0xFFF85149)
                    DiffType.UNCHANGED -> Color(0xFFC9D1D9)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = (line.lineNumber?.toString() ?: "").padStart(3) + " ",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$prefix ${line.content}",
                        color = textColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
