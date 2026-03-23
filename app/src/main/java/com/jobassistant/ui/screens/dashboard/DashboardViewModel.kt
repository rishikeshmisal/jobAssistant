package com.jobassistant.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.usecase.DeleteJobApplicationUseCase
import com.jobassistant.domain.usecase.GetAllJobsUseCase
import com.jobassistant.domain.usecase.UpdateJobStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode { KANBAN, LIST }

data class DashboardUiState(
    val jobsByStatus: Map<ApplicationStatus, List<JobApplication>> = emptyMap(),
    val viewMode: ViewMode = ViewMode.KANBAN,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAllJobsUseCase: GetAllJobsUseCase,
    private val updateJobStatusUseCase: UpdateJobStatusUseCase,
    private val deleteJobApplicationUseCase: DeleteJobApplicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadJobs()
    }

    private fun loadJobs() {
        viewModelScope.launch {
            getAllJobsUseCase()
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { jobs ->
                    _uiState.value = _uiState.value.copy(
                        jobsByStatus = jobs.groupBy { it.status },
                        isLoading = false
                    )
                }
        }
    }

    fun setStatus(job: JobApplication, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            updateJobStatusUseCase(job.id, newStatus)
        }
    }

    fun deleteJob(job: JobApplication) {
        viewModelScope.launch {
            deleteJobApplicationUseCase(job)
        }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }
}
