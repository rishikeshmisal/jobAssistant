package com.jobassistant.domain.usecase

import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.EmailActionType
import com.jobassistant.data.repository.ClaudeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ParseEmailUseCaseTest {

    private lateinit var claudeRepository: ClaudeRepository
    private lateinit var useCase: ParseEmailUseCase

    private val fakeEmailAction = EmailAction(
        actionType = EmailActionType.INTERVIEW,
        targetCompany = "Acme Corp",
        roleTitle = "Android Engineer"
    )

    @Before
    fun setUp() {
        claudeRepository = mockk()
        useCase = ParseEmailUseCase(claudeRepository)
    }

    @Test
    fun invoke_callsRepositoryWithSubjectAndBody() = runTest {
        coEvery { claudeRepository.parseEmail(any(), any()) } returns fakeEmailAction

        useCase("Interview invite", "Please join us for an interview")

        coVerify(exactly = 1) { claudeRepository.parseEmail("Interview invite", "Please join us for an interview") }
    }

    @Test
    fun invoke_returnsSuccessWithEmailAction() = runTest {
        coEvery { claudeRepository.parseEmail(any(), any()) } returns fakeEmailAction

        val result = useCase("Subject", "Body")

        assertTrue(result is ClaudeResult.Success)
        assertEquals(fakeEmailAction, (result as ClaudeResult.Success).data)
    }

    @Test
    fun invoke_repositoryThrows_returnsError() = runTest {
        coEvery { claudeRepository.parseEmail(any(), any()) } throws
            com.jobassistant.data.remote.model.ClaudeParseException("Parse failed")

        val result = useCase("Subject", "Body")

        assertTrue(result is ClaudeResult.Error)
    }

    @Test
    fun invoke_actionTypeCorrectlyReturned() = runTest {
        coEvery { claudeRepository.parseEmail(any(), any()) } returns fakeEmailAction

        val result = useCase("subject", "body")

        val action = (result as ClaudeResult.Success).data
        assertEquals(EmailActionType.INTERVIEW, action.actionType)
    }
}
