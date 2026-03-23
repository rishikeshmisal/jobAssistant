package com.jobassistant.domain.model

data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val resumeText: String = "",
    val keywords: List<String> = emptyList(),
    val careerGoal: String = "",
    val targetSalaryMin: Int = 0,
    val targetSalaryMax: Int = 0,
    val selectedTheme: AppTheme = AppTheme.GREEN,
    val isOnboardingComplete: Boolean = false
)
