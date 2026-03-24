package com.jobassistant.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.domain.model.CareerInsights
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Generic / unknown errors use snackbar; typed errors are shown inline in the section card
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
        val stats = uiState.stats
        val hasData = stats.totalApplied > 0 || uiState.insights != null

        if (!hasData) {
            EmptyInsightsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats cards row
                StatsCardsRow(stats = stats)

                // Funnel: Applied → Interviewing → Offers
                FunnelRow(
                    totalApplied = stats.totalApplied,
                    interviews = stats.interviews,
                    offers = stats.offers
                )

                // Rate bars
                RateSection(
                    interviewRate = stats.interviewRate,
                    rejectionRate = stats.rejectionRate
                )

                // AI Career Insights
                AiInsightsSection(
                    insights = uiState.insights,
                    isRefreshing = uiState.isRefreshing,
                    isRefreshEnabled = uiState.isRefreshEnabled,
                    errorType = uiState.errorType,
                    retryAvailableAt = uiState.retryAvailableAt,
                    onRefresh = { viewModel.refreshInsights() }
                )
            }
        }
    }
}

@Composable
private fun StatsCardsRow(stats: InsightsStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Applied",
            value = stats.totalApplied.toString(),
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Filled.Send,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Interviews",
            value = stats.interviews.toString(),
            color = Color(0xFFFB8C00),
            icon = Icons.Filled.Event,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Rejected",
            value = stats.rejections.toString(),
            color = Color(0xFFE53935),
            icon = Icons.Filled.Cancel,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Offers",
            value = stats.offers.toString(),
            color = Color(0xFF43A047),
            icon = Icons.Filled.EmojiEvents,
            modifier = Modifier.weight(1f)
        )
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FunnelRow(
    totalApplied: Int,
    interviews: Int,
    offers: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FunnelStage(
                label = "Applied",
                value = totalApplied,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FunnelStage(
                label = "Interviews",
                value = interviews,
                color = Color(0xFFFB8C00)
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FunnelStage(
                label = "Offers",
                value = offers,
                color = Color(0xFF43A047)
            )
        }
    }
}

@Composable
private fun FunnelStage(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiInsightsSection(
    insights: CareerInsights?,
    isRefreshing: Boolean,
    isRefreshEnabled: Boolean,
    errorType: ApiErrorType? = null,
    retryAvailableAt: Long? = null,
    onRefresh: () -> Unit
) {
    val isRateLimited = retryAvailableAt != null && System.currentTimeMillis() < retryAvailableAt
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AI Career Insights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onRefresh,
                    enabled = isRefreshEnabled && !isRefreshing && !isRateLimited
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp))
                    } else {
                        Text(if (isRateLimited) "Service busy…" else "Refresh")
                    }
                }
                // Typed inline error message
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
            }

            insights?.let { it ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Last updated: ${
                        SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                            .format(Date(it.generatedDate))
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (it.identifiedGaps.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Identified Gaps",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
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
                                }
                            )
                        }
                    }
                }

                if (it.recommendedActions.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Recommended Actions",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                if (it.summaryAnalysis.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Summary Analysis",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
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
            } ?: run {
                Spacer(Modifier.height(8.dp))
                Text(
                    "No insights generated yet. Tap Refresh to analyze your job search.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyInsightsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
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
            text = "Apply to some jobs first to see insights",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
