package com.jobassistant.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.jobassistant.domain.model.AppTheme

@Composable
fun JobAssistantTheme(
    appTheme: AppTheme = AppTheme.GREEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val seedColor = ThemeSeedColors[appTheme] ?: ThemeSeedColors.getValue(AppTheme.GREEN)
    val context = LocalContext.current

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor
        )
        else -> lightColorScheme(
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
