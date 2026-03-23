Project Specification: AI Job Assistant (Android)

1. Overview

A high-performance Android application built with Kotlin and Jetpack Compose. The app serves as an intelligent career companion that manages job applications, analyzes job-to-resume fit using the Claude API, integrates directly with Gmail for automated tracking, and provides proactive career planning.

Data Privacy Architecture: All persistent data storage occurs strictly offline on the user's local encrypted device database. No custom backend, external databases, or third-party storage solutions are used. Data is only transmitted in-transit to Google (for Gmail API) and Anthropic (for Claude LLM inference).

2. Core Features

2.1 Profile & Intelligence Engine

Professional Vault: Store and manage PDF/Text resumes, target job titles, keywords, and long-term career goals entirely in the local database. Includes a Local PDF Parsing Engine to extract text from uploaded resumes before sending it to the LLM.

Intent Analysis: On first run, the app sends the extracted resume text and user interests to the Claude API to generate a structured "Future Goal Map," which is then saved locally.

Fit Scoring & Pros/Cons: Users can input job descriptions (via URL text scraping, manual paste, or screenshot). The app uses Claude to score the job against the user's profile (1-100) and provides a structured "Pros/Cons/Missing Skills" analysis.

2.2 Gmail Integration (Direct Device-to-Provider)

Direct Email Fetching: OAuth2 integration with Gmail API, utilizing a local WorkManager for background polling. The app communicates directly with Google's servers to fetch emails, with absolutely no intermediary backend.

Processing Pipeline: Downloaded emails are scanned using local heuristics (regex/sender matching) to save on API costs. Only relevant emails are sent to the Claude API to extract context. The resulting parsed data is saved exclusively to the local device.

Status Automation & Extraction:

Alerts: Identify job alerts from boards (LinkedIn, Indeed) and trigger high-priority local notifications.

Applied: Detect "Thank you for applying" emails, use Claude to extract company/role, and auto-create entries in the local "Applied" database.

Rejections: Detect "Rejection" context and auto-update the specific job's status locally.

Interviews: Detect interview invites, extract datetime/links, and auto-prompt the user to add it to the native Android Calendar.

2.3 Job Management Dashboard

Kanban/List View: Interactive UI tracking jobs through stages: Saved, Applied, Interviewing, Offered, Rejected.

Complete Offline Mode: Full read/write functionality at all times. All data is written to a local encrypted database.

Screenshot Processing: On-device OCR (Google ML Kit Vision) to extract text from screenshots, which is then passed to the Claude API for JobApplication extraction.

2.4 Career Insights ("What have we learnt" Tab)

Gap Analysis: An analytical dashboard that aggregates the user's application history (ratio of applied vs. interviews vs. rejections).

LLM Retrospective: Periodically sends the user's profile alongside their anonymized job tracking history to Claude to generate an active "Lessons Learnt" report.

Actionable Feedback: Identifies missing skills (e.g., "You are consistently rejected for Senior roles mentioning AWS, consider adding this to your learning path"), suggests resume tweaks, and recommends pivoting strategies based on real-world market feedback.

2.5 Intuitive Dynamic Theming

Color Theme Selector: The UI features an intuitive, accessible design with a persistent theme selector at the top of the app allowing the user to pick between "Green, Red, Blue, Yellow".

Dynamic Palette Application: Once a color is selected, it immediately becomes the app's primary color. Jetpack Compose Material 3 theming dynamically generates all corresponding shades and tonal variants, automatically applying them to buttons, backgrounds, cards, and text across the entire application for a cohesive look.

3. Technical Stack

Language: Kotlin

UI Framework: Jetpack Compose (Material Design 3 with dynamic programmatic color generation).

Architecture: Clean Architecture with MVVM (Model-View-ViewModel).

Concurrency & State: Kotlin Coroutines and StateFlow.

Local Database: Room DB secured with SQLCipher for 256-bit AES encrypted local storage.

Networking: Retrofit + OkHttp (Restricted to official Gmail API and Anthropic API endpoints).

AI Integration: Claude API (Anthropic REST API) utilizing structured JSON output (tools or system prompts).

Authentication: Google Sign-In (Credential Manager API) for direct OAuth tokens.

Background Tasks: WorkManager (for periodic Gmail polling and local database cleanup).

PDF Processing: Tomcat PDFBox-Android or PdfiumAndroid for local text extraction.

OCR: Google ML Kit Vision (On-Device Text Recognition module) for pre-processing screenshots before LLM analysis.

4. API & Integration Details

4.1 Claude API Implementation

Model: claude-3-5-sonnet (Optimal balance of reasoning and cost).

System Prompts: Strict system prompts enforcing JSON responses to avoid brittle string parsing in Kotlin.

Primary Endpoints/Tools:

analyze_intent(resumeText, userInterests) -> Returns CareerProfile JSON.

evaluate_fit(resumeText, jobDescription) -> Returns FitAnalysis JSON (score, pros, cons, missing_skills).

parse_email_context(emailBody, emailSubject) -> Returns EmailAction JSON (action_type: REJECT, APPLY, INTERVIEW, target_company, date).

generate_career_insights(profileText, jobHistorySummary) -> Returns CareerInsights JSON (identified_gaps, recommended_actions, market_feedback_summary).

4.2 Gmail API Scopes

https://www.googleapis.com/auth/gmail.readonly: To read and parse job-related emails directly to the device.

Note: Since we are strictly avoiding backend servers, we rely on Android's WorkManager for periodic polling rather than server-side webhooks/PubSub.

5. Data Models

JobApplication (Encrypted Room Entity)

id: UUID (Primary Key)

companyName: String

roleTitle: String

jobUrl: String?

status: Enum (SAVED, APPLIED, INTERVIEWING, OFFERED, REJECTED)

fitScore: Int?

location: String?

salaryRange: String?

appliedDate: Long?

interviewDate: Long?

notes: String

linkedEmailThreadIds: List<String>

UserProfile (Encrypted SharedPreferences / DataStore)

userId: String

fullName: String

resumeText: String (Extracted locally via PDF)

keywords: List<String>

careerGoal: String

targetSalaryRange: Pair<Int, Int>

selectedTheme: String (e.g., "GREEN", "RED", "BLUE", "YELLOW" to persist UI state)

CareerInsights (Encrypted Room Entity)

id: UUID (Primary Key)

generatedDate: Long

identifiedGaps: List<String>

recommendedActions: List<String>

summaryAnalysis: String

6. Execution Plan for Claude CLI

Phase 1: Project & Security Setup: Initialize Android Studio project, configure Hilt, setup EncryptedSharedPreferences, and Room DB with SQLCipher.

Phase 2: Data & Domain Layer: Create Entities, DAOs, and Repositories (including the new CareerInsights entity). Implement local PDF text extraction.

Phase 3: AI Service Layer: Build the Retrofit client for the Claude API. Design strict JSON-enforcing system prompts and parse responses into Kotlin Data Classes (including the generate_career_insights prompt).

Phase 4: Gmail Direct Sync: Implement Google Sign-In. Set up local pre-filtering (Regex/Keywords) to minimize API costs, and integrate direct Gmail API polling via WorkManager.

Phase 5: Job Processing Logic: Wire up the logic that takes a new email -> Pre-filters -> Sends to Claude API -> Updates Encrypted Room DB -> Triggers local Notification/Calendar Intent.

Phase 6: UI / Jetpack Compose: Build the dynamic Material 3 theme engine. Implement the top-level Color Theme Selector. Build the Kanban Dashboard, Job Detail screen, Profile Editor, and the "What have we learnt" Insights Tab.

Phase 7: Vision & OCR: Integrate On-Device ML Kit to extract text from screenshots, then pass the clean text to Claude for JobApplication extraction.

7. Security, Cost & Privacy

Strict Local Storage: All app data, job histories, and user profiles are stored only in the local SQLCipher database. No external database (like Firebase Firestore, Supabase, or custom SQL) will be implemented.

Data in Transit: Data is securely transmitted via HTTPS strictly to Anthropic's API for stateless inference and Google's API for email fetching.

Cost Mitigation: To prevent excessive Claude API billing, the app uses a robust local Regex/Keyword pre-filtering engine so only highly probable job-related emails are actually sent to the LLM. Insight generation is cached locally and only regenerated on demand or periodically.