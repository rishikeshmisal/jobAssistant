---
name: claude-api-tool-use
description: >
  Exact patterns for calling the Anthropic Claude API using Tool Use (function calling)
  in this Android project. Use this skill whenever writing or modifying ANY code that calls
  the Claude/Anthropic API — including ClaudeRepository, AnthropicApiService, all four
  endpoint methods (analyze_intent, evaluate_fit, parse_email_context,
  generate_career_insights), Retrofit setup, response parsing, error handling, or cost
  controls. Also use when the model ID, tool_choice, or Gson deserialization of API
  responses is involved. Do NOT skip this skill for "quick" API additions — the patterns
  here prevent the most common and costly mistakes.
---

# Claude API Tool Use — Project Reference

## Critical constants (use exactly, no substitutions)

```kotlin
const val MODEL_ID   = "claude-sonnet-4-6"          // NOT claude-3-5-sonnet, NOT claude-sonnet-4-20250514
const val BASE_URL   = "https://api.anthropic.com/v1/"
const val API_VERSION_HEADER = "anthropic-version"
const val API_VERSION_VALUE  = "2023-06-01"
const val CONTENT_TYPE_HEADER = "x-api-key"
const val MAX_TOKENS = 1024                          // sufficient for all four tools; raise only if truncation observed
```

> **Model ID is the #1 source of 400 errors in this project.** The spec.md, execution.md, and
> LEARNING.md historically contained three different strings. `claude-sonnet-4-6` is the
> canonical value. It lives in a single `NetworkModule` constant — never inline it elsewhere.

---

## Retrofit interface

```kotlin
interface AnthropicApiService {
    @POST("messages")
    suspend fun sendMessage(
        @Body request: AnthropicRequest
    ): AnthropicResponse
}
```

## Request / response data classes

```kotlin
data class AnthropicRequest(
    val model: String = MODEL_ID,
    @SerializedName("max_tokens") val maxTokens: Int = MAX_TOKENS,
    val tools: List<AnthropicTool>,
    @SerializedName("tool_choice") val toolChoice: ToolChoice,
    val messages: List<AnthropicMessage>
)

data class AnthropicTool(
    val name: String,
    val description: String,
    @SerializedName("input_schema") val inputSchema: JsonObject   // Gson JsonObject
)

data class ToolChoice(
    val type: String = "tool",    // forces the model to call exactly one specific tool
    val name: String              // must match AnthropicTool.name exactly
)

data class AnthropicMessage(
    val role: String,             // "user" only for single-turn tool calls
    val content: String
)

data class AnthropicResponse(
    val id: String,
    val content: List<ContentBlock>,
    @SerializedName("stop_reason") val stopReason: String        // expect "tool_use"
)

data class ContentBlock(
    val type: String,             // "tool_use" when tool_choice forces a call
    val id: String?,
    val name: String?,
    val input: JsonObject?        // parse this with Gson into your domain model
)
```

---

## Parsing pattern — extract tool result

Always use index-safe filtering, never `content[0]`:

```kotlin
fun AnthropicResponse.toolInput(): JsonObject {
    return content
        .firstOrNull { it.type == "tool_use" }
        ?.input
        ?: throw IllegalStateException("No tool_use block in response. stop_reason=$stopReason")
}
```

Then deserialize with Gson:

```kotlin
val fitAnalysis: FitAnalysis = gson.fromJson(response.toolInput(), FitAnalysis::class.java)
```

---

## The four tools — schemas and domain models

### 1. `analyze_intent`

**When:** First run / profile setup only. Cache result — never re-call on every launch.

```kotlin
val analyzeIntentTool = AnthropicTool(
    name = "analyze_intent",
    description = "Analyze a resume and career interests to generate a structured career profile.",
    inputSchema = buildJsonSchema {
        property("current_level", "string", "Seniority level inferred from resume")
        property("target_roles", "array of strings", "Recommended job titles to target")
        property("skill_gaps", "array of strings", "Skills missing vs target roles")
        property("recommended_focus_areas", "array of strings", "Topics to prioritize learning")
        property("goal_map", "string", "Narrative summary of career trajectory")
    }
)

data class CareerProfile(
    @SerializedName("current_level") val currentLevel: String,
    @SerializedName("target_roles") val targetRoles: List<String>,
    @SerializedName("skill_gaps") val skillGaps: List<String>,
    @SerializedName("recommended_focus_areas") val recommendedFocusAreas: List<String>,
    @SerializedName("goal_map") val goalMap: String
)
```

**User message:**
```
Analyze this resume and career interests.

RESUME:
{resumeText}

INTERESTS:
{userInterests}
```

---

### 2. `evaluate_fit`

**When:** User submits a job description (paste / URL / OCR). This is the most-called tool — keep token count lean.

```kotlin
val evaluateFitTool = AnthropicTool(
    name = "evaluate_fit",
    description = "Score a job description against a candidate resume. Return score, pros, cons, and missing skills.",
    inputSchema = buildJsonSchema {
        property("score", "integer 1-100", "Overall fit score")
        property("pros", "array of strings", "Matching strengths")
        property("cons", "array of strings", "Weaknesses or mismatches")
        property("missing_skills", "array of strings", "Skills in JD not present in resume")
    }
)

data class FitAnalysis(
    val score: Int,
    val pros: List<String>,
    val cons: List<String>,
    @SerializedName("missing_skills") val missingSkills: List<String>
)
```

**User message:**
```
Score this job against my resume.

RESUME (excerpt — first 800 words only for cost control):
{resumeText.take(4000)}

JOB DESCRIPTION:
{jobDescription.take(3000)}
```

> **Cost note:** Truncate resume to first 4000 chars and JD to 3000 chars. Full text is
> rarely needed for scoring. This alone reduces input tokens by ~60% on typical resumes.

---

### 3. `parse_email_context`

**When:** ONLY after `EmailPreFilter.isJobRelated()` returns `true`. Never call this on raw
inbox — the pre-filter must run first to cut ~80% of emails before they reach the API.

```kotlin
val parseEmailTool = AnthropicTool(
    name = "parse_email_context",
    description = "Classify a job-related email and extract structured context.",
    inputSchema = buildJsonSchema {
        property("action_type", "enum: APPLIED|REJECTION|INTERVIEW|ALERT|IRRELEVANT")
        property("target_company", "string or null")
        property("role_title", "string or null")
        property("date", "unix timestamp long or null", "Interview/event date if present")
        property("interview_link", "string or null", "Video call URL if present")
    }
)

data class EmailAction(
    @SerializedName("action_type") val actionType: EmailActionType,
    @SerializedName("target_company") val targetCompany: String?,
    @SerializedName("role_title") val roleTitle: String?,
    val date: Long?,
    @SerializedName("interview_link") val interviewLink: String?
)

enum class EmailActionType { APPLIED, REJECTION, INTERVIEW, ALERT, IRRELEVANT }
```

**User message:**
```
Classify this job-related email.

SUBJECT: {emailSubject}

BODY (first 2000 chars):
{emailBody.take(2000)}
```

> **Cost note:** Email bodies are truncated to 2000 chars. Classification context is always
> in the subject and first paragraph — full body adds tokens without improving accuracy.

---

### 4. `generate_career_insights`

**When:** User-triggered OR when cached insights are older than 7 days. Always check cache
age before calling — this is the most expensive tool.

```kotlin
val careerInsightsTool = AnthropicTool(
    name = "generate_career_insights",
    description = "Analyze job application history against a profile to generate actionable career insights.",
    inputSchema = buildJsonSchema {
        property("identified_gaps", "array of strings", "Recurring skill/experience gaps from rejections")
        property("recommended_actions", "array of strings", "Specific actionable next steps")
        property("market_feedback_summary", "string", "Narrative summary of what the market is signalling")
    }
)
```

**User message:**
```
Generate career insights based on this profile and application history.

PROFILE SUMMARY:
{profileSummary}

APPLICATION HISTORY (anonymized — last 90 days):
Total applied: {appliedCount}
Interview rate: {interviewRate}%
Rejection rate: {rejectionRate}%
Roles with most rejections: {topRejectedRoles}
Common missing skills from rejections: {missingSkillsAggregated}
```

> **Cost note:** Send aggregated statistics, NOT raw job records. Never send company names
> or full job descriptions to this endpoint. The summary above costs ~400 tokens vs ~4000
> for raw records — a 10x saving with no loss in insight quality.

---

## OkHttp client setup

```kotlin
@Provides @Singleton
fun provideOkHttpClient(userProfileDataStore: UserProfileDataStore): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(userProfileDataStore))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE  // never log bodies in release
        })
        .build()
}
```

```kotlin
class ApiKeyInterceptor @Inject constructor(
    private val dataStore: UserProfileDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = runBlocking {
            dataStore.userApiKey.firstOrNull()?.takeIf { it.isNotBlank() }
        } ?: BuildConfig.CLAUDE_API_KEY

        val request = chain.request().newBuilder()
            .header(CONTENT_TYPE_HEADER, apiKey)
            .header(API_VERSION_HEADER, API_VERSION_VALUE)
            .build()
        return chain.proceed(request)
    }
}
```

---

## Error handling — mandatory for every API call

```kotlin
sealed class ClaudeResult<out T> {
    data class Success<T>(val data: T) : ClaudeResult<T>()
    data class Error(val message: String, val isRetryable: Boolean) : ClaudeResult<Nothing>()
}

suspend fun <T> safeClaudeCall(block: suspend () -> T): ClaudeResult<T> {
    return try {
        ClaudeResult.Success(block())
    } catch (e: HttpException) {
        val retryable = e.code() in listOf(429, 529)   // rate limit, overloaded
        ClaudeResult.Error("API error ${e.code()}: ${e.message()}", retryable)
    } catch (e: IOException) {
        ClaudeResult.Error("Network error: ${e.message}", isRetryable = true)
    } catch (e: JsonSyntaxException) {
        ClaudeResult.Error("Response parse error — tool schema mismatch", isRetryable = false)
    }
}
```

Surface errors via snackbar in the ViewModel — never crash on API failures.

---

## Cost control checklist

Before every new API call, verify:

- [ ] `evaluate_fit`: resume truncated to 4000 chars, JD to 3000 chars
- [ ] `parse_email_context`: `EmailPreFilter.isJobRelated()` returned `true` first; body truncated to 2000 chars
- [ ] `generate_career_insights`: cache age checked; sending aggregated stats, not raw records
- [ ] `analyze_intent`: only fires on first run or explicit profile re-analysis, not on every launch
- [ ] `MAX_TOKENS = 1024`: do not raise without measuring actual response lengths first

---

## Helper — JsonSchema builder (avoids boilerplate)

```kotlin
fun buildJsonSchema(block: JsonSchemaBuilder.() -> Unit): JsonObject {
    val builder = JsonSchemaBuilder()
    builder.block()
    return builder.build()
}

class JsonSchemaBuilder {
    private val properties = JsonObject()
    private val required = com.google.gson.JsonArray()

    fun property(name: String, type: String, description: String = "") {
        val prop = JsonObject().apply {
            addProperty("type", type)
            if (description.isNotBlank()) addProperty("description", description)
        }
        properties.add(name, prop)
        required.add(name)
    }

    fun build() = JsonObject().apply {
        addProperty("type", "object")
        add("properties", properties)
        add("required", required)
    }
}
```
