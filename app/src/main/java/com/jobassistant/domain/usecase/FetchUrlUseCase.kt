package com.jobassistant.domain.usecase

import com.jobassistant.util.HtmlStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named

class FetchUrlUseCase @Inject constructor(
    @Named("plain") private val httpClient: OkHttpClient
) {
    suspend operator fun invoke(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(
                Request.Builder().url(url).build()
            ).execute()
            response.body?.string()?.let { HtmlStripper.stripHtml(it).take(4000) }
        } catch (e: Exception) {
            null
        }
    }
}
