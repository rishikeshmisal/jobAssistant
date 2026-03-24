package com.jobassistant.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.jobassistant.BuildConfig
import com.jobassistant.data.remote.model.CareerInsightsResult
import com.jobassistant.data.remote.model.CareerProfile
import com.jobassistant.data.remote.model.ClaudeParseException
import com.jobassistant.data.remote.model.EmailAction
import com.jobassistant.data.remote.model.FitAnalysis
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val userProfileDataStore: UserProfileDataStore,
    private val gson: Gson
) : ClaudeRepository {

    private suspend fun buildModel(): GenerativeModel {
        val apiKey = userProfileDataStore.userApiKey.firstOrNull()
            ?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.2f
                maxOutputTokens = 1024
            }
        )
    }

    private suspend fun generateJson(prompt: String): String {
        val fullPrompt = "You MUST respond with ONLY a valid JSON object. " +
            "No markdown, no code blocks, no explanation. Raw JSON only.\n\n$prompt"
        val rawText = buildModel().generateContent(fullPrompt).text?.trim()
            ?: throw ClaudeParseException("Empty response from Gemini")
        return rawText
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
    }

    override suspend fun analyzeIntent(resumeText: String, userInterests: String): CareerProfile {
        val prompt = """
            Analyze the following resume and career interests. Return ONLY a JSON object with these exact fields:
            {
              "current_level": "string (e.g. Junior, Mid-level, Senior)",
              "target_roles": ["array of recommended job titles"],
              "skill_gaps": ["array of skills missing vs target roles"],
              "recommended_focus_areas": ["array of topics to prioritize"],
              "goal_map": "string narrative summary (1-2 sentences)"
            }

            RESUME:
            ${resumeText.take(4000)}

            INTERESTS:
            ${userInterests.take(1000)}
        """.trimIndent()
        return gson.fromJson(generateJson(prompt), CareerProfile::class.java)
    }

    override suspend fun evaluateFit(resumeText: String, jobDescription: String): FitAnalysis {
        val prompt = """
            Score this job description against the candidate's resume. Return ONLY a JSON object with these exact fields:
            {
              "score": integer 1-100,
              "pros": ["matching strengths"],
              "cons": ["weaknesses or mismatches"],
              "missing_skills": ["skills in job description not present in resume"]
            }

            RESUME (excerpt):
            ${resumeText.take(4000)}

            JOB DESCRIPTION:
            ${jobDescription.take(3000)}
        """.trimIndent()
        return gson.fromJson(generateJson(prompt), FitAnalysis::class.java)
    }

    override suspend fun parseEmail(subject: String, body: String): EmailAction {
        val prompt = """
            Classify this job-related email and extract structured context. Return ONLY a JSON object with these exact fields:
            {
              "action_type": "one of: APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT",
              "target_company": "string or null",
              "role_title": "string or null",
              "date": unix timestamp in milliseconds as integer or null,
              "interview_link": "video call URL string or null"
            }

            SUBJECT: $subject

            BODY:
            ${body.take(2000)}
        """.trimIndent()
        return gson.fromJson(generateJson(prompt), EmailAction::class.java)
    }

    override suspend fun generateInsights(profileSummary: String, historySummary: String): CareerInsightsResult {
        val prompt = """
            Analyze job application history against a career profile to generate actionable insights. Return ONLY a JSON object with these exact fields:
            {
              "identified_gaps": ["recurring skill or experience gaps from rejections"],
              "recommended_actions": ["specific actionable next steps"],
              "market_feedback_summary": "string narrative of what the market is signalling (2-3 sentences)"
            }

            PROFILE SUMMARY:
            $profileSummary

            APPLICATION HISTORY (last 90 days, anonymized):
            $historySummary
        """.trimIndent()
        return gson.fromJson(generateJson(prompt), CareerInsightsResult::class.java)
    }
}
