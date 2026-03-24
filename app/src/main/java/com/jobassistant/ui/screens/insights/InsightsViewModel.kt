package com.jobassistant.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.usecase.GenerateInsightsUseCase
import com.jobassistant.domain.usecase.GetAllJobsUseCase
import com.jobassistant.domain.usecase.GetCareerInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RATE_LIMIT_COOLDOWN_MS = 60_000L   // 1 min UI cooldown after a 429
const val REFRESH_UNLOCK_MS = 8L * 60 * 60 * 1000   // 8 h time-based unlock (read by Screen)

data class InsightsStats(
    val totalApplied: Int = 0,
    val interviews: Int = 0,
    val rejections: Int = 0,
    val offers: Int = 0,
    val withdrawn: Int = 0,
    val noResponse: Int = 0,
    val interviewRate: Float = 0f,
    val rejectionRate: Float = 0f,
    val topCompanies: List<Pair<String, Int>> = emptyList()
)

data class InsightsUiState(
    val stats: InsightsStats = InsightsStats(),
    val insights: CareerInsights? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val errorType: ApiErrorType? = null,
    /** Epoch ms after which the refresh button re-enables after a 429 rate-limit error. */
    val retryAvailableAt: Long? = null,
    val userProfile: UserProfile = UserProfile(),
    /**
     * True when job data has changed since the last refresh, or no refresh has happened yet.
     * Used together with the 8h time-based unlock in InsightsScreen to decide button state.
     */
    val dataChangedSinceRefresh: Boolean = true
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getAllJobsUseCase: GetAllJobsUseCase,
    private val generateInsightsUseCase: GenerateInsightsUseCase,
    private val getCareerInsightsUseCase: GetCareerInsightsUseCase,
    private val userProfileDataStore: UserProfileDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    /** Fingerprint of jobs at the time insights were last generated. Blank = never refreshed. */
    private var snapshotAtLastRefresh: String = ""

    init {
        observeData()
    }

    private fun jobsSnapshot(jobs: List<JobApplication>): String =
        "${jobs.size}:${jobs.sumOf { it.status.ordinal }}:${jobs.count { it.fitScore != null }}"

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getAllJobsUseCase(),
                getCareerInsightsUseCase(),
                userProfileDataStore.userProfileFlow
            ) { jobs, insights, profile ->
                Triple(jobs, insights, profile)
            }.collect { (jobs, insights, profile) ->
                val stats = computeStats(jobs)
                // Data-change unlock: blank snapshot = never refreshed (always unlocked).
                // Non-blank snapshot = compare current vs snapshot at last refresh.
                val dataChanged = snapshotAtLastRefresh.isBlank() ||
                    jobsSnapshot(jobs) != snapshotAtLastRefresh
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    insights = insights,
                    userProfile = profile,
                    dataChangedSinceRefresh = dataChanged
                )
            }
        }
    }

    private fun computeStats(jobs: List<JobApplication>): InsightsStats {
        // "Applied" = everything that is not INTERESTED (i.e. something was submitted)
        val applied = jobs.count { it.status != ApplicationStatus.INTERESTED }
        // "Interviews" = SCREENING + INTERVIEWING + ASSESSMENT (any live interaction)
        val interviews = jobs.count {
            it.status in listOf(
                ApplicationStatus.SCREENING,
                ApplicationStatus.INTERVIEWING,
                ApplicationStatus.ASSESSMENT
            )
        }
        val rejections = jobs.count { it.status == ApplicationStatus.REJECTED }
        // "Offers" = OFFER + ACCEPTED
        val offers = jobs.count {
            it.status in listOf(ApplicationStatus.OFFER, ApplicationStatus.ACCEPTED)
        }
        val withdrawn = jobs.count { it.status == ApplicationStatus.WITHDRAWN }
        val noResponse = jobs.count { it.status == ApplicationStatus.NO_RESPONSE }

        val interviewRate = if (applied > 0) interviews.toFloat() / applied * 100f else 0f
        val rejectionRate = if (applied > 0) rejections.toFloat() / applied * 100f else 0f

        val topCompanies = jobs
            .groupBy { it.companyName }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { Pair(it.key, it.value) }

        return InsightsStats(
            totalApplied = applied,
            interviews = interviews,
            rejections = rejections,
            offers = offers,
            withdrawn = withdrawn,
            noResponse = noResponse,
            interviewRate = interviewRate,
            rejectionRate = rejectionRate,
            topCompanies = topCompanies
        )
    }

    fun refreshInsights() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            val profile = userProfileDataStore.userProfileFlow.first()
            val jobs = getAllJobsUseCase().first()
            val historySummary = buildHistorySummary(jobs)
            val profileSummary = buildProfileSummary(profile)

            when (val result = generateInsightsUseCase(profileSummary, historySummary)) {
                is ClaudeResult.Success -> {
                    // Capture snapshot so the button locks until data changes or 8h passes
                    snapshotAtLastRefresh = jobsSnapshot(jobs)
                    _uiState.value = _uiState.value.copy(
                        insights = result.data,
                        isRefreshing = false,
                        dataChangedSinceRefresh = false
                    )
                }
                is ClaudeResult.Error -> {
                    val cooldown = if (result.errorType == ApiErrorType.RATE_LIMIT)
                        System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS else null
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = result.message,
                        errorType = result.errorType,
                        retryAvailableAt = cooldown
                    )
                }
            }
        }
    }

    private fun buildHistorySummary(jobs: List<JobApplication>): String {
        val applied = jobs.filter { it.status != ApplicationStatus.INTERESTED }
        if (applied.isEmpty()) return "No applications submitted yet."

        val sb = StringBuilder()
        sb.appendLine("Total: ${applied.size} applications")
        sb.appendLine("Interviewing: ${applied.count { it.status in listOf(ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEWING, ApplicationStatus.ASSESSMENT) }}")
        sb.appendLine("Offers: ${applied.count { it.status in listOf(ApplicationStatus.OFFER, ApplicationStatus.ACCEPTED) }}")
        sb.appendLine("Rejections: ${applied.count { it.status == ApplicationStatus.REJECTED }}")
        sb.appendLine()

        // Include up to 20 most recent jobs with role, company, status, and fit score
        sb.appendLine("Recent applications (anonymised):")
        applied
            .sortedByDescending { it.appliedDate ?: it.lastSeenDate }
            .take(20)
            .forEach { job ->
                val score = job.fitScore?.let { " (fit: $it/100)" } ?: ""
                sb.appendLine("- ${job.roleTitle} at ${job.companyName} — ${job.status.name}$score")
            }

        return sb.toString().trim()
    }

    private fun buildProfileSummary(profile: com.jobassistant.domain.model.UserProfile): String {
        val sb = StringBuilder()
        if (profile.careerGoal.isNotBlank()) sb.appendLine("Career goal: ${profile.careerGoal}")
        if (profile.keywords.isNotEmpty()) sb.appendLine("Skills/keywords: ${profile.keywords.joinToString(", ")}")
        if (profile.targetSalaryMin > 0) sb.appendLine("Target salary: £${profile.targetSalaryMin}–£${profile.targetSalaryMax}")
        if (profile.resumeText.isNotBlank()) sb.appendLine("Resume length: ${profile.resumeText.split("\\s+".toRegex()).size} words")
        return sb.toString().trim().ifBlank { "No profile information provided." }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, errorType = null, retryAvailableAt = null)
    }
}
