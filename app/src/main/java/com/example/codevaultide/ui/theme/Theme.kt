package com.example.codevaultide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CodeVaultDarkColors = darkColorScheme(
    primary = Color(0xFF7EBCFB), // Professional Blue
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2948),
    background = Color(0xFF0F1115), // Deep IDE Background
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF171A20),    // Slightly lighter surface
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF21242B),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    error = Color(0xFFFFB4AB)
)

private val CodeVaultLightColors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF0061A3),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E)
)

@Composable
fun CodeVaultTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CodeVaultDarkColors else CodeVaultLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}