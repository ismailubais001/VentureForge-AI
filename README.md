# VentureForge AI — Enterprise Android Application

**VentureForge AI** is a commercial-grade, production-ready Android mobile application built for entrepreneurs, startup founders, and venture strategists. It provides deep strategic venture modeling, opportunity evaluation, SWOT analysis, a 9-block Business Model Canvas, competitor mapping, execution roadmap tracking, and a 100% deterministic local financial calculation engine.

---

## 1. Architectural Highlights

- **UI Layer**: Jetpack Compose using Material 3 design system, responsive window handling, full Dark & Light mode persistence, and bidirectional layout support (LTR English & RTL Arabic).
- **Navigation**: Navigation Compose with safe routing and system `BackHandler` integrations.
- **Data Persistence**: Offline-first Room Database (`VentureDatabase`) caching all ventures, analyses, action milestones, and user-reported content.
- **Settings & State**: Android DataStore Preferences for user preferences (Theme, Language, Onboarding, AI Backend URL).
- **Financial Calculation Engine**: Pure deterministic Kotlin engine (`FinancialEngine.kt`). Zero AI guesswork for arithmetic; calculates exact monthly projections, break-even unit volumes, cash runway, customer churn, CAC, and LTV across Conservative, Base, and Optimistic models.
- **AI Integration**: Hybrid architecture supporting both secure custom server-side proxy (`/backend`) and Google Gemini API (via server-side Gemini capability and AI Studio Secrets panel). Rigorous JSON schema validation, input safety filters, and resilient fallback parsing.
- **In-App Content Reporting**: Native report dialog logging flagged responses locally to Room and forwarding to the moderation endpoint (`/api/report-content`).
- **Monetization (AdMob)**: Official Google Mobile Ads SDK with Banner and Interstitial placements. Interstitial ads follow strict frequency cooldowns (minimum 180 seconds interval) and never interrupt critical user workflows, form inputs, onboarding, startup, or app exit.

---

## 2. Directory Structure

```
ventureforge-ai/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── VentureViewModel.kt
│   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   ├── core/
│   │   │   │   │   ├── model/Result.kt
│   │   │   │   │   └── network/NetworkMonitor.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/VentureModels.kt
│   │   │   │   │   ├── financial/FinancialEngine.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── VentureRepository.kt
│   │   │   │   │       └── AiRepository.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── ads/AdMobManager.kt
│   │   │   │   │   ├── ai/AiRepositoryImpl.kt
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── VentureDatabase.kt
│   │   │   │   │   │   ├── converters/Converters.kt
│   │   │   │   │   │   ├── dao/VentureDao.kt
│   │   │   │   │   │   └── entity/VentureEntities.kt
│   │   │   │   │   ├── datastore/UserPreferencesRepository.kt
│   │   │   │   │   └── repository/VentureRepositoryImpl.kt
│   │   │   │   └── ui/
│   │   │   │       ├── components/
│   │   │   │       │   ├── EmptyState.kt
│   │   │   │       │   ├── FinancialChart.kt
│   │   │   │       │   ├── MetricCard.kt
│   │   │   │       │   └── SafetyDisclaimerCard.kt
│   │   │   │       ├── navigation/Screen.kt
│   │   │   │       ├── screens/
│   │   │   │       │   ├── SplashScreen.kt
│   │   │   │       │   ├── OnboardingScreen.kt
│   │   │   │       │   ├── DashboardScreen.kt
│   │   │   │       │   ├── CreateVentureScreen.kt
│   │   │   │       │   ├── VentureDetailScreen.kt
│   │   │   │       │   ├── FinancialSimulatorScreen.kt
│   │   │   │       │   ├── PortfolioScreen.kt
│   │   │   │       │   ├── ComparisonScreen.kt
│   │   │   │       │   ├── ReportContentDialog.kt
│   │   │   │       │   ├── SettingsScreen.kt
│   │   │   │       │   └── LegalScreen.kt
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt
│   │   │   │           ├── Theme.kt
│   │   │   │           └── Type.kt
│   │   │   └── res/
│   │   │       ├── values/strings.xml
│   │   │       ├── values-ar/strings.xml (Arabic RTL)
│   │   │       ├── drawable/ (App icon & hero artwork)
│   │   │       └── mipmap-*/ (Adaptive launcher icons)
│   │   └── test/java/com/example/
│   │       ├── ExampleRobolectricTest.kt
│   │       ├── financial/FinancialEngineTest.kt
│   │       └── ai/AiValidationTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── backend/
│   ├── Dockerfile
│   ├── package.json
│   ├── tsconfig.json
│   ├── src/index.ts
│   └── README.md
├── .env.example
├── build.gradle.kts
├── metadata.json
└── settings.gradle.kts
```

---

## 3. Configuration & Secrets Management

### AdMob Configuration
The application automatically segregates Test and Production ad unit IDs:
- **Debug Builds**: Uses Google official test ad units (`ca-app-pub-3940256099942544~3347511713`).
- **Release Builds**:
  - Application ID: `ca-app-pub-8520330803942207~6190012516`
  - Banner Unit ID: `ca-app-pub-8520330803942207/5263943658`
  - Interstitial Unit ID: `ca-app-pub-8520330803942207/9585050783`
  - Rewarded Ads: Not implemented.

### AI Service Connectivity
1. **Direct Gemini Mode**: Inject `GEMINI_API_KEY` through the AI Studio Secrets panel or `.env`.
2. **Enterprise Proxy Mode**: Run the included Node.js TypeScript server (`backend/`) on Google Cloud Run or a VPS, and enter its base URL in Settings.

---

## 4. Google Play Store Declarations & Compliance

### Target API Audit
- `compileSdk`: 36 (Android 16 release)
- `targetSdk`: 36
- `minSdk`: 24 (Android 7.0+)
- `applicationId`: `com.aistudio.ventureforge.aiwkrp`
- `supportsRtl`: `true`

### Permissions Declaration
- `android.permission.INTERNET`: Normal install-time permission for AI analysis and AdMob ads.
- `android.permission.ACCESS_NETWORK_STATE`: Normal install-time permission for offline detection.
- **Zero Dangerous Permissions**: No camera, microphone, contacts, location, SMS, or broad storage requested.

### Data Safety Declaration
- **Data Collected & Shared**: No personal identifiable data collected.
- **Venture Data**: Kept exclusively local on device in Room SQLite database.
- **AI Processing**: Venture description text transmitted over TLS/HTTPS to AI provider only upon explicit user request.
- **Advertising**: AdMob SDK collects standard anonymous advertising identifiers in compliance with Google Play Families and Advertising policies.
- **Data Deletion**: Complete in-app data deletion implemented in Settings ("Reset Local Storage").
