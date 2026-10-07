package com.example.ui.screens.equalizer

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.playback.AudioOutputType
import com.example.playback.MusicPlayerClient
import com.example.playback.PulsePlayerState
import com.example.ui.components.VuMeter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    playerState: PulsePlayerState,
    playerClient: MusicPlayerClient,
    onNavigateBack: () -> Unit
) {
    var boostPercent by remember(playerState.volumeBoostPercent) {
        mutableIntStateOf(playerState.volumeBoostPercent)
    }
    var showSafetyWarning by remember { mutableStateOf(false) }
    var pendingBoostValue by remember { mutableIntStateOf(100) }

    var eqEnabled by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableIntStateOf(0) }
    var bassBoostVal by remember { mutableFloatStateOf(0f) }
    var virtualizerVal by remember { mutableFloatStateOf(0f) }

    val presets = listOf("Flat", "Bass Boost", "Rock", "Pop", "Jazz", "Classical", "Vocal")

    val effectsController = playerClient.getEffectsController()

    // 5-band levels
    var band0 by remember { mutableFloatStateOf(0f) }
    var band1 by remember { mutableFloatStateOf(0f) }
    var band2 by remember { mutableFloatStateOf(0f) }
    var band3 by remember { mutableFloatStateOf(0f) }
    var band4 by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sound & Maximum Boost", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            // Section 1: Maximum Volume Engine (Beyond VLC 200%)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VOLUME BOOST ENGINE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${boostPercent}%" + if (boostPercent > 100) " (+${((boostPercent - 100) * 0.04).toInt()} dB)" else " (Unity)",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Output Profile Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = when (playerState.audioOutputType) {
                                    AudioOutputType.BLUETOOTH -> "BLUETOOTH"
                                    AudioOutputType.HEADPHONES -> "HEADPHONES"
                                    AudioOutputType.SPEAKER -> "SPEAKER"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live VU Level & Limiter Meter
                    VuMeter(
                        boostPercent = boostPercent,
                        isLimiterEngaged = playerState.isLimiterActive,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Boost Slider
                    Slider(
                        value = boostPercent.toFloat(),
                        onValueChange = { value ->
                            val target = value.toInt()
                            if (target > 200 && boostPercent <= 200) {
                                pendingBoostValue = target
                                showSafetyWarning = true
                            } else {
                                boostPercent = target
                                playerClient.setVolumeBoost(target)
                            }
                        },
                        valueRange = 0f..400f,
                        steps = 39,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("boost_slider")
                    )

                    // Presets chips: 100% Normal, 200% Loud, 300% Super, 400% Max (Horizontally Scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            100 to "100% Normal",
                            200 to "200% Loud",
                            300 to "300% Super",
                            400 to "400% Max"
                        ).forEach { (pct, label) ->
                            FilterChip(
                                selected = boostPercent == pct,
                                onClick = {
                                    if (pct > 200 && boostPercent <= 200) {
                                        pendingBoostValue = pct
                                        showSafetyWarning = true
                                    } else {
                                        boostPercent = pct
                                        playerClient.setVolumeBoost(pct)
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hardware-safe LoudnessEnhancer with API 28+ DynamicsProcessing brickwall limiter stage (-0.5 dBFS). Exceeding 200% safely increases volume while clamping distortion.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section 2: Audio Offload & Battery Trade-off card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (playerState.isAudioOffloadActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (playerState.isAudioOffloadActive) MaterialTheme.colorScheme.primary else Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.height(0.dp))
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = if (playerState.isAudioOffloadActive)
                                "Efficiency Mode: DSP Audio Offload is Active"
                            else
                                "Efficiency Mode: Audio Offload Suspended",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (playerState.isAudioOffloadActive)
                                "Audio is decoded directly on the low-power DSP, allowing CPU sleep (~1.8% battery/hr)."
                            else
                                "Active software equalizer or volume boost requires CPU audio post-processing (~4.5% battery/hr).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 3: Equalizer
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GRAPHIC EQUALIZER",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (eqEnabled) "Enabled" else "Bypassed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = eqEnabled,
                            onCheckedChange = {
                                eqEnabled = it
                                effectsController?.setEqualizerEnabled(it)
                            }
                        )
                    }

                    if (eqEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset chips (Scrollable row for all presets)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEachIndexed { index, name ->
                                FilterChip(
                                    selected = selectedPreset == index,
                                    onClick = {
                                        selectedPreset = index
                                        effectsController?.setEqualizerPreset(index.toShort())
                                    },
                                    label = { Text(name, maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5 Bands
                        listOf(
                            "60 Hz" to band0,
                            "230 Hz" to band1,
                            "910 Hz" to band2,
                            "3.6 kHz" to band3,
                            "14 kHz" to band4
                        ).forEachIndexed { bandIdx, (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Slider(
                                    value = value,
                                    onValueChange = { newVal ->
                                        when (bandIdx) {
                                            0 -> band0 = newVal
                                            1 -> band1 = newVal
                                            2 -> band2 = newVal
                                            3 -> band3 = newVal
                                            4 -> band4 = newVal
                                        }
                                        effectsController?.setEqualizerBandLevel(
                                            bandIdx.toShort(),
                                            (newVal * 100).toInt().toShort()
                                        )
                                    },
                                    valueRange = -10f..10f,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${value.toInt()} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bass Boost & Virtualizer
                        Text("Bass Boost", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Slider(
                            value = bassBoostVal,
                            onValueChange = {
                                bassBoostVal = it
                                effectsController?.setBassBoost((it * 10).toInt().toShort())
                            },
                            valueRange = 0f..100f
                        )

                        Text("Virtualizer (Surround)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Slider(
                            value = virtualizerVal,
                            onValueChange = {
                                virtualizerVal = it
                                effectsController?.setVirtualizer((it * 10).toInt().toShort())
                            },
                            valueRange = 0f..100f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Safety warning dialog when enabling boost > 200%
        if (showSafetyWarning) {
            AlertDialog(
                onDismissRequest = { showSafetyWarning = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
                title = { Text("Hearing & Hardware Protection Warning") },
                text = {
                    Text(
                        "Listening at extreme volume levels above 200% (+6 dB to +12 dB) for extended periods can cause hearing impairment and may strain phone speakers. AxSound engages a limiter stage to clamp audio spikes, but user caution is advised."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            boostPercent = pendingBoostValue
                            playerClient.setVolumeBoost(pendingBoostValue)
                            showSafetyWarning = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("I Understand, Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSafetyWarning = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
