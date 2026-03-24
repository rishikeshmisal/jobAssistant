package com.jobassistant.ui.screens.detail

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.domain.model.ALL_STATUSES
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.displayName
import com.jobassistant.ui.components.FitScoreRing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val EXPIRY_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000

private fun isExpired(lastSeenDate: Long, status: ApplicationStatus): Boolean =
    status == ApplicationStatus.INTERESTED &&
        (System.currentTimeMillis() - lastSeenDate) > EXPIRY_THRESHOLD_MS

private val JOB_DESCRIPTION_TABS = listOf("Paste Text", "Paste URL", "Screenshot")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    onBack: () -> Unit = {},
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val salaryRange by viewModel.salaryRange.collectAsStateWithLifecycle()
    val appliedDate by viewModel.appliedDate.collectAsStateWithLifecycle()
    val interviewDate by viewModel.interviewDate.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val jobDescription by viewModel.jobDescription.collectAsStateWithLifecycle()
    val ocrText by viewModel.ocrText.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.analyzeFromScreenshot(it, context) }
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar("Changes saved")
            viewModel.clearSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        val isTyped = uiState.errorType != null && uiState.errorType != ApiErrorType.UNKNOWN
        if (!isTyped) {
            uiState.error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.job?.companyName ?: "Job Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete job",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.job == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Job not found") }

            else -> {
                val job = uiState.job!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Header ──────────────────────────────────────────────
                    Column {
                        Text(
                            text = job.companyName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = job.roleTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isExpired(job.lastSeenDate, job.status)) {
                            Spacer(Modifier.height(4.dp))
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Posting may be expired") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }

                    // ── Status dropdown ─────────────────────────────────────
                    StatusDropdown(
                        currentStatus = status,
                        onStatusSelected = { viewModel.status.value = it }
                    )

                    // ── Fit Score card ──────────────────────────────────────
                    val displayScore = uiState.fitAnalysis?.score ?: job.fitScore
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FitScoreRing(score = displayScore, size = 96.dp, strokeWidth = 10.dp)
                            Spacer(Modifier.height(4.dp))
                            Text("Fit Score", style = MaterialTheme.typography.labelMedium)

                            val inlineError = when (uiState.errorType) {
                                ApiErrorType.AUTH -> "API key invalid — check your key in Settings"
                                ApiErrorType.RATE_LIMIT -> "Service busy — please try again in a minute"
                                else -> null
                            }
                            if (inlineError != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = inlineError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // ── Pros / Cons / Missing skills ────────────────────────
                    val analysis = uiState.fitAnalysis
                    if (analysis != null) {
                        ExpandableSection("Pros (${analysis.pros.size})", analysis.pros)
                        ExpandableSection("Cons (${analysis.cons.size})", analysis.cons)
                        ExpandableSection("Missing Skills (${analysis.missingSkills.size})", analysis.missingSkills)
                    }

                    // ── Job Description evaluation section ──────────────────
                    JobDescriptionSection(
                        jobDescription = jobDescription,
                        ocrText = ocrText,
                        isAnalyzing = uiState.isAnalyzing,
                        retryAvailableAt = uiState.retryAvailableAt,
                        onJobDescriptionChange = { viewModel.jobDescription.value = it },
                        onAnalyzePaste = { viewModel.analyzeFromPaste(jobDescription) },
                        onAnalyzeUrl = { viewModel.analyzeFromUrl(jobDescription) },
                        onPickScreenshot = { imagePickerLauncher.launch("image/*") },
                        onAnalyzeOcr = { viewModel.analyzeFromPaste(ocrText) }
                    )

                    // ── Editable fields ─────────────────────────────────────
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.notes.value = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { viewModel.location.value = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = salaryRange,
                        onValueChange = { viewModel.salaryRange.value = it },
                        label = { Text("Salary Range") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "Applied Date",
                        dateMs = appliedDate,
                        onDateSelected = { viewModel.appliedDate.value = it }
                    )

                    DatePickerField(
                        label = "Interview Date",
                        dateMs = interviewDate,
                        onDateSelected = { viewModel.interviewDate.value = it }
                    )

                    if (job.linkedEmailThreadIds.isNotEmpty()) {
                        LinkedEmailsSection(threadIds = job.linkedEmailThreadIds)
                    }

                    Button(
                        onClick = { viewModel.saveChanges() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Job") },
            text = { Text("Are you sure you want to delete this job application?") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onBack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Job Description section ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobDescriptionSection(
    jobDescription: String,
    ocrText: String,
    isAnalyzing: Boolean,
    retryAvailableAt: Long?,
    onJobDescriptionChange: (String) -> Unit,
    onAnalyzePaste: () -> Unit,
    onAnalyzeUrl: () -> Unit,
    onPickScreenshot: () -> Unit,
    onAnalyzeOcr: () -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val isRateLimited = retryAvailableAt != null && System.currentTimeMillis() < retryAvailableAt

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Job Description",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

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
                JOB_DESCRIPTION_TABS.forEachIndexed { index, label ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isAnalyzing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text("Analyzing fit…", style = MaterialTheme.typography.bodySmall)
            } else {
                when (tabIndex) {
                    0 -> { // Paste Text
                        OutlinedTextField(
                            value = jobDescription,
                            onValueChange = { if (it.length <= 4000) onJobDescriptionChange(it) },
                            label = { Text("Paste job description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 10,
                            supportingText = { Text("${jobDescription.length} / 4000") }
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onAnalyzePaste,
                            enabled = jobDescription.isNotBlank() && !isRateLimited,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Analyze Fit")
                        }
                    }
                    1 -> { // Paste URL
                        OutlinedTextField(
                            value = jobDescription,
                            onValueChange = onJobDescriptionChange,
                            label = { Text("Job posting URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onAnalyzeUrl,
                            enabled = jobDescription.isNotBlank() && !isRateLimited,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fetch & Analyze")
                        }
                    }
                    2 -> { // Screenshot
                        Button(
                            onClick = onPickScreenshot,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pick Screenshot")
                        }
                        if (ocrText.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = ocrText,
                                onValueChange = {},
                                label = { Text("Extracted text") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                minLines = 4,
                                maxLines = 8
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onAnalyzeOcr,
                                enabled = !isRateLimited,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Analyze Fit")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Expandable section ────────────────────────────────────────────────────────

@Composable
private fun ExpandableSection(title: String, items: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                items.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

// ── Status dropdown ───────────────────────────────────────────────────────────

@Composable
private fun StatusDropdown(
    currentStatus: ApplicationStatus,
    onStatusSelected: (ApplicationStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(currentStatus.displayName())
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ALL_STATUSES.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.displayName()) },
                    onClick = { onStatusSelected(s); expanded = false }
                )
            }
        }
    }
}

// ── Date picker ───────────────────────────────────────────────────────────────

@Composable
private fun DatePickerField(label: String, dateMs: Long?, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    OutlinedButton(onClick = {
        val cal = Calendar.getInstance()
        dateMs?.let { cal.timeInMillis = it }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = Calendar.getInstance().also {
                    it.set(year, month, day, 0, 0, 0); it.set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selected.timeInMillis)
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }, modifier = Modifier.fillMaxWidth()) {
        Text(if (dateMs != null) "$label: ${dateFormat.format(Date(dateMs))}" else "Set $label")
    }
}

// ── Linked emails ─────────────────────────────────────────────────────────────

@Composable
private fun LinkedEmailsSection(threadIds: List<String>) {
    Column {
        Text("Linked Emails", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        threadIds.forEach { threadId ->
            SuggestionChip(onClick = {}, label = { Text(threadId, maxLines = 1) })
            Spacer(Modifier.height(4.dp))
        }
    }
}
