package com.jobassistant.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobScreen(
    onBack: () -> Unit = {},
    onSaved: (String) -> Unit = {},
    viewModel: AddJobViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var companyName by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }

    // Navigate to job detail when saved
    LaunchedEffect(uiState) {
        if (uiState is AddJobUiState.Saved) {
            onSaved((uiState as AddJobUiState.Saved).jobId.toString())
        }
    }

    // Duplicate confirmation dialog
    if (uiState is AddJobUiState.Duplicate) {
        val dupState = uiState as AddJobUiState.Duplicate
        DuplicateJobDialog(
            companyName = dupState.companyName,
            roleTitle = dupState.roleTitle,
            onDismiss = { viewModel.dismissDuplicate() },
            onSaveAnyway = { viewModel.saveJobForce() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Job") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth().testTag("company_name_field"),
                singleLine = true
            )

            OutlinedTextField(
                value = roleTitle,
                onValueChange = { roleTitle = it },
                label = { Text("Role Title") },
                modifier = Modifier.fillMaxWidth().testTag("role_title_field"),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (optional)") },
                modifier = Modifier.fillMaxWidth().testTag("location_field"),
                singleLine = true
            )

            OutlinedTextField(
                value = salaryRange,
                onValueChange = { salaryRange = it },
                label = { Text("Salary Range (optional)") },
                modifier = Modifier.fillMaxWidth().testTag("salary_range_field"),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveJob(
                        companyName = companyName,
                        roleTitle = roleTitle,
                        location = location,
                        salaryRange = salaryRange
                    )
                },
                enabled = companyName.isNotBlank() && roleTitle.isNotBlank()
                        && uiState !is AddJobUiState.Saving,
                modifier = Modifier.fillMaxWidth().testTag("save_job_button")
            ) {
                if (uiState is AddJobUiState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Job")
                }
            }

            if (uiState is AddJobUiState.Error) {
                Text(
                    text = (uiState as AddJobUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("error_text")
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun DuplicateJobDialog(
    companyName: String,
    roleTitle: String,
    onDismiss: () -> Unit,
    onSaveAnyway: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duplicate Job") },
        text = {
            Text(
                "A job at $companyName for $roleTitle already exists. " +
                        "Update the existing entry or create a new one?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSaveAnyway,
                modifier = Modifier.testTag("save_anyway_button")
            ) {
                Text("Save Anyway")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_duplicate_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
