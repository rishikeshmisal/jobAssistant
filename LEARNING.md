# AI Job Assistant (Android) — Learning & Reference Document

## Context

This document summarizes the full spec for the AI Job Assistant Android app and the technical decisions made before implementation begins. It serves as a living reference for any development session working on this project.

The app is a **privacy-first, offline-first** career companion. All persistent data lives in an encrypted local Room database. No custom backend exists — data flows only to Google (Gmail API) and Anthropic (Claude API inference).

---

## Resolved Technical Decisions

| Decision | Choice | Reason |
|---|---|---|
| Claude model | `claude-sonnet-4-6` | Claude Sonnet 4.6 — latest model, valid API model ID as of 2026 |
| PDF library | `PdfiumAndroid` | Lighter, simpler Android integration |
| Min SDK | API 26 (Android 8.0) | EncryptedSharedPreferences + Credential Manager support |
| Target SDK | API 35 (Android 15) | Latest stable |
| Build start | Phase 1 (spec order) | Project setup → data layer → AI → Gmail → UI |

---

## Architecture Overview

**Pattern:** Clean Architecture + MVVM

```
app/src/main/java/com/jobassistant/
├── data/           # Room entities, DAOs, repositories, Retrofit clients
├── domain/         # Use cases, domain models, interfaces
├── ui/             # Jetpack Compose screens, ViewModels, theme engine
├── service/        # WorkManager workers (Gmail polling, DB cleanup)
└── di/             # Hilt modules
```

**Concurrency:** Kotlin Coroutines + StateFlow throughout. No RxJava.

---

## Full Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| DI | Hilt |
| Local DB | Room + SQLCipher (256-bit AES) |
| Prefs | EncryptedDataStore (DataStore + EncryptedFile) |
| Networking | Retrofit + OkHttp |
| AI | Anthropic REST API (`claude-sonnet-4-6`) |
| Auth | Google Sign-In via Credential Manager API |
| Background | WorkManager |
| PDF | PdfiumAndroid (text extraction) |
| OCR | Google ML Kit Vision (unbundled via Play Services — saves ~15MB APK) |

---

## Data Models

### JobApplication (Room Entity — Encrypted)

```kotlin
@Entity
data class JobApplication(
    @PrimaryKey val id: UUID,
    val companyName: String,
    val roleTitle: String,
    val jobUrl: String?,
    val status: ApplicationStatus,   // SAVED, APPLIED, INTERVIEWING, OFFERED, REJECTED
    val fitScore: Int?,
    val location: String?,
    val salaryRange: String?,
    val appliedDate: Long?,
    val interviewDate: Long?,
    val notes: String,
    val linkedEmailThreadIds: List<String>   // TypeConverter needed
)
```

### UserProfile (EncryptedDataStore)

```kotlin
data class UserProfile(
    val userId: String,
    val fullName: String,
    val resumeText: String,          // Extracted locally via PdfiumAndroid
    val keywords: List<String>,
    val careerGoal: String,
    val targetSalaryRange: Pair<Int, Int>,
    val selectedTheme: AppTheme      // GREEN, RED, BLUE, YELLOW
)
```

### CareerInsights (Room Entity — Encrypted)

```kotlin
@Entity
data class CareerInsights(
    @PrimaryKey val id: UUID,
    val generatedDate: Long,
    val identifiedGaps: List<String>,     // TypeConverter needed
    val recommendedActions: List<String>, // TypeConverter needed
    val summaryAnalysis: String
)
```

---

## Claude API Integration

**Base URL:** `https://api.anthropic.com/v1/messages`
**Model:** `claude-sonnet-4-6`
**Strategy:** Anthropic Tool Use (Function Calling) API — define each response schema in the `tools` array and set `tool_choice` to force structured output. This is more reliable than prompt engineering ("respond only with JSON") because Claude is guaranteed to output a valid tool call object, eliminating the risk of conversational text wrapping that breaks Gson parsing.

> **Note on model ID:** `claude-sonnet-4-6` is the correct API model ID for Claude Sonnet 4.6 (the latest model). Do not substitute `claude-3-5-sonnet-20241022` — that is an older model. The API will return a 400 error for any unrecognised model string.

### Endpoints (logical — all hit the same REST endpoint)

#### 1. `analyze_intent` — Career Profile Generation
- **Input:** `resumeText: String`, `userInterests: String`
- **Output:** `CareerProfile` JSON
- **When:** On first run / profile setup
- **Response fields:** `current_level`, `target_roles`, `skill_gaps`, `recommended_focus_areas`, `goal_map`

#### 2. `evaluate_fit` — Job Fit Scoring
- **Input:** `resumeText: String`, `jobDescription: String`
- **Output:** `FitAnalysis` JSON
- **When:** User inputs a job description (URL scrape / manual paste / screenshot OCR)
- **Response fields:** `score: Int (1-100)`, `pros: List<String>`, `cons: List<String>`, `missing_skills: List<String>`

#### 3. `parse_email_context` — Email Classification
- **Input:** `emailSubject: String`, `emailBody: String`
- **Output:** `EmailAction` JSON
- **When:** After local regex pre-filter passes an email as "job-related"
- **Response fields:** `action_type: Enum (APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT)`, `target_company: String?`, `role_title: String?`, `date: Long?`, `interview_link: String?`
- **Cost mitigation:** Only ~20% of emails should reach this call — most are filtered locally first

#### 4. `generate_career_insights` — Retrospective Analysis
- **Input:** `profileText: String`, `jobHistorySummary: String` (anonymized)
- **Output:** `CareerInsights` JSON
- **When:** User-triggered or periodic (cached locally, not regenerated unless stale)
- **Response fields:** `identified_gaps`, `recommended_actions`, `market_feedback_summary`

---

## Gmail Integration

**OAuth2 Scope:** `https://www.googleapis.com/auth/gmail.readonly`
**Auth:** Google Sign-In via Android Credential Manager API (no backend token storage)
**Polling:** WorkManager `PeriodicWorkRequest` (every 15–30 minutes)

> **Doze Mode & App Standby Warning:** Android's Doze Mode and App Standby Buckets will prevent the worker from running precisely on schedule when the device is idle or the app is not in active use. This is by design (battery saving). For reliable background sync, prompt the user in the Profile screen to exempt the app from battery optimisation (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`). WorkManager will still respect Doze windows but will execute as soon as the next maintenance window opens.

### Email Processing Pipeline

```
Gmail API fetch
    → Local Regex/Keyword pre-filter
        → IRRELEVANT: discard
        → RELEVANT: send to Claude parse_email_context
            → APPLIED:    auto-create JobApplication (status=APPLIED)
            → REJECTION:  find existing job → update status=REJECTED
            → INTERVIEW:  extract datetime/link → prompt Calendar intent
            → ALERT:      fire high-priority local notification
```

### Pre-filter Signals (regex / keyword matching)

| Category | Signal phrases / patterns |
|---|---|
| Applied | "thank you for applying", "application received", "we received your application" |
| Rejection | "not moving forward", "other candidates", "position has been filled", "regret to inform" |
| Interview | "interview", "schedule a call", "meet with our team", `calendly.com`, `zoom.us` |
| Alerts | Sender domains: `linkedin.com`, `indeed.com`, `glassdoor.com` |

---

## Feature Areas

### 2.1 Profile & Intelligence Engine
- Resume upload (PDF) → PdfiumAndroid extracts plain text → saved to `UserProfile`
- First run: extracted text + user interests → `analyze_intent` → save `CareerProfile` locally
- Job input via: URL (scrape page text via OkHttp), manual paste, or screenshot (ML Kit OCR)
- Any input → `evaluate_fit` → display score + pros/cons card

### 2.2 Gmail Sync (background WorkManager)
- Google Sign-In → store OAuth token in EncryptedDataStore
- WorkManager `PeriodicWorkRequest` fetches recent emails on schedule
- Pipeline: fetch → pre-filter → Claude parse → Room update → Notification / Calendar intent

### 2.3 Job Management Dashboard
- **Kanban view:** Horizontal scrolling lanes per status (SAVED → APPLIED → INTERVIEWING → OFFERED → REJECTED)
- **List view:** Sortable / filterable flat list
- Full offline read/write — Room is single source of truth
- Swipe gestures to move jobs between stages
- Tap job card → detail screen with full info, fit score, notes, linked emails

### 2.4 Career Insights Tab ("What have we learnt")
- Aggregate stats: applied/interviewed/rejected ratios, time-to-rejection, top rejecting companies
- LLM retrospective: `generate_career_insights` → display `CareerInsights`
- Cached: only re-generate when user explicitly requests or on weekly schedule
- Actionable callouts: missing skills, role pivot suggestions

### 2.5 Dynamic Theming
- Theme options: GREEN, RED, BLUE, YELLOW
- Selected color becomes Material 3 `primary` seed color
- `MaterialTheme.colorScheme` dynamically generated from seed via `ColorScheme` builder
- Theme persisted in `UserProfile.selectedTheme` via DataStore
- Persistent theme selector row at top of app (chip/button group)

---

## Execution Plan (9 Phases)

> This maps exactly to the 9 phases in `execution.md`. Each phase is a runnable MVP.

### Phase 1 — Prerequisites & Project Scaffold
- Init Android Studio project (Kotlin, Compose, Hilt)
- Configure `build.gradle`: Room, SQLCipher, Hilt, Retrofit, OkHttp, JaCoCo (≥80% coverage enforcement)
- SQLCipher passphrase: generated via `SecureRandom`, stored directly in Android Keystore
- Configure `network_security_config.xml` (allowlist: `api.anthropic.com`, `*.googleapis.com`)
- **MVP:** App launches, encrypted DB initialises, no plain-text DB file on device

### Phase 2 — Data & Domain Layer
- Define Room Entities: `JobApplication`, `CareerInsights`
- Write TypeConverters for `List<String>`, `UUID`, `ApplicationStatus`
- Write DAOs with Flow-returning queries
- Write Repositories (interface in `domain/`, impl in `data/`)
- Implement `UserProfileDataStore` using `EncryptedDataStore` (DataStore + EncryptedFile)
- Implement PdfiumAndroid text extraction utility
- **MVP:** Full offline CRUD for jobs; `UserProfile` persists across restarts

### Phase 3 — Navigation Shell + Dynamic Theming
- Dynamic Material 3 theme engine (`AppColors.kt`, `JobAssistantTheme.kt`)
- `ThemeSelector` component, `NavHost` + bottom nav, all placeholder screens
- Onboarding shell (3-step pager)
- **MVP:** Full navigation works; theme switches live; onboarding shown on first run

### Phase 4 — AI Service Layer
- Build `AnthropicApiService` (Retrofit interface)
- Build `OkHttpClient` with API key interceptor
- Use **Anthropic Tool Use API** for all 4 Claude endpoints (guaranteed structured output)
- Write `ClaudeRepository` wrapping all 4 calls with error handling
- Wire `evaluate_fit` to the manual paste AddJob screen
- **MVP:** Paste a job description → fit score + pros/cons displayed

### Phase 5 — Profile + Full Job Input
- Complete onboarding (PDF upload, `analyze_intent` on first run)
- URL scraping input path + `HtmlStripper`
- Duplicate detection UI (dialog on conflict)
- **MVP:** All 3 input paths work; resume upload and analyze_intent functional

### Phase 6 — Full Dashboard UI
- Full Kanban board + List view + `JobDetailScreen`
- `InsightsScreen` with aggregate stats and LLM retrospective
- Empty states for all screens
- **MVP:** Full job lifecycle; insights tab showing stats and AI analysis

### Phase 7 — Gmail Integration
- Google Sign-In (Credential Manager) → tokens in `EncryptedDataStore`
- `EmailPreFilter` (regex/keyword pre-filter engine)
- `GmailSyncWorker` (WorkManager `CoroutineWorker`) with exponential backoff
- Full email pipeline → Room updates → Calendar intent → Notifications
- Battery optimisation exemption prompt for reliable Doze-mode sync
- **MVP:** Gmail syncs in background; emails auto-update job statuses

### Phase 8 — OCR + Screenshot Job Input
- Integrate `com.google.android.gms:play-services-mlkit-text-recognition` (unbundled, saves ~15MB)
- `OcrProcessor`: `Bitmap` → extracted `String`
- Screenshot tab in AddJob; system share sheet target
- **MVP:** Screenshot → OCR → fit score → save job

### Phase 9 — Polish & Security Hardening
- R8 full-mode obfuscation; release build verification
- Encrypted JSON export/backup
- Job expiry warning (SAVED jobs >30 days old)
- BYOK (bring-your-own-key) Anthropic API key in Profile screen
- **MVP:** Release-ready; full regression suite passes; total coverage ≥80%

---

## Security & Privacy Constraints

- **Zero backend rule:** No Firebase, Supabase, or custom server. Enforced via `network_security_config.xml` domain allowlist.
- **SQLCipher passphrase:** Generated on first run using `SecureRandom`, stored directly in the Android Keystore (`KeyStore.getInstance("AndroidKeyStore")`).
- **OAuth tokens:** Stored only in `EncryptedDataStore` (DataStore + EncryptedFile), never logged or transmitted to any server other than Google.
- **Claude API key:** Stored in `local.properties` (never committed to git), injected via `BuildConfig` at compile time.
- **Data in transit:** HTTPS only. `resumeText` and email bodies are never logged.
- **Cost controls:** `EmailPreFilter` must be implemented before any Gmail → Claude pipeline goes live. `generate_career_insights` must check local cache age before calling the API.

---

## Improvements & Suggestions

### Architecture & Technical

**1. Local AI fallback (offline fit scoring)**
The app is fully dependent on Claude for fit scoring. Cache the last-used prompt+response locally so users get results even when offline or the API is down.

**2. Exponential backoff on WorkManager**
The Gmail polling worker should have retry policies with exponential backoff — not just periodic scheduling. Gmail API rate limits will hit eventually.

**3. Passphrase rotation**
The SQLCipher passphrase is generated once and stored forever with no mechanism to rotate it (e.g., after biometric re-auth). Design a migration path early before data accumulates.

**4. DataStore over EncryptedSharedPreferences for UserProfile**
The spec mixes both. Standardize on `EncryptedDataStore` (DataStore + EncryptedFile) — it's the modern approach and avoids known reliability issues with `EncryptedSharedPreferences` on some Android versions.

---

### Features

**5. Duplicate detection** ⚠️ High priority
If a user manually adds a job AND Gmail auto-detects the same application, duplicates will be created. Need a dedup strategy — fuzzy match on company name + role title before inserting a new `JobApplication`.

**6. Job description expiry / archiving**
Job postings go stale. Add a `lastSeenDate` field and surface a "this posting may be expired" warning if a saved job hasn't been checked in 30+ days.

**7. Interview prep mode**
Since the app already has the job description and user profile, Claude can generate likely interview questions + talking points per job. High value, low cost to add as an extra action on the `JobDetailScreen`.

**8. Export / backup**
Users will fear losing their job hunt history. An encrypted export to a local file (JSON or CSV) gives peace of mind without violating the no-backend rule.

**9. Calendar integration is one-way**
The spec only mentions creating calendar events for interviews. Reading back from the calendar (e.g., detecting a rescheduled or cancelled interview) would keep tracking more accurate.

---

### Gmail Pipeline

**10. Thread-level deduplication**
The spec links `emailThreadIds` to jobs but the pipeline doesn't explicitly deduplicate by thread ID. The same thread (e.g., interview rescheduled email) could trigger multiple Room updates. Guard against this in `GmailSyncWorker`.

**11. Unsubscribe / marketing email handling**
Job alert emails from LinkedIn and Indeed often contain irrelevant listings, unsubscribe confirmations, and marketing content. The pre-filter should explicitly detect and discard these to reduce noise and API calls.

---

### UX

**12. Onboarding flow is undefined** ⚠️ High priority
The spec jumps straight to features but doesn't describe the first-run experience: sign in → upload resume → set goals → connect Gmail. This is the most critical UX path and needs to be designed explicitly.

**13. Fit score visual breakdown**
Displaying a raw number (1–100) without context is confusing. Show a visual breakdown — a small bar or ring showing pros/cons weight — not just the score integer.

**14. Empty states are undefined**
What does the Kanban board look like with zero jobs? What does the Insights tab show before any data exists? Empty states need explicit designs or screens will feel broken on first use.

---

### Security

**15. API key exposure risk** ⚠️ High priority
Storing the Claude API key in `BuildConfig` embeds it in the APK where it can be extracted via reverse engineering. Options:
- Enable ProGuard/R8 full-mode obfuscation (minimum mitigation)
- Add certificate pinning for `api.anthropic.com`
- Consider a bring-your-own-key (BYOK) model where the user enters their own Anthropic API key — this aligns perfectly with the privacy-first philosophy and eliminates the risk entirely

**16. No mention of ProGuard/R8**
The spec doesn't mention code obfuscation. For an app handling career data and API keys, enabling R8 full-mode shrinking + obfuscation should be a default in the release build config.

---

### Priority Summary

| Priority | Item |
|---|---|
| High — address before shipping | #5 Duplicate detection, #12 Onboarding flow, #15 API key exposure |
| Medium — next iteration | #7 Interview prep, #8 Export/backup, #13 Fit score visual, #14 Empty states |
| Low — polish | #1 Offline fallback, #2 Backoff, #3 Passphrase rotation, #6 Expiry, #9 Calendar read, #10 Thread dedup, #11 Unsubscribe filter, #16 R8 |

---

## Verification Checklist (per phase)

- [ ] **Phase 1:** App launches; Room DB initialises with SQLCipher; no plain-text DB file on device; unit tests pass; coverage ≥80%
- [ ] **Phase 2:** Can insert/query `JobApplication` and `CareerInsights` via DAO; `UserProfile` persists across restarts; duplicate detection works; DAO instrumented tests pass
- [ ] **Phase 3:** Full navigation works; theme switches live across all screens; theme persists after restart; onboarding shown on first run; Compose UI tests pass
- [ ] **Phase 4:** All 4 Claude Tool Use endpoints return correctly typed Kotlin objects; paste a job description → fit score + pros/cons displayed; `MockWebServer` tests pass
- [ ] **Phase 5:** All 3 job input paths (URL, paste, screenshot placeholder) produce a `JobApplication` with a fit score; `analyze_intent` fires after PDF upload; duplicate dialog appears on conflict
- [ ] **Phase 6:** Kanban lanes display all statuses; job detail screen editable; insights tab shows correct stats and AI analysis; empty states visible with no data
- [ ] **Phase 7:** Gmail syncs in background; APPLIED email → job auto-created; rejection email → status updated to REJECTED; thread dedup prevents duplicates; `EmailPreFilter` classifies all 20 sample emails correctly
- [ ] **Phase 8:** Screenshot → OCR → extracted text feeds correctly into `evaluate_fit`; share sheet target opens AddJob with pre-loaded image
- [ ] **Phase 9:** Release build passes R8 without errors; encrypted JSON export readable; BYOK key overrides BuildConfig key; overall project coverage ≥80%
