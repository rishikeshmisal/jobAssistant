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
import com.jobassistant.domain.model.CsvColumnMapping
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
            modelName = "gemini-3.1-pro-preview",
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

    override suspend fun mapCsvColumns(
        headers: List<String>,
        sampleRows: List<List<String>>
    ): CsvColumnMapping {
        val headersJson = headers.joinToString(", ") { "\"$it\"" }
        val sampleRowsText = sampleRows.take(5).mapIndexed { i, row ->
            val cells = row.joinToString(", ") { "\"$it\"" }
            "Row ${i + 1}: [$cells]"
        }.joinToString("\n")

        val prompt = """
You are mapping a CSV file of job applications to a database schema.

CSV headers: [$headersJson]
Sample rows (up to 5):
$sampleRowsText

Return ONLY a JSON object with these exact fields:
{
  "column_mappings": {
    "<header>": "<db_field or IGNORE>"
  },
  "status_mappings": {
    "<csv_status_value>": "<ApplicationStatus>"
  },
  "date_pattern": "<SimpleDateFormat pattern or null>"
}

Rules:
- Every header must appear in column_mappings. Allowed db_field values: companyName, roleTitle, status, appliedDate, location, salaryRange, notes, fitScore, IGNORE.
- companyName and roleTitle are required — map the closest matching columns even if names differ.
- status_mappings must cover every distinct status value visible in the sample rows. ApplicationStatus must be one of: INTERESTED, APPLIED, SCREENING, INTERVIEWING, ASSESSMENT, OFFER, ACCEPTED, REJECTED, WITHDRAWN, NO_RESPONSE.
- For date_pattern: detect the SimpleDateFormat pattern from sample data (e.g. "yyyy-MM-dd", "dd/MM/yyyy", "MMM d, yyyy"). Use null (not the string "null") if no date column exists.
        """.trimIndent()

        val rawJson = generateJson(prompt)

        val jsonObj = try {
            gson.fromJson(rawJson, com.google.gson.JsonObject::class.java)
        } catch (e: Exception) {
            throw ClaudeParseException("CSV mapping response was not valid JSON: $rawJson")
        }

        if (!jsonObj.has("column_mappings") || !jsonObj.has("status_mappings")) {
            throw ClaudeParseException("CSV mapping response missing required keys: $rawJson")
        }

        val columnMappings = jsonObj.getAsJsonObject("column_mappings")
            .entrySet().associate { it.key to it.value.asString }

        val statusMappings = jsonObj.getAsJsonObject("status_mappings")
            .entrySet().associate { it.key to it.value.asString }

        val datePattern = if (jsonObj.has("date_pattern") && !jsonObj.get("date_pattern").isJsonNull)
            jsonObj.get("date_pattern").asString.takeIf { it.isNotBlank() }
        else null

        return CsvColumnMapping(
            columnMappings = columnMappings,
            statusMappings = statusMappings,
            datePattern = datePattern
        )
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
