package com.jobassistant.ui.screens.detail

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.FitAnalysis
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.EvaluateFitUseCase
import com.jobassistant.domain.usecase.FetchUrlUseCase
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.domain.usecase.SaveResult
import com.jobassistant.util.OcrProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class AddJobUiState {
    object Idle : AddJobUiState()
    object Analyzing : AddJobUiState()
    data class FitResult(val analysis: FitAnalysis) : AddJobUiState()
    object Saving : AddJobUiState()
    object Saved : AddJobUiState()
    data class Duplicate(val companyName: String, val roleTitle: String) : AddJobUiState()
    data class Error(val message: String) : AddJobUiState()
}

@HiltViewModel
class AddJobViewModel @Inject constructor(
    private val evaluateFitUseCase: EvaluateFitUseCase,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase,
    private val userProfileDataStore: UserProfileDataStore,
    private val fetchUrlUseCase: FetchUrlUseCase,
    private val jobApplicationRepository: JobApplicationRepository,
    private val ocrProcessor: OcrProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddJobUiState>(AddJobUiState.Idle)
    val uiState: StateFlow<AddJobUiState> = _uiState.asStateFlow()

    private val _ocrText = MutableStateFlow("")
    val ocrText: StateFlow<String> = _ocrText.asStateFlow()

    private var pendingJob: JobApplication? = null

    fun analyzeFit(jobDescription: String) {
        viewModelScope.launch {
            _uiState.value = AddJobUiState.Analyzing
            val resumeText = userProfileDataStore.userProfileFlow.first().resumeText
            when (val result = evaluateFitUseCase(resumeText, jobDescription)) {
                is ClaudeResult.Success -> _uiState.value = AddJobUiState.FitResult(result.data)
                is ClaudeResult.Error -> _uiState.value = AddJobUiState.Error(result.message)
            }
        }
    }

    fun fetchAndAnalyzeUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = AddJobUiState.Analyzing
            val strippedText = fetchUrlUseCase(url)
            if (strippedText.isNullOrBlank()) {
                _uiState.value = AddJobUiState.Error("Could not fetch content from URL")
                return@launch
            }
            val resumeText = userProfileDataStore.userProfileFlow.first().resumeText
            when (val result = evaluateFitUseCase(resumeText, strippedText)) {
                is ClaudeResult.Success -> _uiState.value = AddJobUiState.FitResult(result.data)
                is ClaudeResult.Error -> _uiState.value = AddJobUiState.Error(result.message)
            }
        }
    }

    fun processScreenshot(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = AddJobUiState.Analyzing
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (bitmap == null) {
                    _uiState.value = AddJobUiState.Error("Could not load image")
                    return@launch
                }
                val text = ocrProcessor.extractText(bitmap)
                _ocrText.value = text
                _uiState.value = AddJobUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AddJobUiState.Error("OCR failed: ${e.message}")
            }
        }
    }

    @VisibleForTesting
    fun setOcrTextForTesting(text: String) {
        _ocrText.value = text
    }

    fun saveJob(
        companyName: String,
        roleTitle: String,
        jobDescription: String,
        location: String,
        salaryRange: String,
        fitScore: Int
    ) {
        viewModelScope.launch {
            _uiState.value = AddJobUiState.Saving
            val job = JobApplication(
                id = UUID.randomUUID(),
                companyName = companyName,
                roleTitle = roleTitle,
                status = ApplicationStatus.SAVED,
                fitScore = fitScore,
                location = location,
                salaryRange = salaryRange,
                appliedDate = null,
                interviewDate = null,
                notes = jobDescription.take(500),
                linkedEmailThreadIds = emptyList(),
                lastSeenDate = System.currentTimeMillis()
            )
            when (val result = saveJobApplicationUseCase(job)) {
                is SaveResult.Saved -> {
                    pendingJob = null
                    _uiState.value = AddJobUiState.Saved
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
            _uiState.value = AddJobUiState.Saved
        }
    }

    fun dismissDuplicate() {
        pendingJob = null
        _uiState.value = AddJobUiState.Idle
    }
}
