# Execute Commands

Copy and paste the exact command for the phase you want to run.

---

## Phase 1 — Prerequisites & Project Scaffold
```
Read .claude/skills/android-hilt-patterns/SKILL.md and .claude/skills/room-sqlcipher-migrations/SKILL.md first. Then read execution.md and implement Phase 1 in full. Follow every step in sections 1.1–1.15 exactly. After all files are written, write the unit tests listed in the "Phase 1 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification, and confirm all Phase 1 MVP Checkpoints are green before stopping.
```

## Phase 2 — Data & Domain Layer
```
Read .claude/skills/room-sqlcipher-migrations/SKILL.md and .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md and implement Phase 2 in full. Follow every step in sections 2.1–2.12 exactly. After all files are written, write the unit tests and instrumented tests listed in the "Phase 2 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 2 MVP Checkpoints are green before stopping.
```

## Phase 3 — Navigation Shell + Dynamic Theming
```
Read .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md and implement Phase 3 in full. Follow every step in sections 3.1–3.8 exactly. After all files are written, write the unit tests and Compose UI tests listed in the "Phase 3 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 3 MVP Checkpoints are green before stopping.
```

## Phase 4 — AI Service Layer
```
Read .claude/skills/claude-api-tool-use/SKILL.md and .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md and implement Phase 4 in full. Follow every step in sections 4.1–4.7 exactly. After all files are written, write the unit tests and Compose UI tests listed in the "Phase 4 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification, and confirm all Phase 4 MVP Checkpoints are green before stopping.
```

## Phase 5 — Profile + Full Job Input
```
Read .claude/skills/claude-api-tool-use/SKILL.md and .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md and implement Phase 5 in full. Follow every step in sections 5.1–5.6 exactly. After all files are written, write the unit tests and Compose UI tests listed in the "Phase 5 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 5 MVP Checkpoints are green before stopping.
```

## Phase 6 — Full Dashboard UI
```
Read .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md and implement Phase 6 in full. Follow every step in sections 6.1–6.8 exactly. After all files are written, write the unit tests and Compose UI tests listed in the "Phase 6 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 6 MVP Checkpoints are green before stopping.
```

## Phase 7 — Gmail Integration
```
Read .claude/skills/android-hilt-patterns/SKILL.md and .claude/skills/claude-api-tool-use/SKILL.md first. Then read execution.md and implement Phase 7 in full. Follow every step in sections 7.1–7.8 exactly. After all files are written, write the unit tests and instrumented tests listed in the "Phase 7 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 7 MVP Checkpoints are green before stopping.
```

## Phase 8 — OCR + Screenshot Job Input
```
Read .claude/skills/claude-api-tool-use/SKILL.md and .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md and implement Phase 8 in full. Follow every step in sections 8.1–8.3 exactly. After all files are written, write the unit tests and Compose UI tests listed in the "Phase 8 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 8 MVP Checkpoints are green before stopping.
```

## Phase 9 — Polish & Security Hardening
```
Read .claude/skills/claude-api-tool-use/SKILL.md, .claude/skills/android-hilt-patterns/SKILL.md, and .claude/skills/room-sqlcipher-migrations/SKILL.md first. Then read execution.md and implement Phase 9 in full. Follow every step in sections 9.1–9.4 exactly. After all files are written, write the unit tests listed in the "Phase 9 Testing Requirements" section, run ./gradlew assembleRelease, ./gradlew testReleaseUnitTest jacocoCoverageVerification, and ./gradlew connectedAndroidTest, then generate the coverage report with ./gradlew jacocoTestReport, and confirm all Phase 9 Final Verification items are green before stopping.
```

## Phase 10 — UI/UX Overhaul
```
Read .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md Phase 10 in full (sections 10.1–10.12) before writing a single line of code. Implement each section in order:

10.1 — Enable enableEdgeToEdge() in MainActivity, remove the ThemeSelector wrapper column, pass window insets through Scaffold.
10.2 — Create ui/components/CompanyAvatar.kt, FitScoreRing.kt, RelativeTimeText.kt, SectionHeader.kt, and StatusChip.kt exactly as specified. Write unit tests for RelativeTimeText, FitScoreRing sweep calculation, StatusChip colors, and CompanyAvatar letter extraction before moving on.
10.3 — Remove ThemeSelector from MainActivity layout. Add the Appearance section (4 color-circle picker) to ProfileScreen. Add setTheme() to ProfileViewModel. Verify theme still persists across restarts.
10.4 — Add HeroStatsStrip to DashboardScreen below the TopAppBar.
10.5 — Update JobCard and ListJobRow to use CompanyAvatar + StatusChip + RelativeTimeText + FitScoreRing.
10.6 — Upgrade StatusChangeSheet to use ListItem rows with current-status checkmark.
10.7 — Update JobDetailScreen: replace score display with FitScoreRing(96dp), make pros/cons/missing-skills sections collapsible with AnimatedVisibility, add BottomAppBar with Re-analyze and Delete actions.
10.8 — Update AddJobScreen: rounded tab indicator, 4000-char counter on job description field, AnimatedVisibility score reveal, LinearProgressIndicator loading state.
10.9 — Update ProfileScreen: wrap each section in a Card with SectionHeader, add user avatar + name at top, add resume file card.
10.10 — Update InsightsScreen: add icons to StatCards, add FunnelRow, upgrade recommended actions to individual tinted cards.
10.11 — Update OnboardingScreen: add step dot indicator with animated pill, add step illustration icons, add back button on steps 2+.
10.12 — Add fade transitions to NavHost, animateItemPlacement() on Kanban LazyColumns.

After all files are written, write the Compose UI tests listed in the "Phase 10 Testing Requirements" section, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 10 MVP Checkpoints are green before stopping. Do not introduce any regressions — all Phase 1–9 MVP checkpoints must still pass.
```
