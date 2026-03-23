package com.jobassistant.data.repository

import com.google.gson.GsonBuilder
import com.jobassistant.data.remote.model.CareerInsightsResult
import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.EmailActionType
import com.jobassistant.data.remote.model.FitAnalysis
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests the JSON→model deserialization logic used inside GeminiRepository.
 *
 * GeminiRepository.generateJson() calls the Gemini SDK and then parses the raw JSON
 * string with Gson. These tests verify the Gson parsing step in isolation,
 * covering all four response schemas.
 */
class GeminiRepositoryTest {

    private val gson = GsonBuilder().create()

    // ── CareerProfile ──────────────────────────────────────────────────────

    @Test
    fun parseCareerProfile_validJson_correctFields() {
        val json = """
            {
              "current_level": "Mid-level",
              "target_roles": ["Android Engineer", "Mobile Developer"],
              "skill_gaps": ["Kotlin Multiplatform", "CI/CD"],
              "recommended_focus_areas": ["Compose", "Testing"],
              "goal_map": "Become a senior Android engineer."
            }
        """.trimIndent()

        val result = gson.fromJson(json, CareerProfile::class.java)

        assertEquals("Mid-level", result.currentLevel)
        assertEquals(listOf("Android Engineer", "Mobile Developer"), result.targetRoles)
        assertEquals(listOf("Kotlin Multiplatform", "CI/CD"), result.skillGaps)
        assertEquals(listOf("Compose", "Testing"), result.recommendedFocusAreas)
        assertEquals("Become a senior Android engineer.", result.goalMap)
    }

    @Test
    fun parseCareerProfile_emptyArrays_returnsEmptyLists() {
        val json = """
            {
              "current_level": "Junior",
              "target_roles": [],
              "skill_gaps": [],
              "recommended_focus_areas": [],
              "goal_map": ""
            }
        """.trimIndent()

        val result = gson.fromJson(json, CareerProfile::class.java)

        assertEquals("Junior", result.currentLevel)
        assertEquals(emptyList<String>(), result.targetRoles)
        assertEquals(emptyList<String>(), result.skillGaps)
    }

    // ── FitAnalysis ────────────────────────────────────────────────────────

    @Test
    fun parseFitAnalysis_validJson_correctFields() {
        val json = """
            {
              "score": 78,
              "pros": ["Kotlin expertise", "Compose experience"],
              "cons": ["No team lead experience"],
              "missing_skills": ["Docker", "AWS"]
            }
        """.trimIndent()

        val result = gson.fromJson(json, FitAnalysis::class.java)

        assertEquals(78, result.score)
        assertEquals(listOf("Kotlin expertise", "Compose experience"), result.pros)
        assertEquals(listOf("No team lead experience"), result.cons)
        assertEquals(listOf("Docker", "AWS"), result.missingSkills)
    }

    @Test
    fun parseFitAnalysis_missingOptionalFields_noException() {
        val json = """{"score": 50}"""

        val result = gson.fromJson(json, FitAnalysis::class.java)

        assertEquals(50, result.score)
        assertEquals(emptyList<String>(), result.pros)
        assertEquals(emptyList<String>(), result.missingSkills)
    }

    @Test
    fun parseFitAnalysis_boundaryScore100_parsed() {
        val json = """{"score": 100, "pros": [], "cons": [], "missing_skills": []}"""

        val result = gson.fromJson(json, FitAnalysis::class.java)

        assertEquals(100, result.score)
    }

    // ── EmailAction ────────────────────────────────────────────────────────

    @Test
    fun parseEmailAction_interviewType_correctFields() {
        val json = """
            {
              "action_type": "INTERVIEW",
              "target_company": "Acme Corp",
              "role_title": "Android Engineer",
              "date": null,
              "interview_link": "https://meet.google.com/abc"
            }
        """.trimIndent()

        val result = gson.fromJson(json, EmailAction::class.java)

        assertEquals(EmailActionType.INTERVIEW, result.actionType)
        assertEquals("Acme Corp", result.targetCompany)
        assertEquals("https://meet.google.com/abc", result.interviewLink)
    }

    @Test
    fun parseEmailAction_appliedType_correctFields() {
        val json = """
            {
              "action_type": "APPLIED",
              "target_company": "Test Corp",
              "role_title": "QA Engineer",
              "date": null,
              "interview_link": null
            }
        """.trimIndent()

        val result = gson.fromJson(json, EmailAction::class.java)

        assertEquals(EmailActionType.APPLIED, result.actionType)
        assertEquals("Test Corp", result.targetCompany)
        assertEquals("QA Engineer", result.roleTitle)
    }

    @Test
    fun parseEmailAction_rejectionType_correctActionType() {
        val json = """{"action_type": "REJECTION", "target_company": "Corp", "role_title": "Dev"}"""

        val result = gson.fromJson(json, EmailAction::class.java)

        assertEquals(EmailActionType.REJECTION, result.actionType)
    }

    @Test
    fun parseEmailAction_irrelevantType_correctActionType() {
        val json = """{"action_type": "IRRELEVANT"}"""

        val result = gson.fromJson(json, EmailAction::class.java)

        assertEquals(EmailActionType.IRRELEVANT, result.actionType)
    }

    // ── CareerInsightsResult ───────────────────────────────────────────────

    @Test
    fun parseCareerInsightsResult_validJson_correctFields() {
        val json = """
            {
              "identified_gaps": ["Leadership skills", "System design"],
              "recommended_actions": ["Take a lead role", "Practice system design"],
              "market_feedback_summary": "Strong technical candidate with growth potential."
            }
        """.trimIndent()

        val result = gson.fromJson(json, CareerInsightsResult::class.java)

        assertEquals(listOf("Leadership skills", "System design"), result.identifiedGaps)
        assertEquals(listOf("Take a lead role", "Practice system design"), result.recommendedActions)
        assertEquals("Strong technical candidate with growth potential.", result.marketFeedbackSummary)
    }

    @Test
    fun parseCareerInsightsResult_emptyArrays_noException() {
        val json = """
            {
              "identified_gaps": [],
              "recommended_actions": [],
              "market_feedback_summary": ""
            }
        """.trimIndent()

        val result = gson.fromJson(json, CareerInsightsResult::class.java)

        assertEquals(emptyList<String>(), result.identifiedGaps)
        assertEquals("", result.marketFeedbackSummary)
    }

    // ── Markdown fence stripping ───────────────────────────────────────────

    @Test
    fun stripMarkdownFences_jsonFence_parsesCorrectly() {
        val rawText = "```json\n{\"score\": 85, \"pros\": [], \"cons\": [], \"missing_skills\": []}\n```"
        val stripped = rawText
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        val result = gson.fromJson(stripped, FitAnalysis::class.java)

        assertEquals(85, result.score)
    }

    @Test
    fun stripMarkdownFences_plainFence_parsesCorrectly() {
        val rawText = "```\n{\"score\": 72}\n```"
        val stripped = rawText
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        val result = gson.fromJson(stripped, FitAnalysis::class.java)

        assertEquals(72, result.score)
    }
}
