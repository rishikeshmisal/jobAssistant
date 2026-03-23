package com.jobassistant.ui.screens.profile

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jobassistant.BuildConfig
import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.GmailTokenManager
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.UserProfile
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.AnalyzeIntentUseCase
import com.jobassistant.service.WorkManagerScheduler
import com.jobassistant.util.ExportManager
import com.jobassistant.util.PdfTextExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object ExtractingPdf : ProfileUiState()
    data class PdfExtracted(val previewText: String) : ProfileUiState()
    object AnalyzingIntent : ProfileUiState()
    data class IntentAnalyzed(val profile: CareerProfile) : ProfileUiState()
    object Saved : ProfileUiState()
    object GmailConnecting : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileDataStore: UserProfileDataStore,
    private val analyzeIntentUseCase: AnalyzeIntentUseCase,
    private val pdfTextExtractor: PdfTextExtractor,
    private val jobApplicationRepository: JobApplicationRepository,
    private val exportManager: ExportManager,
    private val gmailTokenManager: GmailTokenManager
) : ViewModel() {

    val profileState: StateFlow<UserProfile> = userProfileDataStore.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    val gmailEmail: StateFlow<String?> = userProfileDataStore.gmailEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // 9.4 BYOK — expose current user API key (null = not set)
    val userApiKey: StateFlow<String?> = userProfileDataStore.userApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Gap 1 — expose re-auth flag so ProfileScreen can show a banner
    val gmailNeedsReauth: StateFlow<Boolean> = userProfileDataStore.gmailNeedsReauth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Gap 1 — one-shot PendingIntent emitted when interactive re-auth is required
    private val _reauthIntent = MutableStateFlow<PendingIntent?>(null)
    val reauthIntent: StateFlow<PendingIntent?> = _reauthIntent.asStateFlow()

    // 9.2 Export — one-shot URI emitted after exportToJson completes
    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri: StateFlow<Uri?> = _exportUri.asStateFlow()

    fun onResumePicked(uri: Uri, context: Context) {
        viewModelScope.launch {
            _profileUiState.value = ProfileUiState.ExtractingPdf
            val text = pdfTextExtractor.extract(uri, context)
            if (text != null) {
                userProfileDataStore.update { copy(resumeText = text) }
                _profileUiState.value = ProfileUiState.PdfExtracted(text.take(300))
                analyzeIntentIfNeeded()
            } else {
                _profileUiState.value = ProfileUiState.Error("Could not extract text from PDF")
            }
        }
    }

    fun analyzeIntentIfNeeded() {
        viewModelScope.launch {
            val profile = userProfileDataStore.userProfileFlow.first()
            if (profile.resumeText.isBlank()) return@launch
            _profileUiState.value = ProfileUiState.AnalyzingIntent
            when (val result = analyzeIntentUseCase(profile.resumeText, profile.careerGoal)) {
                is ClaudeResult.Success -> {
                    userProfileDataStore.update { copy(careerGoal = result.data.goalMap) }
                    _profileUiState.value = ProfileUiState.IntentAnalyzed(result.data)
                }
                is ClaudeResult.Error -> {
                    _profileUiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }

    fun saveProfile(
        fullName: String,
        careerGoal: String,
        keywords: List<String>,
        targetSalaryMin: Int,
        targetSalaryMax: Int
    ) {
        viewModelScope.launch {
            userProfileDataStore.update {
                copy(
                    fullName = fullName,
                    careerGoal = careerGoal,
                    keywords = keywords,
                    targetSalaryMin = targetSalaryMin,
                    targetSalaryMax = targetSalaryMax
                )
            }
            _profileUiState.value = ProfileUiState.Saved
        }
    }

    // 9.4 — save user-supplied Anthropic API key
    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            userProfileDataStore.saveUserApiKey(apiKey)
            _profileUiState.value = ProfileUiState.Saved
        }
    }

    // 9.2 — export all current jobs to JSON and emit the resulting URI
    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val jobs = jobApplicationRepository.getAllAsFlow().first()
                val uri = exportManager.exportToJson(context, jobs)
                _exportUri.value = uri
            } catch (e: Exception) {
                _profileUiState.value = ProfileUiState.Error("Export failed: ${e.message}")
            }
        }
    }

    // Called by ProfileScreen after the share intent has been fired
    fun consumeExportUri() {
        _exportUri.value = null
    }

    /**
     * Starts an interactive Gmail re-authentication flow.
     * If silent re-auth is already possible (e.g., token was just refreshed) the flag is cleared
     * in-place. Otherwise emits a [PendingIntent] that the UI must launch via
     * [ActivityResultContracts.StartIntentSenderForResult].
     */
    fun requestInteractiveReauth(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = AuthorizationRequest.Builder()
                    .setRequestedScopes(listOf(Scope(GmailTokenManager.GMAIL_SCOPE)))
                    .build()
                val result = Tasks.await(
                    Identity.getAuthorizationClient(context).authorize(request)
                )
                when {
                    result.accessToken != null -> {
                        // Silent re-auth succeeded — save token and clear the flag
                        gmailTokenManager.saveNewToken(result.accessToken!!)
                        userProfileDataStore.clearGmailNeedsReauth()
                    }
                    result.hasResolution() -> {
                        // User interaction required — surface the intent to the UI
                        _reauthIntent.value = result.pendingIntent
                    }
                    else -> {
                        _profileUiState.value = ProfileUiState.Error("Re-authentication unavailable")
                    }
                }
            } catch (e: Exception) {
                _profileUiState.value = ProfileUiState.Error("Re-authentication failed: ${e.message}")
            }
        }
    }

    /** Called after ProfileScreen has launched the re-auth intent. */
    fun consumeReauthIntent() {
        _reauthIntent.value = null
    }

    /** Called after the user completes the re-auth intent flow successfully. */
    fun onReauthCompleted(accessToken: String) {
        viewModelScope.launch {
            gmailTokenManager.saveNewToken(accessToken)
            userProfileDataStore.clearGmailNeedsReauth()
        }
    }

    fun connectGmail(context: Context) {
        viewModelScope.launch {
            _profileUiState.value = ProfileUiState.GmailConnecting
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    userProfileDataStore.saveGmailCredentials(
                        token = credential.idToken,
                        email = credential.id
                    )
                    WorkManagerScheduler.scheduleGmailSync(context)
                    _profileUiState.value = ProfileUiState.Idle
                } else {
                    _profileUiState.value = ProfileUiState.Error("Sign-in failed: unexpected credential type")
                }
            } catch (e: GetCredentialException) {
                _profileUiState.value = ProfileUiState.Error("Sign-in failed: ${e.message}")
            } catch (e: Exception) {
                _profileUiState.value = ProfileUiState.Error("Sign-in failed: ${e.message}")
            }
        }
    }

    fun disconnectGmail(context: Context) {
        viewModelScope.launch {
            userProfileDataStore.clearGmailCredentials()
            WorkManagerScheduler.cancelGmailSync(context)
            _profileUiState.value = ProfileUiState.Idle
        }
    }

    fun syncGmailNow(context: Context) {
        androidx.work.WorkManager.getInstance(context)
            .enqueue(
                androidx.work.OneTimeWorkRequestBuilder<com.jobassistant.service.GmailSyncWorker>()
                    .build()
            )
    }

    fun resetUiState() {
        _profileUiState.value = ProfileUiState.Idle
    }
}
