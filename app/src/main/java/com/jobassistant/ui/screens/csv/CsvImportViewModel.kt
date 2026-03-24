package com.jobassistant.ui.screens.csv

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.domain.model.CsvImportPreview
import com.jobassistant.domain.usecase.ImportCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class CsvImportUiState {
    object Idle : CsvImportUiState()
    object ReadingFile : CsvImportUiState()
    object MappingColumns : CsvImportUiState()
    data class Preview(val preview: CsvImportPreview) : CsvImportUiState()
    data class Importing(val jobCount: Int) : CsvImportUiState()
    data class Done(val imported: Int, val duplicates: Int) : CsvImportUiState()
    data class Error(val message: String) : CsvImportUiState()
}

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val importCsvUseCase: ImportCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CsvImportUiState>(CsvImportUiState.Idle)
    val uiState: StateFlow<CsvImportUiState> = _uiState.asStateFlow()

    fun onCsvPicked(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = CsvImportUiState.ReadingFile

            val csvText = try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CsvImportUiState.Error("Could not read file: ${e.message}")
                return@launch
            }

            if (csvText.isNullOrBlank()) {
                _uiState.value = CsvImportUiState.Error("The selected file is empty")
                return@launch
            }

            _uiState.value = CsvImportUiState.MappingColumns

            when (val result = importCsvUseCase.preview(csvText)) {
                is ClaudeResult.Success -> _uiState.value = CsvImportUiState.Preview(result.data)
                is ClaudeResult.Error   -> _uiState.value = CsvImportUiState.Error(result.message)
            }
        }
    }

    fun confirmImport() {
        val current = _uiState.value as? CsvImportUiState.Preview ?: return
        viewModelScope.launch {
            _uiState.value = CsvImportUiState.Importing(current.preview.jobs.size)
            val (imported, duplicates) = importCsvUseCase.commit(current.preview)
            _uiState.value = CsvImportUiState.Done(imported, duplicates)
        }
    }

    fun reset() {
        _uiState.value = CsvImportUiState.Idle
    }
}
