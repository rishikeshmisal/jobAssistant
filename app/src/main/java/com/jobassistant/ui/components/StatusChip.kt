package com.jobassistant.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jobassistant.domain.model.ApplicationStatus

fun statusContainerColor(status: ApplicationStatus): Color = when (status) {
    ApplicationStatus.SAVED        -> Color(0xFFE8EAF6)
    ApplicationStatus.APPLIED      -> Color(0xFFE3F2FD)
    ApplicationStatus.INTERVIEWING -> Color(0xFFE8F5E9)
    ApplicationStatus.OFFERED      -> Color(0xFFF1F8E9)
    ApplicationStatus.REJECTED     -> Color(0xFFFFEBEE)
}

fun statusLabelColor(status: ApplicationStatus): Color = when (status) {
    ApplicationStatus.SAVED        -> Color(0xFF3949AB)
    ApplicationStatus.APPLIED      -> Color(0xFF1565C0)
    ApplicationStatus.INTERVIEWING -> Color(0xFF2E7D32)
    ApplicationStatus.OFFERED      -> Color(0xFF33691E)
    ApplicationStatus.REJECTED     -> Color(0xFFC62828)
}

private fun ApplicationStatus.displayName() = when (this) {
    ApplicationStatus.SAVED        -> "Saved"
    ApplicationStatus.APPLIED      -> "Applied"
    ApplicationStatus.INTERVIEWING -> "Interviewing"
    ApplicationStatus.OFFERED      -> "Offered"
    ApplicationStatus.REJECTED     -> "Rejected"
}

@Composable
fun StatusChip(status: ApplicationStatus, modifier: Modifier = Modifier) {
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = status.displayName(),
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = statusContainerColor(status),
            labelColor = statusLabelColor(status)
        ),
        modifier = modifier
    )
}
