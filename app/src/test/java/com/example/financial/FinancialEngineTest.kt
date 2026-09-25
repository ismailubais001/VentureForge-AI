package com.example.financial

import com.example.domain.financial.FinancialEngine
import com.example.domain.model.FinancialInputs
import com.example.domain.model.ScenarioType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialEngineTest {

    @Test
    fun testBaseScenarioDeterminism() {
        val inputs = FinancialInputs(
            startupCost = 10000.0,
            monthlyFixedCosts = 2000.0,
            variableCostPerUnit = 10.0,
            unitPrice = 50.0,
            initialCustomers = 100,
            monthlyGrowthRatePercent = 10.0,
            churnRatePercent = 2.0,
            marketingBudgetMonthly = 500.0,
            otherExpensesMonthly = 200.0,
            taxRatePercent = 15.0,
            runwayCashReserve = 30000.0,
            projectionMonths = 12
        )

        val result1 = FinancialEngine.calculateScenario(inputs, ScenarioType.BASE)
        val result2 = FinancialEngine.calculateScenario(inputs, ScenarioType.BASE)

        // Must be 100% deterministic
        assertEquals(result1.totalFirstYearRevenue, result2.totalFirstYearRevenue, 0.001)
        assertEquals(result1.totalFirstYearExpenses, result2.totalFirstYearExpenses, 0.001)
        assertEquals(result1.totalFirstYearNetProfit, result2.totalFirstYearNetProfit, 0.001)
        assertEquals(result1.breakEvenUnitsPerMonth, result2.breakEvenUnitsPerMonth)
        assertEquals(12, result1.projections.size)
    }

    @Test
    fun testBreakEvenCalculation() {
        // Price = 50, VariableCost = 10 -> Contribution Margin = 40
        // Fixed = 2000, Marketing = 500, Other = 200 -> Overhead = 2700
        // BreakEvenUnits = ceil(2700 / 40) = 68 units
        val inputs = FinancialInputs(
            unitPrice = 50.0,
            variableCostPerUnit = 10.0,
            monthlyFixedCosts = 2000.0,
            marketingBudgetMonthly = 500.0,
            otherExpensesMonthly = 200.0
        )

        val result = FinancialEngine.calculateScenario(inputs, ScenarioType.BASE)
        assertEquals(68, result.breakEvenUnitsPerMonth)
    }

    @Test
    fun testScenarioComparisonVariations() {
        val inputs = FinancialInputs(
            unitPrice = 100.0,
            variableCostPerUnit = 20.0,
            monthlyFixedCosts = 3000.0,
            initialCustomers = 50,
            monthlyGrowthRatePercent = 15.0,
            churnRatePercent = 3.0
        )

        val conservative = FinancialEngine.calculateScenario(inputs, ScenarioType.CONSERVATIVE)
        val base = FinancialEngine.calculateScenario(inputs, ScenarioType.BASE)
        val optimistic = FinancialEngine.calculateScenario(inputs, ScenarioType.OPTIMISTIC)

        // Optimistic revenue should exceed Base, which exceeds Conservative
        assertTrue(optimistic.totalFirstYearRevenue > base.totalFirstYearRevenue)
        assertTrue(base.totalFirstYearRevenue > conservative.totalFirstYearRevenue)

        // Optimistic net profit should exceed Conservative
        assertTrue(optimistic.totalFirstYearNetProfit > conservative.totalFirstYearNetProfit)
    }

    @Test
    fun testZeroAndExtremeValues() {
        val zeroInputs = FinancialInputs(
            startupCost = 0.0,
            monthlyFixedCosts = 0.0,
            variableCostPerUnit = 0.0,
            unitPrice = 0.0,
            initialCustomers = 0,
            monthlyGrowthRatePercent = 0.0,
            churnRatePercent = 0.0,
            marketingBudgetMonthly = 0.0,
            otherExpensesMonthly = 0.0,
            taxRatePercent = 0.0,
            runwayCashReserve = 0.0
        )

        val result = FinancialEngine.calculateScenario(zeroInputs, ScenarioType.BASE)
        assertNotNull(result)
        assertEquals(0.0, result.totalFirstYearRevenue, 0.001)
        assertEquals(0.0, result.totalFirstYearExpenses, 0.001)
        assertEquals(0.0, result.totalFirstYearNetProfit, 0.001)
    }
}
