package com.example.ui.screens.appearance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.CuratedAccentColors
import com.example.ui.theme.GradientThemes
import com.example.ui.theme.parseHexColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    userPreferences: UserPreferences,
    preferencesRepository: PreferencesRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var customHexInput by remember { mutableStateOf(userPreferences.accentColorHex) }
    var showCustomHexDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance & Personalization", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Export Theme JSON")
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Import Theme JSON")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Live Preview Card
            Surface(
                shape = RoundedCornerShape(userPreferences.cornerRoundnessDp.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_live_preview")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE THEME PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape((userPreferences.cornerRoundnessDp * 0.5f).dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Midnight Resonance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aura Synthetics • AxSound Lossless",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Theme Mode (Light, Dark, AMOLED, System)
            Column {
                Text(
                    text = "THEME MODE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        FilterChip(
                            selected = userPreferences.themeMode == mode,
                            onClick = {
                                scope.launch { preferencesRepository.setThemeMode(mode) }
                            },
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> "System"
                                        ThemeMode.LIGHT -> "Light"
                                        ThemeMode.DARK -> "Dark"
                                        ThemeMode.AMOLED -> "AMOLED Black"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Curated Accent Color Palette (16+ Colors)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCENT COLOR",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showCustomHexDialog = true }) {
                        Text("Custom HEX")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Color Swatches Grid (4x4)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CuratedAccentColors.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row.forEach { (hex, name) ->
                                val color = parseHexColor(hex)
                                val isSelected = userPreferences.accentColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            scope.launch { preferencesRepository.setAccentColor(hex) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Gradient Theme Presets
            Column {
                Text(
                    text = "BACKGROUND GRADIENT THEMES",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradientThemes.presets.forEach { preset ->
                        FilterChip(
                            selected = userPreferences.gradientThemeName.equals(preset.name, ignoreCase = true),
                            onClick = {
                                scope.launch { preferencesRepository.setGradientTheme(preset.name) }
                            },
                            label = { Text(preset.name, maxLines = 1) }
                        )
                    }
                }
            }

            // Dynamic Album Art and Material You Wallpaper Toggles
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adaptive Album Art Colors",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Extract player gradient accents dynamically from the current album cover",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = userPreferences.useAdaptiveAlbumArtColor,
                        onCheckedChange = {
                            scope.launch { preferencesRepository.setUseAdaptiveAlbumArtColor(it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Material You Dynamic Wallpaper Color",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Derive app palette from device system wallpaper (Android 12+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = userPreferences.useDynamicColor,
                        onCheckedChange = {
                            scope.launch { preferencesRepository.setUseDynamicColor(it) }
                        }
                    )
                }
            }

            // Corner Roundness Slider (12dp to 28dp)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CORNER ROUNDNESS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("${userPreferences.cornerRoundnessDp} dp", style = MaterialTheme.typography.labelMedium)
                }
                Slider(
                    value = userPreferences.cornerRoundnessDp.toFloat(),
                    onValueChange = {
                        scope.launch { preferencesRepository.setCornerRoundness(it.toInt()) }
                    },
                    valueRange = 12f..28f,
                    steps = 7
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Custom HEX dialog
        if (showCustomHexDialog) {
            AlertDialog(
                onDismissRequest = { showCustomHexDialog = false },
                title = { Text("Enter Custom Hex Color") },
                text = {
                    OutlinedTextField(
                        value = customHexInput,
                        onValueChange = { customHexInput = it },
                        label = { Text("Color Code (e.g. #FF007F)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                preferencesRepository.setAccentColor(customHexInput.trim())
                            }
                            showCustomHexDialog = false
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomHexDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Export Theme JSON Dialog
        if (showExportDialog) {
            val json = """
                {
                  "themeMode": "${userPreferences.themeMode.name}",
                  "accentColorHex": "${userPreferences.accentColorHex}",
                  "gradientThemeName": "${userPreferences.gradientThemeName}",
                  "cornerRoundnessDp": ${userPreferences.cornerRoundnessDp}
                }
            """.trimIndent()

            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Theme JSON") },
                text = {
                    Text(json, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(json))
                            Toast.makeText(context, "Theme JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        }
                    ) {
                        Text("Copy to Clipboard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) { Text("Close") }
                }
            )
        }

        // Import Theme JSON Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import Theme JSON") },
                text = {
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Paste Theme JSON") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                if (importJsonText.contains("accentColorHex")) {
                                    val regex = "\"accentColorHex\"\\s*:\\s*\"(#[A-Fa-f0-9]+)\"".toRegex()
                                    val match = regex.find(importJsonText)
                                    match?.groupValues?.get(1)?.let { hex ->
                                        scope.launch { preferencesRepository.setAccentColor(hex) }
                                    }
                                }
                                Toast.makeText(context, "Theme imported successfully", Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {}
                            showImportDialog = false
                        }
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
