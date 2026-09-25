package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ads.AdMobManager
import com.example.data.ai.AiRepositoryImpl
import com.example.data.database.VentureDatabase
import com.example.data.datastore.UserPreferencesRepository
import com.example.data.repository.VentureRepositoryImpl
import com.example.domain.model.Venture
import com.example.ui.SettingsViewModel
import com.example.ui.VentureViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.ComparisonScreen
import com.example.ui.screens.CreateVentureScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancialSimulatorScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.ReportContentDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VentureDetailScreen
import com.example.ui.theme.VentureForgeTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var database: VentureDatabase
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var ventureRepository: VentureRepositoryImpl
    private lateinit var aiRepository: AiRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize production architecture components
        database = VentureDatabase.getInstance(applicationContext)
        preferencesRepository = UserPreferencesRepository(applicationContext)
        ventureRepository = VentureRepositoryImpl(database)

        // Initialize AdMob Mobile Ads SDK
        AdMobManager.initialize(applicationContext)
        AdMobManager.preloadInterstitial(this)

        setContent {
            val themeMode by preferencesRepository.themeMode.collectAsState(initial = "SYSTEM")
            val language by preferencesRepository.language.collectAsState(initial = "SYSTEM")
            val backendUrl by preferencesRepository.backendUrl.collectAsState(initial = "")

            // Wire AI repository with dynamic backend URL getter
            aiRepository = remember(backendUrl) {
                AiRepositoryImpl(getBackendUrl = { backendUrl })
            }

            val ventureViewModel = remember {
                VentureViewModel(ventureRepository, aiRepository)
            }
            val settingsViewModel = remember {
                SettingsViewModel(preferencesRepository, ventureRepository)
            }

            val darkTheme = when (themeMode.uppercase()) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            val isRtl = language.equals("AR", ignoreCase = true)
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                VentureForgeTheme(darkTheme = darkTheme) {
                    VentureForgeApp(
                        activity = this,
                        ventureViewModel = ventureViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun VentureForgeApp(
    activity: ComponentActivity,
    ventureViewModel: VentureViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val onboardingCompleted by settingsViewModel.onboardingCompleted.collectAsState()
    val allVentures by ventureViewModel.allVentures.collectAsState()
    val selectedVenture by ventureViewModel.selectedVenture.collectAsState()
    val analysisState by ventureViewModel.analysisState.collectAsState()
    val currentAnalysis by ventureViewModel.currentAnalysis.collectAsState()
    val tasks by ventureViewModel.tasks.collectAsState()
    val financialInputs by ventureViewModel.financialInputs.collectAsState()
    val scenarioType by ventureViewModel.selectedScenarioType.collectAsState()
    val financialResult by ventureViewModel.financialResult.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val backendUrl by settingsViewModel.backendUrl.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportVentureId by remember { mutableStateOf(0L) }
    var reportSnippet by remember { mutableStateOf("") }

    if (showReportDialog) {
        ReportContentDialog(
            ventureId = reportVentureId,
            responseSnippet = reportSnippet,
            onDismiss = { showReportDialog = false },
            onSubmitReport = { report ->
                ventureViewModel.submitReport(report) {
                    showReportDialog = false
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (onboardingCompleted) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    settingsViewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                ventures = allVentures,
                onCreateVentureClick = {
                    navController.navigate(Screen.CreateVenture.createRoute())
                },
                onVentureClick = { id ->
                    ventureViewModel.selectVenture(id)
                    navController.navigate(Screen.VentureDetail.createRoute(id))
                },
                onFinancialSimulatorClick = {
                    navController.navigate(Screen.FinancialSimulator.createRoute())
                },
                onPortfolioClick = {
                    navController.navigate(Screen.Portfolio.route)
                },
                onComparisonClick = {
                    navController.navigate(Screen.Comparison.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAiSafetyClick = {
                    navController.navigate(Screen.AiSafety.route)
                }
            )
        }

        composable(
            route = Screen.CreateVenture.route,
            arguments = listOf(navArgument("ventureId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val vIdStr = backStackEntry.arguments?.getString("ventureId")
            val vId = vIdStr?.toLongOrNull()
            val existing = remember(vId, allVentures) {
                if (vId != null) allVentures.find { it.id == vId } else null
            }

            BackHandler {
                navController.popBackStack()
            }

            CreateVentureScreen(
                existingVenture = existing,
                onSaveClick = { venture ->
                    ventureViewModel.saveVenture(venture) { newId ->
                        // Natural transition point for interstitial frequency test
                        AdMobManager.showInterstitialIfEligible(activity) {
                            ventureViewModel.selectVenture(newId)
                            navController.navigate(Screen.VentureDetail.createRoute(newId)) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VentureDetail.route,
            arguments = listOf(navArgument("ventureId") { type = NavType.LongType })
        ) { backStackEntry ->
            val ventureId = backStackEntry.arguments?.getLong("ventureId") ?: 0L

            LaunchedEffect(ventureId) {
                if (ventureId > 0) {
                    ventureViewModel.selectVenture(ventureId)
                }
            }

            BackHandler {
                navController.popBackStack()
            }

            VentureDetailScreen(
                venture = selectedVenture,
                analysisState = analysisState,
                currentAnalysis = currentAnalysis,
                tasks = tasks,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.CreateVenture.createRoute(id))
                },
                onRunAiAnalysis = { v ->
                    ventureViewModel.runAiAnalysis(v)
                },
                onToggleTask = { task ->
                    ventureViewModel.toggleTaskCompletion(task)
                },
                onFinancialSimulatorClick = { id ->
                    navController.navigate(Screen.FinancialSimulator.createRoute(id))
                },
                onReportContentClick = {
                    reportVentureId = ventureId
                    reportSnippet = currentAnalysis?.executiveSummary ?: ""
                    showReportDialog = true
                },
                onExportReport = { ctx, v, an ->
                    ventureViewModel.exportVentureReport(ctx, v, an)
                }
            )
        }

        composable(
            route = Screen.FinancialSimulator.route,
            arguments = listOf(navArgument("ventureId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            BackHandler {
                navController.popBackStack()
            }

            FinancialSimulatorScreen(
                scenarioResult = financialResult,
                currentInputs = financialInputs,
                currentScenarioType = scenarioType,
                onInputsChanged = { ventureViewModel.updateFinancialInputs(it) },
                onScenarioTypeChanged = { ventureViewModel.setScenarioType(it) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Portfolio.route) {
            BackHandler {
                navController.popBackStack()
            }

            PortfolioScreen(
                ventures = allVentures,
                onVentureClick = { id ->
                    ventureViewModel.selectVenture(id)
                    navController.navigate(Screen.VentureDetail.createRoute(id))
                },
                onCreateClick = {
                    navController.navigate(Screen.CreateVenture.createRoute())
                },
                onDuplicateClick = { id ->
                    ventureViewModel.duplicateVenture(id)
                },
                onDeleteClick = { id ->
                    ventureViewModel.deleteVenture(id)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Comparison.route) {
            BackHandler {
                navController.popBackStack()
            }

            ComparisonScreen(
                ventures = allVentures,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            BackHandler {
                navController.popBackStack()
            }

            SettingsScreen(
                currentThemeMode = themeMode,
                currentLanguage = language,
                backendUrl = backendUrl,
                onThemeModeChanged = { settingsViewModel.setThemeMode(it) },
                onLanguageChanged = { settingsViewModel.setLanguage(it) },
                onBackendUrlChanged = { settingsViewModel.setBackendUrl(it) },
                onDeleteAllData = { callback ->
                    settingsViewModel.deleteAllLocalData(callback)
                },
                onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                onTermsClick = { navController.navigate(Screen.TermsOfService.route) },
                onAiSafetyClick = { navController.navigate(Screen.AiSafety.route) },
                onAboutClick = { navController.navigate(Screen.About.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            BackHandler { navController.popBackStack() }
            LegalScreen(
                title = "Privacy & Data Policy",
                contentSections = listOf(
                    "Local Data Storage" to "VentureForge AI operates offline-first. Your venture drafts, SWOT profiles, Business Model Canvas notes, and financial inputs are stored exclusively on your device inside an encrypted/sandboxed SQLite Room database.",
                    "AI Analysis Processing" to "When you explicitly choose to run an AI Analysis, only the relevant venture description is transmitted over secure HTTPS to the backend AI endpoint. Data is not sold, retained for training, or linked to personal identifiers.",
                    "Advertising SDK (AdMob)" to "Google Mobile Ads SDK displays non-intrusive banner and milestone interstitial advertisements. Advertising SDKs do NOT receive your venture data, financial models, or startup confidential plans.",
                    "User Control & Deletion" to "You maintain 100% control over your data. You can delete any individual venture or wipe the entire local database from Settings at any time."
                ),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TermsOfService.route) {
            BackHandler { navController.popBackStack() }
            LegalScreen(
                title = "Terms of Service",
                contentSections = listOf(
                    "Planning & Educational Purpose" to "VentureForge AI is a strategic modeling, business planning, and simulation utility. It does not provide certified financial, legal, tax, or investment advisory services.",
                    "Estimates & No Guarantees" to "AI feasibility scores and financial projections are hypothesis models based on user-entered assumptions. Startup success depends on real-world execution, market demand, and external forces.",
                    "Intellectual Property" to "Users retain full ownership of their ideas, plans, and business strategies generated and saved within the application."
                ),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AiSafety.route) {
            BackHandler { navController.popBackStack() }
            LegalScreen(
                title = "AI Safety & Moderation",
                contentSections = listOf(
                    "Prohibited Content Filters" to "VentureForge AI enforces strict safety guidelines preventing the generation of illegal instructions, deceptive schemes, weapons, exploitation, fraud, or malicious code.",
                    "Transparent Hypothesis Labeling" to "All strategic evaluations are transparently labeled as AI estimates based on input assumptions to avoid misleading founders.",
                    "In-App Content Reporting" to "If any AI output appears offensive, incorrect, misleading, or unsafe, tap 'Report AI Content' on any analysis screen to log and submit the response for human review."
                ),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            BackHandler { navController.popBackStack() }
            LegalScreen(
                title = "About VentureForge AI",
                contentSections = listOf(
                    "Mission" to "VentureForge AI equips founders, entrepreneurs, and product leaders with rigorous strategic analysis, unit economics simulation, and execution roadmaps.",
                    "Technical Architecture" to "Engineered with Kotlin, Jetpack Compose Material 3, Clean Architecture MVVM, Room SQLite Database, deterministic Kotlin financial engine, and Google Mobile Ads SDK.",
                    "Version & Compliance" to "Version 1.0 (Build 36) • Configured for Android 16 (API 36) Google Play target requirements."
                ),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
