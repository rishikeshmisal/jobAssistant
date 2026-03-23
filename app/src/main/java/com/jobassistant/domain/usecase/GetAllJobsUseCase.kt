package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllJobsUseCase @Inject constructor(
    private val repository: JobApplicationRepository
) {
    operator fun invoke(): Flow<List<JobApplication>> = repository.getAllAsFlow()
}
