package com.jobassistant.service

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.repository.JobApplicationRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * End-to-end instrumented test for [GmailSyncWorker].
 *
 * Uses [WorkManagerTestInitHelper] with [HiltWorkerFactory] so the worker can
 * receive all its @AssistedInject + @Inject dependencies from the Hilt graph.
 *
 * [TestNetworkModule] (in androidTest/di/) replaces [NetworkModule] with fakes:
 * - [FakeGmailApiService]  → returns a single APPLIED email for "Test Corp / QA Engineer"
 * - [FakeClaudeRepository] → returns a canned parse_email response (via TestAiModule)
 *
 * [TestDatabaseModule] (already present) replaces [DatabaseModule] with an in-memory Room DB.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GmailSyncWorkerIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var jobRepository: JobApplicationRepository
    @Inject lateinit var userProfileDataStore: UserProfileDataStore

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        hiltRule.inject()

        // Seed Gmail token so GmailSyncWorker doesn't short-circuit
        runBlocking {
            userProfileDataStore.saveGmailCredentials(
                token = "integration-test-token",
                email = "tester@gmail.com"
            )
        }

        // Initialize WorkManager with HiltWorkerFactory for @HiltWorker support
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun gmailSyncWorker_appliedEmail_insertsJobApplicationInDb() {
        val request = OneTimeWorkRequestBuilder<GmailSyncWorker>().build()
        val workManager = WorkManager.getInstance(context)

        // Enqueue and wait for the enqueue operation itself to complete
        workManager.enqueue(request).result.get(30, TimeUnit.SECONDS)

        // Poll until the worker reaches a terminal state (SUCCEEDED/FAILED/CANCELLED)
        val terminalStates = setOf(
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED
        )
        val deadlineMs = System.currentTimeMillis() + 60_000L
        var workInfo = workManager.getWorkInfoById(request.id).get()
        while (workInfo.state !in terminalStates && System.currentTimeMillis() < deadlineMs) {
            Thread.sleep(200)
            workInfo = workManager.getWorkInfoById(request.id).get()
        }

        assertEquals(
            "Worker should succeed (was ${workInfo.state})",
            WorkInfo.State.SUCCEEDED,
            workInfo.state
        )

        val allJobs = runBlocking { jobRepository.getAllAsFlow().first() }
        assertTrue(
            "Expected at least one JobApplication after APPLIED email sync",
            allJobs.isNotEmpty()
        )

        val inserted = allJobs.firstOrNull { it.companyName == "Test Corp" }
        assertTrue("Expected a job for 'Test Corp' to be inserted", inserted != null)
        assertEquals("QA Engineer", inserted!!.roleTitle)
        assertTrue(
            "Expected thread-id to be linked",
            inserted.linkedEmailThreadIds.contains("test-thread-1")
        )
    }
}
