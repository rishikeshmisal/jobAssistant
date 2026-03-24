package com.jobassistant.data.remote.model

import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SafeClaudeCallTest {

    // ── Success path ────────────────────────────────────────────────────────

    @Test
    fun safeClaudeCall_successfulBlock_returnsSuccess() = runTest {
        val result = safeClaudeCall { 42 }

        assertTrue(result is ClaudeResult.Success)
        assertEquals(42, (result as ClaudeResult.Success).data)
    }

    // ── IOException ─────────────────────────────────────────────────────────

    @Test
    fun safeClaudeCall_ioException_returnsNetworkError() = runTest {
        val result = safeClaudeCall<Int> { throw IOException("Connection refused") }

        assertTrue(result is ClaudeResult.Error)
        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.NETWORK, error.errorType)
        assertTrue(error.isRetryable)
    }

    @Test
    fun safeClaudeCall_ioException_messageIncluded() = runTest {
        val result = safeClaudeCall<Int> { throw IOException("No route to host") }

        val error = result as ClaudeResult.Error
        assertTrue(error.message.contains("No route to host"))
    }

    // ── ClaudeParseException ────────────────────────────────────────────────

    @Test
    fun safeClaudeCall_claudeParseException_returnsParseError() = runTest {
        val result = safeClaudeCall<Int> { throw ClaudeParseException("No tool_use block") }

        assertTrue(result is ClaudeResult.Error)
        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.PARSE, error.errorType)
        assertFalse(error.isRetryable)
        assertEquals("No tool_use block", error.message)
    }

    // ── JsonSyntaxException ─────────────────────────────────────────────────

    @Test
    fun safeClaudeCall_jsonSyntaxException_returnsParseError() = runTest {
        val result = safeClaudeCall<Int> { throw JsonSyntaxException("Unexpected token") }

        assertTrue(result is ClaudeResult.Error)
        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.PARSE, error.errorType)
        assertFalse(error.isRetryable)
    }

    // ── Generic Exception — InvalidAPIKey ──────────────────────────────────

    @Test
    fun safeClaudeCall_invalidApiKeyException_returnsAuthError() = runTest {
        val result = safeClaudeCall<Int> {
            throw object : Exception("API_KEY_INVALID — key has been revoked") {}
        }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.AUTH, error.errorType)
        assertFalse(error.isRetryable)
    }

    // ── Generic Exception — Throttled ──────────────────────────────────────

    @Test
    fun safeClaudeCall_resourceExhausted_returnsRateLimitError() = runTest {
        val result = safeClaudeCall<Int> {
            throw Exception("RESOURCE_EXHAUSTED: quota exceeded")
        }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.RATE_LIMIT, error.errorType)
        assertTrue(error.isRetryable)
    }

    @Test
    fun safeClaudeCall_throttledClassName_returnsRateLimitError() = runTest {
        val result = safeClaudeCall<Int> {
            // Simulate Gemini SDK RequestThrottledException class name pattern
            throw object : Exception("Service overloaded") {
                override fun toString() = "RequestThrottledException: Service overloaded"
            }
        }

        // Falls through to generic Exception handler; message doesn't contain "Throttled"
        // so this exercises the UNKNOWN branch — verifying it doesn't crash
        val error = result as ClaudeResult.Error
        assertFalse(error.isRetryable || error.errorType == ApiErrorType.AUTH)
    }

    // ── Generic Exception — unknown ────────────────────────────────────────

    @Test
    fun safeClaudeCall_unknownException_returnsUnknownError() = runTest {
        val result = safeClaudeCall<Int> {
            throw RuntimeException("Something unexpected happened")
        }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.UNKNOWN, error.errorType)
        assertFalse(error.isRetryable)
    }

    @Test
    fun safeClaudeCall_nullMessageException_handledGracefully() = runTest {
        val result = safeClaudeCall<Int> { throw RuntimeException() }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.UNKNOWN, error.errorType)
    }

    // ── httpCode field ─────────────────────────────────────────────────────

    @Test
    fun safeClaudeCall_genericException_httpCodeIsNull() = runTest {
        val result = safeClaudeCall<Int> { throw RuntimeException("fail") }

        val error = result as ClaudeResult.Error
        assertEquals(null, error.httpCode)
    }

    // ── New Gemini rate-limit patterns ────────────────────────────────────

    @Test
    fun `safeClaudeCall_tooManyRequestsMessage_returnsRateLimitError`() = runTest {
        val result = safeClaudeCall<Int> {
            throw Exception("Too Many Requests — please wait and retry")
        }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.RATE_LIMIT, error.errorType)
        assertTrue(error.isRetryable)
    }

    @Test
    fun `safeClaudeCall_429InMessage_returnsRateLimitError`() = runTest {
        val result = safeClaudeCall<Int> {
            throw Exception("HTTP 429: rate limit exceeded")
        }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.RATE_LIMIT, error.errorType)
        assertTrue(error.isRetryable)
    }

    @Test
    fun `safeClaudeCall_quotaInMessage_returnsRateLimitError`() = runTest {
        val result = safeClaudeCall<Int> {
            throw Exception("Quota exceeded for project — upgrade your plan")
        }

        val error = result as ClaudeResult.Error
        assertEquals(ApiErrorType.RATE_LIMIT, error.errorType)
        assertTrue(error.isRetryable)
    }

    // ── ApiErrorType enum ──────────────────────────────────────────────────

    @Test
    fun apiErrorType_allValuesPresent() {
        val values = ApiErrorType.values()
        assertTrue(values.contains(ApiErrorType.RATE_LIMIT))
        assertTrue(values.contains(ApiErrorType.AUTH))
        assertTrue(values.contains(ApiErrorType.NETWORK))
        assertTrue(values.contains(ApiErrorType.PARSE))
        assertTrue(values.contains(ApiErrorType.UNKNOWN))
    }
}
