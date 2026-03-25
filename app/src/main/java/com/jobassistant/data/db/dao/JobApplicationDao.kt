package com.jobassistant.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.jobassistant.data.db.entity.JobApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobApplicationDao {

    @Query("SELECT * FROM job_applications ORDER BY appliedDate DESC")
    fun getAllAsFlow(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE status = :status")
    fun getByStatusAsFlow(status: String): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getById(id: String): JobApplicationEntity?

    @Query("SELECT * FROM job_applications WHERE companyName LIKE :company AND roleTitle LIKE :role LIMIT 1")
    suspend fun findDuplicate(company: String, role: String): JobApplicationEntity?

    @Upsert
    suspend fun upsert(entity: JobApplicationEntity)

    @Delete
    suspend fun delete(entity: JobApplicationEntity)

    @Query("DELETE FROM job_applications")
    suspend fun deleteAll()
}
