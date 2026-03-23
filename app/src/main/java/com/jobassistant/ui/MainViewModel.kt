package com.jobassistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val selectedTheme: AppTheme = AppTheme.GREEN,
    val isOnboardingComplete: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userProfileDataStore: UserProfileDataStore
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = userProfileDataStore.userProfileFlow
        .map { profile ->
            MainUiState(
                selectedTheme = profile.selectedTheme,
                isOnboardingComplete = profile.isOnboardingComplete
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState()
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userProfileDataStore.update { copy(selectedTheme = theme) }
        }
    }

    fun completeOnboarding(
        name: String,
        careerGoal: String,
        keywords: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            userProfileDataStore.update {
                copy(
                    fullName = name,
                    careerGoal = careerGoal,
                    keywords = keywords,
                    isOnboardingComplete = true
                )
            }
        }
    }
}
