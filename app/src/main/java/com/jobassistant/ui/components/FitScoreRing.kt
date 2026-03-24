package com.jobassistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val ARC_START = 135f
private const val ARC_TOTAL = 270f

internal fun fitScoreToSweep(score: Int?): Float =
    if (score == null) 0f else (score / 100f) * ARC_TOTAL

fun fitScoreColor(score: Int?): Color = when {
    score == null -> Color.Gray
    score < 40   -> Color(0xFFE53935)
    score <= 70  -> Color(0xFFFB8C00)
    else         -> Color(0xFF43A047)
}

@Composable
fun FitScoreRing(
    score: Int?,
    size: Dp = 72.dp,
    strokeWidth: Dp = 7.dp,
    modifier: Modifier = Modifier
) {
    val animatedSweep by animateFloatAsState(
        targetValue = fitScoreToSweep(score),
        animationSpec = tween(durationMillis = 800),
        label = "fitScoreSweep"
    )
    val ringColor = fitScoreColor(score)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)
            // Background track
            drawArc(
                color = trackColor,
                startAngle = ARC_START,
                sweepAngle = ARC_TOTAL,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            // Filled arc
            if (animatedSweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = ARC_START,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
        }
        Text(
            text = if (score != null) "$score" else "N/A",
            style = if (size >= 72.dp) MaterialTheme.typography.labelLarge
                    else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ringColor
        )
    }
}
