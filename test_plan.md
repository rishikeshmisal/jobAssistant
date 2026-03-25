# Test Plan — Job Assistant Android App

## 1. Selenium equivalent for Android

The short answer: yes — Android has multiple equivalents, and the project already uses two of them.

### Framework landscape

| Framework | Analogous to | How it works | Already in project? |
|---|---|---|---|
| **Espresso** | Selenium (unit) | White-box: runs inside the app process, interacts with Views/Compose nodes via the semantic tree | ✅ `libs.espresso` |
| **Compose UI Testing** | Selenium (Compose) | Finds composables by `testTag`, text, or semantics role; asserts state | ✅ `libs.compose.ui.test` |
| **UI Automator** | Selenium (system) | Black-box: drives the whole device screen regardless of app; can interact with system dialogs | ❌ not wired |
| **Appium** | Selenium (cross-platform) | Runs outside the device, speaks WebDriver protocol over ADB; supports Android + iOS from the same test suite | ❌ external |
| **Maestro** | Cypress / Playwright | YAML-based flow files, zero boilerplate; runs via `maestro test flow.yaml`; best for user-journey smoke tests | ❌ external |

### Recommendation

| Goal | Best tool |
|---|---|
| Fast unit-level UI tests in CI | **Compose UI Testing** (already set up) |
| Full user-journey automation | **Maestro** — lowest setup cost, most readable |
| Cross-platform (Android + iOS) | **Appium** |
| System dialogs (file picker, notifications) | **UI Automator** |

---

## 2. Maestro — the recommended addition

Maestro is the closest to "Selenium for mobile apps" in terms of developer experience.
A test looks like:

```yaml
# flows/evaluate_fit.yaml
appId: com.jobassistant
---
- launchApp
- tapOn: "Dashboard"
- tapOn:
    id: "speed_dial_fab"
- tapOn: "Evaluate Fit"
- tapOn: "Paste Text"
- inputText:
    text: "Senior Android Engineer — 5+ years Kotlin, Compose, Hilt required"
- tapOn: "Check Fit"
- assertVisible: "Fit Score"
- assertVisible: "Strengths"
```

Install: `curl -Ls "https://get.maestro.mobile.dev" | bash`
Run: `maestro test flows/`

---

## 3. Manual Test Plan

> Pre-condition: seed the app with synthetic data first (Profile → Data → "Seed Test Data").

---

### Flow 1 — Onboarding (first-time user)

**Setup:** Clear app data (`adb shell pm clear com.jobassistant`)

| Step | Action | Expected result |
|---|---|---|
| 1 | Launch app | Onboarding screen shown, step dot on step 1 |
| 2 | Enter name "Alex Johnson", tap Next | Step dot moves to step 2 |
| 3 | Enter career goal, tap Next | Step dot moves to step 3 |
| 4 | Tap "Choose PDF" → select a PDF file | "Resume loaded!" message shown |
| 5 | Tap Next | Step dot moves to step 4 |
| 6 | Tap "Get Started" | Dashboard shown, bottom nav visible |
| 7 | Restart app | Dashboard shown directly (onboarding not repeated) |

---

### Flow 2 — Add Job (tracking only)

| Step | Action | Expected result |
|---|---|---|
| 1 | Tap `+` FAB → "Track Job" | Add Job screen opens |
| 2 | Fill: Company = "Acme Corp", Role = "Android Lead" | Save Job button enabled |
| 3 | Tap "Save Job" | Navigates directly to Job Detail for Acme Corp |
| 4 | Navigate back to Dashboard | "Acme Corp" card visible in INTERESTED column with `?` ring |

---

### Flow 3 — Quick Evaluate (no save required)

| Step | Action | Expected result |
|---|---|---|
| 1 | Tap `+` FAB on Dashboard | FAB expands, scrim appears, two options visible |
| 2 | Tap "Evaluate Fit" | EvaluateJobScreen opens |
| 3 | Select "Paste Text" tab | Text field shown |
| 4 | Paste a job description (~200 words), tap "Check Fit" | Progress bar, then score ring animates |
| 5 | Expand "Strengths", "Weaknesses", "Missing Skills" | Each section shows bullet items |
| 6 | Fill "Company Name" = "TestCo", tap "Save & Track" | Navigates to Job Detail with score and job description pre-filled |
| 7 | Navigate back to Dashboard | TestCo card visible with score ring filled |

---

### Flow 4 — Evaluate Fit via URL

| Step | Action | Expected result |
|---|---|---|
| 1 | Open Evaluate Fit screen | Paste Text tab active |
| 2 | Tap "Paste URL" tab | URL field shown |
| 3 | Paste a job posting URL, tap "Check Fit" | Progress bar while fetching + analyzing |
| 4 | Score result shown | Same result screen as Flow 3 |
| 5 | Tap "Start Over" | Returns to input state, fields cleared |

---

### Flow 5 — Job Detail: Analyze Fit

| Step | Action | Expected result |
|---|---|---|
| 1 | From Dashboard, tap a job card with `?` ring | Job Detail opens, score ring shows `?` |
| 2 | Scroll to "Job Description" section | Paste Text tab visible |
| 3 | Paste a job description, tap "Analyze Fit" | LinearProgressIndicator, then ring animates to score |
| 4 | Tap "Analyze Fit" again | Score updates; Ring re-animates |
| 5 | Change status from dropdown | Status chip on card updates on Dashboard |
| 6 | Tap "Save Changes" | Snackbar "Changes saved" shown |

---

### Flow 6 — Dashboard: Kanban view

| Step | Action | Expected result |
|---|---|---|
| 1 | Open Dashboard, ensure Kanban mode active | INTERESTED → APPLIED → ... → ACCEPTED columns; Closed divider → REJECTED/WITHDRAWN/NO_RESPONSE |
| 2 | Scroll horizontally | All 10 columns accessible |
| 3 | Long-press a job card | Status change bottom sheet opens with all 10 statuses |
| 4 | Select "Interviewing" | Job card moves to INTERVIEWING column |
| 5 | Tap Hero Stats Strip "Total" chip | (visual verification only) |

---

### Flow 7 — Dashboard: List view

| Step | Action | Expected result |
|---|---|---|
| 1 | Tap list icon in TopAppBar | List view shown, all jobs sorted by date |
| 2 | Tap "Rejected" filter chip | Only rejected jobs shown |
| 3 | Tap filter chip again | Filter cleared, all jobs shown |
| 4 | Swipe a job card left | Job deleted, "Deleted …" snackbar shown |
| 5 | (Undo not wired — just verify deletion) | Job gone from list |

---

### Flow 8 — Insights tabs

| Step | Action | Expected result |
|---|---|---|
| 1 | Tap "Insights" in bottom nav | Career Profile tab active |
| 2 | Verify Career Profile tab | Avatar "A", resume word count, keywords as chips, AI Career Summary card visible |
| 3 | Tap "AI Coach" tab | Empty state OR identified gaps + lightbulb action cards |
| 4 | Tap "Refresh" button | Spinner, then insights generated (if API key set) |
| 5 | Tap "Applied Jobs" tab | 6 stat cards (Applied/Interviews/Offers/Rejected/Withdrawn/No Response) |
| 6 | Verify Funnel Row | Applied → Interviews → Offers arrow chain visible |
| 7 | Verify Top Companies | Monzo, Klarna, Spotify shown with counts |
| 8 | Tap Refresh button (top of any tab) | Re-generates insights regardless of which tab is active |

---

### Flow 9 — Profile: Theme switching

| Step | Action | Expected result |
|---|---|---|
| 1 | Open Profile screen | Appearance section shows 4 color circles (Green, Red, Blue, Purple) |
| 2 | Tap Red circle | Entire app re-themes to red immediately |
| 3 | Tap Purple circle | Entire app re-themes to purple, background tint changes |
| 4 | Restart app | Purple theme persists |

---

### Flow 10 — Profile: Resume upload + AI analysis

| Step | Action | Expected result |
|---|---|---|
| 1 | Open Profile → Resume section | "Upload PDF Resume" button shown |
| 2 | Tap button → select a PDF | "Extracting text…" spinner |
| 3 | Wait for extraction | "Resume loaded — N characters" file card shown |
| 4 | Wait for AI analysis | "Analyzing career intent…" spinner, then AI Career Summary updates |
| 5 | Navigate to Insights → Career Profile | Resume word count visible, keywords updated |

---

### Flow 11 — CSV Import

| Step | Action | Expected result |
|---|---|---|
| 1 | Open Profile → Data section | "Import from CSV" button visible |
| 2 | Tap it | CsvImportScreen opens: UploadFile icon + instructions |
| 3 | Tap "Choose CSV File" → select `sample_jobs.csv` | "Reading file…" spinner |
| 4 | Wait | "Mapping columns with AI…" spinner |
| 5 | Preview screen shown | Summary card: "Found 5 jobs across 5 rows" |
| 6 | Expand "Detected Column Mapping" | Columns mapped: Company→companyName, Role→roleTitle, etc. |
| 7 | Verify job list | 5 cards with StatusChip and relative dates |
| 8 | Tap "Import 5 Jobs" | LinearProgressIndicator, then Done screen |
| 9 | Done screen | "5 jobs added • 0 already existed" |
| 10 | Tap "View Dashboard" | New jobs visible across their respective status columns |

---

### Flow 12 — Seed Test Data (developer flow)

| Step | Action | Expected result |
|---|---|---|
| 1 | Open Profile → Data section | Red "Seed Test Data" button visible (debug only) |
| 2 | Tap it | Brief loading, then Snackbar "Test data loaded — 32 jobs seeded" |
| 3 | Navigate to Dashboard | 32 jobs visible across all 10 status columns |
| 4 | Open Insights → Applied Jobs | Stats: Applied ≥20, Interviews ~9, Offers 3, Rejected 5, Withdrawn 2, No Response 3 |
| 5 | Open Insights → Career Profile | "Alex Johnson" avatar, 9 keyword chips, salary "£80k – £120k" |

---

### Flow 13 — Edge cases

| Step | Action | Expected result |
|---|---|---|
| 1 | Add duplicate job (same company + role) | DuplicateJobDialog appears |
| 2 | Tap "Save Anyway" | New entry created |
| 3 | Evaluate Fit with no resume uploaded | Red "Upload your resume…" banner visible |
| 4 | Trigger rate limit (rapid API calls) | "Service busy…" inline error; Refresh button disabled for 60 seconds |
| 5 | Open a SAVED/INTERESTED job >30 days old | "Posting may be expired" chip shown |

---

## 4. Automated UI Test Coverage Plan (Compose + Espresso)

The project already has `androidTest/` instrumented tests. These cover the test cases above
at the screen level. Gaps to fill after synthetic data seeding is implemented:

| Test class | What to add |
|---|---|
| `DashboardScreenTest` | Verify SpeedDial expands; verify all 10 status columns visible after seed |
| `EvaluateJobScreenTest` | New file: paste flow → score ring visible; save → navigates to Job Detail |
| `CsvImportScreenTest` | New file: mock ViewModel states → verify each state renders correctly |
| `InsightsScreenTest` | Verify 6 stat cards visible; verify tab switching |
| `ProfileScreenTest` | Verify debug seed button visible; verify import CSV button navigates |

Run instrumented tests: `./gradlew connectedDebugAndroidTest`

---

## 5. Maestro smoke tests (to create in `flows/` directory)

| File | Journey covered |
|---|---|
| `flows/onboarding.yaml` | Full onboarding: name → goal → skip resume → get started |
| `flows/add_job.yaml` | SpeedDial → Track Job → fill form → save → job detail |
| `flows/quick_evaluate.yaml` | SpeedDial → Evaluate Fit → paste → score visible → save |
| `flows/csv_import.yaml` | Profile → Import CSV → pick file → preview → import → dashboard |
| `flows/status_change.yaml` | Long-press job → change status → verify column move |
| `flows/theme_switch.yaml` | Profile → tap Purple → verify background tint |

Install Maestro and run: `maestro test flows/`

---

## 6. Demo Recording

### Option A — Maestro with `--video` (recommended)

Maestro automatically records an `.mp4` of everything happening on-screen while it
executes a flow. No extra setup needed:

```bash
maestro test flows/full_demo.yaml --video
# Output: demo_recording.mp4 saved in the current directory
```

The video is a clean screen capture — no cursor, no IDE chrome, just the app running
on the device. Ideal for sharing with potential users.

**Tips for a polished demo video:**
- Use the **Samsung phone** (not the emulator) — real hardware looks more professional
- Seed test data first so the app has content from frame 1
- Use `--format junit` alongside `--video` if you also want a test report
- Slow down interactions with `sleepFor` between taps to make transitions readable:
  ```yaml
  - tapOn: "Evaluate Fit"
  - sleepFor: 800   # ms — lets the animation finish before the next action
  ```

---

### Option B — `adb screenrecord` (manual demo)

If you want to manually walk through the app yourself (rather than automating it),
the Android emulator and real device both support screen recording via adb:

```bash
# Start recording (max 3 minutes by default)
adb shell screenrecord /sdcard/demo.mp4

# Ctrl+C to stop, then pull the file
adb pull /sdcard/demo.mp4 ./demo.mp4
```

This works for both the emulator and the Samsung phone.
Resolution is full device resolution (1080p on the Samsung).

---

### Option C — Android Studio built-in recorder

In the Android Studio **emulator panel**, there is a camera icon in the toolbar.
Click it → Record → Stop → saves as `.mp4` or `.gif`.
Good for quick feature previews without any command line.

---

### Option D — Maestro Studio (interactive recorder)

```bash
maestro studio
```

Opens a browser-based IDE that:
- Shows a **live view** of the device screen
- Lets you **point-and-click** to build flows (no YAML writing needed)
- Records the session as you build it
- Exports the YAML flow + a video replay

Best for recording an interactive walkthrough that you can also replay
as an automated test later.

---

### Recommended demo flow for a polished recording

Create `flows/full_demo.yaml` that tells the complete story in ~2 minutes:

```yaml
appId: com.jobassistant
---
# 1. Open app — show dashboard with real data
- launchApp
- sleepFor: 1500

# 2. Glance at the Kanban board
- scrollRight   # reveal more columns
- sleepFor: 1000
- scrollLeft

# 3. Quick Evaluate — the headline feature
- tapOn:
    description: "Add"       # main FAB
- sleepFor: 600
- tapOn: "Evaluate Fit"
- sleepFor: 800
- inputText:
    text: "Senior Android Engineer at Acme Corp. We require 5+ years Kotlin and Compose. Experience with Hilt, Room, and REST APIs essential."
- tapOn: "Check Fit"
- sleepFor: 3000             # wait for AI response
- tapOn: "Strengths"
- sleepFor: 1000
- tapOn: "Save & Track"
- sleepFor: 1500

# 4. Job Detail — show score ring and job description
- scrollDown
- sleepFor: 1000
- scrollUp
- tapOn: "Back"

# 5. Insights — AI Coach tab
- tapOn: "Insights"
- sleepFor: 800
- tapOn: "AI Coach"
- sleepFor: 1000
- tapOn: "Applied Jobs"
- sleepFor: 1000

# 6. Theme switch — visual wow factor
- tapOn: "Profile"
- sleepFor: 800
- tapOn:
    description: "Purple selected"
- sleepFor: 1200
```

Run: `maestro test flows/full_demo.yaml --video`

---

### Output quality

| Method | Resolution | Audio | Shareable link | Best for |
|---|---|---|---|---|
| `maestro --video` | Device resolution | ❌ | ❌ (local file) | Demo clips, investor decks |
| `adb screenrecord` | Device resolution | ❌ | ❌ (local file) | Manual walkthroughs |
| Maestro Cloud | Device resolution | ❌ | ✅ public URL | Sharing with remote users |
| Android Studio recorder | Device resolution | ❌ | ❌ | Quick GIFs |

**Maestro Cloud** (free tier available at `maestro.mobile.dev`) uploads your flow,
runs it on a cloud device, and gives you a shareable URL with the video — no need to
send `.mp4` files.

```bash
maestro cloud --apiKey <key> flows/full_demo.yaml
# Returns: https://cloud.mobile.dev/run/abc123
```

---

## 8. Summary

| Test type | Tool | When to run |
|---|---|---|
| Unit tests (pure JVM) | JUnit + MockK + Turbine | Every commit — `./gradlew testDebugUnitTest` |
| Instrumented UI tests | Espresso + Compose | Before PRs — `./gradlew connectedDebugAndroidTest` |
| User-journey smoke tests | Maestro | Before releases — `maestro test flows/` |
| Manual exploratory | This document | After each major feature |
