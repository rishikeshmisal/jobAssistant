package com.jobassistant.data.remote.model

import com.google.gson.annotations.SerializedName

data class CareerInsightsResult(
    @SerializedName("identified_gaps") val identifiedGaps: List<String> = emptyList(),
    @SerializedName("recommended_actions") val recommendedActions: List<String> = emptyList(),
    @SerializedName("market_feedback_summary") val marketFeedbackSummary: String = ""
)
