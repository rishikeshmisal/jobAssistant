package com.jobassistant.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val STATUS_ORDER = listOf(
    ApplicationStatus.SAVED,
    ApplicationStatus.APPLIED,
    ApplicationStatus.INTERVIEWING,
    ApplicationStatus.OFFERED,
    ApplicationStatus.REJECTED
)

private fun ApplicationStatus.displayName() = when (this) {
    ApplicationStatus.SAVED -> "Saved"
    ApplicationStatus.APPLIED -> "Applied"
    ApplicationStatus.INTERVIEWING -> "Interviewing"
    ApplicationStatus.OFFERED -> "Offered"
    ApplicationStatus.REJECTED -> "Rejected"
}

private const val EXPIRY_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000  // 30 days

private fun isExpired(job: JobApplication): Boolean =
    job.status == ApplicationStatus.SAVED &&
        (System.currentTimeMillis() - job.lastSeenDate) > EXPIRY_THRESHOLD_MS

private fun fitScoreColor(score: Int?): Color = when {
    score == null -> Color.Gray
    score < 40 -> Color(0xFFE53935)
    score <= 70 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onJobClick: (String) -> Unit = {},
    onAddJobClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var bottomSheetJob by remember { mutableStateOf<JobApplication?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.setViewMode(ViewMode.KANBAN) }) {
                        Icon(
                            Icons.Filled.ViewModule,
                            contentDescription = "Kanban view",
                            tint = if (uiState.viewMode == ViewMode.KANBAN)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.setViewMode(ViewMode.LIST) }) {
                        Icon(
                            Icons.Filled.List,
                            contentDescription = "List view",
                            tint = if (uiState.viewMode == ViewMode.LIST)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJobClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add job")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val allJobs = uiState.jobsByStatus.values.flatten()

        if (allJobs.isEmpty() && !uiState.isLoading) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onAddJobClick = onAddJobClick
            )
        } else {
            when (uiState.viewMode) {
                ViewMode.KANBAN -> KanbanBoard(
                    jobsByStatus = uiState.jobsByStatus,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onJobClick = onJobClick,
                    onJobLongPress = { bottomSheetJob = it }
                )
                ViewMode.LIST -> ListView(
                    jobsByStatus = uiState.jobsByStatus,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onJobClick = onJobClick,
                    onJobDelete = { job ->
                        viewModel.deleteJob(job)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Deleted ${job.companyName}",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                // re-save with original state
                                // The repository save will not trigger duplicate because
                                // it's the same id — we use force-save via the repository
                            }
                        }
                    }
                )
            }
        }
    }

    // Bottom sheet for status change
    val sheetJob = bottomSheetJob
    if (sheetJob != null) {
        ModalBottomSheet(
            onDismissRequest = { bottomSheetJob = null },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            StatusChangeSheet(
                job = sheetJob,
                onStatusSelected = { newStatus ->
                    viewModel.setStatus(sheetJob, newStatus)
                    bottomSheetJob = null
                },
                onDismiss = { bottomSheetJob = null }
            )
        }
    }
}

@Composable
private fun KanbanBoard(
    jobsByStatus: Map<ApplicationStatus, List<JobApplication>>,
    modifier: Modifier = Modifier,
    onJobClick: (String) -> Unit,
    onJobLongPress: (JobApplication) -> Unit
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(STATUS_ORDER) { status ->
            val jobs = jobsByStatus[status] ?: emptyList()
            KanbanColumn(
                status = status,
                jobs = jobs,
                onJobClick = onJobClick,
                onJobLongPress = onJobLongPress
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    status: ApplicationStatus,
    jobs: List<JobApplication>,
    onJobClick: (String) -> Unit,
    onJobLongPress: (JobApplication) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status.displayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Badge { Text(jobs.size.toString()) }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        onClick = { onJobClick(job.id.toString()) },
                        onLongPress = { onJobLongPress(job) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JobCard(
    job: JobApplication,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = job.companyName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = job.roleTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                FitScoreBadge(score = job.fitScore)
                job.appliedDate?.let { date ->
                    Text(
                        text = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 9.3 — expiry warning for stale SAVED postings
            if (isExpired(job)) {
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            "Posting may be expired",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }
    }
}

@Composable
internal fun FitScoreBadge(score: Int?) {
    val color = fitScoreColor(score)
    val animatedColor by animateColorAsState(targetValue = color, label = "fitScoreColor")
    Surface(
        shape = MaterialTheme.shapes.small,
        color = animatedColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = if (score != null) "$score%" else "N/A",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = animatedColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusChangeSheet(
    job: JobApplication,
    onStatusSelected: (ApplicationStatus) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Change status for ${job.companyName}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(16.dp))
        STATUS_ORDER.forEach { status ->
            TextButton(
                onClick = { onStatusSelected(status) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(status.displayName())
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListView(
    jobsByStatus: Map<ApplicationStatus, List<JobApplication>>,
    modifier: Modifier = Modifier,
    onJobClick: (String) -> Unit,
    onJobDelete: (JobApplication) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<ApplicationStatus?>(null) }

    val allJobs = jobsByStatus.values
        .flatten()
        .sortedByDescending { it.appliedDate ?: it.lastSeenDate }

    val filtered = if (selectedFilter == null) allJobs
    else allJobs.filter { it.status == selectedFilter }

    Column(modifier = modifier) {
        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All") }
                )
            }
            items(STATUS_ORDER) { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = if (selectedFilter == status) null else status },
                    label = { Text(status.displayName()) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { job ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onJobDelete(job)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    ListJobRow(job = job, onClick = { onJobClick(job.id.toString()) })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListJobRow(job: JobApplication, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = job.roleTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = job.status.displayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(8.dp))
            FitScoreBadge(score = job.fitScore)
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onAddJobClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No jobs yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAddJobClick) {
            Text("Add your first job")
        }
    }
}
