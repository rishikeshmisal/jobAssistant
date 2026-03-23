package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.safeClaudeCall
import com.jobassistant.data.repository.ClaudeRepository
import javax.inject.Inject

class ParseEmailUseCase @Inject constructor(
    private val claudeRepository: ClaudeRepository
) {
    suspend operator fun invoke(
        subject: String,
        body: String
    ): ClaudeResult<EmailAction> = safeClaudeCall {
        claudeRepository.parseEmail(subject, body)
    }
}
