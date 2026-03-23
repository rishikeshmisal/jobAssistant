package com.jobassistant.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jobassistant.domain.model.ApplicationStatus

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val roleTitle: String,
    val jobUrl: String?,
    val status: ApplicationStatus,
    val fitScore: Int?,
    val location: String?,
    val salaryRange: String?,
    val appliedDate: Long?,
    val interviewDate: Long?,
    val notes: String,
    val linkedEmailThreadIds: List<String>,
    val lastSeenDate: Long
)
