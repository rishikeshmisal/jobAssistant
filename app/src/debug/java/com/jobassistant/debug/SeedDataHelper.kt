package com.jobassistant.debug

import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.dao.JobApplicationDao
import com.jobassistant.data.db.entity.CareerInsightsEntity
import com.jobassistant.data.db.mapper.toEntity
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.model.AppTheme
import com.jobassistant.domain.model.ApplicationStatus
import com.jobassistant.domain.model.JobApplication
import com.jobassistant.domain.model.UserProfile
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SeedDataHelper @Inject constructor(
    private val jobApplicationDao: JobApplicationDao,
    private val careerInsightsDao: CareerInsightsDao,
    private val userProfileDataStore: UserProfileDataStore
) {

    private fun daysAgo(days: Long): Long =
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days)

    private fun daysFromNow(days: Long): Long =
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days)

    suspend fun seed(): Int {
        // 1. Clear existing data
        jobApplicationDao.deleteAll()
        careerInsightsDao.deleteAll()

        // 2. Write synthetic user profile
        userProfileDataStore.save(syntheticProfile())

        // 3. Insert all synthetic jobs
        val jobs = syntheticJobs()
        jobs.forEach { jobApplicationDao.upsert(it.toEntity()) }

        // 4. Insert synthetic career insights
        careerInsightsDao.upsert(syntheticInsights())

        return jobs.size
    }

    // ── User Profile ──────────────────────────────────────────────────────────

    private fun syntheticProfile() = UserProfile(
        userId = "demo-user-001",
        fullName = "Alex Johnson",
        resumeText = RESUME_TEXT,
        keywords = listOf(
            "Android", "Kotlin", "Jetpack Compose", "Hilt", "Room",
            "Coroutines", "Flow", "REST APIs", "CI/CD", "Agile"
        ),
        careerGoal = "Experienced Android engineer with 5 years building consumer-facing " +
                "apps at scale. Strong foundation in Kotlin, Compose, and clean architecture. " +
                "Targeting senior or lead Android roles at product companies where I can own " +
                "features end-to-end and mentor junior engineers. Open to fintech, health, " +
                "or productivity verticals where mobile is the primary product surface.",
        targetSalaryMin = 80000,
        targetSalaryMax = 120000,
        selectedTheme = AppTheme.GREEN,
        isOnboardingComplete = true
    )

    // ── Synthetic Jobs — 32 across all 10 statuses ───────────────────────────

    private fun syntheticJobs(): List<JobApplication> = buildList {

        // INTERESTED (4) — saved but not applied
        add(job("Monzo", "Senior Android Engineer", ApplicationStatus.INTERESTED, null, 5))
        add(job("Revolut", "Android Developer", ApplicationStatus.INTERESTED, null, 3))
        add(job("Starling Bank", "Lead Android Engineer", ApplicationStatus.INTERESTED, null, 1))
        add(job("Wise", "Mobile Engineer (Android)", ApplicationStatus.INTERESTED, null, 0))

        // APPLIED (7)
        add(job("Google", "Android Developer Advocate", ApplicationStatus.APPLIED, 82, 45,
            loc = "London", salary = "£95,000 – £120,000",
            desc = "Drive developer relations for the Android platform. 5+ years Kotlin required."))
        add(job("Meta", "Software Engineer — Android", ApplicationStatus.APPLIED, 74, 40,
            loc = "London", salary = "£90,000 – £115,000"))
        add(job("Spotify", "Android Engineer", ApplicationStatus.APPLIED, 88, 35,
            loc = "Stockholm / Remote", salary = "€85,000 – €110,000",
            desc = "Build the next generation of the Spotify Android client. Compose expertise essential."))
        add(job("Deliveroo", "Senior Android Developer", ApplicationStatus.APPLIED, 65, 30,
            loc = "London", salary = "£80,000 – £100,000"))
        add(job("Babylon Health", "Android Engineer", ApplicationStatus.APPLIED, 71, 25,
            loc = "Remote", salary = "£75,000 – £95,000"))
        add(job("Sky", "Senior Mobile Engineer", ApplicationStatus.APPLIED, null, 20,
            loc = "London"))
        add(job("BBC", "Android Developer", ApplicationStatus.APPLIED, null, 15,
            loc = "London / Hybrid"))

        // SCREENING (3)
        add(job("Booking.com", "Android Engineer", ApplicationStatus.SCREENING, 79, 28,
            loc = "Amsterdam"))
        add(job("Klarna", "Senior Android Developer", ApplicationStatus.SCREENING, 85, 22,
            loc = "Stockholm / Remote", salary = "€90,000 – €115,000"))
        add(job("Monzo", "Android Platform Engineer", ApplicationStatus.SCREENING, 77, 10,
            loc = "London", salary = "£85,000 – £110,000"))

        // INTERVIEWING (4)
        add(job("Spotify", "Android Engineer", ApplicationStatus.INTERVIEWING, 88, 35,
            interviewDaysFromNow = 3))
        add(job("Airbnb", "Senior Android Engineer", ApplicationStatus.INTERVIEWING, 91, 30,
            loc = "Remote", salary = "£100,000 – £130,000", interviewDaysFromNow = 7))
        add(job("Shopify", "Android Developer", ApplicationStatus.INTERVIEWING, 76, 18,
            loc = "Remote"))
        add(job("Citadel", "Mobile Software Engineer", ApplicationStatus.INTERVIEWING, 69, 14,
            loc = "London", salary = "£95,000 – £140,000"))

        // ASSESSMENT (2)
        add(job("Wise", "Android Engineer", ApplicationStatus.ASSESSMENT, 83, 7,
            loc = "London / Remote",
            desc = "Take-home: build a currency converter with Compose, Hilt, and offline support."))
        add(job("N26", "Senior Android Developer", ApplicationStatus.ASSESSMENT, 78, 4,
            loc = "Berlin / Remote"))

        // OFFER (2)
        add(job("Klarna", "Senior Android Developer", ApplicationStatus.OFFER, 85, 2,
            loc = "Stockholm / Remote", salary = "€90,000 – €115,000"))
        add(job("Monzo", "Android Platform Engineer", ApplicationStatus.OFFER, 77, 1,
            loc = "London", salary = "£85,000 – £110,000"))

        // ACCEPTED (1)
        add(job("Klarna", "Senior Android Developer — ACCEPTED", ApplicationStatus.ACCEPTED, 85, 0,
            loc = "Stockholm / Remote", salary = "€90,000 – €115,000"))

        // REJECTED (5)
        add(job("Apple", "iOS/Android Engineer", ApplicationStatus.REJECTED, 58, 60))
        add(job("Amazon", "SDE II — Android", ApplicationStatus.REJECTED, 61, 50,
            loc = "London"))
        add(job("Goldman Sachs", "Mobile Engineer", ApplicationStatus.REJECTED, 54, 42,
            loc = "London"))
        add(job("Palantir", "Software Engineer", ApplicationStatus.REJECTED, 63, 32,
            loc = "London"))
        add(job("Stripe", "Android Developer", ApplicationStatus.REJECTED, 70, 18,
            loc = "Remote", salary = "£90,000 – £115,000"))

        // WITHDRAWN (2)
        add(job("TikTok", "Senior Android Engineer", ApplicationStatus.WITHDRAWN, 72, 38,
            loc = "London"))
        add(job("Bytedance", "Android Developer", ApplicationStatus.WITHDRAWN, 68, 25,
            loc = "London"))

        // NO_RESPONSE (3)
        add(job("Uber", "Android Engineer", ApplicationStatus.NO_RESPONSE, 75, 55,
            loc = "Amsterdam"))
        add(job("Lyft", "Senior Android Developer", ApplicationStatus.NO_RESPONSE, 67, 48,
            loc = "Remote"))
        add(job("Twitter/X", "Mobile Engineer", ApplicationStatus.NO_RESPONSE, 60, 44,
            loc = "Remote"))
    }

    private fun job(
        company: String,
        role: String,
        status: ApplicationStatus,
        fitScore: Int?,
        appliedDaysAgo: Long,
        loc: String? = null,
        salary: String? = null,
        desc: String = "",
        interviewDaysFromNow: Long? = null
    ) = JobApplication(
        id = UUID.randomUUID(),
        companyName = company,
        roleTitle = role,
        status = status,
        fitScore = fitScore,
        location = loc,
        salaryRange = salary,
        appliedDate = if (appliedDaysAgo > 0) daysAgo(appliedDaysAgo) else null,
        interviewDate = interviewDaysFromNow?.let { daysFromNow(it) },
        notes = if (desc.isNotBlank()) desc else "",
        jobDescription = desc,
        lastSeenDate = daysAgo(appliedDaysAgo)
    )

    // ── Career Insights ───────────────────────────────────────────────────────

    private fun syntheticInsights() = CareerInsightsEntity(
        id = UUID.randomUUID().toString(),
        generatedDate = daysAgo(14),
        identifiedGaps = listOf(
            "AWS / cloud infrastructure experience",
            "System design for distributed systems at scale",
            "Team leadership or tech lead track record",
            "iOS cross-platform or KMM familiarity"
        ),
        recommendedActions = listOf(
            "Complete the AWS Solutions Architect Associate course — cited in 3 of 5 rejections",
            "Take on a tech lead role in your side project to build documented leadership evidence",
            "Publish 2 blog posts on Compose architecture to improve discoverability",
            "Apply to 3 more fintech roles this week — your interview conversion rate is highest there",
            "Add a KMM proof-of-concept to your GitHub to address the iOS skills gap"
        ),
        summaryAnalysis = "You are consistently progressing to late interview stages at product-focused " +
                "companies (Spotify, Airbnb, Klarna) but losing out on roles with infrastructure or " +
                "distributed systems components. Fintech is your strongest vertical — 3 of your 5 most " +
                "advanced applications are in that sector. Prioritising roles where Kotlin/Compose is " +
                "the primary stack rather than a secondary concern will significantly improve your offer rate."
    )

    // ── Resume text ───────────────────────────────────────────────────────────

    companion object {
        private val RESUME_TEXT = """
Alex Johnson | Senior Android Engineer
alex@example.com | github.com/alexjohnson | linkedin.com/in/alexjohnson

SUMMARY
Experienced Android engineer with 5+ years delivering consumer-facing mobile products
at scale. Deep expertise in Kotlin, Jetpack Compose, and clean architecture (MVVM +
Clean). Passionate about developer experience, testability, and mentoring junior engineers.

EXPERIENCE

Senior Android Developer — FinPay (Fintech Startup) | 2022–Present
• Built the core payments and onboarding flow using Kotlin + Compose, reducing
  transaction error rates by 40% through improved input validation and retry logic.
• Architected offline-first data sync using Room + WorkManager, enabling full feature
  parity in areas with poor network connectivity.
• Introduced Hilt dependency injection across the codebase, reducing boilerplate by 35%
  and improving testability across all layers.
• Mentored 2 junior engineers through weekly code reviews and pair programming sessions.
• Reduced CI build times by 28% by migrating from Bitrise to GitHub Actions with
  Gradle build caching.

Android Developer — ShopCart (E-commerce) | 2020–2022
• Delivered 6 major feature releases across a 3M DAU Android app using Kotlin + XML.
• Led the migration of 12 legacy XML screens to Jetpack Compose, establishing internal
  patterns and a component library used by the whole mobile team.
• Integrated Retrofit + OkHttp with custom interceptors for auth token refresh and
  request logging.
• Wrote unit and integration tests achieving 82% coverage across business logic layers.

Junior Android Developer — Appify Agency | 2019–2020
• Built 4 client Android apps from scratch using Kotlin and MVVM architecture.
• Delivered features including Google Maps integration, Firebase push notifications,
  and in-app purchase flows.

SKILLS
Languages:   Kotlin (expert), Java (proficient), Python (basic)
UI:          Jetpack Compose, XML layouts, Material Design 3, custom Canvas drawing
Architecture: MVVM, Clean Architecture, MVI
DI:          Hilt, Dagger 2
Async:       Coroutines, Flow, StateFlow, LiveData (legacy)
Storage:     Room, DataStore, SQLCipher, SharedPreferences
Networking:  Retrofit, OkHttp, Gson, Moshi
Testing:     JUnit, MockK, Turbine, Espresso, Compose UI Testing, Robolectric
Tools:       Android Studio, Git, GitHub Actions, Bitrise, Firebase, Sentry
Other:       WorkManager, Jetpack Navigation, Paging 3, CameraX, ML Kit

EDUCATION
BSc Computer Science — University of Manchester | 2015–2019 | First Class Honours

PROJECTS
JobAssistant (Personal) — AI-powered job application tracker for Android
• Jetpack Compose + Hilt + Room + Gemini API. End-to-end encrypted with SQLCipher.
        """.trimIndent()
    }
}
