package com.jobassistant.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailPreFilterTest {

    // ── APPLIED (5 samples) ──────────────────────────────────────────────────

    @Test
    fun `classify APPLIED - thank you for applying`() {
        assertEquals(
            EmailCategory.APPLIED,
            EmailPreFilter.classify(
                subject = "Thank you for applying to Acme Corp",
                body = "We received your application and will review it.",
                senderEmail = "careers@acme.com"
            )
        )
    }

    @Test
    fun `classify APPLIED - application received subject`() {
        assertEquals(
            EmailCategory.APPLIED,
            EmailPreFilter.classify(
                subject = "Application received - Senior Android Engineer",
                body = "Your application has been received.",
                senderEmail = "hr@startup.io"
            )
        )
    }

    @Test
    fun `classify APPLIED - we received your application in body`() {
        assertEquals(
            EmailCategory.APPLIED,
            EmailPreFilter.classify(
                subject = "Your application",
                body = "We received your application for the iOS Developer role. We will be in touch.",
                senderEmail = "noreply@company.com"
            )
        )
    }

    @Test
    fun `classify APPLIED - successfully submitted`() {
        assertEquals(
            EmailCategory.APPLIED,
            EmailPreFilter.classify(
                subject = "Application successfully submitted",
                body = "You have successfully submitted your application to Google.",
                senderEmail = "no-reply@google.com"
            )
        )
    }

    @Test
    fun `classify APPLIED - thank you for applying case insensitive`() {
        assertEquals(
            EmailCategory.APPLIED,
            EmailPreFilter.classify(
                subject = "THANK YOU FOR APPLY TO OUR COMPANY",
                body = "We will get back to you shortly.",
                senderEmail = "jobs@bigtech.com"
            )
        )
    }

    // ── REJECTION (5 samples) ────────────────────────────────────────────────

    @Test
    fun `classify REJECTION - not moving forward`() {
        assertEquals(
            EmailCategory.REJECTION,
            EmailPreFilter.classify(
                subject = "Update on your application",
                body = "We are not moving forward with your candidacy at this time.",
                senderEmail = "hr@corp.com"
            )
        )
    }

    @Test
    fun `classify REJECTION - other candidates`() {
        assertEquals(
            EmailCategory.REJECTION,
            EmailPreFilter.classify(
                subject = "Re: Senior Engineer Position",
                body = "After careful consideration, we have decided to pursue other candidates.",
                senderEmail = "recruiting@tech.com"
            )
        )
    }

    @Test
    fun `classify REJECTION - position filled`() {
        assertEquals(
            EmailCategory.REJECTION,
            EmailPreFilter.classify(
                subject = "Position Update",
                body = "The position has been filled. Thank you for your interest.",
                senderEmail = "careers@startup.com"
            )
        )
    }

    @Test
    fun `classify REJECTION - regret to inform`() {
        assertEquals(
            EmailCategory.REJECTION,
            EmailPreFilter.classify(
                subject = "Application Decision",
                body = "We regret to inform you that we will not be proceeding with your application.",
                senderEmail = "hr@enterprise.com"
            )
        )
    }

    @Test
    fun `classify REJECTION - unfortunately not`() {
        assertEquals(
            EmailCategory.REJECTION,
            EmailPreFilter.classify(
                subject = "Your application status",
                body = "Unfortunately, we will not be moving forward with your candidacy.",
                senderEmail = "jobs@bigco.com"
            )
        )
    }

    // ── INTERVIEW (5 samples) ────────────────────────────────────────────────

    @Test
    fun `classify INTERVIEW - interview invitation`() {
        assertEquals(
            EmailCategory.INTERVIEW,
            EmailPreFilter.classify(
                subject = "Interview Invitation - Software Engineer",
                body = "We would like to schedule an interview with you.",
                senderEmail = "recruiter@tech.com"
            )
        )
    }

    @Test
    fun `classify INTERVIEW - schedule a call`() {
        assertEquals(
            EmailCategory.INTERVIEW,
            EmailPreFilter.classify(
                subject = "Next steps",
                body = "We'd love to schedule a call to discuss the role further.",
                senderEmail = "talent@company.com"
            )
        )
    }

    @Test
    fun `classify INTERVIEW - meet with team`() {
        assertEquals(
            EmailCategory.INTERVIEW,
            EmailPreFilter.classify(
                subject = "Invitation to meet with our team",
                body = "We'd like you to meet with the engineering team.",
                senderEmail = "recruiting@corp.com"
            )
        )
    }

    @Test
    fun `classify INTERVIEW - calendly link`() {
        assertEquals(
            EmailCategory.INTERVIEW,
            EmailPreFilter.classify(
                subject = "Please book a time",
                body = "Use this link to book: https://calendly.com/recruiter/30min",
                senderEmail = "hr@startup.io"
            )
        )
    }

    @Test
    fun `classify INTERVIEW - zoom link`() {
        assertEquals(
            EmailCategory.INTERVIEW,
            EmailPreFilter.classify(
                subject = "Virtual interview details",
                body = "Join us at zoom.us/j/123456789 for your technical interview.",
                senderEmail = "recruiter@bigtech.com"
            )
        )
    }

    // ── ALERT (5 samples from job boards) ───────────────────────────────────

    @Test
    fun `classify ALERT - linkedin sender`() {
        assertEquals(
            EmailCategory.ALERT,
            EmailPreFilter.classify(
                subject = "New jobs matching your search",
                body = "5 new jobs match your criteria.",
                senderEmail = "jobs-noreply@linkedin.com"
            )
        )
    }

    @Test
    fun `classify ALERT - indeed sender`() {
        assertEquals(
            EmailCategory.ALERT,
            EmailPreFilter.classify(
                subject = "Jobs you might like",
                body = "Based on your profile, here are some recommendations.",
                senderEmail = "alert@indeed.com"
            )
        )
    }

    @Test
    fun `classify ALERT - glassdoor sender`() {
        assertEquals(
            EmailCategory.ALERT,
            EmailPreFilter.classify(
                subject = "New job alert from Glassdoor",
                body = "Here are the latest openings in your area.",
                senderEmail = "noreply@glassdoor.com"
            )
        )
    }

    @Test
    fun `classify ALERT - lever sender`() {
        assertEquals(
            EmailCategory.ALERT,
            EmailPreFilter.classify(
                subject = "Your application was forwarded",
                body = "A hiring manager viewed your application.",
                senderEmail = "notifications@lever.co"
            )
        )
    }

    @Test
    fun `classify ALERT - greenhouse sender`() {
        assertEquals(
            EmailCategory.ALERT,
            EmailPreFilter.classify(
                subject = "Application update",
                body = "Your application status has changed.",
                senderEmail = "do-not-reply@greenhouse.io"
            )
        )
    }

    // ── IRRELEVANT (5 samples) ───────────────────────────────────────────────

    @Test
    fun `classify IRRELEVANT - unsubscribe marketing email`() {
        assertEquals(
            EmailCategory.IRRELEVANT,
            EmailPreFilter.classify(
                subject = "50% off this weekend only!",
                body = "Click here to unsubscribe from our mailing list.",
                senderEmail = "promo@retail.com"
            )
        )
    }

    @Test
    fun `classify IRRELEVANT - newsletter`() {
        assertEquals(
            EmailCategory.IRRELEVANT,
            EmailPreFilter.classify(
                subject = "Your weekly newsletter",
                body = "Here are the top stories this week. Unsubscribe at any time.",
                senderEmail = "news@digest.com"
            )
        )
    }

    @Test
    fun `classify IRRELEVANT - bank statement`() {
        assertEquals(
            EmailCategory.IRRELEVANT,
            EmailPreFilter.classify(
                subject = "Your monthly statement is ready",
                body = "Log in to view your account statement.",
                senderEmail = "noreply@bank.com"
            )
        )
    }

    @Test
    fun `classify IRRELEVANT - package delivery`() {
        assertEquals(
            EmailCategory.IRRELEVANT,
            EmailPreFilter.classify(
                subject = "Your package has been shipped",
                body = "Track your order at the link below.",
                senderEmail = "shipping@amazon.com"
            )
        )
    }

    @Test
    fun `classify IRRELEVANT - social media notification`() {
        assertEquals(
            EmailCategory.IRRELEVANT,
            EmailPreFilter.classify(
                subject = "Someone liked your photo",
                body = "You have a new notification. Click to view.",
                senderEmail = "noreply@social.com"
            )
        )
    }

    // ── isJobRelated helper ──────────────────────────────────────────────────

    @Test
    fun `isJobRelated returns false for IRRELEVANT email`() {
        assertFalse(
            EmailPreFilter.isJobRelated(
                subject = "Flash sale today only",
                body = "Unsubscribe from our marketing list.",
                senderEmail = "promo@shop.com"
            )
        )
    }

    @Test
    fun `isJobRelated returns true for APPLIED email`() {
        assertTrue(
            EmailPreFilter.isJobRelated(
                subject = "Application received",
                body = "Thank you for applying.",
                senderEmail = "hr@company.com"
            )
        )
    }

    @Test
    fun `unsubscribe link alone does not make email job-related`() {
        assertEquals(
            EmailCategory.IRRELEVANT,
            EmailPreFilter.classify(
                subject = "Your subscription",
                body = "Click here to unsubscribe from future emails.",
                senderEmail = "marketing@example.com"
            )
        )
    }
}
