package com.example.domain.model

data class Venture(
    val id: Long = 0,
    val name: String,
    val businessIdea: String,
    val industry: String,
    val targetMarket: String,
    val targetAudience: String,
    val customerProblem: String,
    val proposedSolution: String,
    val businessModel: String,
    val estimatedStartupBudget: Double,
    val monthlyOperatingBudget: Double,
    val teamSize: Int,
    val founderExperience: String,
    val expectedLaunchTimeframe: String,
    val revenueModel: String,
    val pricingAssumptions: String,
    val customerAcquisitionAssumptions: String,
    val competitors: String,
    val uniqueSellingProposition: String,
    val additionalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class VentureAnalysis(
    val id: Long = 0,
    val ventureId: Long,
    val executiveSummary: String,
    val problemAnalysis: String,
    val solutionEvaluation: String,
    val targetMarketValidation: String,
    val businessModelFit: String,
    val valuePropositionAssessment: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val opportunities: List<String>,
    val threats: List<String>,
    val competitors: List<CompetitorInsight>,
    val marketRisks: List<String>,
    val recommendations: List<String>,
    val actionPlan: List<ActionMilestone>,
    val feasibilityScore: FeasibilityScore,
    val confidenceNotes: List<String>,
    val generatedAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = true
)

data class FeasibilityScore(
    val overallScore: Int, // 0-100
    val problemClarityScore: Int, // 0-100
    val marketDemandScore: Int, // 0-100
    val competitionDifficultyScore: Int, // 0-100
    val unitEconomicsScore: Int, // 0-100
    val executionFeasibilityScore: Int, // 0-100
    val riskLevel: String, // "Low", "Moderate", "High", "Critical"
    val keyAssumptions: List<String>,
    val disclaimerNote: String = "AI-generated estimate based on assumptions provided. Not an objective guarantee of business success."
)

data class CompetitorInsight(
    val name: String,
    val differentiation: String,
    val threatLevel: String, // "Low", "Medium", "High"
    val marketPositioning: String,
    val notes: String
)

data class ActionMilestone(
    val phase: String, // "Phase 1: Validation", "Phase 2: MVP", etc.
    val taskName: String,
    val description: String,
    val priority: String, // "High", "Medium", "Low"
    val estimatedDays: Int
)

data class ActionTask(
    val id: Long = 0,
    val ventureId: Long,
    val phase: String,
    val taskName: String,
    val description: String,
    val priority: String,
    val estimatedDays: Int,
    val isCompleted: Boolean = false
)

data class BusinessModelCanvas(
    val ventureId: Long,
    val customerSegments: List<String>,
    val valuePropositions: List<String>,
    val channels: List<String>,
    val customerRelationships: List<String>,
    val revenueStreams: List<String>,
    val keyResources: List<String>,
    val keyActivities: List<String>,
    val keyPartnerships: List<String>,
    val costStructure: List<String>
)

enum class ScenarioType {
    CONSERVATIVE,
    BASE,
    OPTIMISTIC
}

data class FinancialInputs(
    val startupCost: Double = 10000.0,
    val monthlyFixedCosts: Double = 2500.0,
    val variableCostPerUnit: Double = 15.0,
    val unitPrice: Double = 49.0,
    val initialCustomers: Int = 20,
    val monthlyGrowthRatePercent: Double = 12.0,
    val churnRatePercent: Double = 4.0,
    val conversionRatePercent: Double = 2.5,
    val marketingBudgetMonthly: Double = 1000.0,
    val otherExpensesMonthly: Double = 300.0,
    val taxRatePercent: Double = 15.0,
    val runwayCashReserve: Double = 25000.0,
    val projectionMonths: Int = 12
)

data class MonthlyMetric(
    val monthNumber: Int,
    val customerCount: Int,
    val revenue: Double,
    val fixedCosts: Double,
    val variableCosts: Double,
    val marketingCosts: Double,
    val otherCosts: Double,
    val totalExpenses: Double,
    val grossProfit: Double,
    val netProfit: Double,
    val cashBalance: Double
)

data class FinancialScenarioResult(
    val scenarioType: ScenarioType,
    val inputs: FinancialInputs,
    val monthlyRevenueMonth12: Double,
    val totalFirstYearRevenue: Double,
    val totalFirstYearExpenses: Double,
    val totalFirstYearNetProfit: Double,
    val breakEvenUnitsPerMonth: Int,
    val breakEvenMonth: Int?, // null if not reached within projection window
    val runwayMonthsRemaining: Double, // Double.POSITIVE_INFINITY if profitable
    val customerAcquisitionCostEstimated: Double,
    val estimatedCustomerLifetimeValue: Double,
    val projections: List<MonthlyMetric>,
    val assumptionSummary: String,
    val calculationTimestamp: Long = System.currentTimeMillis()
)

data class ContentReport(
    val id: Long = 0,
    val ventureId: Long,
    val responseSnippet: String,
    val category: String, // "Offensive", "Unsafe advice", "Incorrect info", "Misleading info", "Other"
    val userComment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Logged Locally"
)
