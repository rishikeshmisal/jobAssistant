package com.jobassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.jobassistant.domain.model.AppTheme

@Composable
fun JobAssistantTheme(
    appTheme: AppTheme = AppTheme.GREEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val seedColor = ThemeSeedColors[appTheme] ?: ThemeSeedColors.getValue(AppTheme.GREEN)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
