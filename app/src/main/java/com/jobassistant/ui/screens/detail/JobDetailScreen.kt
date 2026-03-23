package com.jobassistant.ui.screens.detail

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.domain.model.ApplicationStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ALL_STATUSES = ApplicationStatus.values().toList()

private const val EXPIRY_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000  // 30 days

private fun isExpired(lastSeenDate: Long, status: ApplicationStatus): Boolean =
    status == ApplicationStatus.SAVED &&
        (System.currentTimeMillis() - lastSeenDate) > EXPIRY_THRESHOLD_MS

private fun ApplicationStatus.displayName() = when (this) {
    ApplicationStatus.SAVED -> "Saved"
    ApplicationStatus.APPLIED -> "Applied"
    ApplicationStatus.INTERVIEWING -> "Interviewing"
    ApplicationStatus.OFFERED -> "Offered"
    ApplicationStatus.REJECTED -> "Rejected"
}

private fun fitScoreColor(score: Int): Color = when {
    score < 40 -> Color(0xFFE53935)
    score <= 70 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

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

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar("Changes saved")
            viewModel.clearSaved()
        }
    }

    // Generic errors (non-API-typed) still surface as a snackbar
    LaunchedEffect(uiState.error) {
        val isTyped = uiState.errorType != null &&
            uiState.errorType != ApiErrorType.UNKNOWN
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
                },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.job == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                    // Header
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
                        // 9.3 — expiry warning for stale SAVED postings
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

                    // Status dropdown
                    StatusDropdown(
                        currentStatus = status,
                        onStatusSelected = { viewModel.status.value = it }
                    )

                    // Fit Score card
                    val displayAnalysis = uiState.fitAnalysis
                    val displayScore = displayAnalysis?.score ?: job.fitScore
                    FitScoreCard(
                        score = displayScore,
                        fitAnalysis = displayAnalysis,
                        isAnalyzing = uiState.isAnalyzing,
                        errorType = uiState.errorType,
                        retryAvailableAt = uiState.retryAvailableAt,
                        onReAnalyze = { viewModel.reAnalyzeFit() },
                        onDismissError = { viewModel.clearError() }
                    )

                    // Editable fields
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.notes.value = it },
                        label = { Text("Notes / Job Description") },
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

                    // Date pickers
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

                    // Linked emails
                    if (job.linkedEmailThreadIds.isNotEmpty()) {
                        LinkedEmailsSection(threadIds = job.linkedEmailThreadIds)
                    }

                    // Save button
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
                    onClick = {
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusDropdown(
    currentStatus: ApplicationStatus,
    onStatusSelected: (ApplicationStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(currentStatus.displayName())
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ALL_STATUSES.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.displayName()) },
                    onClick = {
                        onStatusSelected(s)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FitScoreCard(
    score: Int?,
    fitAnalysis: FitAnalysis?,
    isAnalyzing: Boolean,
    errorType: ApiErrorType? = null,
    retryAvailableAt: Long? = null,
    onReAnalyze: () -> Unit,
    onDismissError: () -> Unit = {}
) {
    val isRateLimited = retryAvailableAt != null && System.currentTimeMillis() < retryAvailableAt
    var prosExpanded by remember { mutableStateOf(false) }
    var consExpanded by remember { mutableStateOf(false) }
    var missingExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Fit Score", style = MaterialTheme.typography.labelMedium)
                    if (score != null) {
                        Text(
                            text = "$score%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = fitScoreColor(score)
                        )
                    } else {
                        Text(
                            text = "N/A",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onReAnalyze,
                    enabled = !isAnalyzing && !isRateLimited
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (isRateLimited) "Service busy…" else "Re-analyze Fit")
                }
            }

            // Typed inline error — shown in-card for known error types
            val inlineError = when (errorType) {
                ApiErrorType.AUTH -> "API key invalid — check your key in Settings"
                ApiErrorType.RATE_LIMIT -> "Service busy — please try again in a minute"
                else -> null
            }
            if (inlineError != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = inlineError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (score != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = fitScoreColor(score),
                    trackColor = fitScoreColor(score).copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }

            val analysis = fitAnalysis
            if (analysis != null) {
                Spacer(Modifier.height(12.dp))

                ExpandableSection(
                    title = "Pros (${analysis.pros.size})",
                    expanded = prosExpanded,
                    onToggle = { prosExpanded = !prosExpanded }
                ) {
                    analysis.pros.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }

                Spacer(Modifier.height(4.dp))

                ExpandableSection(
                    title = "Cons (${analysis.cons.size})",
                    expanded = consExpanded,
                    onToggle = { consExpanded = !consExpanded }
                ) {
                    analysis.cons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }

                Spacer(Modifier.height(4.dp))

                ExpandableSection(
                    title = "Missing Skills (${analysis.missingSkills.size})",
                    expanded = missingExpanded,
                    onToggle = { missingExpanded = !missingExpanded }
                ) {
                    analysis.missingSkills.forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    dateMs: Long?,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    OutlinedButton(
        onClick = {
            val cal = Calendar.getInstance()
            dateMs?.let { cal.timeInMillis = it }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selected = Calendar.getInstance().also {
                        it.set(year, month, dayOfMonth, 0, 0, 0)
                        it.set(Calendar.MILLISECOND, 0)
                    }
                    onDateSelected(selected.timeInMillis)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (dateMs != null) "$label: ${dateFormat.format(Date(dateMs))}" else "Set $label"
        )
    }
}

@Composable
private fun LinkedEmailsSection(threadIds: List<String>) {
    Column {
        Text(
            "Linked Emails",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        threadIds.forEach { threadId ->
            SuggestionChip(
                onClick = { /* Phase 7: open Gmail intent */ },
                label = { Text(threadId, maxLines = 1) }
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}
