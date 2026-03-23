package com.jobassistant.data.db.mapper

import com.jobassistant.data.db.entity.JobApplicationEntity
import com.jobassistant.domain.model.JobApplication
import java.util.UUID

fun JobApplicationEntity.toDomain(): JobApplication = JobApplication(
    id = UUID.fromString(id),
    companyName = companyName,
    roleTitle = roleTitle,
    jobUrl = jobUrl,
    status = status,
    fitScore = fitScore,
    location = location,
    salaryRange = salaryRange,
    appliedDate = appliedDate,
    interviewDate = interviewDate,
    notes = notes,
    linkedEmailThreadIds = linkedEmailThreadIds,
    lastSeenDate = lastSeenDate
)

fun JobApplication.toEntity(): JobApplicationEntity = JobApplicationEntity(
    id = id.toString(),
    companyName = companyName,
    roleTitle = roleTitle,
    jobUrl = jobUrl,
    status = status,
    fitScore = fitScore,
    location = location,
    salaryRange = salaryRange,
    appliedDate = appliedDate,
    interviewDate = interviewDate,
    notes = notes,
    linkedEmailThreadIds = linkedEmailThreadIds,
    lastSeenDate = lastSeenDate
)
