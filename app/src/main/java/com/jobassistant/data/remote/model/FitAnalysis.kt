package com.jobassistant.data.remote.model

import com.google.gson.annotations.SerializedName

data class FitAnalysis(
    val score: Int = 0,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    @SerializedName("missing_skills") val missingSkills: List<String> = emptyList()
)
