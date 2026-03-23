package com.jobassistant.domain.repository

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface JobApplicationRepository {
    fun getAllAsFlow(): Flow<List<JobApplication>>
    fun getByStatusAsFlow(status: ApplicationStatus): Flow<List<JobApplication>>
    suspend fun getById(id: UUID): JobApplication?
    suspend fun save(job: JobApplication)
    suspend fun delete(job: JobApplication)
    suspend fun findDuplicate(companyName: String, roleTitle: String): JobApplication?
}
