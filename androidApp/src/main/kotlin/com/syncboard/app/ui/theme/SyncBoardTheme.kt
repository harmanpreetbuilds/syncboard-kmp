package com.syncboard.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF111827)
private val MutedInk = Color(0xFF667085)
private val Canvas = Color(0xFFF7F8FA)
private val Surface = Color(0xFFFFFFFF)
private val Border = Color(0xFFE4E7EC)
private val Brand = Color(0xFF4F46E5)
private val BrandSoft = Color(0xFFEEF2FF)
private val Danger = Color(0xFFB42318)

private val SyncBoardColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = Ink,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF2F4F7),
    onSurfaceVariant = MutedInk,
    outline = Border,
    error = Danger
)

@Composable
fun SyncBoardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SyncBoardColors,
        content = content
    )
}
