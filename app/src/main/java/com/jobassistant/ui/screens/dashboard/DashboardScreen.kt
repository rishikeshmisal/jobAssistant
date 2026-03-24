package com.jobassistant.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
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
// FloatingActionButton replaced by SpeedDialFab
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import com.jobassistant.domain.model.ACTIVE_PIPELINE
import com.jobassistant.domain.model.ALL_STATUSES
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.TERMINAL_STATUSES
import com.jobassistant.domain.model.displayName
import com.jobassistant.ui.components.CompanyAvatar
import com.jobassistant.ui.components.SpeedDialFab
import com.jobassistant.ui.components.FitScoreRing
import com.jobassistant.ui.components.RelativeTimeText
import com.jobassistant.ui.components.StatusChip
import com.jobassistant.ui.components.statusContainerColor
import com.jobassistant.ui.components.statusLabelColor
import kotlinx.coroutines.launch

private const val EXPIRY_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000

private fun isExpired(job: JobApplication): Boolean =
    job.status == ApplicationStatus.INTERESTED &&
        (System.currentTimeMillis() - job.lastSeenDate) > EXPIRY_THRESHOLD_MS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onJobClick: (String) -> Unit = {},
    onAddJobClick: () -> Unit = {},
    onEvaluateFitClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var bottomSheetJob by remember { mutableStateOf<JobApplication?>(null) }
    var speedDialExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
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
            SpeedDialFab(
                expanded = speedDialExpanded,
                onToggle = { speedDialExpanded = !speedDialExpanded },
                onTrackJob = { speedDialExpanded = false; onAddJobClick() },
                onEvaluateFit = { speedDialExpanded = false; onEvaluateFitClick() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val allJobs = uiState.jobsByStatus.values.flatten()

        Box(modifier = Modifier.fillMaxSize()) {
        if (allJobs.isEmpty() && !uiState.isLoading) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onAddJobClick = onAddJobClick
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Hero stats strip — always visible above the board/list
                HeroStatsStrip(jobsByStatus = uiState.jobsByStatus)

                when (uiState.viewMode) {
                    ViewMode.KANBAN -> KanbanBoard(
                        jobsByStatus = uiState.jobsByStatus,
                        modifier = Modifier.fillMaxSize(),
                        onJobClick = onJobClick,
                        onJobLongPress = { bottomSheetJob = it }
                    )
                    ViewMode.LIST -> ListView(
                        jobsByStatus = uiState.jobsByStatus,
                        modifier = Modifier.fillMaxSize(),
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
                                    // re-save handled by repository upsert
                                }
                            }
                        }
                    )
                }
            }
        }

        // Scrim when SpeedDial is expanded
        androidx.compose.animation.AnimatedVisibility(
            visible = speedDialExpanded,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.32f))
                    .clickable { speedDialExpanded = false }
            )
        }
        } // end outer Box
    }

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

// ── Hero Stats Strip ──────────────────────────────────────────────────────────

@Composable
private fun HeroStatsStrip(
    jobsByStatus: Map<ApplicationStatus, List<JobApplication>>,
    modifier: Modifier = Modifier
) {
    val total = jobsByStatus.values.sumOf { it.size }
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            MiniStatCard(
                label = "Total",
                value = total.toString(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        items(ALL_STATUSES) { status ->
            val count = jobsByStatus[status]?.size ?: 0
            MiniStatCard(
                label = status.displayName(),
                value = count.toString(),
                containerColor = statusContainerColor(status),
                labelColor = statusLabelColor(status)
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(64.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor.copy(alpha = 0.8f)
            )
        }
    }
}

// ── Kanban Board ──────────────────────────────────────────────────────────────

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
        // Active pipeline columns
        items(ACTIVE_PIPELINE) { status ->
            val jobs = jobsByStatus[status] ?: emptyList()
            KanbanColumn(
                status = status,
                jobs = jobs,
                onJobClick = onJobClick,
                onJobLongPress = onJobLongPress
            )
        }
        // "Closed" divider + terminal columns
        item {
            KanbanTerminalGroup(
                jobsByStatus = jobsByStatus,
                onJobClick = onJobClick,
                onJobLongPress = onJobLongPress
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KanbanTerminalGroup(
    jobsByStatus: Map<ApplicationStatus, List<JobApplication>>,
    onJobClick: (String) -> Unit,
    onJobLongPress: (JobApplication) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Faint vertical divider with "Closed" label
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "Closed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        TERMINAL_STATUSES.forEach { status ->
            val jobs = jobsByStatus[status] ?: emptyList()
            KanbanColumn(
                status = status,
                jobs = jobs,
                onJobClick = onJobClick,
                onJobLongPress = onJobLongPress,
                faded = true
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KanbanColumn(
    status: ApplicationStatus,
    jobs: List<JobApplication>,
    onJobClick: (String) -> Unit,
    onJobLongPress: (JobApplication) -> Unit,
    faded: Boolean = false
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (faded) 0.3f else 0.5f)
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
                        onLongPress = { onJobLongPress(job) },
                        modifier = Modifier.animateItem()
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
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompanyAvatar(companyName = job.companyName)

            Column(modifier = Modifier.weight(1f)) {
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
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = job.status)
                    Spacer(Modifier.weight(1f))
                    RelativeTimeText(
                        epochMillis = job.appliedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

            FitScoreRing(score = job.fitScore, size = 48.dp, strokeWidth = 5.dp)
        }
    }
}

// ── Status Change Bottom Sheet ────────────────────────────────────────────────

@Composable
private fun StatusChangeSheet(
    job: JobApplication,
    onStatusSelected: (ApplicationStatus) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Move ${job.companyName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        ALL_STATUSES.forEach { status ->
            val isCurrent = status == job.status
            ListItem(
                headlineContent = {
                    Text(
                        text = status.displayName(),
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingContent = { StatusChip(status = status) },
                trailingContent = {
                    if (isCurrent) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Current status",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable { onStatusSelected(status) }
            )
        }
    }
}

// ── List View ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
            items(ALL_STATUSES) { status ->
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
                    enableDismissFromStartToEnd = false,
                    modifier = Modifier.animateItem()
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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompanyAvatar(companyName = job.companyName)

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
                Spacer(Modifier.height(4.dp))
                StatusChip(status = job.status)
            }

            Column(horizontalAlignment = Alignment.End) {
                FitScoreRing(score = job.fitScore, size = 44.dp, strokeWidth = 5.dp)
                Spacer(Modifier.height(2.dp))
                RelativeTimeText(
                    epochMillis = job.appliedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

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
