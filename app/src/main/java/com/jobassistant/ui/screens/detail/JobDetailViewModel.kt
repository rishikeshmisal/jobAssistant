package com.jobassistant.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.data.remote.model.ApiErrorType
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.EvaluateFitUseCase
import com.jobassistant.domain.usecase.GetAllJobsUseCase
import com.jobassistant.domain.usecase.UpdateJobStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val RATE_LIMIT_COOLDOWN_MS = 60_000L // 1 minute UI cooldown on 429/529

data class JobDetailUiState(
    val job: JobApplication? = null,
    val fitAnalysis: FitAnalysis? = null,
    val isLoading: Boolean = true,
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val errorType: ApiErrorType? = null,
    /** Epoch ms after which the re-analyze button should be re-enabled (rate-limit cooldown). */
    val retryAvailableAt: Long? = null,
    val saved: Boolean = false
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAllJobsUseCase: GetAllJobsUseCase,
    private val jobApplicationRepository: JobApplicationRepository,
    private val updateJobStatusUseCase: UpdateJobStatusUseCase,
    private val evaluateFitUseCase: EvaluateFitUseCase,
    private val userProfileDataStore: UserProfileDataStore
) : ViewModel() {

    private val jobId: UUID = UUID.fromString(
        checkNotNull(savedStateHandle["jobId"]) { "jobId is required" }
    )

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    // Editable field state flows
    val notes = MutableStateFlow("")
    val location = MutableStateFlow("")
    val salaryRange = MutableStateFlow("")
    val appliedDate = MutableStateFlow<Long?>(null)
    val interviewDate = MutableStateFlow<Long?>(null)
    val status = MutableStateFlow(ApplicationStatus.SAVED)

    init {
        loadJob()
    }

    private fun loadJob() {
        viewModelScope.launch {
            getAllJobsUseCase()
                .first()
                .find { it.id == jobId }
                ?.let { job ->
                    _uiState.value = _uiState.value.copy(job = job, isLoading = false)
                    notes.value = job.notes
                    location.value = job.location ?: ""
                    salaryRange.value = job.salaryRange ?: ""
                    appliedDate.value = job.appliedDate
                    interviewDate.value = job.interviewDate
                    status.value = job.status
                } ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Job not found")
            }
        }
    }

    fun reAnalyzeFit() {
        val job = _uiState.value.job ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null)
            val resumeText = userProfileDataStore.userProfileFlow.first().resumeText
            val jobDescription = job.notes
            when (val result = evaluateFitUseCase(resumeText, jobDescription)) {
                is ClaudeResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        fitAnalysis = result.data,
                        isAnalyzing = false
                    )
                }
                is ClaudeResult.Error -> {
                    val cooldown = if (result.errorType == ApiErrorType.RATE_LIMIT)
                        System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS else null
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = result.message,
                        errorType = result.errorType,
                        retryAvailableAt = cooldown
                    )
                }
            }
        }
    }

    fun saveChanges() {
        val job = _uiState.value.job ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val updated = job.copy(
                notes = notes.value,
                location = location.value.takeIf { it.isNotBlank() },
                salaryRange = salaryRange.value.takeIf { it.isNotBlank() },
                appliedDate = appliedDate.value,
                interviewDate = interviewDate.value,
                status = status.value,
                fitScore = _uiState.value.fitAnalysis?.score ?: job.fitScore
            )
            jobApplicationRepository.save(updated)
            _uiState.value = _uiState.value.copy(
                job = updated,
                isSaving = false,
                saved = true
            )
        }
    }

    fun clearSaved() {
        _uiState.value = _uiState.value.copy(saved = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, errorType = null, retryAvailableAt = null)
    }
}
