package com.jobassistant.data.remote.model

data class GmailListResponse(
    val messages: List<GmailMessageRef>? = null
)

data class GmailMessageRef(
    val id: String,
    val threadId: String
)

data class GmailMessage(
    val id: String,
    val threadId: String,
    val payload: GmailPayload
)

data class GmailPayload(
    val headers: List<GmailHeader>,
    val body: GmailBody?,
    val parts: List<GmailPart>?
)

data class GmailHeader(
    val name: String,
    val value: String
)

data class GmailBody(
    val data: String?
)

data class GmailPart(
    val mimeType: String,
    val body: GmailBody?
)
