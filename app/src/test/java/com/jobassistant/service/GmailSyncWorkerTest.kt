package com.jobassistant.service

import android.content.Context
import android.util.Base64 as AndroidBase64
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.jobassistant.data.remote.GmailApiService
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.EmailActionType
import com.jobassistant.data.remote.model.GmailBody
import com.jobassistant.data.remote.model.GmailHeader
import com.jobassistant.data.remote.model.GmailListResponse
import com.jobassistant.data.remote.model.GmailMessage
import com.jobassistant.data.remote.model.GmailMessageRef
import com.jobassistant.data.remote.model.GmailPart
import com.jobassistant.data.remote.model.GmailPayload
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.ParseEmailUseCase
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.domain.usecase.SaveResult
import com.jobassistant.domain.usecase.UpdateJobStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Base64
import java.util.UUID

/**
 * Pure-JVM unit tests for [GmailSyncWorker.doWork].
 *
 * Uses [mockkStatic] to substitute [android.util.Base64] (a stub in JVM tests)
 * with the JVM [java.util.Base64] decoder so that email body content is decoded
 * correctly and the [EmailPreFilter] matches body-only patterns as expected.
 */
class GmailSyncWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)

    private lateinit var gmailApiService: GmailApiService
    private lateinit var parseEmailUseCase: ParseEmailUseCase
    private lateinit var saveJobUseCase: SaveJobApplicationUseCase
    private lateinit var jobRepository: JobApplicationRepository
    private lateinit var updateJobStatusUseCase: UpdateJobStatusUseCase
    private lateinit var userProfileDataStore: UserProfileDataStore

    private lateinit var worker: GmailSyncWorker

    @Before
    fun setUp() {
        // Replace android.util.Base64 stubs with real JVM Base64 decoding
        mockkStatic(AndroidBase64::class)
        every { AndroidBase64.decode(any<String>(), any<Int>()) } answers {
            val data = firstArg<String>()
            try {
                Base64.getUrlDecoder().decode(
                    data.replace('-', '+').replace('_', '/')
                        .let { s -> s + "=".repeat((4 - s.length % 4) % 4) }
                )
            } catch (e: Exception) {
                ByteArray(0)
            }
        }

        gmailApiService = mockk()
        parseEmailUseCase = mockk()
        saveJobUseCase = mockk()
        jobRepository = mockk()
        updateJobStatusUseCase = mockk()
        userProfileDataStore = mockk()

        worker = GmailSyncWorker(
            context = context,
            workerParams = workerParams,
            gmailApiService = gmailApiService,
            parseEmailUseCase = parseEmailUseCase,
            saveJobUseCase = saveJobUseCase,
            jobRepository = jobRepository,
            updateJobStatusUseCase = updateJobStatusUseCase,
            userProfileDataStore = userProfileDataStore
        )

        every { jobRepository.getAllAsFlow() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── No token ─────────────────────────────────────────────────────────────

    @Test
    fun `doWork returns success immediately when no Gmail token`() = runTest {
        every { userProfileDataStore.gmailToken } returns flowOf(null)
        assertEquals(Result.success(), worker.doWork())
    }

    @Test
    fun `doWork returns success when token is blank`() = runTest {
        every { userProfileDataStore.gmailToken } returns flowOf("   ")
        assertEquals(Result.success(), worker.doWork())
    }

    // ── Empty message list ────────────────────────────────────────────────────

    @Test
    fun `doWork returns success when messages list is null`() = runTest {
        setupToken()
        coEvery { gmailApiService.listMessages(any(), any()) } returns
            GmailListResponse(messages = null)

        assertEquals(Result.success(), worker.doWork())
        coVerify(exactly = 0) { parseEmailUseCase(any(), any()) }
    }

    @Test
    fun `doWork returns success when messages list is empty`() = runTest {
        setupToken()
        coEvery { gmailApiService.listMessages(any(), any()) } returns
            GmailListResponse(messages = emptyList())

        assertEquals(Result.success(), worker.doWork())
        coVerify(exactly = 0) { parseEmailUseCase(any(), any()) }
    }

    // ── APPLIED ───────────────────────────────────────────────────────────────

    @Test
    fun `doWork APPLIED - creates new job application`() = runTest {
        setupToken()
        setupSingleMessage(
            subject = "Application received - Android Engineer",
            bodyData = encode("We received your application for the role."),
            threadId = "thread-1"
        )
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(
                actionType = EmailActionType.APPLIED,
                targetCompany = "Acme Corp",
                roleTitle = "Android Engineer"
            )
        )
        val savedSlot = slot<JobApplication>()
        coEvery { saveJobUseCase(capture(savedSlot)) } returns SaveResult.Saved

        worker.doWork()

        assertEquals("Acme Corp", savedSlot.captured.companyName)
        assertEquals("Android Engineer", savedSlot.captured.roleTitle)
        assertEquals(ApplicationStatus.APPLIED, savedSlot.captured.status)
        assertEquals(listOf("thread-1"), savedSlot.captured.linkedEmailThreadIds)
    }

    @Test
    fun `doWork APPLIED - appends threadId to duplicate job`() = runTest {
        setupToken()
        val existing = JobApplication(
            id = UUID.randomUUID(),
            companyName = "Corp",
            roleTitle = "Dev",
            linkedEmailThreadIds = listOf("old-thread")
        )
        setupSingleMessage(
            subject = "We received your application for Dev",
            bodyData = encode("Thank you."),
            threadId = "new-thread"
        )
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(actionType = EmailActionType.APPLIED, targetCompany = "Corp", roleTitle = "Dev")
        )
        coEvery { saveJobUseCase(any()) } returns SaveResult.Duplicate(existing)
        val savedSlot = slot<JobApplication>()
        coEvery { jobRepository.save(capture(savedSlot)) } returns Unit

        worker.doWork()

        assertEquals(listOf("old-thread", "new-thread"), savedSlot.captured.linkedEmailThreadIds)
    }

    // ── REJECTION ─────────────────────────────────────────────────────────────

    @Test
    fun `doWork REJECTION - updates existing job status to REJECTED`() = runTest {
        setupToken()
        val existingJob = JobApplication(
            id = UUID.randomUUID(),
            companyName = "Acme Corp",
            roleTitle = "Android Engineer",
            status = ApplicationStatus.APPLIED
        )
        every { jobRepository.getAllAsFlow() } returns flowOf(listOf(existingJob))
        coEvery { jobRepository.findDuplicate("Acme Corp", "Android Engineer") } returns existingJob
        coEvery { updateJobStatusUseCase(existingJob.id, ApplicationStatus.REJECTED) } returns Unit
        coEvery { jobRepository.save(any()) } returns Unit

        setupSingleMessage(
            subject = "Application Update",
            bodyData = encode("We are not moving forward with your candidacy at this time."),
            threadId = "thread-2"
        )
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(
                actionType = EmailActionType.REJECTION,
                targetCompany = "Acme Corp",
                roleTitle = "Android Engineer"
            )
        )

        worker.doWork()

        coVerify { updateJobStatusUseCase(existingJob.id, ApplicationStatus.REJECTED) }
    }

    @Test
    fun `doWork REJECTION - no-ops when no matching job found`() = runTest {
        setupToken()
        coEvery { jobRepository.findDuplicate(any(), any()) } returns null

        setupSingleMessage(
            subject = "Update on your application",
            bodyData = encode("Unfortunately, we will not be selecting you."),
            threadId = "thread-3"
        )
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(actionType = EmailActionType.REJECTION, targetCompany = "Unknown", roleTitle = "Dev")
        )

        worker.doWork()

        coVerify(exactly = 0) { updateJobStatusUseCase(any(), any()) }
    }

    // ── INTERVIEW ─────────────────────────────────────────────────────────────

    @Test
    fun `doWork INTERVIEW - updates job to SCREENING`() = runTest {
        setupToken()
        val existingJob = JobApplication(
            id = UUID.randomUUID(),
            companyName = "Tech Inc",
            roleTitle = "Senior Engineer",
            status = ApplicationStatus.APPLIED
        )
        every { jobRepository.getAllAsFlow() } returns flowOf(listOf(existingJob))
        coEvery { jobRepository.findDuplicate("Tech Inc", "Senior Engineer") } returns existingJob
        coEvery { jobRepository.save(any()) } returns Unit

        setupSingleMessage(
            subject = "Interview Invitation - Senior Engineer",
            bodyData = encode("We would like to schedule an interview."),
            threadId = "thread-4"
        )
        // date = null so PendingIntent.getActivity() is not triggered in JVM context
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(
                actionType = EmailActionType.INTERVIEW,
                targetCompany = "Tech Inc",
                roleTitle = "Senior Engineer",
                date = null,
                interviewLink = null
            )
        )

        worker.doWork()

        val savedSlot = slot<JobApplication>()
        coVerify { jobRepository.save(capture(savedSlot)) }
        assertEquals(ApplicationStatus.SCREENING, savedSlot.captured.status)
    }

    // ── ALERT ─────────────────────────────────────────────────────────────────

    @Test
    fun `doWork ALERT - does not create or modify any job`() = runTest {
        setupToken()
        // linkedin.com sender → EmailPreFilter classifies as ALERT → isJobRelated = true
        setupSingleMessage(
            subject = "New jobs for you",
            bodyData = encode("5 jobs match your search."),
            threadId = "thread-5",
            senderEmail = "jobs@linkedin.com"
        )
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(actionType = EmailActionType.ALERT)
        )

        worker.doWork()

        coVerify(exactly = 0) { saveJobUseCase(any()) }
        coVerify(exactly = 0) { jobRepository.save(any()) }
    }

    // ── IRRELEVANT ────────────────────────────────────────────────────────────

    @Test
    fun `doWork skips email classified as IRRELEVANT by pre-filter`() = runTest {
        setupToken()
        // Non-job sender, no job keywords → IRRELEVANT
        setupSingleMessage(
            subject = "Flash sale 50% off today",
            bodyData = encode("Click here to unsubscribe from our mailing list."),
            threadId = "thread-6",
            senderEmail = "promo@shop.com"
        )

        worker.doWork()

        coVerify(exactly = 0) { parseEmailUseCase(any(), any()) }
    }

    // ── Thread deduplication ─────────────────────────────────────────────────

    @Test
    fun `doWork skips already-processed thread IDs`() = runTest {
        setupToken()
        val existingJob = JobApplication(
            companyName = "Corp",
            roleTitle = "Dev",
            linkedEmailThreadIds = listOf("already-seen")
        )
        every { jobRepository.getAllAsFlow() } returns flowOf(listOf(existingJob))

        setupSingleMessage(
            subject = "Application received",
            bodyData = encode("Thank you for applying."),
            threadId = "already-seen"
        )

        worker.doWork()

        coVerify(exactly = 0) { parseEmailUseCase(any(), any()) }
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `doWork returns retry on IOException when attempt count less than 3`() = runTest {
        setupToken()
        every { workerParams.runAttemptCount } returns 0
        coEvery { gmailApiService.listMessages(any(), any()) } throws IOException("network error")

        assertEquals(Result.retry(), worker.doWork())
    }

    @Test
    fun `doWork returns failure on IOException after 3 attempts`() = runTest {
        setupToken()
        every { workerParams.runAttemptCount } returns 3
        coEvery { gmailApiService.listMessages(any(), any()) } throws IOException("network error")

        assertEquals(Result.failure(), worker.doWork())
    }

    @Test
    fun `doWork returns failure on generic exception`() = runTest {
        setupToken()
        coEvery { gmailApiService.listMessages(any(), any()) } throws RuntimeException("unexpected")

        assertEquals(Result.failure(), worker.doWork())
    }

    // ── Body parsing ──────────────────────────────────────────────────────────

    @Test
    fun `doWork decodes body from text plain part when body data is null`() = runTest {
        setupToken()
        val messageId = "msg-parts"
        val threadId = "thread-parts"
        coEvery { gmailApiService.listMessages(any(), any()) } returns
            GmailListResponse(messages = listOf(GmailMessageRef(id = messageId, threadId = threadId)))

        coEvery { gmailApiService.getMessage(messageId, any()) } returns GmailMessage(
            id = messageId,
            threadId = threadId,
            payload = GmailPayload(
                headers = listOf(
                    GmailHeader("Subject", "Job application"),
                    GmailHeader("From", "hr@company.com")
                ),
                body = GmailBody(data = null),
                parts = listOf(
                    GmailPart(
                        mimeType = "text/plain",
                        body = GmailBody(data = encode("We received your application for Kotlin Dev."))
                    )
                )
            )
        )
        val savedSlot = slot<JobApplication>()
        coEvery { parseEmailUseCase(any(), any()) } returns ClaudeResult.Success(
            EmailAction(
                actionType = EmailActionType.APPLIED,
                targetCompany = "Corp",
                roleTitle = "Kotlin Dev"
            )
        )
        coEvery { saveJobUseCase(capture(savedSlot)) } returns SaveResult.Saved

        worker.doWork()

        assertEquals("Kotlin Dev", savedSlot.captured.roleTitle)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun setupToken() {
        every { userProfileDataStore.gmailToken } returns flowOf("test-token")
    }

    private fun setupSingleMessage(
        subject: String,
        bodyData: String,
        threadId: String,
        senderEmail: String = "hr@company.com"
    ) {
        val messageId = "msg-$threadId"
        coEvery { gmailApiService.listMessages(any(), any()) } returns
            GmailListResponse(messages = listOf(GmailMessageRef(id = messageId, threadId = threadId)))

        coEvery { gmailApiService.getMessage(messageId, any()) } returns GmailMessage(
            id = messageId,
            threadId = threadId,
            payload = GmailPayload(
                headers = listOf(
                    GmailHeader("Subject", subject),
                    GmailHeader("From", senderEmail)
                ),
                body = GmailBody(data = bodyData),
                parts = null
            )
        )
    }

    /** Encodes [text] as base64url without padding (as Gmail API returns). */
    private fun encode(text: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(text.toByteArray())
}
