package com.jobassistant.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard  : Screen("dashboard")
    object JobDetail  : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    object AddJob    : Screen("add_job")
    object Profile   : Screen("profile")
    object Insights  : Screen("insights")
}
