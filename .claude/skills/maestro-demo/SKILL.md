---
name: maestro-demo
description: >
  Canonical patterns for writing Maestro YAML flows for the Job Assistant Android app.
  Use this skill whenever writing, modifying, or debugging any Maestro flow file in the
  flows/ directory. Covers appId, element-finding strategy (testTag vs text), timing
  conventions, SpeedDial interaction, AI call wait times, and recording commands.
  Always read this skill before touching any .yaml file in flows/.
---

# Maestro Demo Flows — Job Assistant Reference

## App identity

```yaml
appId: com.jobassistant
```

Install the debug APK before running any flow:
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Element-finding strategy

Maestro finds elements two ways: by **testTag** (semantic ID) or by **visible text**.
Always prefer testTag when it exists — it is immune to copy changes.

### ⚠️ testTag / `id:` selector NOTE (Maestro 2.3.0)\n\n> Compose `testTag` values are NOT resolved by `id:` in Maestro 2.3.0.\n> Use visible text or contentDescription for all elements.\n\n### Elements — use visible text or contentDescription

```
seed_test_data_button      Profile → Data section (debug only)
speed_dial_main_fab        Dashboard main FAB (+ / ×)
evaluate_fit_fab           Dashboard SpeedDial mini-FAB "Evaluate Fit"
track_job_fab              Dashboard SpeedDial mini-FAB "Track Job"
name_field                 Onboarding step 1 name input
career_goal_field          Onboarding step 2 career goal
onboarding_upload_button   Onboarding step 3 PDF picker
skip_resume_button         Onboarding step 3 skip link
company_name_field         Add Job screen
role_title_field           Add Job screen
location_field             Add Job screen
salary_range_field         Add Job screen
save_job_button            Add Job screen primary action
upload_resume_button       Profile → Resume section
profile_name_field         Profile → Career Details
save_profile_button        Profile → Career Details
export_data_button         Profile → Data section
import_csv_button          Profile → Data section
choose_csv_button          CSV Import idle state
confirm_import_button      CSV Import preview bottom bar
cancel_import_button       CSV Import preview bottom bar
view_dashboard_button      CSV Import done state
check_fit_button           Evaluate Job → Paste Text tab
save_and_track_button      Evaluate Job → Result state
start_over_button          Evaluate Job → Result state
analyze_fit_button         Job Detail → Job Description section
```

Usage in YAML:
```yaml
- tapOn:
    id: "save_job_button"
```

### Elements without testTags — use visible text

```
"Dashboard"                Bottom navigation tab
"Profile"                  Bottom navigation tab
"Insights"                 Bottom navigation tab
"Career Profile"           Insights tab
"AI Coach"                 Insights tab
"Applied Jobs"             Insights tab
"Refresh"                  Insights refresh button (any tab)
"Paste Text"               Tab label (Evaluate screen + Job Detail)
"Paste URL"                Tab label (Evaluate screen + Job Detail)
                           NOTE: "Screenshot" tab was removed from Job Detail in Phase 16
"Check Fit"                Evaluate Job analyze button (URL tab)
"Save & Track"             Evaluate Job save button
"Start Over"               Evaluate Job reset
"Analyze Fit"              Job Detail — shown when job has NO existing score
"Refresh Score"            Job Detail — shown when job already has a fit score
"Save Changes"             Job Detail Details card save button
"Fit Score"                Assertion target after analysis (visible in State C score card)
"No fit score yet"         Assertion target when no score exists (State A / State B card)
"Strengths"                Expandable section header
"Weaknesses"               Expandable section header
"Missing Skills"           Expandable section header
"Interested" / "Applied" / "Screening" / "Interviewing" /
"Assessment" / "Offer Received" / "Accepted" /
"Rejected" / "Withdrawn" / "No Response"  ← Status FilterChip labels (horizontal scroll row)
                           Tap directly by text — no dropdown overlay needed
"Delete"                   Job Detail overflow menu item (tap ⋮ first, then "Delete")
```

Usage in YAML:
```yaml
- tapOn: "AI Coach"
- assertVisible: "Fit Score"
```

---

## Sleep / delay syntax (Maestro 2.3.0)

> **`sleepFor` does NOT exist in Maestro 2.x.** Use `evalScript: sleep(N)` instead.

```yaml
- evalScript: sleep(800)    # ✅ correct
- sleepFor: 800             # ❌ Invalid Command error
- delay: 800                # ❌ Invalid Command error
- wait: 800                 # ❌ Invalid Command error
```

## Timing conventions

These values are calibrated for the Samsung Galaxy (real device).
Add 20–30% if using the emulator.

```yaml
# Screen transition (Navigation pop/push with fade animation)
- evalScript: sleep(800)

# Short animation (FAB expand, chip tap, status change)
- evalScript: sleep(600)

# Score ring animation (animates for 800ms)
- evalScript: sleep(1500)

# Seed data (Room batch insert, ~32 jobs)
- evalScript: sleep(2500)

# Gemini API call — evaluate_fit (fast model)
- evalScript: sleep(4000)

# Gemini API call — mapCsvColumns (can be slower on first call)
- evalScript: sleep(6000)

# Gemini API call — generateInsights
- evalScript: sleep(5000)
```

---

## SpeedDial FAB pattern

The Dashboard SpeedDial requires a two-step interaction: tap the main FAB to expand,
wait for animation, then tap the desired option.

```yaml
- tapOn: "Add"
- evalScript: sleep(600)
- tapOn: "Evaluate Fit"    # or "Track Job"
- evalScript: sleep(800)
```

> **Never** tap `"Evaluate Fit"` directly without first tapping `"Add"` to expand the
> SpeedDial — the mini-FABs are hidden when collapsed.

## Status chip interaction pattern (Phase 16)

Job Detail status is now a horizontal FilterChip row — no dropdown.
Tap the desired status label directly:

```yaml
- tapOn: "Applied"
- evalScript: sleep(600)
```

Statuses further right (Assessment, Offer Received, Accepted, Rejected, Withdrawn,
No Response) may require a swipe before they are tappable:

```yaml
- swipe:
    direction: LEFT    # swipe left to reveal chips further right
- evalScript: sleep(500)
- tapOn: "Accepted"
- evalScript: sleep(600)
```

## Delete pattern (Phase 16)

Delete moved from BottomAppBar to the TopAppBar overflow menu:

```yaml
- tapOn: "More options"       # taps the ⋮ icon
- evalScript: sleep(400)
- tapOn: "Delete"
- evalScript: sleep(400)
- tapOn: "Delete"             # confirm in AlertDialog
- evalScript: sleep(800)
```

---

## Asserting after AI calls

After any Gemini call, assert visible content before proceeding:

```yaml
# After evaluate_fit
- assertVisible: "Fit Score"

# After generateInsights
- assertVisible: "AI Coach"

# After CSV mapping
- assertVisible: "Detected Column Mapping"
```

---

## Scrolling patterns

```yaml
# Swipe Kanban/chip row horizontally (reveal more columns or chips)
- swipe:
    direction: LEFT    # swipe left = scroll content right (reveals more to the right)

- swipe:
    direction: RIGHT   # swipe right = scroll content left (go back)

# Scroll a vertical list
- scrollDown
- scrollUp
```

> Use `swipe` (not `scroll`) for horizontal navigation. `scroll` alone scrolls the
> current view vertically. `scroll: direction:` is NOT valid syntax in Maestro 2.x.

---

## Multi-line text input

For job descriptions and resume text, use `inputText` with a string under 200 words
(longer inputs slow the flow without adding visual value):

```yaml
- tapOn: "Paste job description"
- inputText:
    text: "Senior Android Engineer at Acme Corp. We are looking for 5+ years of Kotlin
           and Jetpack Compose experience. Hilt, Room, and REST API expertise essential.
           Experience leading small teams and conducting code reviews preferred."
```

---

## Flow file conventions

- One scene per file (`01_onboarding.yaml`, `02_add_job.yaml`, etc.)
- `full_demo.yaml` imports all scenes via `runFlow:`
- `00_setup.yaml` seeds data — run separately before recording, **not** included
  in `full_demo.yaml`
- Each file starts with `appId: com.jobassistant`

```yaml
# Scene template
appId: com.jobassistant
---
# Description of what this scene shows
- evalScript: sleep(500)   # brief pause before scene starts
# ... actions
```

---

## Run and record commands

```bash
# Run a single flow (no video)
maestro test flows/01_onboarding.yaml

# Run the full demo with video
maestro test flows/full_demo.yaml --video
# Saves: ./maestro-recording.mp4

# Run all flows
maestro test flows/

# Upload to Maestro Cloud (shareable link)
maestro cloud --apiKey <YOUR_KEY> flows/full_demo.yaml

# Interactive flow builder (live device preview)
maestro studio
```

---

## Common errors and fixes

| Error | Cause | Fix |
|---|---|---|
| `Element not found: id="speed_dial_main_fab"` | testTag missing | Add `Modifier.testTag("speed_dial_main_fab")` to the FAB |
| `Element not found: "Evaluate Fit"` | SpeedDial not expanded | Tap `speed_dial_main_fab` first |
| Flow times out waiting for "Fit Score" | Gemini API slow | Increase `sleepFor` to 6000 |
| `AppNotRunningException` | App crashed or not installed | Re-install APK, check logcat |
| Text input goes to wrong field | Previous field still focused | Add `- tapOn:` to focus target field before `- inputText:` |
