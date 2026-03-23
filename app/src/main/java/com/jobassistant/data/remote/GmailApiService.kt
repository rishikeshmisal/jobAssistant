package com.jobassistant.data.remote

import com.jobassistant.data.remote.model.GmailListResponse
import com.jobassistant.data.remote.model.GmailMessage
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GmailApiService {

    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Query("q") query: String = "newer_than:1d",
        @Query("maxResults") maxResults: Int = 50
    ): GmailListResponse

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Path("id") id: String,
        @Query("format") format: String = "full"
    ): GmailMessage
}
