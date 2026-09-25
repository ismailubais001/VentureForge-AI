package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object CreateVenture : Screen("create_venture?ventureId={ventureId}") {
        fun createRoute(ventureId: Long? = null): String {
            return if (ventureId != null) "create_venture?ventureId=$ventureId" else "create_venture"
        }
    }
    data object VentureDetail : Screen("venture_detail/{ventureId}") {
        fun createRoute(ventureId: Long): String = "venture_detail/$ventureId"
    }
    data object FinancialSimulator : Screen("financial_simulator?ventureId={ventureId}") {
        fun createRoute(ventureId: Long? = null): String {
            return if (ventureId != null) "financial_simulator?ventureId=$ventureId" else "financial_simulator"
        }
    }
    data object Portfolio : Screen("portfolio")
    data object Comparison : Screen("comparison")
    data object Settings : Screen("settings")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object TermsOfService : Screen("terms_of_service")
    data object AiSafety : Screen("ai_safety")
    data object About : Screen("about")
}
