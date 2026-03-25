package com.jobassistant.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.jobassistant.debug.DebugSeedViewModel
import com.jobassistant.debug.SeedState

@Composable
internal fun DebugSeedSection(
    debugVm: DebugSeedViewModel = hiltViewModel()
) {
    val seedState by debugVm.seedState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(seedState) {
        if (seedState is SeedState.Done) {
            val count = (seedState as SeedState.Done).count
            snackbarHostState.showSnackbar("Test data loaded — $count jobs seeded")
            debugVm.resetSeedState()
        }
    }

    OutlinedButton(
        onClick = { debugVm.seed() },
        enabled = seedState !is SeedState.Seeding,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seed_test_data_button")
    ) {
        if (seedState is SeedState.Seeding) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(8.dp))
        }
        Text("Seed Test Data")
    }
}
