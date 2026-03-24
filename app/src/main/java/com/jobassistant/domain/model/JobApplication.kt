package com.jobassistant.domain.model

import java.util.UUID

data class JobApplication(
    val id: UUID = UUID.randomUUID(),
    val companyName: String,
    val roleTitle: String,
    val jobUrl: String? = null,
    val status: ApplicationStatus = ApplicationStatus.INTERESTED,
    val fitScore: Int? = null,
    val location: String? = null,
    val salaryRange: String? = null,
    val appliedDate: Long? = null,
    val interviewDate: Long? = null,
    val notes: String = "",
    val linkedEmailThreadIds: List<String> = emptyList(),
    val lastSeenDate: Long = System.currentTimeMillis(),
    val jobDescription: String = ""
)
