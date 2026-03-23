package com.jobassistant.ui.screens.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jobassistant.data.remote.model.FitAnalysis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobScreen(
    onBack: () -> Unit = {},
    initialImageUri: Uri? = null,
    onImageConsumed: () -> Unit = {},
    viewModel: AddJobViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ocrText by viewModel.ocrText.collectAsState()
    val context = LocalContext.current

    var companyName by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }
    var jobUrl by remember { mutableStateOf("") }
    var inputMode by remember { mutableStateOf(InputMode.PASTE) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processScreenshot(it, context) }
    }

    // Auto-switch to Screenshot tab and start OCR when launched via share intent
    LaunchedEffect(initialImageUri) {
        initialImageUri?.let { uri ->
            inputMode = InputMode.SCREENSHOT
            viewModel.processScreenshot(uri, context)
            onImageConsumed()  // clear the URI in MainActivity so it isn't re-processed
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
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth().testTag("location_field"),
                singleLine = true
            )

            OutlinedTextField(
                value = salaryRange,
                onValueChange = { salaryRange = it },
                label = { Text("Salary Range") },
                modifier = Modifier.fillMaxWidth().testTag("salary_range_field"),
                singleLine = true
            )

            // Input mode toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = inputMode == InputMode.PASTE,
                    onClick = { inputMode = InputMode.PASTE },
                    label = { Text("Paste Text") },
                    modifier = Modifier.testTag("mode_paste_chip")
                )
                FilterChip(
                    selected = inputMode == InputMode.URL,
                    onClick = { inputMode = InputMode.URL },
                    label = { Text("Paste URL") },
                    modifier = Modifier.testTag("mode_url_chip")
                )
                FilterChip(
                    selected = inputMode == InputMode.SCREENSHOT,
                    onClick = { inputMode = InputMode.SCREENSHOT },
                    label = { Text("Screenshot") },
                    modifier = Modifier.testTag("mode_screenshot_chip")
                )
            }

            when (inputMode) {
                InputMode.PASTE -> {
                    OutlinedTextField(
                        value = jobDescription,
                        onValueChange = { jobDescription = it },
                        label = { Text("Job Description") },
                        modifier = Modifier.fillMaxWidth().testTag("job_description_field"),
                        minLines = 5,
                        maxLines = 10
                    )
                    Button(
                        onClick = { viewModel.analyzeFit(jobDescription) },
                        enabled = jobDescription.isNotBlank() && uiState !is AddJobUiState.Analyzing,
                        modifier = Modifier.fillMaxWidth().testTag("analyze_fit_button")
                    ) {
                        Text("Analyze Fit")
                    }
                }

                InputMode.URL -> {
                    OutlinedTextField(
                        value = jobUrl,
                        onValueChange = { jobUrl = it },
                        label = { Text("Job Posting URL") },
                        modifier = Modifier.fillMaxWidth().testTag("job_url_field"),
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.fetchAndAnalyzeUrl(jobUrl) },
                        enabled = jobUrl.isNotBlank() && uiState !is AddJobUiState.Analyzing,
                        modifier = Modifier.fillMaxWidth().testTag("fetch_url_button")
                    ) {
                        Text("Fetch & Analyze")
                    }
                }

                InputMode.SCREENSHOT -> {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().testTag("pick_screenshot_button")
                    ) {
                        Text("Pick Screenshot")
                    }
                    if (ocrText.isNotBlank()) {
                        OutlinedTextField(
                            value = ocrText,
                            onValueChange = {},
                            label = { Text("Extracted Text") },
                            modifier = Modifier.fillMaxWidth().testTag("ocr_text_preview"),
                            readOnly = true,
                            minLines = 5,
                            maxLines = 10
                        )
                    }
                    Button(
                        onClick = { viewModel.analyzeFit(ocrText) },
                        enabled = ocrText.isNotBlank() && uiState !is AddJobUiState.Analyzing,
                        modifier = Modifier.fillMaxWidth().testTag("analyze_fit_button")
                    ) {
                        Text("Analyze Fit")
                    }
                }
            }

            when (val state = uiState) {
                is AddJobUiState.Analyzing -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().testTag("loading_indicator"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AddJobUiState.FitResult -> {
                    FitResultSection(
                        analysis = state.analysis,
                        onSave = {
                            val description = when (inputMode) {
                                InputMode.PASTE -> jobDescription
                                InputMode.URL -> jobUrl
                                InputMode.SCREENSHOT -> ocrText
                            }
                            viewModel.saveJob(
                                companyName = companyName,
                                roleTitle = roleTitle,
                                jobDescription = description,
                                location = location,
                                salaryRange = salaryRange,
                                fitScore = state.analysis.score
                            )
                        }
                    )
                }

                is AddJobUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("error_text")
                    )
                }

                is AddJobUiState.Saving -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AddJobUiState.Saved -> {
                    Text(
                        text = "Job saved successfully!",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.testTag("saved_text")
                    )
                }

                AddJobUiState.Idle -> Unit
                is AddJobUiState.Duplicate -> Unit // Handled by dialog above
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private enum class InputMode { PASTE, URL, SCREENSHOT }

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

@Composable
private fun FitResultSection(
    analysis: FitAnalysis,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Fit Score: ${analysis.score}/100",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.testTag("fit_score")
        )

        if (analysis.pros.isNotEmpty()) {
            Text("Strengths", style = MaterialTheme.typography.titleMedium)
            analysis.pros.forEach { pro ->
                Text(
                    text = "✓ $pro",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.testTag("pro_item")
                )
            }
        }

        if (analysis.cons.isNotEmpty()) {
            Text("Weaknesses", style = MaterialTheme.typography.titleMedium)
            analysis.cons.forEach { con ->
                Text(
                    text = "✗ $con",
                    color = Color(0xFFC62828),
                    modifier = Modifier.testTag("con_item")
                )
            }
        }

        if (analysis.missingSkills.isNotEmpty()) {
            Text("Missing Skills", style = MaterialTheme.typography.titleMedium)
            analysis.missingSkills.forEach { skill ->
                Text(
                    text = "⚠ $skill",
                    color = Color(0xFFE65100),
                    modifier = Modifier.testTag("missing_skill_item")
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).testTag("save_job_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save Job")
            }
        }
    }
}
