package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class GradientPreset(
    val name: String,
    val darkColors: List<Color>,
    val lightColors: List<Color>,
    val amoledColors: List<Color> = listOf(Color(0xFF000000), Color(0xFF000000)),
    val description: String
) {
    val colors: List<Color> get() = darkColors
    val brush: Brush get() = Brush.verticalGradient(darkColors)

    fun getBrush(isDark: Boolean, isAmoled: Boolean): Brush {
        val selected = when {
            isAmoled -> amoledColors
            isDark -> darkColors
            else -> lightColors
        }
        return Brush.verticalGradient(selected)
    }
}

object GradientThemes {
    val Midnight = GradientPreset(
        name = "Midnight",
        darkColors = listOf(Color(0xFF0F1016), Color(0xFF181A26), Color(0xFF0A0B0E)),
        lightColors = listOf(Color(0xFFF8FAFC), Color(0xFFEDF2F7), Color(0xFFF1F5F9)),
        description = "Deep obsidian with indigo undertones"
    )

    val Cyberpunk = GradientPreset(
        name = "Cyberpunk",
        darkColors = listOf(Color(0xFF20053B), Color(0xFF140D2E), Color(0xFF0B091A)),
        lightColors = listOf(Color(0xFFFAF5FF), Color(0xFFFCE7F3), Color(0xFFF3E8FF)),
        description = "Neon violet, deep magenta, and dark obsidian"
    )

    val Sunset = GradientPreset(
        name = "Sunset",
        darkColors = listOf(Color(0xFF381224), Color(0xFF261021), Color(0xFF120815)),
        lightColors = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5), Color(0xFFFFF1F2)),
        description = "Warm ember, dusky crimson, and plum"
    )

    val Ocean = GradientPreset(
        name = "Ocean",
        darkColors = listOf(Color(0xFF0A2239), Color(0xFF0B1D2D), Color(0xFF060F17)),
        lightColors = listOf(Color(0xFFF0FDF4), Color(0xFFE0F2FE), Color(0xFFF0F9FF)),
        description = "Deep abyss, midnight teal, and dark marine"
    )

    val Aurora = GradientPreset(
        name = "Aurora",
        darkColors = listOf(Color(0xFF0B2E24), Color(0xFF091E19), Color(0xFF050F0D)),
        lightColors = listOf(Color(0xFFF0FDF4), Color(0xFFD1FAE5), Color(0xFFECFDF5)),
        description = "Emerald boreal light and deep pines"
    )

    val Forest = GradientPreset(
        name = "Forest",
        darkColors = listOf(Color(0xFF1C2819), Color(0xFF141D12), Color(0xFF0B100A)),
        lightColors = listOf(Color(0xFFF7FEE7), Color(0xFFDCFCE7), Color(0xFFF5F5F4)),
        description = "Moss, damp evergreen, and earth"
    )

    val Rose = GradientPreset(
        name = "Rose",
        darkColors = listOf(Color(0xFF301524), Color(0xFF24101B), Color(0xFF14080F)),
        lightColors = listOf(Color(0xFFFFF1F2), Color(0xFFFCE7F3), Color(0xFFFDF2F8)),
        description = "Velvet wine, dusky rose, and shadow"
    )

    val Electric = GradientPreset(
        name = "Electric",
        darkColors = listOf(Color(0xFF131738), Color(0xFF0E132B), Color(0xFF080A1A)),
        lightColors = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF), Color(0xFFF8FAFC)),
        description = "Electric neon cobalt and dark space"
    )

    val None = GradientPreset(
        name = "None",
        darkColors = listOf(Color(0xFF12131A), Color(0xFF12131A)),
        lightColors = listOf(Color(0xFFF8F9FD), Color(0xFFF8F9FD)),
        amoledColors = listOf(Color(0xFF000000), Color(0xFF000000)),
        description = "Flat solid background"
    )

    val presets = listOf(Midnight, Cyberpunk, Sunset, Ocean, Aurora, Forest, Rose, Electric, None)

    fun getByName(name: String): GradientPreset {
        return presets.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Midnight
    }
}

val CuratedAccentColors = listOf(
    "#8B5CF6" to "Electric Violet",
    "#6366F1" to "Indigo Glow",
    "#3B82F6" to "Vibrant Blue",
    "#06B6D4" to "Cyan Pulse",
    "#10B981" to "Emerald Neon",
    "#14B8A6" to "Teal Wave",
    "#22C55E" to "Bright Jade",
    "#EAB308" to "Golden Amber",
    "#F97316" to "Solar Flare",
    "#EF4444" to "Hyper Red",
    "#EC4899" to "Neon Pink",
    "#D946EF" to "Fuchsia Beat",
    "#A855F7" to "Deep Purple",
    "#64748B" to "Slate Metal",
    "#78716C" to "Warm Titanium",
    "#F43F5E" to "Rose Carmine"
)
