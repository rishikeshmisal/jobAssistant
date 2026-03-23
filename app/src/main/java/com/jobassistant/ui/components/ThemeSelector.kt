package com.jobassistant.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jobassistant.domain.model.AppTheme
import com.jobassistant.ui.theme.ThemeSeedColors

@Composable
fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTheme.values().forEach { theme ->
            val color = ThemeSeedColors[theme] ?: Color.Gray
            val isSelected = theme == selectedTheme
            FilterChip(
                selected = isSelected,
                onClick = { onThemeSelected(theme) },
                label = { Text(theme.name.take(1)) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color,
                    selectedLeadingIconColor = color
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = color,
                    selectedBorderWidth = 2.dp
                ),
                modifier = Modifier.border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) color else Color.Gray.copy(alpha = 0.4f),
                    shape = CircleShape
                )
            )
        }
    }
}
