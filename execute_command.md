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

## Phase 11 — Insights Screen: Tabbed Layout
```
Read .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md Phase 11 in full (sections 11.1–11.6) before writing a single line of code. Implement each section in order:

11.1 — Add userProfile: UserProfile = UserProfile() to InsightsUiState.
11.2 — Update InsightsViewModel.observeData() to use a 3-way combine with userProfileDataStore.userProfileFlow, emitting userProfile into uiState.
11.3 — Rewrite InsightsScreen.kt: replace the single scrollable Column with a PrimaryTabRow (Career Profile / New Job / Applied Jobs) + when(selectedTab) content switcher.
11.4 — Implement CareerProfileTab: user header (avatar + name), resume summary card (word count + 400-char preview), career interests card (goal text + keyword chips + salary), AI career summary card.
11.5 — Implement NewJobTab: empty state with Generate button when no insights; identified gaps chips, lightbulb action cards, market feedback card, refresh button with loading indicator when insights exist.
11.6 — Implement AppliedJobsTab: empty state when totalApplied==0; StatsCardsRow + FunnelRow + RateSection + top companies card when data exists.

After all files are written, write the InsightsViewModelTabTest unit test, run ./gradlew testDebugUnitTest jacocoCoverageVerification, then build and install with ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk, and confirm all Phase 11 MVP Checkpoints are green before stopping.
```

## Phase 12 — Decouple "Add Job" from "Analyze Fit"
```
Read .claude/skills/android-hilt-patterns/SKILL.md and .claude/skills/room-sqlcipher-migrations/SKILL.md first. Then read execution.md Phase 12 in full (sections 12.1–12.5) before writing a single line of code. Implement each section in order:

12.1 — Simplify AddJobScreen to a 4-field tracking form (Company, Role, Location, Salary). Remove all AI/analysis content. Change AddJobViewModel states to Idle/Saving/Saved(jobId)/Duplicate/Error. On Saved, navigate to the new job's JobDetailScreen. Keep DuplicateJobDialog.
12.2 — Update FitScoreRing to show "?" instead of "N/A" when score is null.
12.3 — Add a "Job Description" section to JobDetailScreen with a TabRow (Paste Text / Paste URL / Screenshot), the 4000-char counter, and an "Analyze Fit" button. Move analyzeFromPaste, analyzeFromUrl, analyzeFromScreenshot into JobDetailViewModel. Remove the "Re-analyze Fit" button from BottomAppBar (analysis is now in the description section). Auto-save the fit score to Room on every successful analysis.
12.4 — Add jobDescription: String = "" to JobApplication domain model and JobApplicationEntity. Write MIGRATION_1_2 (ALTER TABLE ADD COLUMN jobDescription TEXT NOT NULL DEFAULT ''). Bump @Database version to 2. Register migration in DatabaseModule. Update mappers. Use the room-sqlcipher-migrations skill for correct migration wiring.
12.5 — Remove InputMode enum and OcrProcessor/FetchUrlUseCase injections from AddJobViewModel. Delete obsolete OCR-path test cases in AddJobViewModelOcrTest and rewrite them targeting JobDetailViewModel.

After all files are written, write the unit tests listed in Phase 12 Testing Requirements, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, and confirm all Phase 12 MVP Checkpoints are green before stopping. Do not introduce any regressions — all Phase 1–11 checkpoints must still pass.
```

## Phase 13 — CSV Import with AI Column Mapping
```
Read .claude/skills/android-hilt-patterns/SKILL.md and .claude/skills/claude-api-tools-use/SKILL.md first. Then read execution.md Phase 13 in full (sections 13.1–13.8) before writing a single line of code. Implement each section in order:

13.1 — Create domain/model/CsvColumnMapping.kt and domain/model/CsvImportPreview.kt exactly as specified.
13.2 — Create util/CsvParser.kt: a pure Kotlin object that parses CSV text into ParsedCsv(headers, rows). Handle quoted fields, escaped quotes, trailing newlines. No Android dependencies — must be JVM-testable. Write CsvParserTest immediately after.
13.3 — Add mapCsvColumns(headers, sampleRows) to ClaudeRepository interface. Implement in GeminiRepository using the exact prompt template in section 13.3. Parse the JSON response into CsvColumnMapping. Throw ClaudeParseException if column_mappings or status_mappings keys are missing.
13.4 — Create domain/usecase/ImportCsvUseCase.kt with preview() and commit() methods. preview() orchestrates CsvParser → mapCsvColumns → row mapping → CsvImportPreview. commit() batch-inserts via SaveJobApplicationUseCase. Use all 7 date fallback patterns. Write ImportCsvUseCaseTest immediately after.
13.5 — Create ui/screens/csv/CsvImportViewModel.kt with the sealed CsvImportUiState and the three methods (onCsvPicked, confirmImport, reset). Write CsvImportViewModelTest immediately after.
13.6 — Create ui/screens/csv/CsvImportScreen.kt with all 6 state renderings (Idle, ReadingFile, MappingColumns, Preview, Importing, Done, Error) exactly as described. Use CompanyAvatar, StatusChip, RelativeTimeText from ui/components.
13.7 — Add Screen.CsvImport to Screen.kt. Add the composable route to AppNavigation.kt. Add "Import from CSV" OutlinedButton to ProfileScreen's Data section card; pass navController or use a callback.

After all files are written, run ./gradlew testDebugUnitTest jacocoCoverageVerification, then build and install with ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk, and confirm all Phase 13 MVP Checkpoints are green before stopping. Do not introduce any regressions — all Phase 1–12 checkpoints must still pass.
```

## Phase 15 — Quick Evaluate: Frictionless Job Fit Scoring
```
Read .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md Phase 15 in full (sections 15.1–15.4) before writing a single line of code. Implement each section in order:

15.1 — Create ui/screens/evaluate/EvaluateJobViewModel.kt with the sealed EvaluateJobUiState (Idle, Analyzing, Result, Saved, Error) and five methods (analyzeFromPaste, analyzeFromUrl, analyzeFromScreenshot, saveJob, reset). Inject EvaluateFitUseCase, FetchUrlUseCase, OcrProcessor, UserProfileDataStore, SaveJobApplicationUseCase. Create ui/screens/evaluate/EvaluateJobScreen.kt with all five state renderings: Idle/input (TabRow with Paste/URL/Screenshot), Analyzing (LinearProgressIndicator), Result (FitScoreRing + expandable sections + save form), Saved (CheckCircle + navigation), Error (retry). Use FitScoreRing, CompanyAvatar components from ui/components.
15.2 — Create ui/components/SpeedDialFab.kt: a SpeedDial composable that expands into two labeled SmallFloatingActionButton rows with AnimatedVisibility (fadeIn+scaleIn). Add speedDialExpanded: Boolean state and onEvaluateFitClick: () -> Unit parameter to DashboardScreen. Replace the existing FloatingActionButton in DashboardScreen with SpeedDialFab.
15.3 — Add Screen.EvaluateJob to Screen.kt. Add Screen.EvaluateJob.route to screensWithoutBottomNav in AppNavigation.kt. Add composable route for EvaluateJobScreen. Add onEvaluateFitClick callback to the Dashboard composable in AppNavigation, wiring it to navController.navigate(Screen.EvaluateJob.route).
15.4 — Add no-resume banner to EvaluateJobScreen: collect userProfile from EvaluateJobViewModel; show errorContainer Card with warning icon when resumeText is blank.

After all files are written, write EvaluateJobViewModelTest (all 7 test cases listed in Phase 15 Testing Requirements), run ./gradlew testDebugUnitTest jacocoCoverageVerification, then build and install with ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk, and confirm all Phase 15 MVP Checkpoints are green before stopping. Do not introduce any regressions — all Phase 1–14 checkpoints must still pass.
```

## Phase 16 — Job Detail Screen Redesign
```
Read .claude/skills/android-hilt-patterns/SKILL.md and .claude/skills/room-sqlcipher-migrations/SKILL.md first. Then read execution.md Phase 16 in full (sections 16.1–16.10) before writing a single line of code. Implement each section in order:

16.1 — Remove the BottomAppBar entirely from JobDetailScreen's Scaffold. Move the delete action into a three-dot overflow IconButton in the TopAppBar actions slot using a DropdownMenu. The AlertDialog confirmation is unchanged. Remove all BottomAppBar imports.

16.2 — Redesign the header: replace the plain Column with a Row containing CompanyAvatar(size=48.dp) on the left and a Column on the right showing company name (titleLarge + Bold), role title (bodyLarge, onSurfaceVariant), and a metadata Row with location icon + location text and a salary divider + salary text (bodySmall, onSurfaceVariant) — only rendered when non-null/non-blank. Keep the "Posting may be expired" SuggestionChip below the metadata row.

16.3 — Replace the full-width StatusDropdown OutlinedButton with a horizontally scrollable LazyRow of FilterChips, one per status from ALL_STATUSES. The selected chip uses statusContainerColor() and statusLabelColor() from StatusChip.kt. Non-selected chips use default FilterChip surface colors. Remove the StatusDropdown composable entirely.

16.4 — Redesign the Fit Score card into three explicit states. State A (no score, no JD): compact ~80dp card with Analytics outline icon and prompt text "No fit score yet — paste the job description below to analyze". State B (no score, JD saved): same compact card, primary-tinted text "Tap Refresh Score to analyze". State C (score exists): existing ring layout plus a Refresh IconButton (Icons.Filled.Refresh, 20dp) in the top-right corner of the card, and an "Analyzed [date]" label using analysisDate from the job — tapping Refresh calls viewModel.analyzeFromPaste(jobDescription), disabled and shows a snackbar "Add a job description first" when jobDescription is blank. Inline AUTH/RATE_LIMIT error text stays inside State C.

16.5 — Change the three ExpandableSection initial states (pros, cons, missing skills) from expanded=false to expanded=true. No other changes to that composable.

16.6 — In JobDescriptionSection: remove the Screenshot tab — JOB_DESCRIPTION_TABS becomes listOf("Paste Text", "Paste URL") only. Pre-populate the Paste Text OutlinedTextField with the saved jobDescription value on first composition. Change the analyze button label dynamically: "Analyze Fit" when job.fitScore == null, "Refresh Score" when a score already exists. Add a "Saved" Text badge (labelSmall, primary color) next to the "Job Description & Score" card title when jobDescription.isNotBlank(). Remove all Screenshot-related imports, state, and the imagePickerLauncher from JobDetailScreen if it is no longer used elsewhere.

16.7 — Wrap all editable fields (Notes, Location, Salary Range, Applied Date, Interview Date) in a single Card with SectionHeader("Details"). Inside: Notes OutlinedTextField (minLines=2, maxLines=5), then Location + Salary in a Column(spacedBy 8dp), then a date Row with Applied and Interview side by side (weight 1f each) — EXCEPT when status is INTERVIEWING or ASSESSMENT, in which case Interview Date is rendered full-width above Applied Date with a CalendarMonth icon and primaryContainer background. Move the Save Changes Button inside this card directly below the dates. Remove the Save Changes button from the main column.

16.8 — Add analysisDate: Long? = null to JobApplication domain model and a matching nullable Long column to JobApplicationEntity. Update the mapper in both directions. Write MIGRATION_X_Y (ALTER TABLE job_applications ADD COLUMN analysisDate INTEGER) — check the current DB version and increment by 1. Register the migration in DatabaseModule. Set analysisDate = System.currentTimeMillis() in JobDetailViewModel when analyzeFromPaste or analyzeFromUrl completes successfully. Follow room-sqlcipher-migrations skill exactly.

16.9 — In LinkedEmailsSection replace the raw threadId string in each SuggestionChip label with "Email thread ${index + 1}". Set the raw threadId as the composable's contentDescription via Modifier.semantics.

16.10 — Keep company name in the TopAppBar title for navigation context, but reduce the in-content company name text style from headlineMedium to titleLarge so it no longer duplicates the AppBar at the same visual weight.

After all files are written, run ./gradlew testDebugUnitTest, then build and install with ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk, and confirm all Phase 16 MVP Checkpoints in execution.md are green before stopping. Do not introduce any regressions — all Phase 1–15 checkpoints must still pass.
```

## Phase 14 — Redesign ApplicationStatus Lifecycle
```
Read .claude/skills/room-sqlcipher-migrations/SKILL.md and .claude/skills/android-hilt-patterns/SKILL.md first. Then read execution.md Phase 14 in full (sections 14.1–14.7) before writing a single line of code. Implement each section in order:

14.1 — Replace ApplicationStatus.kt with the 10-value enum (INTERESTED, APPLIED, SCREENING, INTERVIEWING, ASSESSMENT, OFFER, ACCEPTED, REJECTED, WITHDRAWN, NO_RESPONSE). Add the displayName() extension function, and define ACTIVE_PIPELINE, TERMINAL_STATUSES, and ALL_STATUSES lists in the same file.
14.2 — Add MIGRATION_2_3 to AppDatabase.kt (UPDATE SAVED→INTERESTED, OFFERED→OFFER). Bump @Database version to 3. Register MIGRATION_1_2 and MIGRATION_2_3 in create(). Update JobApplicationMigrationTest to also verify MIGRATION_2_3.
14.3 — Update StatusChip.kt: replace the 5-value when block with all 10 statuses using the color pairs from the table in section 14.4. Update StatusChipColorTest.
14.4 — Update DashboardScreen.kt: replace STATUS_ORDER with ACTIVE_PIPELINE + TERMINAL_STATUSES. Add a "Closed" section divider before terminal Kanban columns. Update all local displayName() calls to use the extension.
14.5 — Update JobDetailScreen.kt: StatusDropdown lists ALL_STATUSES. Remove local displayName().
14.6 — Update InsightsViewModel.kt: redefine applied/interviews/offers/rejections stats per the new logic in section 14.5. Add withdrawn and noResponse fields to InsightsStats. Update InsightsScreen to show 6 stat cards.
14.7 — Update GmailSyncWorker.kt: map INTERVIEW action_type to ApplicationStatus.SCREENING. Update GeminiRepository.mapCsvColumns() prompt to list all 10 new enum names. Remove all remaining references to old enum values (SAVED, OFFERED) from tests and sources.

After all files are written, write ApplicationStatusTest, run ./gradlew testDebugUnitTest jacocoCoverageVerification and ./gradlew connectedDebugAndroidTest, then install and confirm all Phase 14 MVP Checkpoints are green. Do not introduce any regressions — all Phase 1–13 checkpoints must still pass.
```
