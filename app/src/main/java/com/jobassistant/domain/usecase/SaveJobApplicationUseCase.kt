package com.jobassistant.domain.usecase

import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import javax.inject.Inject

sealed class SaveResult {
    object Saved : SaveResult()
    data class Duplicate(val existing: JobApplication) : SaveResult()
}

class SaveJobApplicationUseCase @Inject constructor(
    private val repository: JobApplicationRepository
) {
    suspend operator fun invoke(job: JobApplication): SaveResult {
        val existing = repository.findDuplicate(job.companyName, job.roleTitle)
        if (existing != null) return SaveResult.Duplicate(existing)
        repository.save(job)
        return SaveResult.Saved
    }
}
