package com.jobassistant.service

enum class EmailCategory { APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT }

object EmailPreFilter {

    private val APPLIED_PATTERNS = listOf(
        Regex("thank you for apply", RegexOption.IGNORE_CASE),
        Regex("application received", RegexOption.IGNORE_CASE),
        Regex("we received your application", RegexOption.IGNORE_CASE),
        Regex("successfully submitted", RegexOption.IGNORE_CASE)
    )

    private val REJECTION_PATTERNS = listOf(
        Regex("not moving forward", RegexOption.IGNORE_CASE),
        Regex("other candidates", RegexOption.IGNORE_CASE),
        Regex("position.*filled", RegexOption.IGNORE_CASE),
        Regex("regret to inform", RegexOption.IGNORE_CASE),
        Regex("unfortunately.*not", RegexOption.IGNORE_CASE),
        Regex("not selected", RegexOption.IGNORE_CASE)
    )

    private val INTERVIEW_PATTERNS = listOf(
        Regex("interview", RegexOption.IGNORE_CASE),
        Regex("schedule.*call", RegexOption.IGNORE_CASE),
        Regex("meet with.*team", RegexOption.IGNORE_CASE),
        Regex("calendly\\.com", RegexOption.IGNORE_CASE),
        Regex("zoom\\.us", RegexOption.IGNORE_CASE),
        Regex("microsoft teams", RegexOption.IGNORE_CASE),
        Regex("google meet", RegexOption.IGNORE_CASE)
    )

    private val ALERT_SENDER_DOMAINS = listOf(
        "linkedin.com",
        "indeed.com",
        "glassdoor.com",
        "lever.co",
        "greenhouse.io"
    )

    fun isJobRelated(subject: String, body: String, senderEmail: String): Boolean {
        return classify(subject, body, senderEmail) != EmailCategory.IRRELEVANT
    }

    fun classify(subject: String, body: String, senderEmail: String): EmailCategory {
        val combined = "$subject $body"

        // Check sender domain for ALERT first
        if (ALERT_SENDER_DOMAINS.any { domain -> senderEmail.contains(domain, ignoreCase = true) }) {
            return EmailCategory.ALERT
        }

        if (REJECTION_PATTERNS.any { it.containsMatchIn(combined) }) {
            return EmailCategory.REJECTION
        }

        if (INTERVIEW_PATTERNS.any { it.containsMatchIn(combined) }) {
            return EmailCategory.INTERVIEW
        }

        if (APPLIED_PATTERNS.any { it.containsMatchIn(combined) }) {
            return EmailCategory.APPLIED
        }

        return EmailCategory.IRRELEVANT
    }
}
