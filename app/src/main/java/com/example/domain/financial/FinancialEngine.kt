package com.example.domain.financial

import com.example.domain.model.FinancialInputs
import com.example.domain.model.FinancialScenarioResult
import com.example.domain.model.MonthlyMetric
import com.example.domain.model.ScenarioType
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Deterministic Financial Engine for VentureForge AI.
 * Computes exact monthly unit economics, break-even thresholds, cash-flow runway,
 * and multi-scenario projections without AI approximation.
 */
object FinancialEngine {

    fun calculateScenario(
        baseInputs: FinancialInputs,
        scenarioType: ScenarioType
    ): FinancialScenarioResult {
        // Adjust parameters according to scenario risk assumptions
        val adjustedInputs = when (scenarioType) {
            ScenarioType.CONSERVATIVE -> baseInputs.copy(
                monthlyGrowthRatePercent = max(0.0, baseInputs.monthlyGrowthRatePercent * 0.70), // -30% growth
                churnRatePercent = baseInputs.churnRatePercent * 1.30, // +30% churn
                monthlyFixedCosts = baseInputs.monthlyFixedCosts * 1.15, // +15% fixed costs
                variableCostPerUnit = baseInputs.variableCostPerUnit * 1.15, // +15% COGS
                unitPrice = baseInputs.unitPrice * 0.90 // -10% realized price/discounting
            )
            ScenarioType.BASE -> baseInputs
            ScenarioType.OPTIMISTIC -> baseInputs.copy(
                monthlyGrowthRatePercent = baseInputs.monthlyGrowthRatePercent * 1.35, // +35% growth
                churnRatePercent = max(0.5, baseInputs.churnRatePercent * 0.75), // -25% churn
                monthlyFixedCosts = baseInputs.monthlyFixedCosts * 0.95, // slight fixed cost efficiency
                variableCostPerUnit = baseInputs.variableCostPerUnit * 0.90, // volume discount on variable cost
                unitPrice = baseInputs.unitPrice * 1.05 // +5% pricing power
            )
        }

        val projectionMonths = max(1, adjustedInputs.projectionMonths)
        val projections = ArrayList<MonthlyMetric>(projectionMonths)

        var currentCustomers = adjustedInputs.initialCustomers.toDouble()
        var currentCash = adjustedInputs.runwayCashReserve - adjustedInputs.startupCost
        var breakEvenMonth: Int? = null

        var totalRevenueAccumulator = 0.0
        var totalExpensesAccumulator = 0.0
        var totalNetProfitAccumulator = 0.0

        val contributionMarginPerUnit = adjustedInputs.unitPrice - adjustedInputs.variableCostPerUnit
        val fixedOverheadPerMonth = adjustedInputs.monthlyFixedCosts + adjustedInputs.marketingBudgetMonthly + adjustedInputs.otherExpensesMonthly
        
        val breakEvenUnitsPerMonth = if (contributionMarginPerUnit > 0.0) {
            ceil(fixedOverheadPerMonth / contributionMarginPerUnit).toInt()
        } else {
            Int.MAX_VALUE // Cannot break even if variable cost >= price
        }

        for (month in 1..projectionMonths) {
            // Net monthly customer changes = acquisitions - churn
            val grossNewCustomers = currentCustomers * (adjustedInputs.monthlyGrowthRatePercent / 100.0)
            val churnedCustomers = currentCustomers * (adjustedInputs.churnRatePercent / 100.0)
            currentCustomers = max(0.0, currentCustomers + grossNewCustomers - churnedCustomers)

            val activeCustomersCount = currentCustomers.roundToInt()
            val monthlyRevenue = activeCustomersCount * adjustedInputs.unitPrice
            val monthlyVariableCosts = activeCustomersCount * adjustedInputs.variableCostPerUnit
            val monthlyFixedCosts = adjustedInputs.monthlyFixedCosts
            val monthlyMarketing = adjustedInputs.marketingBudgetMonthly
            val monthlyOther = adjustedInputs.otherExpensesMonthly

            val totalMonthlyOperatingExpenses = monthlyVariableCosts + monthlyFixedCosts + monthlyMarketing + monthlyOther
            val grossProfit = monthlyRevenue - monthlyVariableCosts
            val operatingProfitBeforeTax = monthlyRevenue - totalMonthlyOperatingExpenses
            val taxAmount = if (operatingProfitBeforeTax > 0.0) {
                operatingProfitBeforeTax * (adjustedInputs.taxRatePercent / 100.0)
            } else 0.0
            val netProfit = operatingProfitBeforeTax - taxAmount

            currentCash += netProfit

            if (breakEvenMonth == null && netProfit >= 0.0) {
                breakEvenMonth = month
            }

            totalRevenueAccumulator += monthlyRevenue
            totalExpensesAccumulator += totalMonthlyOperatingExpenses + taxAmount
            totalNetProfitAccumulator += netProfit

            projections.add(
                MonthlyMetric(
                    monthNumber = month,
                    customerCount = activeCustomersCount,
                    revenue = monthlyRevenue,
                    fixedCosts = monthlyFixedCosts,
                    variableCosts = monthlyVariableCosts,
                    marketingCosts = monthlyMarketing,
                    otherCosts = monthlyOther,
                    totalExpenses = totalMonthlyOperatingExpenses + taxAmount,
                    grossProfit = grossProfit,
                    netProfit = netProfit,
                    cashBalance = currentCash
                )
            )
        }

        // Calculate Customer Acquisition Cost (CAC) and Lifetime Value (LTV) estimates
        val estimatedMonthlyAcquisitions = max(1.0, adjustedInputs.initialCustomers * (adjustedInputs.monthlyGrowthRatePercent / 100.0))
        val estimatedCAC = adjustedInputs.marketingBudgetMonthly / estimatedMonthlyAcquisitions
        
        val averageCustomerLifespanMonths = if (adjustedInputs.churnRatePercent > 0.0) {
            100.0 / adjustedInputs.churnRatePercent
        } else {
            36.0 // cap assumption at 36 months if zero churn
        }
        val estimatedLTV = contributionMarginPerUnit * averageCustomerLifespanMonths

        // Runway calculation: remaining cash reserve divided by average monthly burn if negative
        val firstMonthNet = projections.firstOrNull()?.netProfit ?: 0.0
        val runwayMonthsRemaining = if (firstMonthNet < 0.0) {
            val monthlyBurn = -firstMonthNet
            if (monthlyBurn > 0.0) adjustedInputs.runwayCashReserve / monthlyBurn else Double.POSITIVE_INFINITY
        } else {
            Double.POSITIVE_INFINITY // Self-sustaining
        }

        val assumptionText = when (scenarioType) {
            ScenarioType.CONSERVATIVE -> "Conservative scenario assumes 30% lower customer growth, 30% higher churn, 15% higher operating costs, and 10% price realization discount."
            ScenarioType.BASE -> "Base scenario reflects your original assumptions for pricing, unit costs, growth rate, and fixed overhead."
            ScenarioType.OPTIMISTIC -> "Optimistic scenario models 35% higher customer growth, 25% lower churn, 5% price premium, and 10% variable cost volume efficiencies."
        }

        return FinancialScenarioResult(
            scenarioType = scenarioType,
            inputs = adjustedInputs,
            monthlyRevenueMonth12 = projections.lastOrNull()?.revenue ?: 0.0,
            totalFirstYearRevenue = totalRevenueAccumulator,
            totalFirstYearExpenses = totalExpensesAccumulator,
            totalFirstYearNetProfit = totalNetProfitAccumulator,
            breakEvenUnitsPerMonth = breakEvenUnitsPerMonth,
            breakEvenMonth = breakEvenMonth,
            runwayMonthsRemaining = runwayMonthsRemaining,
            customerAcquisitionCostEstimated = estimatedCAC,
            estimatedCustomerLifetimeValue = estimatedLTV,
            projections = projections,
            assumptionSummary = assumptionText
        )
    }
}
