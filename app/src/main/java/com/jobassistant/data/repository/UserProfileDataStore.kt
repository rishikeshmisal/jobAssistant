package com.jobassistant.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jobassistant.domain.model.AppTheme
import com.jobassistant.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Stores UserProfile fields in a [DataStore<Preferences>] backed by a private file.
 * Android's app sandbox protects this file from other apps.
 *
 * Note: Phase 7 will add an EncryptedFile encryption layer when OAuth tokens are stored here.
 * All OAuth tokens and the BYOK API key (Phase 9) must be stored via this same class.
 */
class UserProfileDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
        val KEY_RESUME_TEXT = stringPreferencesKey("resume_text")
        val KEY_KEYWORDS = stringPreferencesKey("keywords")
        val KEY_CAREER_GOAL = stringPreferencesKey("career_goal")
        val KEY_TARGET_SALARY_MIN = intPreferencesKey("target_salary_min")
        val KEY_TARGET_SALARY_MAX = intPreferencesKey("target_salary_max")
        val KEY_SELECTED_THEME = stringPreferencesKey("selected_theme")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        // Phase 7: Gmail OAuth — identity (ID token)
        val KEY_GMAIL_TOKEN = stringPreferencesKey("gmail_token")
        val KEY_GMAIL_EMAIL = stringPreferencesKey("gmail_email")
        // Gmail OAuth2 — API access token with expiry + re-auth signal
        val KEY_GMAIL_ACCESS_TOKEN = stringPreferencesKey("gmail_access_token")
        val KEY_GMAIL_TOKEN_EXPIRY = longPreferencesKey("gmail_token_expiry")
        val KEY_GMAIL_NEEDS_REAUTH = booleanPreferencesKey("gmail_needs_reauth")
        // Phase 9: BYOK API key
        val KEY_USER_API_KEY = stringPreferencesKey("user_api_key")
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { prefs ->
        val keywordsJson = prefs[KEY_KEYWORDS] ?: ""
        val keywords = if (keywordsJson.isBlank()) emptyList()
        else keywordsJson.split(",").filter { it.isNotBlank() }

        UserProfile(
            userId = prefs[KEY_USER_ID] ?: "",
            fullName = prefs[KEY_FULL_NAME] ?: "",
            resumeText = prefs[KEY_RESUME_TEXT] ?: "",
            keywords = keywords,
            careerGoal = prefs[KEY_CAREER_GOAL] ?: "",
            targetSalaryMin = prefs[KEY_TARGET_SALARY_MIN] ?: 0,
            targetSalaryMax = prefs[KEY_TARGET_SALARY_MAX] ?: 0,
            selectedTheme = prefs[KEY_SELECTED_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.GREEN,
            isOnboardingComplete = prefs[KEY_ONBOARDING_COMPLETE] ?: false
        )
    }

    suspend fun save(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = profile.userId
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_RESUME_TEXT] = profile.resumeText
            prefs[KEY_KEYWORDS] = profile.keywords.joinToString(",")
            prefs[KEY_CAREER_GOAL] = profile.careerGoal
            prefs[KEY_TARGET_SALARY_MIN] = profile.targetSalaryMin
            prefs[KEY_TARGET_SALARY_MAX] = profile.targetSalaryMax
            prefs[KEY_SELECTED_THEME] = profile.selectedTheme.name
            prefs[KEY_ONBOARDING_COMPLETE] = profile.isOnboardingComplete
        }
    }

    suspend fun update(block: UserProfile.() -> UserProfile) {
        val current = userProfileFlow.first()
        save(block(current))
    }

    val gmailToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_GMAIL_TOKEN]?.takeIf { it.isNotBlank() }
    }

    val gmailEmail: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_GMAIL_EMAIL]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveGmailCredentials(token: String, email: String) {
        dataStore.edit { prefs ->
            prefs[KEY_GMAIL_TOKEN] = token
            prefs[KEY_GMAIL_EMAIL] = email
        }
    }

    suspend fun clearGmailCredentials() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_GMAIL_TOKEN)
            prefs.remove(KEY_GMAIL_EMAIL)
            prefs.remove(KEY_GMAIL_ACCESS_TOKEN)
            prefs.remove(KEY_GMAIL_TOKEN_EXPIRY)
            prefs.remove(KEY_GMAIL_NEEDS_REAUTH)
        }
    }

    // Gmail OAuth2 access token (short-lived, ~1 hour)
    val gmailAccessToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_GMAIL_ACCESS_TOKEN]?.takeIf { it.isNotBlank() }
    }

    val gmailTokenExpiry: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[KEY_GMAIL_TOKEN_EXPIRY]
    }

    /** True when the access token has expired and the worker cannot silently refresh. */
    val gmailNeedsReauth: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_GMAIL_NEEDS_REAUTH] ?: false
    }

    suspend fun saveGmailAccessToken(accessToken: String, expiryMs: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_GMAIL_ACCESS_TOKEN] = accessToken
            prefs[KEY_GMAIL_TOKEN_EXPIRY] = expiryMs
            prefs.remove(KEY_GMAIL_NEEDS_REAUTH)
        }
    }

    suspend fun markGmailNeedsReauth() {
        dataStore.edit { prefs ->
            prefs[KEY_GMAIL_NEEDS_REAUTH] = true
            prefs.remove(KEY_GMAIL_ACCESS_TOKEN)
        }
    }

    suspend fun clearGmailNeedsReauth() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_GMAIL_NEEDS_REAUTH)
        }
    }

    // Phase 9: BYOK — user-supplied Anthropic API key (null means not set)
    val userApiKey: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_API_KEY]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveUserApiKey(apiKey: String) {
        dataStore.edit { prefs ->
            if (apiKey.isBlank()) {
                prefs.remove(KEY_USER_API_KEY)
            } else {
                prefs[KEY_USER_API_KEY] = apiKey
            }
        }
    }
}
