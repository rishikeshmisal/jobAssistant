package com.jobassistant.ui.screens.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jobassistant.data.remote.model.FitAnalysis

private val INPUT_TAB_LABELS = listOf("Paste Text", "Paste URL", "Screenshot")

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
    var tabIndex by remember { mutableIntStateOf(0) }   // 0=Paste, 1=URL, 2=Screenshot

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processScreenshot(it, context) }
    }

    // Auto-switch to Screenshot tab and start OCR when launched via share intent
    LaunchedEffect(initialImageUri) {
        initialImageUri?.let { uri ->
            tabIndex = 2
            viewModel.processScreenshot(uri, context)
            onImageConsumed()
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

            // ── Input mode tabs with rounded indicator ────────────────────────
            TabRow(
                selectedTabIndex = tabIndex,
                indicator = { tabPositions ->
                    if (tabIndex < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[tabIndex])
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            ) {
                INPUT_TAB_LABELS.forEachIndexed { index, label ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(label) },
                        modifier = Modifier.testTag("tab_${label.lowercase().replace(" ", "_")}")
                    )
                }
            }

            when (tabIndex) {
                0 -> { // Paste
                    OutlinedTextField(
                        value = jobDescription,
                        onValueChange = { if (it.length <= 4000) jobDescription = it },
                        label = { Text("Job Description") },
                        modifier = Modifier.fillMaxWidth().testTag("job_description_field"),
                        minLines = 5,
                        maxLines = 10,
                        supportingText = { Text("${jobDescription.length} / 4000") }
                    )
                    Button(
                        onClick = { viewModel.analyzeFit(jobDescription) },
                        enabled = jobDescription.isNotBlank() && uiState !is AddJobUiState.Analyzing,
                        modifier = Modifier.fillMaxWidth().testTag("analyze_fit_button")
                    ) {
                        Text("Analyze Fit")
                    }
                }

                1 -> { // URL
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

                2 -> { // Screenshot
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

            // ── Loading state ─────────────────────────────────────────────────
            if (uiState is AddJobUiState.Analyzing) {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("loading_indicator"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text("Analyzing fit…", style = MaterialTheme.typography.bodySmall)
                }
            }

            // ── Score reveal with animation ───────────────────────────────────
            AnimatedVisibility(
                visible = uiState is AddJobUiState.FitResult,
                enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.85f)
            ) {
                if (uiState is AddJobUiState.FitResult) {
                    val state = uiState as AddJobUiState.FitResult
                    FitResultSection(
                        analysis = state.analysis,
                        onSave = {
                            val description = when (tabIndex) {
                                0 -> jobDescription
                                1 -> jobUrl
                                else -> ocrText
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
            }

            when (val state = uiState) {
                is AddJobUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("error_text")
                    )
                }
                is AddJobUiState.Saving -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                is AddJobUiState.Saved -> {
                    Text(
                        text = "Job saved successfully!",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.testTag("saved_text")
                    )
                }
                else -> Unit
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

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().testTag("save_job_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Save Job")
        }
    }
}
