package com.jobassistant.data.remote.model

import com.google.gson.annotations.SerializedName

data class CareerProfile(
    @SerializedName("current_level") val currentLevel: String = "",
    @SerializedName("target_roles") val targetRoles: List<String> = emptyList(),
    @SerializedName("skill_gaps") val skillGaps: List<String> = emptyList(),
    @SerializedName("recommended_focus_areas") val recommendedFocusAreas: List<String> = emptyList(),
    @SerializedName("goal_map") val goalMap: String = ""
)
