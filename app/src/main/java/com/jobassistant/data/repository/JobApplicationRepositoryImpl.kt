package com.jobassistant.data.repository

import com.jobassistant.data.db.dao.JobApplicationDao
import com.jobassistant.data.db.mapper.toDomain
import com.jobassistant.data.db.mapper.toEntity
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class JobApplicationRepositoryImpl @Inject constructor(
    private val dao: JobApplicationDao
) : JobApplicationRepository {

    override fun getAllAsFlow(): Flow<List<JobApplication>> =
        dao.getAllAsFlow().map { entities -> entities.map { it.toDomain() } }

    override fun getByStatusAsFlow(status: ApplicationStatus): Flow<List<JobApplication>> =
        dao.getByStatusAsFlow(status.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: UUID): JobApplication? =
        dao.getById(id.toString())?.toDomain()

    override suspend fun save(job: JobApplication) {
        val existing = dao.findDuplicate(job.companyName, job.roleTitle)
        if (existing != null) {
            // Update existing entry, preserving its id
            dao.upsert(job.copy(id = UUID.fromString(existing.id)).toEntity())
        } else {
            dao.upsert(job.toEntity())
        }
    }

    override suspend fun delete(job: JobApplication) =
        dao.delete(job.toEntity())

    override suspend fun findDuplicate(companyName: String, roleTitle: String): JobApplication? =
        dao.findDuplicate(companyName, roleTitle)?.toDomain()
}
