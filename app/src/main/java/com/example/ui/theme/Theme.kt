package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.preferences.ThemeMode

@Composable
fun PulseWaveTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentHex: String = "#8B5CF6",
    useDynamicColor: Boolean = false,
    cornerRoundnessDp: Int = 20,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val isAmoled = themeMode == ThemeMode.AMOLED

    val accent = parseHexColor(accentHex)

    val baseScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> {
            darkColorScheme(
                primary = accent,
                onPrimary = Color.White,
                primaryContainer = accent.copy(alpha = 0.28f),
                onPrimaryContainer = Color.White,
                secondary = accent.copy(alpha = 0.85f),
                onSecondary = Color.White,
                background = if (isAmoled) AmoledBackground else DarkBackground,
                onBackground = Color(0xFFF9FAFB),
                surface = if (isAmoled) AmoledSurface else DarkSurface,
                onSurface = Color(0xFFF9FAFB),
                surfaceVariant = if (isAmoled) AmoledSurfaceVariant else DarkSurfaceVariant,
                onSurfaceVariant = Color(0xFFD1D5DB)
            )
        }
        else -> {
            lightColorScheme(
                primary = accent,
                onPrimary = Color.White,
                primaryContainer = accent.copy(alpha = 0.18f),
                onPrimaryContainer = Color(0xFF0F172A),
                secondary = accent.copy(alpha = 0.85f),
                onSecondary = Color.White,
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF334155)
            )
        }
    }

    // Animated color scheme transition for smooth color changes without restart
    val animatedPrimary = animateColorAsState(baseScheme.primary, tween(400), label = "primary").value
    val animatedBackground = animateColorAsState(baseScheme.background, tween(400), label = "bg").value
    val animatedSurface = animateColorAsState(baseScheme.surface, tween(400), label = "surface").value

    val animatedColorScheme = baseScheme.copy(
        primary = animatedPrimary,
        background = animatedBackground,
        surface = animatedSurface
    )

    val dynamicShapes = Shapes(
        extraSmall = RoundedCornerShape((cornerRoundnessDp * 0.3f).dp),
        small = RoundedCornerShape((cornerRoundnessDp * 0.5f).dp),
        medium = RoundedCornerShape((cornerRoundnessDp * 0.75f).dp),
        large = RoundedCornerShape(cornerRoundnessDp.dp),
        extraLarge = RoundedCornerShape((cornerRoundnessDp * 1.4f).dp)
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        shapes = dynamicShapes,
        content = content
    )
}
