package com.jobassistant.data.remote.model

import com.google.gson.annotations.SerializedName

enum class EmailActionType { APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT }

data class EmailAction(
    @SerializedName("action_type") val actionType: EmailActionType = EmailActionType.IRRELEVANT,
    @SerializedName("target_company") val targetCompany: String? = null,
    @SerializedName("role_title") val roleTitle: String? = null,
    val date: Long? = null,
    @SerializedName("interview_link") val interviewLink: String? = null
)
