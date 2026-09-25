package com.example.ai

import com.example.data.ai.AiRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiValidationTest {

    private val aiRepository = AiRepositoryImpl()

    @Test
    fun testValidJsonParsing() {
        val validJson = """
            {
              "executiveSummary": "Strong high-margin B2B SaaS proposition with clear beachhead market.",
              "problemAnalysis": "Manual spreadsheets cause 15 hours of weekly reporting waste.",
              "solutionEvaluation": "Automated workflow reduces report generation to under 2 minutes.",
              "targetMarketValidation": "Mid-market logistics providers in North America.",
              "businessModelFit": "High gross margin subscription with annual recurring contracts.",
              "valuePropositionAssessment": "10x speed improvement over legacy ERP tools.",
              "strengths": ["Proprietary automation", "High switching costs", "Experienced domain team"],
              "weaknesses": ["Small sales team", "Initial brand obscurity"],
              "opportunities": ["Expansion into European supply chain", "Enterprise API integration"],
              "threats": ["Consolidation by legacy ERP vendors"],
              "competitors": [
                {
                  "name": "LegacyCorp",
                  "differentiation": "Modern cloud API",
                  "threatLevel": "Medium",
                  "marketPositioning": "Established market incumbent",
                  "notes": "Slow release cycle"
                }
              ],
              "marketRisks": ["Sales cycle elongation", "Regulatory compliance shifts"],
              "recommendations": ["Conduct 20 customer discovery interviews", "Launch self-service pilot"],
              "actionPlan": [
                {
                  "phase": "Phase 1: Validation",
                  "taskName": "Customer Discovery Interviews",
                  "description": "Interview 25 warehouse directors",
                  "priority": "High",
                  "estimatedDays": 14
                }
              ],
              "feasibilityScore": {
                "overallScore": 82,
                "problemClarityScore": 88,
                "marketDemandScore": 80,
                "competitionDifficultyScore": 75,
                "unitEconomicsScore": 85,
                "executionFeasibilityScore": 80,
                "riskLevel": "Low-Moderate",
                "keyAssumptions": ["Customers willing to pay >= $99/mo", "CAC under $400"],
                "disclaimerNote": "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success."
              },
              "confidenceNotes": ["High confidence in problem space; unit economics contingent on CAC validation."]
            }
        """.trimIndent()

        val analysis = aiRepository.parseAiJsonToAnalysis(ventureId = 1L, rawJson = validJson)

        assertNotNull(analysis)
        assertEquals(1L, analysis.ventureId)
        assertEquals("Strong high-margin B2B SaaS proposition with clear beachhead market.", analysis.executiveSummary)
        assertEquals(3, analysis.strengths.size)
        assertEquals(2, analysis.weaknesses.size)
        assertEquals(1, analysis.competitors.size)
        assertEquals("LegacyCorp", analysis.competitors[0].name)
        assertEquals(82, analysis.feasibilityScore.overallScore)
        assertEquals(1, analysis.actionPlan.size)
        assertEquals("Customer Discovery Interviews", analysis.actionPlan[0].taskName)
    }

    @Test
    fun testMarkdownWrappedJsonParsing() {
        val markdownJson = """
            ```json
            {
              "executiveSummary": "Tested wrapped markdown output.",
              "problemAnalysis": "Valid problem.",
              "solutionEvaluation": "Valid solution.",
              "targetMarketValidation": "Valid market.",
              "businessModelFit": "Valid model.",
              "valuePropositionAssessment": "Valid proposition.",
              "strengths": ["Agile development"],
              "weaknesses": ["Capital constraints"],
              "opportunities": ["Niche specialization"],
              "threats": ["Macroeconomic headwinds"],
              "competitors": [],
              "marketRisks": ["Adoption friction"],
              "recommendations": ["Build lean MVP"],
              "actionPlan": [],
              "feasibilityScore": {
                "overallScore": 70,
                "problemClarityScore": 70,
                "marketDemandScore": 70,
                "competitionDifficultyScore": 70,
                "unitEconomicsScore": 70,
                "executionFeasibilityScore": 70,
                "riskLevel": "Moderate",
                "keyAssumptions": ["Standard assumptions"],
                "disclaimerNote": "AI estimate."
              },
              "confidenceNotes": []
            }
            ```
        """.trimIndent()

        val analysis = aiRepository.parseAiJsonToAnalysis(ventureId = 42L, rawJson = markdownJson)
        assertNotNull(analysis)
        assertEquals(42L, analysis.ventureId)
        assertEquals("Tested wrapped markdown output.", analysis.executiveSummary)
        assertEquals(70, analysis.feasibilityScore.overallScore)
    }

    @Test
    fun testFallbackForMissingOptionalFields() {
        val minimalJson = """
            {
              "executiveSummary": "Minimal payload test."
            }
        """.trimIndent()

        val analysis = aiRepository.parseAiJsonToAnalysis(ventureId = 99L, rawJson = minimalJson)
        assertNotNull(analysis)
        assertEquals("Minimal payload test.", analysis.executiveSummary)
        assertTrue(analysis.strengths.isEmpty())
        assertTrue(analysis.competitors.isEmpty())
        // Should default safely without crashing
        assertTrue(analysis.feasibilityScore.overallScore > 0)
    }
}
