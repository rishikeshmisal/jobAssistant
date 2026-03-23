package com.jobassistant.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userProfileDataStore: UserProfileDataStore
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dataStoreScope = CoroutineScope(testDispatcher + Job())
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tmpFolder.newFile("test_prefs_vm.preferences_pb") }
        )
        userProfileDataStore = UserProfileDataStore(dataStore)
        viewModel = MainViewModel(userProfileDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        dataStoreScope.cancel()
    }

    @Test
    fun initialState_hasDefaultThemeAndOnboardingIncomplete() = runTest {
        val state = viewModel.uiState.first()
        assertEquals(AppTheme.GREEN, state.selectedTheme)
        assertFalse(state.isOnboardingComplete)
    }

    @Test
    fun setTheme_updatesSelectedThemeInState() = runTest {
        viewModel.setTheme(AppTheme.BLUE)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(AppTheme.BLUE, state.selectedTheme)
    }

    @Test
    fun setTheme_allThemesCanBeSet() = runTest {
        for (theme in AppTheme.values()) {
            viewModel.setTheme(theme)
            advanceUntilIdle()
            assertEquals(theme, viewModel.uiState.first().selectedTheme)
        }
    }

    @Test
    fun completeOnboarding_setsIsOnboardingCompleteTrue() = runTest {
        viewModel.completeOnboarding("Alice", "Land a senior Android role")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isOnboardingComplete)
    }

    @Test
    fun isOnboardingComplete_false_startsAtOnboarding() = runTest {
        val state = viewModel.uiState.first()
        assertFalse(state.isOnboardingComplete)
    }

    @Test
    fun isOnboardingComplete_true_skipsOnboarding() = runTest {
        viewModel.completeOnboarding("Bob", "Become a tech lead")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.isOnboardingComplete)
    }
}
