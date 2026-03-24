package com.jobassistant.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.displayName

fun statusContainerColor(status: ApplicationStatus): Color = when (status) {
    ApplicationStatus.INTERESTED   -> Color(0xFFE8EAF6)
    ApplicationStatus.APPLIED      -> Color(0xFFE3F2FD)
    ApplicationStatus.SCREENING    -> Color(0xFFE0F7FA)
    ApplicationStatus.INTERVIEWING -> Color(0xFFFFF8E1)
    ApplicationStatus.ASSESSMENT   -> Color(0xFFF3E5F5)
    ApplicationStatus.OFFER        -> Color(0xFFF1F8E9)
    ApplicationStatus.ACCEPTED     -> Color(0xFFE8F5E9)
    ApplicationStatus.REJECTED     -> Color(0xFFFFEBEE)
    ApplicationStatus.WITHDRAWN    -> Color(0xFFFAFAFA)
    ApplicationStatus.NO_RESPONSE  -> Color(0xFFECEFF1)
}

fun statusLabelColor(status: ApplicationStatus): Color = when (status) {
    ApplicationStatus.INTERESTED   -> Color(0xFF3949AB)
    ApplicationStatus.APPLIED      -> Color(0xFF1565C0)
    ApplicationStatus.SCREENING    -> Color(0xFF00838F)
    ApplicationStatus.INTERVIEWING -> Color(0xFFF57F17)
    ApplicationStatus.ASSESSMENT   -> Color(0xFF7B1FA2)
    ApplicationStatus.OFFER        -> Color(0xFF33691E)
    ApplicationStatus.ACCEPTED     -> Color(0xFF1B5E20)
    ApplicationStatus.REJECTED     -> Color(0xFFC62828)
    ApplicationStatus.WITHDRAWN    -> Color(0xFF757575)
    ApplicationStatus.NO_RESPONSE  -> Color(0xFF546E7A)
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
