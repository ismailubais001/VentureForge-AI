package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.core.model.Result
import com.example.data.ads.AdBannerView
import com.example.domain.model.ActionTask
import com.example.domain.model.CompetitorInsight
import com.example.domain.model.FeasibilityScore
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis
import com.example.ui.components.EmptyState
import com.example.ui.components.MetricCard
import com.example.ui.components.SafetyDisclaimerCard
import com.example.ui.theme.ForgeGold
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.RiskRed
import com.example.ui.theme.TechTeal
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentureDetailScreen(
    venture: Venture?,
    analysisState: Result<VentureAnalysis>?,
    currentAnalysis: VentureAnalysis?,
    tasks: List<ActionTask>,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onRunAiAnalysis: (Venture) -> Unit,
    onToggleTask: (ActionTask) -> Unit,
    onFinancialSimulatorClick: (Long) -> Unit,
    onReportContentClick: () -> Unit,
    onExportReport: (Context, Venture, VentureAnalysis?) -> Unit
) {
    if (venture == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            "Overview",
            "Feasibility",
            "SWOT",
            "Canvas",
            "Competitors",
            "Strategy",
            "Tasks"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = venture.name,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${venture.industry} • ${venture.businessModel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEditClick(venture.id) },
                        modifier = Modifier.testTag("detail_edit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = { onExportReport(context, venture, currentAnalysis) },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AdBannerView(modifier = Modifier.padding(vertical = 4.dp))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header AI Generation Banner if not yet analyzed or error
                if (currentAnalysis == null && analysisState !is Result.Loading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = ForgeGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Generate Strategic AI Analysis",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Evaluate market viability, risks, unit economics, and 9-block Business Model Canvas using VentureForge intelligence.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onRunAiAnalysis(venture) },
                                    modifier = Modifier.testTag("run_ai_analysis_button")
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze Venture")
                                }
                            }
                        }
                    }
                }

                if (analysisState is Result.Loading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = ForgeGold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Analyzing Venture Dynamics...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Evaluating market demand, unit economics, competition, and risk profiles...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (analysisState is Result.Error) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Analysis Notice",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysisState.message ?: "Unable to complete AI analysis.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onRunAiAnalysis(venture) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Retry Analysis")
                                }
                            }
                        }
                    }
                }

                // Render selected tab
                when (selectedTabIndex) {
                    0 -> renderOverviewTab(venture, currentAnalysis, onFinancialSimulatorClick)
                    1 -> renderFeasibilityTab(currentAnalysis?.feasibilityScore)
                    2 -> renderSwotTab(currentAnalysis)
                    3 -> renderCanvasTab(venture, currentAnalysis)
                    4 -> renderCompetitorsTab(currentAnalysis?.competitors)
                    5 -> renderStrategyTab(currentAnalysis)
                    6 -> renderTasksTab(tasks, onToggleTask)
                }

                // Safety Disclaimer Card
                item {
                    SafetyDisclaimerCard(onReportClick = onReportContentClick)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-TAB RENDERERS
// -------------------------------------------------------------

private fun androidx.compose.foundation.lazy.LazyListScope.renderOverviewTab(
    venture: Venture,
    analysis: VentureAnalysis?,
    onFinancialSimulatorClick: (Long) -> Unit
) {
    item {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Core Value Proposition",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = venture.businessIdea,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Problem Statement",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = venture.customerProblem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Proposed Solution",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = venture.proposedSolution,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (analysis != null) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Executive Summary (AI Estimate)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = analysis.executiveSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    item {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFinancialSimulatorClick(venture.id) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Financial Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Startup Budget: $${venture.estimatedStartupBudget.toInt()} • Monthly: $${venture.monthlyOperatingBudget.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = TechTeal
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderFeasibilityTab(
    score: FeasibilityScore?
) {
    if (score == null) {
        item {
            EmptyState(
                icon = Icons.Default.Analytics,
                title = "Feasibility Analysis Not Generated",
                description = "Run AI Analysis on this venture to generate multi-dimensional feasibility ratings and risk assessments."
            )
        }
        return
    }

    item {
        val scoreColor = when {
            score.overallScore >= 75 -> ProfitGreen
            score.overallScore >= 50 -> WarningOrange
            else -> RiskRed
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Overall Feasibility Estimate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(scoreColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${score.overallScore}/100",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Risk Profile: ${score.riskLevel}",
                    style = MaterialTheme.typography.labelLarge,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = score.disclaimerNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }

    item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreBarItem("Problem Urgency & Clarity", score.problemClarityScore)
            ScoreBarItem("Market Demand & Accessibility", score.marketDemandScore)
            ScoreBarItem("Competitive Defensibility", score.competitionDifficultyScore)
            ScoreBarItem("Unit Economics & Margin Potential", score.unitEconomicsScore)
            ScoreBarItem("Execution Feasibility", score.executionFeasibilityScore)
        }
    }

    item {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Underlying Assumptions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                score.keyAssumptions.forEach { assumption ->
                    Text(
                        text = "• $assumption",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreBarItem(label: String, value: Int) {
    val color = when {
        value >= 75 -> ProfitGreen
        value >= 50 -> WarningOrange
        else -> RiskRed
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = "$value%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (value / 100f).coerceIn(0f, 1f))
                        .height(8.dp)
                        .background(color, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderSwotTab(
    analysis: VentureAnalysis?
) {
    if (analysis == null) {
        item {
            EmptyState(
                icon = Icons.Default.Analytics,
                title = "SWOT Analysis Not Generated",
                description = "Run AI Analysis on this venture to populate Strengths, Weaknesses, Opportunities, and Threats linked specifically to your venture inputs."
            )
        }
        return
    }

    item { SwotSectionCard("Strengths (Internal)", analysis.strengths, ProfitGreen) }
    item { SwotSectionCard("Weaknesses (Internal)", analysis.weaknesses, WarningOrange) }
    item { SwotSectionCard("Opportunities (External)", analysis.opportunities, TechTeal) }
    item { SwotSectionCard("Threats (External)", analysis.threats, RiskRed) }
}

@Composable
private fun SwotSectionCard(title: String, items: List<String>, accentColor: Color) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text("None identified", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                items.forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderCanvasTab(
    venture: Venture,
    analysis: VentureAnalysis?
) {
    item {
        Text(
            text = "Business Model Canvas (9 Blocks)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    item { CanvasBlockItem("1. Customer Segments", listOf(venture.targetAudience.ifEmpty { "Defined market target segment" })) }
    item { CanvasBlockItem("2. Value Propositions", listOf(venture.uniqueSellingProposition.ifEmpty { venture.proposedSolution })) }
    item { CanvasBlockItem("3. Channels", listOf(venture.customerAcquisitionAssumptions.ifEmpty { "Direct digital channels" })) }
    item { CanvasBlockItem("4. Customer Relationships", listOf("Self-service onboarding", "Direct founder support")) }
    item { CanvasBlockItem("5. Revenue Streams", listOf(venture.revenueModel.ifEmpty { "Unit sales / recurring subscription" }, venture.pricingAssumptions)) }
    item { CanvasBlockItem("6. Key Resources", listOf("Core software & data IP", "Founder team (${venture.teamSize} members)")) }
    item { CanvasBlockItem("7. Key Activities", listOf("Product development", "Customer validation", "Go-to-market execution")) }
    item { CanvasBlockItem("8. Key Partnerships", listOf("Cloud hosting & API providers", "Strategic distribution allies")) }
    item { CanvasBlockItem("9. Cost Structure", listOf("Startup budget: $${venture.estimatedStartupBudget.toInt()}", "Monthly burn: $${venture.monthlyOperatingBudget.toInt()}")) }
}

@Composable
private fun CanvasBlockItem(title: String, items: List<String>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            items.forEach { line ->
                Text(text = "• $line", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderCompetitorsTab(
    competitors: List<CompetitorInsight>?
) {
    if (competitors.isNullOrEmpty()) {
        item {
            EmptyState(
                icon = Icons.Default.Analytics,
                title = "No Competitors Analyzed",
                description = "Run AI Analysis on this venture to generate competitor threat ratings, positioning, and differentiation strategies."
            )
        }
        return
    }

    items(competitors) { comp ->
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comp.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val threatColor = when (comp.threatLevel.lowercase()) {
                        "high" -> RiskRed
                        "medium" -> WarningOrange
                        else -> ProfitGreen
                    }
                    Text(
                        text = "Threat: ${comp.threatLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = threatColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Differentiation Moat: ${comp.differentiation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Strategic Note: ${comp.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderStrategyTab(
    analysis: VentureAnalysis?
) {
    if (analysis == null) {
        item {
            EmptyState(
                icon = Icons.Default.Analytics,
                title = "Strategy Not Generated",
                description = "Run AI Analysis to generate recommendations, market risks, and strategic roadmap."
            )
        }
        return
    }

    item {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Strategic Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                analysis.recommendations.forEach { rec ->
                    Text(text = "• $rec", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }

    item {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Identified Market & Execution Risks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RiskRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                analysis.marketRisks.forEach { risk ->
                    Text(text = "• $risk", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderTasksTab(
    tasks: List<ActionTask>,
    onToggleTask: (ActionTask) -> Unit
) {
    if (tasks.isEmpty()) {
        item {
            EmptyState(
                icon = Icons.Default.CheckCircle,
                title = "No Action Tasks Yet",
                description = "Run AI Analysis to generate an execution roadmap with specific validation, MVP, and launch tasks."
            )
        }
        return
    }

    items(tasks) { task ->
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleTask(task) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) ProfitGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.taskName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${task.phase} • Est: ${task.estimatedDays} days • Priority: ${task.priority}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
