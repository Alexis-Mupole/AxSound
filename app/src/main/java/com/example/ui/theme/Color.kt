package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Obsidian & Slate palette
val DarkBackground = Color(0xFF0F1015)
val DarkSurface = Color(0xFF171821)
val DarkSurfaceVariant = Color(0xFF222430)
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0A)
val AmoledSurfaceVariant = Color(0xFF141414)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF8B5CF6)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> ("FF" + clean).toLong(16)
            8 -> clean.toLong(16)
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}
