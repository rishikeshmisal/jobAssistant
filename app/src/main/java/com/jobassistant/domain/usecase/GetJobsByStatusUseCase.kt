package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJobsByStatusUseCase @Inject constructor(
    private val repository: JobApplicationRepository
) {
    operator fun invoke(status: ApplicationStatus): Flow<List<JobApplication>> =
        repository.getByStatusAsFlow(status)
}
