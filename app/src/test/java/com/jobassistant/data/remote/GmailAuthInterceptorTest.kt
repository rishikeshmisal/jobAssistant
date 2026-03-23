package com.jobassistant.data.remote

import com.jobassistant.data.repository.GmailAuthExpiredException
import com.jobassistant.data.repository.GmailTokenManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class GmailAuthInterceptorTest {

    private val mockWebServer = MockWebServer()
    private lateinit var gmailTokenManager: GmailTokenManager
    private lateinit var interceptor: GmailAuthInterceptor
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        mockWebServer.start()
        gmailTokenManager = mockk()
        interceptor = GmailAuthInterceptor(gmailTokenManager)
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun get(): okhttp3.mockwebserver.RecordedRequest {
        val request = Request.Builder()
            .url(mockWebServer.url("/gmail/v1/users/me/messages"))
            .build()
        client.newCall(request).execute().close()
        return mockWebServer.takeRequest()
    }

    @Test
    fun `valid token is attached as Bearer Authorization header`() = runBlocking {
        coEvery { gmailTokenManager.getValidToken() } returns "ya29.valid-token"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val recorded = get()

        assertEquals("Bearer ya29.valid-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `token is refreshed between requests when getValidToken returns new value`() = runBlocking {
        coEvery { gmailTokenManager.getValidToken() } returnsMany listOf("token-1", "token-2")
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val r1 = get()
        val r2 = get()

        assertEquals("Bearer token-1", r1.getHeader("Authorization"))
        assertEquals("Bearer token-2", r2.getHeader("Authorization"))
    }

    @Test
    fun `HTTP 401 response marks reauth and throws GmailAuthExpiredException`() {
        coEvery { gmailTokenManager.getValidToken() } returns "expired-token"
        coEvery { gmailTokenManager.markNeedsReauth() } returns Unit
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val request = Request.Builder()
            .url(mockWebServer.url("/gmail/v1/users/me/messages"))
            .build()

        assertThrows(GmailAuthExpiredException::class.java) {
            client.newCall(request).execute()
        }

        coVerify(exactly = 1) { gmailTokenManager.markNeedsReauth() }
    }

    @Test
    fun `HTTP 403 response is returned without throwing`() = runBlocking {
        coEvery { gmailTokenManager.getValidToken() } returns "valid-token"
        mockWebServer.enqueue(MockResponse().setResponseCode(403))

        val request = Request.Builder()
            .url(mockWebServer.url("/gmail/v1/users/me/messages"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(403, response.code)
        coVerify(exactly = 0) { gmailTokenManager.markNeedsReauth() }
        response.close()
    }

    @Test
    fun `HTTP 200 response is returned and markNeedsReauth is never called`() = runBlocking {
        coEvery { gmailTokenManager.getValidToken() } returns "good-token"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/gmail/v1/users/me/messages"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        coVerify(exactly = 0) { gmailTokenManager.markNeedsReauth() }
        response.close()
    }

    @Test
    fun `getValidToken is called once per request`() = runBlocking {
        coEvery { gmailTokenManager.getValidToken() } returns "token"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        get()
        get()

        coVerify(exactly = 2) { gmailTokenManager.getValidToken() }
    }
}
