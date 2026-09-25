package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ads.AdMobManager
import com.example.data.database.VentureDatabase
import com.example.data.database.entity.VentureEntity
import com.example.domain.model.FinancialInputs
import com.example.domain.model.ScenarioType
import com.example.domain.financial.FinancialEngine
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testAppIdentityAndStrings() {
        val appName = context.getString(R.string.app_name)
        assertEquals("VentureForge AI", appName)

        val tagline = context.getString(R.string.app_tagline)
        assertTrue(tagline.isNotEmpty())

        val createVenture = context.getString(R.string.create_new_venture)
        assertEquals("Create New Venture", createVenture)
    }

    @Test
    fun testAdMobConfigurationInDebugMode() {
        val bannerId = AdMobManager.getBannerAdUnitId()
        val interstitialId = AdMobManager.getInterstitialAdUnitId()

        // Debug builds MUST use Google test ad unit IDs
        assertTrue("Banner ad ID must be official test ID in debug", bannerId.contains("3940256099942544"))
        assertTrue("Interstitial ad ID must be official test ID in debug", interstitialId.contains("3940256099942544"))

        // Verify AdMobManager initializes safely
        AdMobManager.initialize(context)
    }

    @Test
    fun testNavigationRoutesIntegrity() {
        val routes = listOf(
            Screen.Splash.route,
            Screen.Onboarding.route,
            Screen.Dashboard.route,
            Screen.CreateVenture.createRoute(),
            Screen.CreateVenture.createRoute(123L),
            Screen.VentureDetail.createRoute(456L),
            Screen.FinancialSimulator.createRoute(),
            Screen.FinancialSimulator.createRoute(789L),
            Screen.Portfolio.route,
            Screen.Comparison.route,
            Screen.Settings.route,
            Screen.PrivacyPolicy.route,
            Screen.TermsOfService.route,
            Screen.AiSafety.route,
            Screen.About.route
        )

        routes.forEach { route ->
            assertNotNull("Route should not be null", route)
            assertTrue("Route '$route' should not be blank", route.isNotBlank())
        }

        assertEquals("venture_detail/456", Screen.VentureDetail.createRoute(456L))
        assertEquals("create_venture?ventureId=123", Screen.CreateVenture.createRoute(123L))
    }

    @Test
    fun testRoomDatabaseCrudOperations() = runBlocking {
        val inMemoryDb = Room.inMemoryDatabaseBuilder(
            context,
            VentureDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val dao = inMemoryDb.ventureDao()

            val testEntity = VentureEntity(
                id = 0,
                name = "Fintech Horizon",
                businessIdea = "AI automated micro-treasury management",
                industry = "Fintech",
                targetMarket = "United States",
                targetAudience = "Small Business Owners",
                customerProblem = "Idle cash loses value with 0% interest",
                proposedSolution = "Automated sweep into yielding risk-free instruments",
                businessModel = "B2B SaaS 0.25% AUM fee",
                estimatedStartupBudget = 25000.0,
                monthlyOperatingBudget = 5000.0,
                teamSize = 3,
                founderExperience = "10 years treasury banking",
                expectedLaunchTimeframe = "6 months",
                revenueModel = "Asset fee + subscription",
                pricingAssumptions = "25 bps on balance",
                customerAcquisitionAssumptions = "Organic direct outreach",
                competitors = "Traditional banks",
                uniqueSellingProposition = "Zero minimum balance sweep",
                additionalNotes = "Targeting fast-growth startups",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val insertedId = dao.insertVenture(testEntity)
            assertTrue("Inserted ID should be positive", insertedId > 0)

            val retrieved = dao.getVentureByIdSync(insertedId)
            assertNotNull(retrieved)
            assertEquals("Fintech Horizon", retrieved?.name)
            assertEquals("Fintech", retrieved?.industry)

            // Update
            val updated = retrieved!!.copy(name = "Fintech Horizon Pro", updatedAt = System.currentTimeMillis())
            dao.updateVenture(updated)

            val updatedRetrieved = dao.getVentureByIdSync(insertedId)
            assertEquals("Fintech Horizon Pro", updatedRetrieved?.name)

            // List Flow
            val allVentures = dao.getAllVentures().first()
            assertEquals(1, allVentures.size)

            // Delete
            dao.deleteVenture(insertedId)
            val afterDelete = dao.getVentureByIdSync(insertedId)
            assertEquals(null, afterDelete)
        } finally {
            inMemoryDb.close()
        }
    }

    @Test
    fun testFinancialEngineDeterministicProjections() {
        val inputs = FinancialInputs(
            startupCost = 20000.0,
            monthlyFixedCosts = 4000.0,
            variableCostPerUnit = 25.0,
            unitPrice = 100.0,
            initialCustomers = 200,
            monthlyGrowthRatePercent = 8.0,
            churnRatePercent = 1.5,
            marketingBudgetMonthly = 1000.0,
            otherExpensesMonthly = 500.0,
            taxRatePercent = 20.0,
            runwayCashReserve = 50000.0,
            projectionMonths = 12
        )

        val baseScenario = FinancialEngine.calculateScenario(inputs, ScenarioType.BASE)
        val optimisticScenario = FinancialEngine.calculateScenario(inputs, ScenarioType.OPTIMISTIC)
        val conservativeScenario = FinancialEngine.calculateScenario(inputs, ScenarioType.CONSERVATIVE)

        assertNotNull(baseScenario)
        assertEquals(12, baseScenario.projections.size)
        assertTrue("Revenue must be positive", baseScenario.totalFirstYearRevenue > 0)
        assertTrue("Expenses must be positive", baseScenario.totalFirstYearExpenses > 0)

        // Optimistic growth rate exceeds conservative
        assertTrue(optimisticScenario.totalFirstYearRevenue > conservativeScenario.totalFirstYearRevenue)

        // Break even units calculation verification: Fixed overhead = 4000 + 1000 + 500 = 5500; Margin = 75
        // 5500 / 75 = 73.33 -> 74 units
        assertEquals(74, baseScenario.breakEvenUnitsPerMonth)
    }

    @Test
    fun testNoExposedSecretsInClient() {
        // Verify BuildConfig has no hardcoded GEMINI_API_KEY
        val hasHardcodedSecret = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val value = field.get(null) as? String ?: ""
            value.isNotBlank() && !value.equals("MY_GEMINI_API_KEY", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
        assertFalse("BuildConfig should not expose a hardcoded Gemini API key", hasHardcodedSecret)
    }
}

