package com.jobassistant.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.CareerInsights
import com.jobassistant.domain.model.JobApplication
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

private const val REFRESH_COOLDOWN_MS = 24L * 60 * 60 * 1000 // 24 hours
private const val RATE_LIMIT_COOLDOWN_MS = 60_000L              // 1 minute UI cooldown on 429/529

data class InsightsStats(
    val totalApplied: Int = 0,
    val interviews: Int = 0,
    val rejections: Int = 0,
    val offers: Int = 0,
    val interviewRate: Float = 0f,
    val rejectionRate: Float = 0f,
    val topCompanies: List<Pair<String, Int>> = emptyList()
)

data class InsightsUiState(
    val stats: InsightsStats = InsightsStats(),
    val insights: CareerInsights? = null,
    val isRefreshing: Boolean = false,
    val isRefreshEnabled: Boolean = true,
    val error: String? = null,
    val errorType: ApiErrorType? = null,
    /** Epoch ms after which the refresh button should re-enable after a rate-limit error. */
    val retryAvailableAt: Long? = null
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

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getAllJobsUseCase(),
                getCareerInsightsUseCase()
            ) { jobs, insights ->
                Pair(jobs, insights)
            }.collect { (jobs, insights) ->
                val stats = computeStats(jobs)
                val isRefreshEnabled = insights == null ||
                    (System.currentTimeMillis() - insights.generatedDate) >= REFRESH_COOLDOWN_MS
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    insights = insights,
                    isRefreshEnabled = isRefreshEnabled
                )
            }
        }
    }

    private fun computeStats(jobs: List<JobApplication>): InsightsStats {
        val applied = jobs.count { it.status != ApplicationStatus.SAVED }
        val interviews = jobs.count { it.status == ApplicationStatus.INTERVIEWING }
        val rejections = jobs.count { it.status == ApplicationStatus.REJECTED }
        val offers = jobs.count { it.status == ApplicationStatus.OFFERED }

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
            interviewRate = interviewRate,
            rejectionRate = rejectionRate,
            topCompanies = topCompanies
        )
    }

    fun refreshInsights() {
        if (!_uiState.value.isRefreshEnabled) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            val profile = userProfileDataStore.userProfileFlow.first()
            val jobs = getAllJobsUseCase().first()
            val historySummary = buildHistorySummary(jobs)

            when (val result = generateInsightsUseCase(profile.careerGoal, historySummary)) {
                is ClaudeResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        insights = result.data,
                        isRefreshing = false,
                        isRefreshEnabled = false
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
        val applied = jobs.count { it.status != ApplicationStatus.SAVED }
        val interviews = jobs.count { it.status == ApplicationStatus.INTERVIEWING }
        val offers = jobs.count { it.status == ApplicationStatus.OFFERED }
        val rejections = jobs.count { it.status == ApplicationStatus.REJECTED }
        return "Applied: $applied, Interviewing: $interviews, Offers: $offers, Rejections: $rejections"
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, errorType = null, retryAvailableAt = null)
    }
}
