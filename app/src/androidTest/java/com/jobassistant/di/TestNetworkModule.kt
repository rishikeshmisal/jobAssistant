package com.jobassistant.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jobassistant.data.remote.GmailApiService
import com.jobassistant.data.remote.model.GmailBody
import com.jobassistant.data.remote.model.GmailHeader
import com.jobassistant.data.remote.model.GmailListResponse
import com.jobassistant.data.remote.model.GmailMessage
import com.jobassistant.data.remote.model.GmailMessageRef
import com.jobassistant.data.remote.model.GmailPayload
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Replaces [NetworkModule] in instrumented tests.
 *
 * Provides a [FakeGmailApiService] that returns a single APPLIED email for
 * "Test Corp / QA Engineer", so [GmailSyncWorkerIntegrationTest] can verify
 * end-to-end DB insertion without real network calls.
 *
 * AI calls (ClaudeRepository) are faked by [TestAiModule].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object TestNetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideGmailApiService(): GmailApiService = FakeGmailApiService()
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

private fun encodeBase64Url(text: String): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(text.toByteArray())

class FakeGmailApiService : GmailApiService {

    override suspend fun listMessages(
        query: String,
        maxResults: Int
    ): GmailListResponse = GmailListResponse(
        messages = listOf(GmailMessageRef(id = "test-msg-1", threadId = "test-thread-1"))
    )

    override suspend fun getMessage(
        id: String,
        format: String
    ): GmailMessage = GmailMessage(
        id = "test-msg-1",
        threadId = "test-thread-1",
        payload = GmailPayload(
            headers = listOf(
                GmailHeader("Subject", "Application received - QA Engineer at Test Corp"),
                GmailHeader("From", "careers@testcorp.com")
            ),
            body = GmailBody(
                data = encodeBase64Url(
                    "We received your application for the QA Engineer role. " +
                        "Thank you for applying to Test Corp!"
                )
            ),
            parts = null
        )
    )
}
