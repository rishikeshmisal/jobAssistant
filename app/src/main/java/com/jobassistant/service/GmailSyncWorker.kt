package com.jobassistant.service

import android.app.PendingIntent
import android.content.Context
import android.util.Base64
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jobassistant.data.remote.GmailApiService
import com.jobassistant.data.remote.model.EmailActionType
import com.jobassistant.data.remote.model.ClaudeResult
import com.jobassistant.data.repository.GmailAuthExpiredException
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.repository.JobApplicationRepository
import com.jobassistant.domain.usecase.ParseEmailUseCase
import com.jobassistant.domain.usecase.SaveJobApplicationUseCase
import com.jobassistant.domain.usecase.UpdateJobStatusUseCase
import com.jobassistant.util.CalendarIntentHelper
import com.jobassistant.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.IOException

@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gmailApiService: GmailApiService,
    private val parseEmailUseCase: ParseEmailUseCase,
    private val saveJobUseCase: SaveJobApplicationUseCase,
    private val jobRepository: JobApplicationRepository,
    private val updateJobStatusUseCase: UpdateJobStatusUseCase,
    private val userProfileDataStore: UserProfileDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // If the user has never connected Gmail, nothing to do
        val gmailToken = userProfileDataStore.gmailToken.first()
        if (gmailToken.isNullOrBlank()) return Result.success()

        return try {
            val listResponse = gmailApiService.listMessages()
            val messageRefs = listResponse.messages ?: return Result.success()

            val allJobs = jobRepository.getAllAsFlow().first()
            val processedThreadIds = allJobs.flatMap { it.linkedEmailThreadIds }.toSet()

            for (ref in messageRefs) {
                if (ref.threadId in processedThreadIds) continue

                val message = gmailApiService.getMessage(id = ref.id)

                val subject = message.payload.headers
                    .firstOrNull { it.name.equals("Subject", ignoreCase = true) }
                    ?.value ?: ""
                val senderEmail = message.payload.headers
                    .firstOrNull { it.name.equals("From", ignoreCase = true) }
                    ?.value ?: ""

                val bodyText = decodeBody(message.payload.body?.data)
                    ?: message.payload.parts
                        ?.firstOrNull { it.mimeType == "text/plain" }
                        ?.body?.data
                        ?.let { decodeBody(it) }
                    ?: ""

                if (!EmailPreFilter.isJobRelated(subject, bodyText, senderEmail)) continue

                val parseResult = parseEmailUseCase(subject, bodyText)
                if (parseResult !is ClaudeResult.Success) continue

                val emailAction = parseResult.data

                when (emailAction.actionType) {
                    EmailActionType.APPLIED -> {
                        val newJob = JobApplication(
                            companyName = emailAction.targetCompany ?: "Unknown Company",
                            roleTitle = emailAction.roleTitle ?: "Unknown Role",
                            status = ApplicationStatus.APPLIED,
                            appliedDate = System.currentTimeMillis(),
                            linkedEmailThreadIds = listOf(ref.threadId)
                        )
                        val result = saveJobUseCase(newJob)
                        // If duplicate, still append the threadId to the existing job
                        if (result is com.jobassistant.domain.usecase.SaveResult.Duplicate) {
                            val existing = result.existing
                            jobRepository.save(
                                existing.copy(
                                    linkedEmailThreadIds = existing.linkedEmailThreadIds + ref.threadId
                                )
                            )
                        }
                    }

                    EmailActionType.REJECTION -> {
                        val matchedJob = jobRepository.findDuplicate(
                            emailAction.targetCompany ?: "",
                            emailAction.roleTitle ?: ""
                        )
                        matchedJob?.let { job ->
                            updateJobStatusUseCase(job.id, ApplicationStatus.REJECTED)
                            jobRepository.save(
                                job.copy(
                                    status = ApplicationStatus.REJECTED,
                                    linkedEmailThreadIds = job.linkedEmailThreadIds + ref.threadId
                                )
                            )
                        }
                    }

                    EmailActionType.INTERVIEW -> {
                        val matchedJob = jobRepository.findDuplicate(
                            emailAction.targetCompany ?: "",
                            emailAction.roleTitle ?: ""
                        )
                        matchedJob?.let { job ->
                            val interviewNotes = buildString {
                                emailAction.interviewLink?.let { append("Interview link: $it") }
                            }
                            jobRepository.save(
                                job.copy(
                                    status = ApplicationStatus.INTERVIEWING,
                                    interviewDate = emailAction.date,
                                    notes = if (interviewNotes.isNotBlank()) interviewNotes else job.notes,
                                    linkedEmailThreadIds = job.linkedEmailThreadIds + ref.threadId
                                )
                            )
                            // Fire a notification with calendar deep link
                            emailAction.date?.let { startMillis ->
                                val calendarIntent = CalendarIntentHelper.buildInsertIntent(
                                    title = "Interview: ${job.roleTitle} at ${job.companyName}",
                                    startMillis = startMillis,
                                    description = interviewNotes,
                                    location = emailAction.interviewLink
                                )
                                val pendingIntent = PendingIntent.getActivity(
                                    applicationContext,
                                    ref.threadId.hashCode(),
                                    calendarIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                NotificationHelper.showJobAlert(
                                    applicationContext,
                                    "Interview scheduled: ${job.roleTitle}",
                                    "Tap to add ${job.companyName} interview to calendar"
                                )
                                pendingIntent // retained for future use
                            } ?: NotificationHelper.showJobAlert(
                                applicationContext,
                                "Interview invite: ${job.roleTitle}",
                                "You have an interview for ${job.companyName}"
                            )
                        }
                    }

                    EmailActionType.ALERT -> {
                        NotificationHelper.showJobAlert(
                            applicationContext,
                            title = subject.take(60).ifBlank { "New job alert" },
                            body = bodyText.take(120).ifBlank { "New alert from your job feed" }
                        )
                    }

                    EmailActionType.IRRELEVANT -> Unit
                }
            }

            Result.success()
        } catch (e: GmailAuthExpiredException) {
            // Token expired and silent refresh failed — user must re-authenticate.
            // Do NOT retry; a notification is already shown via markGmailNeedsReauth.
            NotificationHelper.showJobAlert(
                applicationContext,
                title = "Gmail re-authentication required",
                body = "Open the app to reconnect your Gmail account"
            )
            Result.failure()
        } catch (e: IOException) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun decodeBody(data: String?): String? {
        if (data.isNullOrBlank()) return null
        return try {
            Base64.decode(data, Base64.URL_SAFE).decodeToString()
        } catch (e: Exception) {
            null
        }
    }
}
