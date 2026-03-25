package com.jobassistant.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

@Composable
fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onTrackJob: () -> Unit,
    onEvaluateFit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini FABs — visible only when expanded
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.7f),
            exit = fadeOut(tween(100)) + scaleOut(tween(100))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedDialItem(
                    label = "Evaluate Fit",
                    icon = Icons.Filled.Analytics,
                    onClick = onEvaluateFit,
                    testTag = "evaluate_fit_fab"
                )
                SpeedDialItem(
                    label = "Track Job",
                    icon = Icons.Filled.Add,
                    onClick = onTrackJob,
                    testTag = "track_job_fab"
                )
            }
        }

        // Main FAB — always visible
        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier.testTag("speed_dial_main_fab")
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = if (expanded) "Close" else "Add"
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.clearAndSetSemantics {}   // hide label from accessibility tree
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}
