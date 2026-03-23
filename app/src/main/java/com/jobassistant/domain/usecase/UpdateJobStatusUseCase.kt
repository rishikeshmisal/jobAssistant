package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.repository.JobApplicationRepository
import java.util.UUID
import javax.inject.Inject

class UpdateJobStatusUseCase @Inject constructor(
    private val repository: JobApplicationRepository
) {
    suspend operator fun invoke(id: UUID, status: ApplicationStatus) {
        val existing = repository.getById(id) ?: return
        repository.save(existing.copy(status = status))
    }
}
