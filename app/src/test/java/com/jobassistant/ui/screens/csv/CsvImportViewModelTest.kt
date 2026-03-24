package com.jobassistant.ui.screens.csv

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.domain.model.CsvColumnMapping
import com.jobassistant.domain.model.CsvImportPreview
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.usecase.ImportCsvUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CsvImportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var importCsvUseCase: ImportCsvUseCase
    private lateinit var viewModel: CsvImportViewModel

    private val fakeMapping = CsvColumnMapping(
        columnMappings = mapOf("Company" to "companyName", "Role" to "roleTitle"),
        statusMappings = emptyMap(),
        datePattern = null
    )

    private val fakePreview = CsvImportPreview(
        jobs = listOf(
            JobApplication(companyName = "Google", roleTitle = "SWE"),
            JobApplication(companyName = "Meta", roleTitle = "PM")
        ),
        columnMapping = fakeMapping,
        totalRows = 2,
        skippedRows = 0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        importCsvUseCase = mockk()
        viewModel = CsvImportViewModel(importCsvUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is CsvImportUiState.Idle)
    }

    @Test
    fun `reset() returns to Idle`() = runTest {
        coEvery { importCsvUseCase.preview(any()) } returns ClaudeResult.Success(fakePreview)
        // Drive to a non-Idle state first
        viewModel.reset()
        assertTrue(viewModel.uiState.value is CsvImportUiState.Idle)
    }

    @Test
    fun `confirmImport transitions Preview to Importing to Done`() = runTest {
        coEvery { importCsvUseCase.commit(fakePreview) } returns Pair(2, 0)

        // Manually put the VM in Preview state
        val previewState = CsvImportUiState.Preview(fakePreview)
        val field = CsvImportViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<CsvImportUiState>).value = previewState

        viewModel.confirmImport()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CsvImportUiState.Done)
        val done = state as CsvImportUiState.Done
        assertEquals(2, done.imported)
        assertEquals(0, done.duplicates)
    }

    @Test
    fun `confirmImport Done state carries correct imported and duplicates counts`() = runTest {
        coEvery { importCsvUseCase.commit(fakePreview) } returns Pair(1, 1)

        val field = CsvImportViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<CsvImportUiState>).value =
            CsvImportUiState.Preview(fakePreview)

        viewModel.confirmImport()
        advanceUntilIdle()

        val done = viewModel.uiState.value as CsvImportUiState.Done
        assertEquals(1, done.imported)
        assertEquals(1, done.duplicates)
    }

    @Test
    fun `confirmImport is a no-op when state is not Preview`() = runTest {
        viewModel.confirmImport()   // state is Idle
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CsvImportUiState.Idle)
    }
}
