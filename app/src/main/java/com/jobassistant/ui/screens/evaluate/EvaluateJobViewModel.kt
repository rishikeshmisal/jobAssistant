package com.jobassistant.ui.screens.evaluate

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

sealed class EvaluateJobUiState {
    object Idle : EvaluateJobUiState()
    object Analyzing : EvaluateJobUiState()
    data class Result(
        val analysis: FitAnalysis,
        val jobDescription: String
    ) : EvaluateJobUiState()
    data class Saved(val jobId: UUID) : EvaluateJobUiState()
    data class Error(val message: String) : EvaluateJobUiState()
}

@HiltViewModel
class EvaluateJobViewModel @Inject constructor(
    private val evaluateFitUseCase: EvaluateFitUseCase,
    private val fetchUrlUseCase: FetchUrlUseCase,
    private val ocrProcessor: OcrProcessor,
    private val userProfileDataStore: UserProfileDataStore,
    private val saveJobApplicationUseCase: SaveJobApplicationUseCase,
    private val jobApplicationRepository: JobApplicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EvaluateJobUiState>(EvaluateJobUiState.Idle)
    val uiState: StateFlow<EvaluateJobUiState> = _uiState.asStateFlow()

    private val _ocrText = MutableStateFlow("")
    val ocrText: StateFlow<String> = _ocrText.asStateFlow()

    /** True when the user has not uploaded a resume — scoring will be inaccurate. */
    val resumeEmpty: StateFlow<Boolean> = userProfileDataStore.userProfileFlow
        .map { it.resumeText.isBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Analysis paths ────────────────────────────────────────────────────────

    fun analyzeFromPaste(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = EvaluateJobUiState.Analyzing
            val resumeText = userProfileDataStore.userProfileFlow.first().resumeText
            when (val result = evaluateFitUseCase(resumeText, text)) {
                is ClaudeResult.Success ->
                    _uiState.value = EvaluateJobUiState.Result(result.data, text)
                is ClaudeResult.Error ->
                    _uiState.value = EvaluateJobUiState.Error(result.message)
            }
        }
    }

    fun analyzeFromUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _uiState.value = EvaluateJobUiState.Analyzing
            val stripped = fetchUrlUseCase(url)
            if (stripped.isNullOrBlank()) {
                _uiState.value = EvaluateJobUiState.Error("Could not fetch content from URL")
                return@launch
            }
            val resumeText = userProfileDataStore.userProfileFlow.first().resumeText
            when (val result = evaluateFitUseCase(resumeText, stripped)) {
                is ClaudeResult.Success ->
                    _uiState.value = EvaluateJobUiState.Result(result.data, stripped)
                is ClaudeResult.Error ->
                    _uiState.value = EvaluateJobUiState.Error(result.message)
            }
        }
    }

    fun processScreenshot(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = EvaluateJobUiState.Analyzing
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                if (bitmap == null) {
                    _uiState.value = EvaluateJobUiState.Error("Could not load image")
                    return@launch
                }
                val text = ocrProcessor.extractText(bitmap)
                _ocrText.value = text
                _uiState.value = EvaluateJobUiState.Idle
            } catch (e: Exception) {
                _uiState.value = EvaluateJobUiState.Error("OCR failed: ${e.message}")
            }
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveJob(companyName: String, roleTitle: String) {
        val current = _uiState.value as? EvaluateJobUiState.Result ?: return
        viewModelScope.launch {
            val jobId = UUID.randomUUID()
            val job = JobApplication(
                id = jobId,
                companyName = companyName.ifBlank { "Unknown Company" },
                roleTitle = roleTitle.ifBlank { "Unknown Role" },
                status = ApplicationStatus.APPLIED,
                fitScore = current.analysis.score,
                jobDescription = current.jobDescription
            )
            when (val result = saveJobApplicationUseCase(job)) {
                is SaveResult.Saved -> _uiState.value = EvaluateJobUiState.Saved(jobId)
                is SaveResult.Duplicate -> {
                    // Update existing job with the new score + description, then navigate to it
                    val updated = result.existing.copy(
                        fitScore = current.analysis.score,
                        jobDescription = current.jobDescription
                    )
                    jobApplicationRepository.save(updated)
                    _uiState.value = EvaluateJobUiState.Saved(result.existing.id)
                }
            }
        }
    }

    fun reset() {
        _uiState.value = EvaluateJobUiState.Idle
        _ocrText.value = ""
    }
}
