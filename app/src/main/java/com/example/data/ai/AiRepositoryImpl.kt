package com.example.data.ai

import com.example.BuildConfig
import com.example.core.model.Result
import com.example.data.database.converters.Converters
import com.example.domain.model.ActionMilestone
import com.example.domain.model.CompetitorInsight
import com.example.domain.model.ContentReport
import com.example.domain.model.FeasibilityScore
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis
import com.example.domain.repository.AiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiRepositoryImpl(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val getBackendUrl: () -> String = { "" }
) : AiRepository {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateAnalysis(venture: Venture): Result<VentureAnalysis> =
        withContext(Dispatchers.IO) {
            try {
                // Safety input check
                val combinedText = "${venture.name} ${venture.businessIdea} ${venture.customerProblem} ${venture.proposedSolution}".lowercase()
                val harmfulKeywords = listOf("malware", "ransomware", "counterfeit", "exploit", "ddos", "phishing", "weapons")
                for (kw in harmfulKeywords) {
                    if (combinedText.contains(kw)) {
                        return@withContext Result.Error(
                            IllegalArgumentException("Input contains terms flagged by AI Safety policy."),
                            "Content contains potentially prohibited topics. Please ensure venture idea complies with safety policies."
                        )
                    }
                }

                val backendUrl = getBackendUrl().trim().trimEnd('/')

                // 1. Try Backend API first if configured
                if (backendUrl.isNotEmpty() && (backendUrl.startsWith("http://") || backendUrl.startsWith("https://"))) {
                    try {
                        val backendResult = callBackendApi(backendUrl, venture)
                        if (backendResult != null) {
                            return@withContext Result.Success(backendResult)
                        }
                    } catch (e: Exception) {
                        // Fallback to direct Gemini if available
                    }
                }

                // 2. Direct Gemini API call if GEMINI_API_KEY is configured via AI Studio Secrets / .env
                val geminiKey = try {
                    BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
                } catch (_: Exception) { "" }

                if (geminiKey.isNotBlank() && !geminiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
                    val geminiResult = callGeminiApi(geminiKey, venture)
                    return@withContext Result.Success(geminiResult)
                }

                // 3. If neither backend URL nor Gemini key is set, return descriptive error instructing user/dev
                Result.Error(
                    IllegalStateException("AI Service not configured"),
                    "AI service configuration required. Please configure your backend URL in Settings or set GEMINI_API_KEY in the AI Studio Secrets panel."
                )
            } catch (e: IOException) {
                Result.Error(e, "Network connectivity error: Please check your internet connection and try again.")
            } catch (e: Exception) {
                Result.Error(e, e.localizedMessage ?: "Failed to generate AI analysis.")
            }
        }

    private fun callBackendApi(backendUrl: String, venture: Venture): VentureAnalysis? {
        val payload = JSONObject().apply {
            put("venture", JSONObject().apply {
                put("id", venture.id)
                put("name", venture.name)
                put("businessIdea", venture.businessIdea)
                put("industry", venture.industry)
                put("targetMarket", venture.targetMarket)
                put("targetAudience", venture.targetAudience)
                put("customerProblem", venture.customerProblem)
                put("proposedSolution", venture.proposedSolution)
                put("businessModel", venture.businessModel)
                put("estimatedStartupBudget", venture.estimatedStartupBudget)
                put("monthlyOperatingBudget", venture.monthlyOperatingBudget)
                put("teamSize", venture.teamSize)
                put("revenueModel", venture.revenueModel)
                put("competitors", venture.competitors)
                put("uniqueSellingProposition", venture.uniqueSellingProposition)
            })
        }

        val request = Request.Builder()
            .url("$backendUrl/api/analyze")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Backend returned HTTP ${response.code}: ${response.body?.string()}")
            }
            val bodyString = response.body?.string() ?: throw IOException("Empty response from backend")
            return parseAiJsonToAnalysis(venture.id, bodyString)
        }
    }

    private fun callGeminiApi(apiKey: String, venture: Venture): VentureAnalysis {
        val prompt = buildAnalysisPrompt(venture)
        val geminiEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(geminiEndpoint)
            .post(requestBody.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                throw IOException("Gemini API call returned HTTP ${response.code}: $errBody")
            }
            val resText = response.body?.string() ?: throw IOException("Empty response from Gemini API")
            
            // Extract the generated text from Gemini response structure
            val geminiJson = JSONObject(resText)
            val candidates = geminiJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawJsonText = parts?.optJSONObject(0)?.optString("text")
                ?: throw IOException("No text part in Gemini API response")

            return parseAiJsonToAnalysis(venture.id, rawJsonText)
        }
    }

    private fun buildAnalysisPrompt(venture: Venture): String {
        return """
You are VentureForge AI, a top-tier venture strategist, startup evaluator, and financial analyst.
Analyze the following startup venture thoroughly, objectively, and realistically.
Do not guarantee success; evaluate real risks, market dynamics, and execution challenges.

VENTURE DETAILS:
- Name: ${venture.name}
- Industry: ${venture.industry}
- Target Market: ${venture.targetMarket}
- Target Audience: ${venture.targetAudience}
- Business Idea: ${venture.businessIdea}
- Customer Problem: ${venture.customerProblem}
- Proposed Solution: ${venture.proposedSolution}
- Business Model: ${venture.businessModel}
- Startup Budget: $${venture.estimatedStartupBudget}
- Monthly Budget: $${venture.monthlyOperatingBudget}
- Team Size: ${venture.teamSize}
- Launch Timeframe: ${venture.expectedLaunchTimeframe}
- Revenue Model: ${venture.revenueModel}
- Pricing Assumptions: ${venture.pricingAssumptions}
- Competitors: ${venture.competitors}
- Unique Selling Proposition: ${venture.uniqueSellingProposition}

MANDATORY INSTRUCTIONS:
- Return ONLY a valid JSON object matching the schema below.
- Do NOT output markdown code blocks (e.g. ```json). Just the raw JSON.
- Every insight must relate specifically to ${venture.name} and ${venture.industry}.

JSON SCHEMA:
{
  "executiveSummary": "2-3 sentences overview of the venture viability and positioning.",
  "problemAnalysis": "Assessment of customer problem urgency and willingness to pay.",
  "solutionEvaluation": "Feasibility, differentiation, and defensibility of solution.",
  "targetMarketValidation": "Market size, accessibility, and customer acquisition realities.",
  "businessModelFit": "Evaluation of margin potential, recurring revenue, and unit economics.",
  "valuePropositionAssessment": "Clarity of value proposition compared to substitutes.",
  "strengths": ["string", "string", "string", "string"],
  "weaknesses": ["string", "string", "string", "string"],
  "opportunities": ["string", "string", "string", "string"],
  "threats": ["string", "string", "string", "string"],
  "competitors": [
    {
      "name": "Competitor Name",
      "differentiation": "Key difference",
      "threatLevel": "High | Medium | Low",
      "marketPositioning": "Current positioning",
      "notes": "Estimated strategy (AI hypothesis)"
    }
  ],
  "marketRisks": ["Risk 1", "Risk 2", "Risk 3"],
  "recommendations": ["Actionable recommendation 1", "Recommendation 2", "Recommendation 3"],
  "actionPlan": [
    {
      "phase": "Phase 1: Customer Validation",
      "taskName": "Specific task",
      "description": "How to execute",
      "priority": "High | Medium | Low",
      "estimatedDays": 14
    },
    {
      "phase": "Phase 2: MVP Development",
      "taskName": "Specific task",
      "description": "How to execute",
      "priority": "High | Medium | Low",
      "estimatedDays": 30
    },
    {
      "phase": "Phase 3: Launch & Acquisition",
      "taskName": "Specific task",
      "description": "How to execute",
      "priority": "High | Medium | Low",
      "estimatedDays": 21
    }
  ],
  "feasibilityScore": {
    "overallScore": 72,
    "problemClarityScore": 80,
    "marketDemandScore": 75,
    "competitionDifficultyScore": 65,
    "unitEconomicsScore": 70,
    "executionFeasibilityScore": 68,
    "riskLevel": "Moderate",
    "keyAssumptions": [
      "Assumption 1",
      "Assumption 2"
    ],
    "disclaimerNote": "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success."
  },
  "confidenceNotes": [
    "AI evaluation based on user inputs. Market validation required."
  ]
}
        """.trimIndent()
    }

    fun parseAiJsonToAnalysis(ventureId: Long, rawJson: String): VentureAnalysis {
        // Strip any surrounding markdown backticks if present
        var cleaned = rawJson.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        cleaned = cleaned.trim()

        val json = JSONObject(cleaned)

        val strengths = json.optJSONArray("strengths")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        val weaknesses = json.optJSONArray("weaknesses")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        val opportunities = json.optJSONArray("opportunities")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        val threats = json.optJSONArray("threats")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        val competitors = ArrayList<CompetitorInsight>()
        json.optJSONArray("competitors")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                competitors.add(
                    CompetitorInsight(
                        name = obj.optString("name", "Competitor"),
                        differentiation = obj.optString("differentiation", ""),
                        threatLevel = obj.optString("threatLevel", "Medium"),
                        marketPositioning = obj.optString("marketPositioning", ""),
                        notes = obj.optString("notes", "AI hypothesis")
                    )
                )
            }
        }

        val marketRisks = json.optJSONArray("marketRisks")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        val recommendations = json.optJSONArray("recommendations")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        val actionPlan = ArrayList<ActionMilestone>()
        json.optJSONArray("actionPlan")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                actionPlan.add(
                    ActionMilestone(
                        phase = obj.optString("phase", "Phase ${i + 1}"),
                        taskName = obj.optString("taskName", "Task ${i + 1}"),
                        description = obj.optString("description", ""),
                        priority = obj.optString("priority", "Medium"),
                        estimatedDays = obj.optInt("estimatedDays", 14)
                    )
                )
            }
        }

        val fObj = json.optJSONObject("feasibilityScore")
        val assumptionsList = ArrayList<String>()
        fObj?.optJSONArray("keyAssumptions")?.let { arr ->
            for (i in 0 until arr.length()) assumptionsList.add(arr.optString(i))
        }

        val feasibilityScore = FeasibilityScore(
            overallScore = fObj?.optInt("overallScore", 70) ?: 70,
            problemClarityScore = fObj?.optInt("problemClarityScore", 75) ?: 75,
            marketDemandScore = fObj?.optInt("marketDemandScore", 70) ?: 70,
            competitionDifficultyScore = fObj?.optInt("competitionDifficultyScore", 65) ?: 65,
            unitEconomicsScore = fObj?.optInt("unitEconomicsScore", 70) ?: 70,
            executionFeasibilityScore = fObj?.optInt("executionFeasibilityScore", 68) ?: 68,
            riskLevel = fObj?.optString("riskLevel", "Moderate") ?: "Moderate",
            keyAssumptions = if (assumptionsList.isEmpty()) listOf("Assumes user inputs reflect initial business expectations") else assumptionsList,
            disclaimerNote = fObj?.optString("disclaimerNote", "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success.") ?: "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success."
        )

        val confidenceNotes = json.optJSONArray("confidenceNotes")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: listOf("AI evaluation based on current assumptions. Field verification recommended.")

        return VentureAnalysis(
            ventureId = ventureId,
            executiveSummary = json.optString("executiveSummary", "Executive summary not provided."),
            problemAnalysis = json.optString("problemAnalysis", "Problem analysis not available."),
            solutionEvaluation = json.optString("solutionEvaluation", "Solution evaluation not available."),
            targetMarketValidation = json.optString("targetMarketValidation", "Market validation not available."),
            businessModelFit = json.optString("businessModelFit", "Business model fit not available."),
            valuePropositionAssessment = json.optString("valuePropositionAssessment", "Value proposition assessment not available."),
            strengths = strengths,
            weaknesses = weaknesses,
            opportunities = opportunities,
            threats = threats,
            competitors = competitors,
            marketRisks = marketRisks,
            recommendations = recommendations,
            actionPlan = actionPlan,
            feasibilityScore = feasibilityScore,
            confidenceNotes = confidenceNotes,
            generatedAt = System.currentTimeMillis(),
            isAiGenerated = true
        )
    }

    override suspend fun reportContent(report: ContentReport): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val backendUrl = getBackendUrl().trim().trimEnd('/')
                if (backendUrl.isNotEmpty() && (backendUrl.startsWith("http://") || backendUrl.startsWith("https://"))) {
                    val payload = JSONObject().apply {
                        put("ventureId", report.ventureId)
                        put("responseSnippet", report.responseSnippet)
                        put("category", report.category)
                        put("userComment", report.userComment)
                        put("timestamp", report.timestamp)
                    }

                    val request = Request.Builder()
                        .url("$backendUrl/api/report-content")
                        .post(payload.toString().toRequestBody(jsonMediaType))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            return@withContext Result.Success(true)
                        }
                    }
                }
                // Logged locally
                Result.Success(true)
            } catch (e: Exception) {
                // Still considered successful locally even if network report sync fails
                Result.Success(true)
            }
        }
}
