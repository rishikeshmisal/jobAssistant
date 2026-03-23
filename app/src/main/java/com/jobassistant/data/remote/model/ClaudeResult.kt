package com.jobassistant.data.remote.model

import com.google.gson.JsonSyntaxException
import java.io.IOException

class ClaudeParseException(message: String) : Exception(message)

/** Categorises API failures so the UI can show targeted messages. */
enum class ApiErrorType {
    RATE_LIMIT,   // service throttling — temporary overload
    AUTH,         // invalid or missing API key
    NETWORK,      // IOException — no connectivity
    PARSE,        // malformed response from the model
    UNKNOWN       // anything else
}

sealed class ClaudeResult<out T> {
    data class Success<T>(val data: T) : ClaudeResult<T>()
    data class Error(
        val message: String,
        val isRetryable: Boolean,
        val httpCode: Int? = null,
        val errorType: ApiErrorType = ApiErrorType.UNKNOWN
    ) : ClaudeResult<Nothing>()
}

suspend fun <T> safeClaudeCall(block: suspend () -> T): ClaudeResult<T> {
    return try {
        ClaudeResult.Success(block())
    } catch (e: IOException) {
        ClaudeResult.Error("Network error: ${e.message}", isRetryable = true, errorType = ApiErrorType.NETWORK)
    } catch (e: ClaudeParseException) {
        ClaudeResult.Error(e.message ?: "Parse error", isRetryable = false, errorType = ApiErrorType.PARSE)
    } catch (e: JsonSyntaxException) {
        ClaudeResult.Error("Response parse error — schema mismatch", isRetryable = false, errorType = ApiErrorType.PARSE)
    } catch (e: Exception) {
        val msg = e.message ?: "Unknown error"
        val simpleName = e.javaClass.simpleName
        val errorType = when {
            simpleName.contains("InvalidAPIKey") ||
                msg.contains("API_KEY_INVALID", ignoreCase = true) -> ApiErrorType.AUTH
            simpleName.contains("Throttled") ||
                msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) -> ApiErrorType.RATE_LIMIT
            simpleName.contains("Network") -> ApiErrorType.NETWORK
            else -> ApiErrorType.UNKNOWN
        }
        val isRetryable = errorType == ApiErrorType.RATE_LIMIT || errorType == ApiErrorType.NETWORK
        ClaudeResult.Error(msg, isRetryable, errorType = errorType)
    }
}
