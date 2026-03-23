package com.jobassistant.data.remote

import com.jobassistant.data.repository.GmailAuthExpiredException
import com.jobassistant.data.repository.GmailTokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that attaches a valid Gmail OAuth2 Bearer token to every request
 * on the Gmail OkHttpClient.
 *
 * - Calls [GmailTokenManager.getValidToken] before forwarding the request; this may perform
 *   a silent token refresh if the stored token is within 5 minutes of expiry.
 * - On HTTP 401 (server rejects the token despite our expiry check), marks re-auth as needed
 *   and throws [GmailAuthExpiredException] so the caller is not tempted to retry with the
 *   same invalid token.
 */
class GmailAuthInterceptor @Inject constructor(
    private val gmailTokenManager: GmailTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // runBlocking is intentional — OkHttp interceptors are synchronous
        val token = runBlocking { gmailTokenManager.getValidToken() }

        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(request)

        // 401 means the server rejected the token — treat as auth expiry, not a retryable error
        if (response.code == 401) {
            response.close()
            runBlocking { gmailTokenManager.markNeedsReauth() }
            throw GmailAuthExpiredException("Server rejected Gmail token (HTTP 401)")
        }

        return response
    }
}
