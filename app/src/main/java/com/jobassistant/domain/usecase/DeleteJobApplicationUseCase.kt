package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import javax.inject.Inject

class DeleteJobApplicationUseCase @Inject constructor(
    private val repository: JobApplicationRepository
) {
    suspend operator fun invoke(job: JobApplication) = repository.delete(job)
}
