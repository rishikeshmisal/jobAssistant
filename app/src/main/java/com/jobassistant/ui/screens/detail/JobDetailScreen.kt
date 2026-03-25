package com.jobassistant.ui.screens.detail

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.domain.model.ALL_STATUSES
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.displayName
import com.jobassistant.ui.components.CompanyAvatar
import com.jobassistant.ui.components.FitScoreRing
import com.jobassistant.ui.components.SectionHeader
import com.jobassistant.ui.components.statusContainerColor
import com.jobassistant.ui.components.statusLabelColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val EXPIRY_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000

private fun isExpired(lastSeenDate: Long, status: ApplicationStatus): Boolean =
    status == ApplicationStatus.INTERESTED &&
        (System.currentTimeMillis() - lastSeenDate) > EXPIRY_THRESHOLD_MS

private val JOB_DESCRIPTION_TABS = listOf("Paste Text", "Paste URL")

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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { menuExpanded = false; showDeleteDialog = true }
                            )
                        }
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
                    // ── 16.2 Header ──────────────────────────────────────────
                    JobDetailHeader(job = job, location = location, salaryRange = salaryRange)

                    // ── 16.3 Status chip row ─────────────────────────────────
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        items(ALL_STATUSES) { s ->
                            FilterChip(
                                selected = s == status,
                                onClick = { viewModel.status.value = s },
                                label = {
                                    Text(
                                        s.displayName(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = statusContainerColor(s),
                                    selectedLabelColor = statusLabelColor(s)
                                )
                            )
                        }
                    }

                    // ── 16.4 Fit Score card ──────────────────────────────────
                    val displayScore = uiState.fitAnalysis?.score ?: job.fitScore
                    FitScoreCard(
                        displayScore = displayScore,
                        jobDescription = jobDescription,
                        job = job,
                        errorType = uiState.errorType,
                        isAnalyzing = uiState.isAnalyzing,
                        onRefresh = {
                            if (jobDescription.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Add a job description first")
                                }
                            } else {
                                viewModel.analyzeFromPaste(jobDescription)
                            }
                        }
                    )

                    // ── 16.5 Analysis results (default expanded) ─────────────
                    val analysis = uiState.fitAnalysis
                    if (analysis != null) {
                        ExpandableSection(
                            title = "Pros (${analysis.pros.size})",
                            items = analysis.pros,
                            initiallyExpanded = true
                        )
                        ExpandableSection(
                            title = "Cons (${analysis.cons.size})",
                            items = analysis.cons,
                            initiallyExpanded = true
                        )
                        ExpandableSection(
                            title = "Missing Skills (${analysis.missingSkills.size})",
                            items = analysis.missingSkills,
                            initiallyExpanded = true
                        )
                    }

                    // ── 16.6 Job Description section ─────────────────────────
                    JobDescriptionSection(
                        jobDescription = jobDescription,
                        isAnalyzing = uiState.isAnalyzing,
                        retryAvailableAt = uiState.retryAvailableAt,
                        hasExistingScore = job.fitScore != null,
                        onJobDescriptionChange = { viewModel.jobDescription.value = it },
                        onAnalyzePaste = { viewModel.analyzeFromPaste(jobDescription) },
                        onAnalyzeUrl = { viewModel.analyzeFromUrl(jobDescription) }
                    )

                    // ── 16.7 Details card ────────────────────────────────────
                    DetailsCard(
                        notes = notes,
                        location = location,
                        salaryRange = salaryRange,
                        appliedDate = appliedDate,
                        interviewDate = interviewDate,
                        status = status,
                        isSaving = uiState.isSaving,
                        onNotesChange = { viewModel.notes.value = it },
                        onLocationChange = { viewModel.location.value = it },
                        onSalaryChange = { viewModel.salaryRange.value = it },
                        onAppliedDateSelected = { viewModel.appliedDate.value = it },
                        onInterviewDateSelected = { viewModel.interviewDate.value = it },
                        onSave = { viewModel.saveChanges() }
                    )

                    // ── 16.9 Linked emails ───────────────────────────────────
                    if (job.linkedEmailThreadIds.isNotEmpty()) {
                        LinkedEmailsSection(threadIds = job.linkedEmailThreadIds)
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

// ── 16.2 Header ───────────────────────────────────────────────────────────────

@Composable
private fun JobDetailHeader(
    job: JobApplication,
    location: String,
    salaryRange: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompanyAvatar(companyName = job.companyName, size = 48.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // 16.10: titleLarge instead of headlineMedium
            Text(
                text = job.companyName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = job.roleTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Metadata row — location and/or salary
            val hasLocation = location.isNotBlank()
            val hasSalary = salaryRange.isNotBlank()
            if (hasLocation || hasSalary) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (hasLocation) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (hasLocation && hasSalary) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (hasSalary) {
                        Text(
                            text = salaryRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (isExpired(job.lastSeenDate, job.status)) {
                Spacer(Modifier.height(2.dp))
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
    }
}

// ── 16.4 Fit Score card ───────────────────────────────────────────────────────

@Composable
private fun FitScoreCard(
    displayScore: Int?,
    jobDescription: String,
    job: JobApplication,
    errorType: ApiErrorType?,
    isAnalyzing: Boolean,
    onRefresh: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        when {
            // State C — score exists
            displayScore != null -> {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FitScoreRing(
                        score = displayScore,
                        size = 96.dp,
                        strokeWidth = 10.dp
                    )
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Fit Score",
                                style = MaterialTheme.typography.labelMedium
                            )
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "Refresh score",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (job.analysisDate != null) {
                            Text(
                                text = "Analyzed ${dateFormat.format(Date(job.analysisDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val inlineError = when (errorType) {
                            ApiErrorType.AUTH -> "API key invalid"
                            ApiErrorType.RATE_LIMIT -> "Busy — try again shortly"
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
                    }
                }
            }

            // State B — no score but JD is saved
            jobDescription.isNotBlank() -> {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "No fit score yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Tap Refresh Score to analyze",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // State A — no score, no JD
            else -> {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "No fit score yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Paste the job description below to analyze",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (isAnalyzing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

// ── 16.6 Job Description section ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobDescriptionSection(
    jobDescription: String,
    isAnalyzing: Boolean,
    retryAvailableAt: Long?,
    hasExistingScore: Boolean,
    onJobDescriptionChange: (String) -> Unit,
    onAnalyzePaste: () -> Unit,
    onAnalyzeUrl: () -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val isRateLimited = retryAvailableAt != null && System.currentTimeMillis() < retryAvailableAt
    val analyzeLabel = if (hasExistingScore) "Refresh Score" else "Analyze Fit"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title with "Saved" badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Job Description & Score",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (jobDescription.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analyze_fit_button")
                        ) {
                            Text(analyzeLabel)
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
                }
            }
        }
    }
}

// ── 16.7 Details card ─────────────────────────────────────────────────────────

@Composable
private fun DetailsCard(
    notes: String,
    location: String,
    salaryRange: String,
    appliedDate: Long?,
    interviewDate: Long?,
    status: ApplicationStatus,
    isSaving: Boolean,
    onNotesChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
    onAppliedDateSelected: (Long) -> Unit,
    onInterviewDateSelected: (Long) -> Unit,
    onSave: () -> Unit
) {
    val isInterviewProminent = status == ApplicationStatus.INTERVIEWING ||
        status == ApplicationStatus.ASSESSMENT

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = "Details")

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = salaryRange,
                    onValueChange = onSalaryChange,
                    label = { Text("Salary Range") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dates — contextual layout
            if (isInterviewProminent) {
                // Interview date full-width and highlighted
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        DatePickerField(
                            label = "Interview Date",
                            dateMs = interviewDate,
                            onDateSelected = onInterviewDateSelected,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                DatePickerField(
                    label = "Applied Date",
                    dateMs = appliedDate,
                    onDateSelected = onAppliedDateSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Side by side
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        label = "Applied Date",
                        dateMs = appliedDate,
                        onDateSelected = onAppliedDateSelected,
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "Interview Date",
                        dateMs = interviewDate,
                        onDateSelected = onInterviewDateSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}

// ── Expandable section ────────────────────────────────────────────────────────

@Composable
private fun ExpandableSection(
    title: String,
    items: List<String>,
    initiallyExpanded: Boolean = true
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
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

// ── Date picker ───────────────────────────────────────────────────────────────

@Composable
private fun DatePickerField(
    label: String,
    dateMs: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    OutlinedButton(
        onClick = {
            val cal = Calendar.getInstance()
            dateMs?.let { cal.timeInMillis = it }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().also {
                        it.set(year, month, day, 0, 0, 0)
                        it.set(Calendar.MILLISECOND, 0)
                    }
                    onDateSelected(selected.timeInMillis)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = modifier
    ) {
        Text(
            if (dateMs != null) "$label: ${dateFormat.format(Date(dateMs))}" else "Set $label",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

// ── 16.9 Linked emails ────────────────────────────────────────────────────────

@Composable
private fun LinkedEmailsSection(threadIds: List<String>) {
    Column {
        Text(
            "Linked Emails",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        threadIds.forEachIndexed { index, threadId ->
            SuggestionChip(
                onClick = {},
                label = { Text("Email thread ${index + 1}") },
                modifier = Modifier.semantics { contentDescription = threadId }
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}
