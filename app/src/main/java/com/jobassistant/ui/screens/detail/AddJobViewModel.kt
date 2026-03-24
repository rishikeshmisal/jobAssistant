package com.jobassistant.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.domain.usecase.SaveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class AddJobUiState {
    object Idle : AddJobUiState()
    object Saving : AddJobUiState()
    data class Saved(val jobId: UUID) : AddJobUiState()
    data class Duplicate(val companyName: String, val roleTitle: String) : AddJobUiState()
    data class Error(val message: String) : AddJobUiState()
}

@HiltViewModel
class AddJobViewModel @Inject constructor(
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase,
    private val jobApplicationRepository: JobApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddJobUiState>(AddJobUiState.Idle)
    val uiState: StateFlow<AddJobUiState> = _uiState.asStateFlow()

    private var pendingJob: JobApplication? = null

    fun saveJob(
        companyName: String,
        roleTitle: String,
        location: String,
        salaryRange: String
    ) {
        viewModelScope.launch {
            _uiState.value = AddJobUiState.Saving
            val job = JobApplication(
                id = UUID.randomUUID(),
                companyName = companyName,
                roleTitle = roleTitle,
                status = ApplicationStatus.INTERESTED,
                fitScore = null,
                location = location.takeIf { it.isNotBlank() },
                salaryRange = salaryRange.takeIf { it.isNotBlank() },
                lastSeenDate = System.currentTimeMillis()
            )
            when (val result = saveJobApplicationUseCase(job)) {
                is SaveResult.Saved -> {
                    pendingJob = null
                    _uiState.value = AddJobUiState.Saved(job.id)
                }
                is SaveResult.Duplicate -> {
                    pendingJob = job
                    _uiState.value = AddJobUiState.Duplicate(job.companyName, job.roleTitle)
                }
            }
        }
    }

    fun saveJobForce() {
        val job = pendingJob ?: return
        viewModelScope.launch {
            _uiState.value = AddJobUiState.Saving
            jobApplicationRepository.save(job)
            pendingJob = null
            _uiState.value = AddJobUiState.Saved(job.id)
        }
    }

    fun dismissDuplicate() {
        pendingJob = null
        _uiState.value = AddJobUiState.Idle
    }
}
