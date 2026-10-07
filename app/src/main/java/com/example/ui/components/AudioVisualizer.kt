package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormatBadge(
    formatText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = formatText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    if (!isPlaying) {
        // Flat bars when paused to save CPU/battery
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(barCount) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(activeColor.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
            }
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "visualizer")
    val anim1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "v1"
    )
    val anim2 by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "v2"
    )
    val anim3 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "v3"
    )

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val barWidth = (totalWidth / barCount) * 0.7f
        val spacing = (totalWidth / barCount) * 0.3f
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val factor = when (i % 3) {
                0 -> anim1 * (0.5f + 0.5f * (i % 5) / 5f)
                1 -> anim2 * (0.6f + 0.4f * (i % 4) / 4f)
                else -> anim3 * (0.4f + 0.6f * (i % 6) / 6f)
            }.coerceIn(0.1f, 1f)

            val currentHeight = maxHeight * factor
            val left = i * (barWidth + spacing)
            val top = (maxHeight - currentHeight) / 2f

            drawRoundRect(
                color = activeColor.copy(alpha = 0.65f + 0.35f * factor),
                topLeft = Offset(left, top),
                size = Size(barWidth, currentHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun VuMeter(
    boostPercent: Int,
    isLimiterEngaged: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PEAK LIMITER & SIGNAL STRENGTH",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isLimiterEngaged) "LIMITER ENGAGED (-0.5 dBFS)" else if (boostPercent > 100) "BOOSTED" else "UNITY (0 dB)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isLimiterEngaged) Color(0xFFF59E0B) else if (boostPercent > 200) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
            )
        }

        // Multi-segment LED bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val segments = 20
            val activeSegments = ((boostPercent / 400f) * segments).toInt().coerceIn(1, segments)

            repeat(segments) { index ->
                val color = when {
                    index >= 16 -> Color(0xFFEF4444) // Red: > 300%
                    index >= 10 -> Color(0xFFF59E0B) // Amber: 200% - 300%
                    else -> Color(0xFF10B981)        // Green: Safe unity
                }
                val alpha = if (index < activeSegments) 1.0f else 0.15f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(color.copy(alpha = alpha), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
