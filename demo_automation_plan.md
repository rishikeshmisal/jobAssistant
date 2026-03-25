# Demo Automation Plan — Job Assistant

## Overview

This document is the complete plan for building a recorded, automated demo of the app
for potential users. It covers:

1. Assessment of existing Claude skills and what's missing
2. A new `maestro-demo` skill to create
3. Missing `testTag`s to add to the app (the only app-code change)
4. Synthetic data implementation plan (`SeedDataHelper` + debug button)
5. Maestro flow architecture — scene-by-scene
6. File structure (everything outside `app/`)
7. Recording and delivery

Nothing in this plan changes functional app behaviour. The only app-code change is
adding `testTag`s to elements Maestro needs to interact with, and the debug-only seed
button. All Maestro YAML files and the skill live outside the `app/` directory.

---

## 1. Existing Skills Assessment

| Skill | Useful for this plan? | Why |
|---|---|---|
| `android-hilt-patterns` | ✅ Partially | `SeedDataHelper` and `DebugSeedViewModel` must follow Hilt patterns |
| `room-sqlcipher-migrations` | ✅ Partially | `SeedDataHelper` writes directly via Room DAOs — need correct entity patterns |
| `gemini-kotlin` | ❌ No | Not relevant to Maestro or seed data |
| `claude-api-tools-use` | ❌ No | Not relevant |

**Gap:** There is no skill for Maestro YAML authoring, element-finding strategies,
or the demo-specific conventions for this app. This must be created.

---

## 2. New Skill to Create — `maestro-demo`

**Location:** `.claude/skills/maestro-demo/SKILL.md`

**Purpose:** Canonical reference for writing Maestro flows for this specific app.
Any future Claude session asked to modify or extend the demo flows should read this
skill first.

**Contents:**

### 2a. App identity and install commands
```
appId: com.jobassistant
Install: ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
Record: maestro test flows/full_demo.yaml --video
```

### 2b. Element-finding strategy (what testTag exists vs what Maestro finds by text)

Elements **with testTags** (use `id:` in Maestro):
```
seed_test_data_button      ← debug seed button (to be added)
name_field                 ← onboarding name input
career_goal_field          ← onboarding career goal
onboarding_upload_button   ← onboarding PDF picker
skip_resume_button         ← onboarding skip
company_name_field         ← Add Job screen
role_title_field           ← Add Job screen
location_field             ← Add Job screen
salary_range_field         ← Add Job screen
save_job_button            ← Add Job screen
upload_resume_button       ← Profile resume section
profile_name_field         ← Profile name field
save_profile_button        ← Profile save
export_data_button         ← Profile data section
import_csv_button          ← Profile data section
choose_csv_button          ← CSV Import idle state
confirm_import_button      ← CSV Import preview
view_dashboard_button      ← CSV Import done state
evaluate_fit_fab           ← SpeedDial "Evaluate Fit" option (to be added)
track_job_fab              ← SpeedDial "Track Job" option (to be added)
speed_dial_main_fab        ← The main + / × FAB (to be added)
```

Elements **without testTags** — found by visible text or content description:
```
"Dashboard"                ← bottom nav tab
"Profile"                  ← bottom nav tab
"Insights"                 ← bottom nav tab
"Career Profile"           ← Insights tab
"AI Coach"                 ← Insights tab
"Applied Jobs"             ← Insights tab
"Refresh"                  ← Insights refresh button
"Check Fit"                ← EvaluateJobScreen analyze button
"Save & Track"             ← EvaluateJobScreen save button
"Start Over"               ← EvaluateJobScreen reset
"Paste Text"               ← tab label (Evaluate + Job Detail)
"Paste URL"                ← tab label
"Screenshot"               ← tab label
"Analyze Fit"              ← Job Detail analyze button
"Save Changes"             ← Job Detail save button
"Interested" / "Applied" / "Screening" etc. ← status dropdown items
```

### 2c. Standard timing conventions for this app
```yaml
# After screen transitions
- sleepFor: 800

# After AI/network calls (Gemini)
- sleepFor: 4000

# After animations (score ring, seed data loading)
- sleepFor: 1500

# After tapping FAB (SpeedDial expand animation)
- sleepFor: 600

# After CSV mapping (longer Gemini call)
- sleepFor: 5000
```

### 2d. SpeedDial FAB interaction pattern
```yaml
# The SpeedDial requires: tap FAB → wait → tap mini-FAB
- tapOn:
    id: "speed_dial_main_fab"
- sleepFor: 600
- tapOn:
    id: "evaluate_fit_fab"   # or "track_job_fab"
```

### 2e. Asserting content after AI calls
```yaml
# Score ring — assert a number is visible (not "?")
- assertVisible:
    text: "Fit Score"
# Status chips
- assertVisible:
    text: "Applied"
```

### 2f. Run and record commands
```bash
# Run without recording
maestro test flows/full_demo.yaml

# Run with video recording
maestro test flows/full_demo.yaml --video

# Upload to Maestro Cloud (shareable link)
maestro cloud --apiKey <key> flows/full_demo.yaml

# Run all flows
maestro test flows/
```

---

## 3. testTags to Add to the App

These are the **only app code changes** required. All are purely additive
(`Modifier.testTag(...)` on existing composables) and have no effect on
release builds or functional behaviour.

| File | Composable / element | testTag to add |
|---|---|---|
| `DashboardScreen.kt` | Main `FloatingActionButton` in `SpeedDialFab` | `speed_dial_main_fab` |
| `SpeedDialFab.kt` | "Evaluate Fit" `SmallFloatingActionButton` | `evaluate_fit_fab` |
| `SpeedDialFab.kt` | "Track Job" `SmallFloatingActionButton` | `track_job_fab` |
| `ProfileScreen.kt` (debug block) | "Seed Test Data" `OutlinedButton` | `seed_test_data_button` |
| `EvaluateJobScreen.kt` | "Check Fit" `Button` (paste tab) | `check_fit_button` |
| `EvaluateJobScreen.kt` | "Save & Track" `Button` | `save_and_track_button` |
| `EvaluateJobScreen.kt` | "Start Over" `TextButton` | `start_over_button` |
| `JobDetailScreen.kt` | "Analyze Fit" `Button` in job description section | `analyze_fit_button` |

Total: **8 testTag additions** across 5 files.

---

## 4. Synthetic Data Implementation Plan

### 4a. Files to create

```
app/src/debug/java/com/jobassistant/debug/
    SeedDataHelper.kt
    DebugSeedViewModel.kt
```

These live in the `debug` source set — **never compiled into release APKs**.

### 4b. SeedDataHelper.kt

A plain class with `@Inject constructor` receiving:
- `JobApplicationDao` — direct DAO access for upsert + deleteAll
- `CareerInsightsDao` — direct DAO access for upsert + deleteAll
- `UserProfileDataStore` — writes the synthetic profile

**`deleteAll` queries to add to DAOs:**
```kotlin
// JobApplicationDao.kt
@Query("DELETE FROM job_applications")
suspend fun deleteAll()

// CareerInsightsDao.kt
@Query("DELETE FROM career_insights")
suspend fun deleteAll()
```

**`seed()` function flow:**
1. `jobApplicationDao.deleteAll()`
2. `careerInsightsDao.deleteAll()`
3. `userProfileDataStore.save(syntheticProfile)`
4. `syntheticJobs.forEach { jobApplicationDao.upsert(it.toEntity()) }`
5. `careerInsightsDao.upsert(syntheticInsights.toEntity())`
6. Returns count of inserted jobs

**Date calculation:** all `appliedDate` / `lastSeenDate` values computed as
`System.currentTimeMillis() - N.days` at seed time — labels stay fresh every run.

**32 synthetic jobs** covering all 10 statuses — see `synthetic_data.md` for the
full dataset (companies, roles, scores, dates).

**Synthetic profile:**
```
fullName        = "Alex Johnson"
resumeText      = 600-word Android developer resume (embedded as a constant)
keywords        = [Android, Kotlin, Jetpack Compose, Hilt, Room, Coroutines,
                   REST APIs, CI/CD, Agile]
careerGoal      = AI-generated narrative (embedded as a constant)
targetSalaryMin = 80000
targetSalaryMax = 120000
```

**Synthetic CareerInsights:**
```
identifiedGaps        = [AWS experience, System design, Leadership, iOS familiarity]
recommendedActions    = [4 realistic actions — see synthetic_data.md]
marketFeedbackSummary = Narrative paragraph — see synthetic_data.md
generatedDate         = System.currentTimeMillis() - 14.days
```

### 4c. DebugSeedViewModel.kt

`@HiltViewModel` wrapping `SeedDataHelper`. Exposes:
```kotlin
sealed class SeedState {
    object Idle : SeedState()
    object Seeding : SeedState()
    data class Done(val count: Int) : SeedState()
    data class Error(val message: String) : SeedState()
}
val seedState: StateFlow<SeedState>
fun seed()
```

### 4d. ProfileScreen.kt change (debug-guarded)

Inside the "Data" section Card, after the Export button:
```kotlin
if (BuildConfig.DEBUG) {
    val debugVm: DebugSeedViewModel = hiltViewModel()
    val seedState by debugVm.seedState.collectAsStateWithLifecycle()
    OutlinedButton(
        onClick = { debugVm.seed() },
        enabled = seedState !is SeedState.Seeding,
        colors = OutlinedButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier.fillMaxWidth().testTag("seed_test_data_button")
    ) {
        if (seedState is SeedState.Seeding)
            CircularProgressIndicator(modifier = Modifier.size(14.dp))
        else
            Text("Seed Test Data")
    }
    if (seedState is SeedState.Done) {
        // Snackbar shown via LaunchedEffect in the screen
    }
}
```

---

## 5. Maestro Flow Architecture

### 5a. File structure

```
flows/
    00_setup.yaml          ← seed data + verify app state
    01_onboarding.yaml     ← first-time user experience
    02_add_job.yaml        ← tracking a new job
    03_quick_evaluate.yaml ← evaluate fit without saving
    04_job_detail.yaml     ← analyze fit on a saved job, change status
    05_dashboard.yaml      ← Kanban scroll, SpeedDial, list view, filter
    06_insights.yaml       ← all 3 tabs, refresh AI Coach
    07_csv_import.yaml     ← pick sample_jobs.csv → preview → import
    08_theme.yaml          ← theme switcher visual moment
    full_demo.yaml         ← imports all scenes in sequence (the deliverable)
```

### 5b. Scene-by-scene breakdown

#### Scene 00 — Setup (hidden from demo video, run separately)
- Clear app data via `clearState: true` in appId block
- Seed test data: navigate to Profile → tap `seed_test_data_button` → wait for Done

#### Scene 01 — Onboarding (~25 seconds)
- Launch fresh (after clear)
- Enter name "Alex Johnson" → Next
- Enter career goal → Next
- Skip resume upload (tap `skip_resume_button`)
- "Get Started" → Dashboard visible
- *Shows:* step dots animating, smooth transitions, polished first-run flow

#### Scene 02 — Add Job (~20 seconds)
- Tap `speed_dial_main_fab` → SpeedDial expands (scrim visible)
- Tap `track_job_fab`
- Fill company "Stripe" + role "Android Engineer"
- Tap `save_job_button`
- *Auto-navigates* to Stripe Job Detail
- *Shows:* fast 4-field form, `?` ring in detail, smooth navigation

#### Scene 03 — Quick Evaluate (~40 seconds)
- Back to Dashboard
- Tap `speed_dial_main_fab` → tap `evaluate_fit_fab`
- Paste realistic job description (~150 words)
- Tap `check_fit_button` → wait for Gemini
- Score ring animates in
- Expand "Strengths", "Weaknesses"
- Fill company "Netflix", role "Senior Android"
- Tap `save_and_track_button`
- *Shows:* the headline feature — score before committing, animated ring

#### Scene 04 — Job Detail (~30 seconds)
- Navigate back → tap a job card with existing score (e.g. "Spotify")
- Show score ring (88), expand pros/cons
- Change status via dropdown: Applied → Interviewing
- Tap "Save Changes"
- *Shows:* job tracking depth, status lifecycle

#### Scene 05 — Dashboard (~25 seconds)
- Scroll Kanban horizontally across all 10 columns
- Pause on "Closed" divider (REJECTED / WITHDRAWN / NO_RESPONSE)
- Switch to List view
- Tap a filter chip ("Rejected")
- Switch back to Kanban
- *Shows:* full status coverage, two view modes

#### Scene 06 — Insights (~35 seconds)
- Navigate to Insights
- Career Profile tab: avatar, keyword chips, salary visible
- Tap "AI Coach" tab: gaps + lightbulb cards
- Tap "Applied Jobs" tab: 6 stat cards, funnel row, top companies (Monzo, Klarna, Spotify)
- Tap Refresh → spinner → updated insights
- *Shows:* data-rich analytics, AI coaching

#### Scene 07 — CSV Import (~40 seconds)
- Navigate to Profile → Data section
- Tap `import_csv_button`
- Tap `choose_csv_button` → select `sample_jobs.csv`
- "Mapping columns with AI…" spinner
- Preview: "Found 5 jobs" summary + column mapping card (expand it)
- Tap `confirm_import_button`
- `view_dashboard_button` → Dashboard with 5 new jobs
- *Shows:* onboarding power-user path, AI column mapping

#### Scene 08 — Theme (~15 seconds)
- Navigate to Profile → Appearance section
- Tap Purple → entire app re-themes
- Tap Green → back to default
- *Shows:* visual identity customisation

### 5c. full_demo.yaml structure

```yaml
# flows/full_demo.yaml
appId: com.jobassistant
---
# Scene 00: Data is pre-seeded (run 00_setup.yaml first separately)

# Scene 01: Onboarding
- runFlow: 01_onboarding.yaml

# Scene 02: Add Job
- runFlow: 02_add_job.yaml

# Scene 03: Quick Evaluate
- runFlow: 03_quick_evaluate.yaml

# Scene 04: Job Detail
- runFlow: 04_job_detail.yaml

# Scene 05: Dashboard
- runFlow: 05_dashboard.yaml

# Scene 06: Insights
- runFlow: 06_insights.yaml

# Scene 07: CSV Import
- runFlow: 07_csv_import.yaml

# Scene 08: Theme
- runFlow: 08_theme.yaml
```

Total estimated run time: **~4 minutes**

---

## 6. Supporting Files

```
flows/
    *.yaml                 ← all flows above
sample_jobs.csv            ← 5 realistic rows for CSV import scene
demo_automation_plan.md    ← this file
synthetic_data.md          ← synthetic data specification
test_plan.md               ← broader test strategy
```

`sample_jobs.csv` content (5 rows, realistic, exercises all CSV mapping logic):
```csv
Company,Job Title,Application Status,Date Applied,City,Salary Range
Netflix,Senior Android Engineer,Applied,2025-01-15,Remote,£110000
Zalando,Android Developer,Rejected,2024-11-20,Berlin,€80000
Nubank,Mobile Engineer (Android),Interviewing,2025-02-01,Remote,
Figma,Android Engineer,No Response,2024-10-30,San Francisco,
Canva,Senior Android Developer,Offer Received,2025-03-10,Remote,£95000
```

Column names are **deliberately non-standard** to demonstrate that Gemini maps them
correctly ("Job Title" → `roleTitle`, "Application Status" → `status`,
"City" → `location`, "Salary Range" → `salaryRange`).

---

## 7. Recording and Delivery

### Run setup (once, before recording)
```bash
# 1. Build and install
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Seed data (run this flow separately — not part of the recorded demo)
maestro test flows/00_setup.yaml

# 3. Record the full demo
maestro test flows/full_demo.yaml --video
# Output: video saved to ./maestro-recording.mp4
```

### For a shareable link (no file attachments)
```bash
maestro cloud --apiKey <key> flows/full_demo.yaml
# Returns: https://cloud.mobile.dev/run/xxxxx
```

### Device recommendation
Use the **Samsung Galaxy** (real device) rather than the emulator for the demo —
real hardware produces smoother animations and a more credible visual.

---

## 8. Implementation Order (for the coding session)

1. Create `.claude/skills/maestro-demo/SKILL.md` (new skill)
2. Add 8 `testTag`s to app files (minimal app-code change)
3. Add `deleteAll()` to `JobApplicationDao` and `CareerInsightsDao`
4. Create `app/src/debug/java/.../SeedDataHelper.kt`
5. Create `app/src/debug/java/.../DebugSeedViewModel.kt`
6. Add debug seed button to `ProfileScreen.kt`
7. Create `sample_jobs.csv`
8. Create `flows/00_setup.yaml` through `flows/08_theme.yaml`
9. Create `flows/full_demo.yaml`
10. Build, install, run `00_setup.yaml`, run `full_demo.yaml --video`

---

## 9. Execute Command

```
Read .claude/skills/maestro-demo/SKILL.md, .claude/skills/android-hilt-patterns/SKILL.md,
and .claude/skills/room-sqlcipher-migrations/SKILL.md first. Then read
demo_automation_plan.md in full before writing a single line of code.
Implement in the exact order listed in section 8. After all files are written:
  - Run ./gradlew assembleDebug and fix any compile errors
  - Run adb install -r app/build/outputs/apk/debug/app-debug.apk
  - Run maestro test flows/00_setup.yaml to verify seed works
  - Run maestro test flows/full_demo.yaml --video to produce the demo recording
```
