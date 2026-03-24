package com.jobassistant.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.ui.components.CompanyAvatar
import com.jobassistant.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TAB_TITLES = listOf("Career Profile", "AI Coach", "Applied Jobs")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

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
        topBar = { TopAppBar(title = { Text("Insights") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val retryAt = uiState.retryAvailableAt
            val isRateLimited = retryAt != null && System.currentTimeMillis() < retryAt
            val cooldownPassed = uiState.insights?.let {
                System.currentTimeMillis() - it.generatedDate >= REFRESH_UNLOCK_MS
            } ?: true  // no insights yet → always allow
            val refreshEnabled = (uiState.dataChangedSinceRefresh || cooldownPassed) &&
                !uiState.isRefreshing && !isRateLimited

            when (selectedTab) {
                0 -> CareerProfileTab(
                    profile = uiState.userProfile,
                    isRefreshing = uiState.isRefreshing,
                    refreshEnabled = refreshEnabled,
                    lastUpdated = uiState.insights?.generatedDate,
                    onRefresh = { viewModel.refreshInsights() }
                )
                1 -> NewJobTab(
                    insights = uiState.insights,
                    isRefreshing = uiState.isRefreshing,
                    refreshEnabled = refreshEnabled,
                    errorType = uiState.errorType,
                    retryAvailableAt = uiState.retryAvailableAt,
                    onRefresh = { viewModel.refreshInsights() }
                )
                2 -> AppliedJobsTab(
                    stats = uiState.stats,
                    isRefreshing = uiState.isRefreshing,
                    refreshEnabled = refreshEnabled,
                    lastUpdated = uiState.insights?.generatedDate,
                    onRefresh = { viewModel.refreshInsights() }
                )
            }
        }
    }
}

// ── Shared refresh bar ────────────────────────────────────────────────────────

@Composable
private fun RefreshBar(
    isRefreshing: Boolean,
    refreshEnabled: Boolean,
    lastUpdated: Long?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lastUpdated != null) {
            Text(
                text = "Updated ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(lastUpdated))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "No AI insights generated yet",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onRefresh,
            enabled = refreshEnabled,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(6.dp))
                Text("Refreshing…", style = MaterialTheme.typography.labelMedium)
            } else {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Refresh", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ── Tab 0: Career Profile ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CareerProfileTab(
    profile: UserProfile,
    isRefreshing: Boolean,
    refreshEnabled: Boolean,
    lastUpdated: Long?,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RefreshBar(isRefreshing, refreshEnabled, lastUpdated, onRefresh)
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(Modifier.height(8.dp))

        // User header
        CompanyAvatar(
            companyName = profile.fullName.ifBlank { "?" },
            size = 56.dp
        )
        if (profile.fullName.isNotBlank()) {
            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "Complete your profile in Settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Resume summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader("Resume")
                if (profile.resumeText.isBlank()) {
                    Text(
                        text = "No resume uploaded yet — go to Profile to upload your PDF",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val wordCount = profile.resumeText.trim().split("\\s+".toRegex()).size
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "~$wordCount words",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${profile.resumeText.length} characters",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = profile.resumeText.take(400) + if (profile.resumeText.length > 400) "…" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Career interests
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader("Career Interests")

                // Career goal
                if (profile.careerGoal.isNotBlank()) {
                    Text(
                        text = profile.careerGoal,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "No career goal set — add one in Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Keywords
                if (profile.keywords.isNotEmpty()) {
                    Text(
                        text = "Skills & Keywords",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        profile.keywords.forEach { keyword ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(keyword) }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No keywords set — add skills in Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Target salary
                val salaryText = when {
                    profile.targetSalaryMin > 0 && profile.targetSalaryMax > 0 ->
                        "£${profile.targetSalaryMin.formatSalary()} – £${profile.targetSalaryMax.formatSalary()}"
                    profile.targetSalaryMin > 0 ->
                        "From £${profile.targetSalaryMin.formatSalary()}"
                    else -> null
                }
                if (salaryText != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Target Salary",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = salaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // AI career summary (show if careerGoal looks AI-generated — heuristic: >80 chars)
        if (profile.careerGoal.length > 80) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("AI Career Summary")
                    Text(
                        text = profile.careerGoal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Generated from your resume by AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        } // end scrollable Column
    } // end outer Column
}

private fun Int.formatSalary(): String =
    if (this >= 1000) "${this / 1000}k" else toString()

// ── Tab 1: New Job ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewJobTab(
    insights: CareerInsights?,
    isRefreshing: Boolean,
    refreshEnabled: Boolean,
    errorType: ApiErrorType?,
    retryAvailableAt: Long?,
    onRefresh: () -> Unit
) {
    val isRateLimited = retryAvailableAt != null && System.currentTimeMillis() < retryAvailableAt

    Column(modifier = Modifier.fillMaxSize()) {
        RefreshBar(
            isRefreshing = isRefreshing,
            refreshEnabled = refreshEnabled,
            lastUpdated = insights?.generatedDate,
            onRefresh = onRefresh
        )
        HorizontalDivider()

    if (insights == null && !isRefreshing) {
        // Empty state
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No insights yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Generate AI recommendations based on your job search history",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRefresh,
                enabled = refreshEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Insights")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        insights?.let { it ->
            // Identified gaps
            if (it.identifiedGaps.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("Identified Gaps")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            it.identifiedGaps.forEach { gap ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(gap) },
                                    icon = {
                                        Icon(
                                            Icons.Filled.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        labelColor = MaterialTheme.colorScheme.onErrorContainer,
                                        iconContentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Recommended actions
            if (it.recommendedActions.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader("What to Improve")
                        it.recommendedActions.forEach { action ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = action,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Market feedback
            if (it.summaryAnalysis.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("Market Feedback")
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = it.summaryAnalysis,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

        }

        // Typed inline error
        val inlineError = when (errorType) {
            ApiErrorType.AUTH -> "API key invalid — check your key in Settings"
            ApiErrorType.RATE_LIMIT -> "Service busy — please try again in a minute"
            else -> null
        }
        if (inlineError != null) {
            Text(
                text = inlineError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))
    } // end scrollable Column
    } // end outer Column
}

// ── Tab 2: Applied Jobs ───────────────────────────────────────────────────────

@Composable
private fun AppliedJobsTab(
    stats: InsightsStats,
    isRefreshing: Boolean,
    refreshEnabled: Boolean,
    lastUpdated: Long?,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RefreshBar(isRefreshing, refreshEnabled, lastUpdated, onRefresh)
        HorizontalDivider()

        if (stats.totalApplied == 0) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Apply to some jobs first to see stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCardsRow(stats = stats)
                FunnelRow(
                    totalApplied = stats.totalApplied,
                    interviews = stats.interviews,
                    offers = stats.offers
                )
                RateSection(
                    interviewRate = stats.interviewRate,
                    rejectionRate = stats.rejectionRate
                )
                if (stats.topCompanies.isNotEmpty()) {
                    TopCompaniesCard(companies = stats.topCompanies)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TopCompaniesCard(companies: List<Pair<String, Int>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Top Companies")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                companies.forEach { (company, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompanyAvatar(companyName = company, size = 32.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = company,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$count application${if (count == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── Shared sub-components (used by Applied Jobs tab) ─────────────────────────

@Composable
private fun StatsCardsRow(stats: InsightsStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Applied",    stats.totalApplied.toString(), MaterialTheme.colorScheme.primary, Icons.Filled.Send,        Modifier.weight(1f))
            StatCard("Interviews", stats.interviews.toString(),   Color(0xFFFB8C00),                 Icons.Filled.Event,       Modifier.weight(1f))
            StatCard("Offers",     stats.offers.toString(),       Color(0xFF43A047),                 Icons.Filled.EmojiEvents, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Rejected",    stats.rejections.toString(), Color(0xFFE53935), Icons.Filled.Cancel,                      Modifier.weight(1f))
            StatCard("Withdrawn",   stats.withdrawn.toString(),  Color(0xFF757575), Icons.Filled.Cancel,                      Modifier.weight(1f))
            StatCard("No Response", stats.noResponse.toString(), Color(0xFF546E7A), Icons.AutoMirrored.Filled.ArrowForward,    Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FunnelRow(totalApplied: Int, interviews: Int, offers: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FunnelStage("Applied", totalApplied, MaterialTheme.colorScheme.primary)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            FunnelStage("Interviews", interviews, Color(0xFFFB8C00))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            FunnelStage("Offers", offers, Color(0xFF43A047))
        }
    }
}

@Composable
private fun FunnelStage(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RateSection(interviewRate: Float, rejectionRate: Float) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Conversion Rates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Interview Rate: ${interviewRate.toInt()}%", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { interviewRate / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFB8C00),
                trackColor = Color(0xFFFB8C00).copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(12.dp))
            Text("Rejection Rate: ${rejectionRate.toInt()}%", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { rejectionRate / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE53935),
                trackColor = Color(0xFFE53935).copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
