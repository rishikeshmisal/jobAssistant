package com.jobassistant.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SeedState {
    object Idle : SeedState()
    object Seeding : SeedState()
    data class Done(val count: Int) : SeedState()
    data class Error(val message: String) : SeedState()
}

@HiltViewModel
class DebugSeedViewModel @Inject constructor(
    private val seedDataHelper: SeedDataHelper
) : ViewModel() {

    private val _seedState = MutableStateFlow<SeedState>(SeedState.Idle)
    val seedState: StateFlow<SeedState> = _seedState.asStateFlow()

    fun seed() {
        if (_seedState.value is SeedState.Seeding) return
        viewModelScope.launch {
            _seedState.value = SeedState.Seeding
            _seedState.value = try {
                val count = seedDataHelper.seed()
                SeedState.Done(count)
            } catch (e: Exception) {
                SeedState.Error(e.message ?: "Seeding failed")
            }
        }
    }

    fun resetSeedState() {
        _seedState.value = SeedState.Idle
    }
}
