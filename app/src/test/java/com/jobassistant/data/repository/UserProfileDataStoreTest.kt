package com.jobassistant.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.jobassistant.domain.model.AppTheme
import com.jobassistant.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileDataStoreTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    // DataStore gets its own scope, separate from the runTest scope, so that
    // DataStore's internal coroutines don't appear as "uncompleted" in runTest.
    private val dataStoreDispatcher = UnconfinedTestDispatcher()
    private val dataStoreScope = CoroutineScope(dataStoreDispatcher + Job())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userProfileDataStore: UserProfileDataStore

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { tmpFolder.newFile("test_user_profile.preferences_pb") }
        )
        userProfileDataStore = UserProfileDataStore(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun `default profile has empty fields`() = runTest {
        val profile = userProfileDataStore.userProfileFlow.first()

        assertEquals("", profile.userId)
        assertEquals("", profile.fullName)
        assertEquals("", profile.resumeText)
        assertTrue(profile.keywords.isEmpty())
        assertEquals("", profile.careerGoal)
        assertEquals(0, profile.targetSalaryMin)
        assertEquals(0, profile.targetSalaryMax)
        assertEquals(AppTheme.GREEN, profile.selectedTheme)
        assertFalse(profile.isOnboardingComplete)
    }

    @Test
    fun `save and reload fullName`() = runTest {
        val profile = UserProfile(fullName = "John Doe")
        userProfileDataStore.save(profile)

        val loaded = userProfileDataStore.userProfileFlow.first()
        assertEquals("John Doe", loaded.fullName)
    }

    @Test
    fun `save and reload all fields`() = runTest {
        val profile = UserProfile(
            userId = "user123",
            fullName = "Jane Smith",
            resumeText = "10 years experience",
            keywords = listOf("Kotlin", "Android"),
            careerGoal = "Senior Engineer",
            targetSalaryMin = 100000,
            targetSalaryMax = 150000,
            selectedTheme = AppTheme.BLUE,
            isOnboardingComplete = true
        )
        userProfileDataStore.save(profile)

        val loaded = userProfileDataStore.userProfileFlow.first()
        assertEquals("user123", loaded.userId)
        assertEquals("Jane Smith", loaded.fullName)
        assertEquals("10 years experience", loaded.resumeText)
        assertEquals(listOf("Kotlin", "Android"), loaded.keywords)
        assertEquals("Senior Engineer", loaded.careerGoal)
        assertEquals(100000, loaded.targetSalaryMin)
        assertEquals(150000, loaded.targetSalaryMax)
        assertEquals(AppTheme.BLUE, loaded.selectedTheme)
        assertTrue(loaded.isOnboardingComplete)
    }

    @Test
    fun `update applies transform correctly`() = runTest {
        val initial = UserProfile(fullName = "Alice", selectedTheme = AppTheme.GREEN)
        userProfileDataStore.save(initial)

        userProfileDataStore.update { copy(selectedTheme = AppTheme.RED, isOnboardingComplete = true) }

        val result = userProfileDataStore.userProfileFlow.first()
        assertEquals("Alice", result.fullName)
        assertEquals(AppTheme.RED, result.selectedTheme)
        assertTrue(result.isOnboardingComplete)
    }

    @Test
    fun `save with empty keywords list roundtrips correctly`() = runTest {
        userProfileDataStore.save(UserProfile(keywords = emptyList()))

        val loaded = userProfileDataStore.userProfileFlow.first()
        assertTrue(loaded.keywords.isEmpty())
    }

    @Test
    fun `save isOnboardingComplete true persists`() = runTest {
        userProfileDataStore.save(UserProfile(isOnboardingComplete = true))

        val loaded = userProfileDataStore.userProfileFlow.first()
        assertTrue(loaded.isOnboardingComplete)
    }

    @Test
    fun `all AppTheme values save and reload correctly`() = runTest {
        for (theme in AppTheme.values()) {
            userProfileDataStore.save(UserProfile(selectedTheme = theme))
            val loaded = userProfileDataStore.userProfileFlow.first()
            assertEquals(theme, loaded.selectedTheme)
        }
    }

    // ── Phase 7: Gmail credential tests ─────────────────────────────────────

    @Test
    fun `gmailToken is null before saving credentials`() = runTest {
        val token = userProfileDataStore.gmailToken.first()
        assertEquals(null, token)
    }

    @Test
    fun `gmailEmail is null before saving credentials`() = runTest {
        val email = userProfileDataStore.gmailEmail.first()
        assertEquals(null, email)
    }

    @Test
    fun `saveGmailCredentials persists token and email`() = runTest {
        userProfileDataStore.saveGmailCredentials("id-token-abc", "user@gmail.com")

        assertEquals("id-token-abc", userProfileDataStore.gmailToken.first())
        assertEquals("user@gmail.com", userProfileDataStore.gmailEmail.first())
    }

    @Test
    fun `clearGmailCredentials removes token and email`() = runTest {
        userProfileDataStore.saveGmailCredentials("id-token-abc", "user@gmail.com")
        userProfileDataStore.clearGmailCredentials()

        assertEquals(null, userProfileDataStore.gmailToken.first())
        assertEquals(null, userProfileDataStore.gmailEmail.first())
    }

    @Test
    fun `gmailToken returns null for blank value`() = runTest {
        userProfileDataStore.saveGmailCredentials("   ", "user@gmail.com")

        assertEquals(null, userProfileDataStore.gmailToken.first())
    }

    @Test
    fun `saveGmailCredentials overwrites previous credentials`() = runTest {
        userProfileDataStore.saveGmailCredentials("token-1", "old@gmail.com")
        userProfileDataStore.saveGmailCredentials("token-2", "new@gmail.com")

        assertEquals("token-2", userProfileDataStore.gmailToken.first())
        assertEquals("new@gmail.com", userProfileDataStore.gmailEmail.first())
    }

    // ── Gmail access token (OAuth2 short-lived token) ────────────────────────

    @Test
    fun `gmailAccessToken is null before saving`() = runTest {
        assertEquals(null, userProfileDataStore.gmailAccessToken.first())
    }

    @Test
    fun `gmailTokenExpiry is null before saving`() = runTest {
        assertEquals(null, userProfileDataStore.gmailTokenExpiry.first())
    }

    @Test
    fun `saveGmailAccessToken persists token and expiry`() = runTest {
        userProfileDataStore.saveGmailAccessToken("access-token-xyz", 1_700_000_000_000L)

        assertEquals("access-token-xyz", userProfileDataStore.gmailAccessToken.first())
        assertEquals(1_700_000_000_000L, userProfileDataStore.gmailTokenExpiry.first())
    }

    @Test
    fun `saveGmailAccessToken clears needsReauth flag`() = runTest {
        userProfileDataStore.markGmailNeedsReauth()
        userProfileDataStore.saveGmailAccessToken("token", 12345L)

        assertFalse(userProfileDataStore.gmailNeedsReauth.first())
    }

    // ── gmailNeedsReauth flag ────────────────────────────────────────────────

    @Test
    fun `gmailNeedsReauth is false by default`() = runTest {
        assertFalse(userProfileDataStore.gmailNeedsReauth.first())
    }

    @Test
    fun `markGmailNeedsReauth sets flag to true`() = runTest {
        userProfileDataStore.markGmailNeedsReauth()

        assertTrue(userProfileDataStore.gmailNeedsReauth.first())
    }

    @Test
    fun `markGmailNeedsReauth removes access token`() = runTest {
        userProfileDataStore.saveGmailAccessToken("token", 12345L)
        userProfileDataStore.markGmailNeedsReauth()

        assertEquals(null, userProfileDataStore.gmailAccessToken.first())
    }

    @Test
    fun `clearGmailNeedsReauth resets flag to false`() = runTest {
        userProfileDataStore.markGmailNeedsReauth()
        userProfileDataStore.clearGmailNeedsReauth()

        assertFalse(userProfileDataStore.gmailNeedsReauth.first())
    }

    @Test
    fun `clearGmailCredentials also removes access token and expiry`() = runTest {
        userProfileDataStore.saveGmailAccessToken("access-token", 99999L)
        userProfileDataStore.clearGmailCredentials()

        assertEquals(null, userProfileDataStore.gmailAccessToken.first())
        assertEquals(null, userProfileDataStore.gmailTokenExpiry.first())
    }

    @Test
    fun `clearGmailCredentials clears reauth flag`() = runTest {
        userProfileDataStore.markGmailNeedsReauth()
        userProfileDataStore.clearGmailCredentials()

        assertFalse(userProfileDataStore.gmailNeedsReauth.first())
    }

    // ── BYOK API key ─────────────────────────────────────────────────────────

    @Test
    fun `userApiKey is null before saving`() = runTest {
        assertEquals(null, userProfileDataStore.userApiKey.first())
    }

    @Test
    fun `saveUserApiKey persists non-blank key`() = runTest {
        userProfileDataStore.saveUserApiKey("my-gemini-key")

        assertEquals("my-gemini-key", userProfileDataStore.userApiKey.first())
    }

    @Test
    fun `saveUserApiKey with blank string removes key`() = runTest {
        userProfileDataStore.saveUserApiKey("my-gemini-key")
        userProfileDataStore.saveUserApiKey("")

        assertEquals(null, userProfileDataStore.userApiKey.first())
    }

    @Test
    fun `saveUserApiKey overwrites previous key`() = runTest {
        userProfileDataStore.saveUserApiKey("key-1")
        userProfileDataStore.saveUserApiKey("key-2")

        assertEquals("key-2", userProfileDataStore.userApiKey.first())
    }
}
