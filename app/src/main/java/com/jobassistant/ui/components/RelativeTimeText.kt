package com.jobassistant.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun relativeTimeString(
    epochMillis: Long?,
    now: Long = System.currentTimeMillis()
): String {
    if (epochMillis == null) return "—"
    val diffMs = now - epochMillis
    return when {
        diffMs < 0 ->
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
        diffMs < 60 * 60 * 1000L ->
            "just now"
        diffMs < 24 * 60 * 60 * 1000L -> {
            val hours = diffMs / (60 * 60 * 1000L)
            "$hours hour${if (hours == 1L) "" else "s"} ago"
        }
        diffMs < 7 * 24 * 60 * 60 * 1000L -> {
            val days = diffMs / (24 * 60 * 60 * 1000L)
            "$days day${if (days == 1L) "" else "s"} ago"
        }
        else ->
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
    }
}

@Composable
fun RelativeTimeText(
    epochMillis: Long?,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = relativeTimeString(epochMillis),
        style = style,
        color = color,
        modifier = modifier
    )
}
