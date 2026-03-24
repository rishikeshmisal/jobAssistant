package com.jobassistant.domain.model

enum class ApplicationStatus {
    /** Found the role, saved to apply later. Has not yet submitted anything. */
    INTERESTED,

    /** Application submitted — CV sent, form filled, or "Easy Apply" clicked. */
    APPLIED,

    /** Initial contact from the company — HR screen, recruiter call, or automated pre-screen. */
    SCREENING,

    /** Active interview rounds — phone, video, or on-site technical/behavioural interviews. */
    INTERVIEWING,

    /** Take-home task, coding challenge, or technical/psychometric assessment. */
    ASSESSMENT,

    /** Formal written offer received but not yet accepted or declined. */
    OFFER,

    /** Offer accepted — role secured. Terminal success state. */
    ACCEPTED,

    /** Not progressing — rejected at any stage by the company. Terminal failure state. */
    REJECTED,

    /** Application withdrawn by the user before a decision was made. Terminal state. */
    WITHDRAWN,

    /** No reply received after applying. Different from REJECTED — company went silent. */
    NO_RESPONSE
}

fun ApplicationStatus.displayName(): String = when (this) {
    ApplicationStatus.INTERESTED   -> "Interested"
    ApplicationStatus.APPLIED      -> "Applied"
    ApplicationStatus.SCREENING    -> "Screening"
    ApplicationStatus.INTERVIEWING -> "Interviewing"
    ApplicationStatus.ASSESSMENT   -> "Assessment"
    ApplicationStatus.OFFER        -> "Offer Received"
    ApplicationStatus.ACCEPTED     -> "Accepted"
    ApplicationStatus.REJECTED     -> "Rejected"
    ApplicationStatus.WITHDRAWN    -> "Withdrawn"
    ApplicationStatus.NO_RESPONSE  -> "No Response"
}

val ACTIVE_PIPELINE = listOf(
    ApplicationStatus.INTERESTED,
    ApplicationStatus.APPLIED,
    ApplicationStatus.SCREENING,
    ApplicationStatus.INTERVIEWING,
    ApplicationStatus.ASSESSMENT,
    ApplicationStatus.OFFER,
    ApplicationStatus.ACCEPTED
)

val TERMINAL_STATUSES = listOf(
    ApplicationStatus.REJECTED,
    ApplicationStatus.WITHDRAWN,
    ApplicationStatus.NO_RESPONSE
)

val ALL_STATUSES = ACTIVE_PIPELINE + TERMINAL_STATUSES
