# Synthetic Data Plan — Job Assistant Emulator Testing

## Goal

Populate the emulator with realistic test data that exercises every feature of the app:
all 10 `ApplicationStatus` values, fit scores, job descriptions, AI insights, user profile,
and enough volume to make the Dashboard, Insights, and CSV import all look real.

---

## Delivery Mechanism

### Option chosen: Debug-only "Seed Data" button in Profile screen

- A `SeedDataHelper` object lives in `app/src/debug/java/` — it is **never compiled into
  release builds**.
- The Profile screen's Data section shows a destructive **"Seed Test Data"** button only
  when `BuildConfig.DEBUG == true`.
- Tapping it:
  1. Deletes all existing `job_applications` and `career_insights` rows.
  2. Writes the synthetic `UserProfile` to `UserProfileDataStore`.
  3. Upserts all synthetic `JobApplicationEntity` rows directly via the DAO.
  4. Upserts one `CareerInsightsEntity`.
- A `Snackbar` confirms "Test data loaded — 32 jobs seeded".

This approach requires no adb scripts, no external tooling, and survives app restarts.

---

## Synthetic User Profile

| Field | Value |
|---|---|
| `fullName` | Alex Johnson |
| `careerGoal` | AI-generated goal map (see below) |
| `keywords` | Android, Kotlin, Jetpack Compose, Hilt, Room, Coroutines, REST APIs, CI/CD, Agile |
| `targetSalaryMin` | 80000 |
| `targetSalaryMax` | 120000 |
| `resumeText` | ~600-word realistic Android developer resume (see below) |

**Career goal (AI-generated narrative):**
> "Experienced Android engineer with 5 years building consumer-facing apps at scale.
> Strong foundation in Kotlin, Compose, and clean architecture. Targeting senior/lead
> Android roles at product companies where I can own features end-to-end and mentor
> junior engineers. Open to fintech, health, or productivity verticals."

**Resume text (excerpt — full text embedded in helper):**
> Alex Johnson | Android Engineer | alex@example.com | github.com/alexj
>
> EXPERIENCE
> Senior Android Developer — Fintech Startup (2022–present)
> Built the core payments flow using Kotlin + Compose, reducing transaction errors by 40%.
> Architected offline-first sync using Room + WorkManager. Mentored 2 junior engineers.
>
> Android Developer — E-commerce Platform (2020–2022)
> Delivered 6 major feature releases across a 3M DAU app. Migrated XML layouts to Compose.
> Introduced Hilt DI, cutting boilerplate by 35%. Improved CI pipeline with GitHub Actions.
>
> SKILLS
> Kotlin · Jetpack Compose · Coroutines · Flow · Hilt · Room · Retrofit · OkHttp
> MVVM · Clean Architecture · WorkManager · Firebase · Git · GitHub Actions
>
> EDUCATION
> BSc Computer Science — University of Manchester, 2020

---

## Synthetic Job Applications — 32 jobs across all 10 statuses

### INTERESTED (4) — saved but not applied

| Company | Role | Fit Score | Date Saved |
|---|---|---|---|
| Monzo | Senior Android Engineer | null | 5 days ago |
| Revolut | Android Developer | null | 3 days ago |
| Starling Bank | Lead Android Engineer | null | 1 day ago |
| Wise | Mobile Engineer (Android) | null | Today |

### APPLIED (7) — applications submitted

| Company | Role | Fit Score | Applied Date |
|---|---|---|---|
| Google | Android Developer Advocate | 82 | 45 days ago |
| Meta | Software Engineer — Android | 74 | 40 days ago |
| Spotify | Android Engineer | 88 | 35 days ago |
| Deliveroo | Senior Android Developer | 65 | 30 days ago |
| Babylon Health | Android Engineer | 71 | 25 days ago |
| Sky | Senior Mobile Engineer | null | 20 days ago |
| BBC | Android Developer | null | 15 days ago |

### SCREENING (3) — HR/recruiter initial contact

| Company | Role | Fit Score | Date |
|---|---|---|---|
| Booking.com | Android Engineer | 79 | 28 days ago |
| Klarna | Senior Android Developer | 85 | 22 days ago |
| Monzo | Android Platform Engineer | 77 | 10 days ago |

### INTERVIEWING (4) — active interview rounds

| Company | Role | Fit Score | Interview Date |
|---|---|---|---|
| Spotify | Android Engineer | 88 | +3 days (upcoming) |
| Airbnb | Senior Android Engineer | 91 | +7 days (upcoming) |
| Shopify | Android Developer | 76 | 5 days ago |
| Citadel | Mobile Software Engineer | 69 | 3 days ago |

### ASSESSMENT (2) — take-home tasks

| Company | Role | Fit Score | Date |
|---|---|---|---|
| Wise | Android Engineer | 83 | 7 days ago |
| N26 | Senior Android Developer | 78 | 4 days ago |

### OFFER (2) — offers received

| Company | Role | Fit Score | Date |
|---|---|---|---|
| Klarna | Senior Android Developer | 85 | 2 days ago |
| Monzo | Android Platform Engineer | 77 | 1 day ago |

### ACCEPTED (1) — offer accepted

| Company | Role | Fit Score | Date |
|---|---|---|---|
| Klarna | Senior Android Developer | 85 | Today |

### REJECTED (5) — rejections at various stages

| Company | Role | Fit Score | Date |
|---|---|---|---|
| Apple | iOS/Android Engineer | 58 | 60 days ago |
| Amazon | SDE II — Android | 61 | 50 days ago |
| Goldman Sachs | Mobile Engineer | 54 | 42 days ago |
| Palantir | Software Engineer | 63 | 32 days ago |
| Stripe | Android Developer | 70 | 18 days ago |

### WITHDRAWN (2) — user pulled out

| Company | Role | Fit Score | Date |
|---|---|---|---|
| TikTok | Senior Android Engineer | 72 | 38 days ago |
| Bytedance | Android Developer | 68 | 25 days ago |

### NO_RESPONSE (3) — ghosted

| Company | Role | Fit Score | Date |
|---|---|---|---|
| Uber | Android Engineer | 75 | 55 days ago |
| Lyft | Senior Android Developer | 67 | 48 days ago |
| Twitter/X | Mobile Engineer | 60 | 44 days ago |

---

## Synthetic Career Insights

**Generated date:** 14 days ago

**Identified gaps:**
- AWS / cloud infrastructure experience
- System design for distributed systems
- Team leadership or tech lead experience
- iOS cross-platform familiarity

**Recommended actions:**
- Complete an AWS Solutions Architect course to address the most cited rejection reason
- Take on a tech lead role in current side project to build leadership evidence
- Write 2 blog posts about Compose architecture patterns to boost profile visibility
- Apply to 3 more fintech roles this week — interview conversion rate is highest there

**Market feedback summary:**
> "You are consistently progressing to late stages at product-focused companies
> (Spotify, Airbnb, Klarna) but losing out on roles with infrastructure or distributed
> systems components. Fintech is your strongest vertical — 3 of your 5 most advanced
> applications are in that sector. Prioritising roles where Kotlin/Compose is the
> primary stack rather than a secondary concern will significantly improve your offer rate."

---

## Implementation Plan

### Files to create

```
app/src/debug/java/com/jobassistant/debug/
    SeedDataHelper.kt        ← inserts all synthetic data via injected DAOs + DataStore
    DebugSeedViewModel.kt    ← @HiltViewModel wrapping SeedDataHelper, exposes seeding state
```

### Files to modify (debug-guarded)

```
app/src/main/java/com/jobassistant/ui/screens/profile/ProfileScreen.kt
    ← add "Seed Test Data" button inside `if (BuildConfig.DEBUG)` block in Data section
    ← button calls DebugSeedViewModel.seed()
    ← show snackbar on completion
```

### Build variant

- `app/src/debug/` source set is automatically included in debug builds only.
- No `debugImplementation` dependency changes needed — `SeedDataHelper` uses the same
  DAOs and DataStore that are already in the DI graph.

### Hilt wiring for debug source set

`SeedDataHelper` is a plain `class` annotated with `@Inject constructor` so Hilt resolves
it automatically in debug builds. `DebugSeedViewModel` is a `@HiltViewModel` that Hilt
discovers in the same way.

### Seeding logic (SeedDataHelper.seed())

```
1. jobApplicationDao.deleteAll()        ← add @Query("DELETE FROM job_applications") to DAO
2. careerInsightsDao.deleteAll()        ← add @Query("DELETE FROM career_insights") to DAO
3. userProfileDataStore.save(syntheticProfile)
4. syntheticJobs.forEach { jobApplicationDao.upsert(it.toEntity()) }
5. careerInsightsDao.upsert(syntheticInsights.toEntity())
```

### Date calculation

All dates are computed relative to `System.currentTimeMillis()` at seed time so the
"2 days ago" labels stay current on every seed, not frozen to a build date.

---

## Features exercised by this dataset

| Feature | Covered by |
|---|---|
| Dashboard Kanban — all 10 columns | 32 jobs across all statuses |
| Dashboard List view + filter chips | Same dataset |
| Hero stats strip | Counts across all statuses |
| FitScoreRing `?` on unscored jobs | INTERESTED + BBC/Sky jobs |
| FitScoreRing animated arc | All scored jobs |
| StatusChip 10 color variants | One job per status minimum |
| RelativeTimeText brackets | Jobs from today → 60 days ago |
| Job Detail — score ring + expandable | Any scored job |
| Job Detail — job description pre-fill | Jobs seeded with jobDescription |
| Insights → Career Profile tab | Full profile with keywords + resume |
| Insights → AI Coach tab | Synthetic CareerInsights |
| Insights → Applied Jobs — 6 stat cards | 32 jobs with varied statuses |
| Insights → Applied Jobs — Funnel row | Applied/Interviews/Offers counts |
| Insights → Applied Jobs — Top Companies | Monzo (×3), Klarna (×2), Spotify (×2) |
| Quick Evaluate (EvaluateJobScreen) | Manual testing — no data needed |
| CSV Import | Manual testing with a sample CSV file |
| Theme switcher | Manual testing |

---

## Sample CSV file for import testing

A `sample_jobs.csv` file will also be created at the project root to test the CSV import
feature independently:

```csv
Company,Role,Status,Date Applied,Location,Salary
Netflix,Senior Android Engineer,Applied,2024-11-15,Remote,£110000
Zalando,Android Developer,Rejected,2024-10-20,Berlin,€80000
Nubank,Mobile Engineer,Interviewing,2024-12-01,Remote,
Figma,Android Engineer,No Response,2024-09-30,San Francisco,
Canva,Senior Android Developer,Offer,2025-01-10,Remote,£95000
```

---

## Out of scope for synthetic data

- **Gmail integration** — requires a real OAuth token; test manually with a real account
- **OCR screenshot** — test manually by sharing a job screenshot from the browser
- **URL fetch** — test manually by pasting a real job URL

---

## Next step

Run the execute command below to implement:

```
Create the debug source set files as planned in synthetic_data.md.

1. Add @Query("DELETE FROM job_applications") deleteAll() to JobApplicationDao and @Query("DELETE FROM career_insights") deleteAll() to CareerInsightsDao.
2. Create app/src/debug/java/com/jobassistant/debug/SeedDataHelper.kt with all 32 synthetic jobs and the synthetic profile + insights exactly as specified in synthetic_data.md. Use System.currentTimeMillis() for all dates, offset by the number of days shown in the table.
3. Create app/src/debug/java/com/jobassistant/debug/DebugSeedViewModel.kt as a @HiltViewModel that calls SeedDataHelper.seed() and exposes a StateFlow<SeedState> (Idle / Seeding / Done(count) / Error).
4. In ProfileScreen.kt, inside the Data section Card, add an if (BuildConfig.DEBUG) block after the Export Data button that shows a red OutlinedButton("Seed Test Data") which calls debugSeedViewModel.seed() and shows a Snackbar on completion.
5. Create sample_jobs.csv at the project root with the 5 rows from synthetic_data.md.
6. Build and install: ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```
