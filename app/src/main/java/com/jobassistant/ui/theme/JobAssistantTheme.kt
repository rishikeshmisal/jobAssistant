package com.jobassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.jobassistant.domain.model.AppTheme

@Composable
fun JobAssistantTheme(
    appTheme: AppTheme = AppTheme.GREEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val seedColor = ThemeSeedColors[appTheme] ?: ThemeSeedColors.getValue(AppTheme.GREEN)

    // 20% seed blended into white (light mode) or black (dark mode)
    val background = if (darkTheme)
        lerp(Color.Black, seedColor, 0.20f)
    else
        lerp(Color.White, seedColor, 0.20f)

    val surface = if (darkTheme)
        lerp(Color.Black, seedColor, 0.15f)
    else
        lerp(Color.White, seedColor, 0.15f)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor,
            background = background,
            surface = surface
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor,
            background = background,
            surface = surface
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
