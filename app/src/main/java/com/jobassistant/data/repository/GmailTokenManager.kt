package com.jobassistant.data.repository

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the Gmail OAuth2 access token lifecycle.
 *
 * Android's OAuth2 tokens expire in ~60 minutes. This manager:
 * 1. Returns the stored token immediately if it is still valid (>5 min remaining).
 * 2. Attempts a silent re-authorization via [Identity.getAuthorizationClient] if the token
 *    is within the expiry buffer. Silent re-auth succeeds when the user has previously granted
 *    the Gmail scope and the permission has not been revoked.
 * 3. If silent re-auth requires user interaction (first grant or revocation), marks
 *    [UserProfileDataStore.gmailNeedsReauth] = true and throws [GmailAuthExpiredException].
 *    The caller ([GmailSyncWorker]) is responsible for surfacing a notification.
 */
@Singleton
class GmailTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDataStore: UserProfileDataStore
) {

    companion object {
        const val GMAIL_SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
        private const val TOKEN_LIFETIME_MS = 3_600_000L    // 1 hour (Google's default)
        private const val EXPIRY_BUFFER_MS = 5 * 60_000L   // refresh 5 min before expiry
    }

    /**
     * Returns a valid Bearer token string, refreshing silently if needed.
     * @throws GmailAuthExpiredException if user interaction is required to re-authenticate.
     */
    suspend fun getValidToken(): String {
        val storedToken = userProfileDataStore.gmailAccessToken.firstOrNull()
        val expiry = userProfileDataStore.gmailTokenExpiry.firstOrNull() ?: 0L

        // Token is fresh — return it immediately
        if (!storedToken.isNullOrBlank() &&
            System.currentTimeMillis() < expiry - EXPIRY_BUFFER_MS
        ) {
            return storedToken
        }

        // Token is expired or nearly so — attempt silent re-authorization
        return trySilentReauthorize()
            ?: throw GmailAuthExpiredException()
    }

    /**
     * Stores a freshly obtained access token (called by [ProfileViewModel] after a
     * successful interactive authorization in the UI).
     */
    suspend fun saveNewToken(accessToken: String) {
        val expiry = System.currentTimeMillis() + TOKEN_LIFETIME_MS
        userProfileDataStore.saveGmailAccessToken(accessToken, expiry)
    }

    /** Called by [GmailAuthInterceptor] when the server returns HTTP 401. */
    suspend fun markNeedsReauth() {
        userProfileDataStore.markGmailNeedsReauth()
    }

    // ── private ──────────────────────────────────────────────────────────────

    /**
     * Tries to silently obtain a new access token via [Identity.getAuthorizationClient].
     * Returns the token string on success, or null if user interaction is required.
     */
    private suspend fun trySilentReauthorize(): String? {
        return try {
            val request = AuthorizationRequest.Builder()
                .setRequestedScopes(listOf(Scope(GMAIL_SCOPE)))
                .build()

            // Tasks.await is intentional here — this is called from an OkHttp interceptor
            // thread (blocking IO context), not from the main thread.
            val result = Tasks.await(
                Identity.getAuthorizationClient(context).authorize(request)
            )

            if (result.hasResolution() || result.accessToken == null) {
                // User interaction required — cannot silently refresh
                userProfileDataStore.markGmailNeedsReauth()
                null
            } else {
                val token = result.accessToken!!
                saveNewToken(token)
                token
            }
        } catch (e: Exception) {
            userProfileDataStore.markGmailNeedsReauth()
            null
        }
    }
}
