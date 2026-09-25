package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ads.AdBannerView
import com.example.domain.model.FinancialInputs
import com.example.domain.model.FinancialScenarioResult
import com.example.domain.model.ScenarioType
import com.example.ui.components.FinancialChart
import com.example.ui.components.MetricCard
import com.example.ui.components.SafetyDisclaimerCard
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.RiskRed
import com.example.ui.theme.TechTeal
import com.example.ui.theme.WarningOrange
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialSimulatorScreen(
    scenarioResult: FinancialScenarioResult?,
    currentInputs: FinancialInputs,
    currentScenarioType: ScenarioType,
    onInputsChanged: (FinancialInputs) -> Unit,
    onScenarioTypeChanged: (ScenarioType) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Deterministic Financial Simulator", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scenario Selection Chips
            item {
                Column {
                    Text(
                        text = "Modeling Scenario",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScenarioType.entries.forEach { type ->
                            FilterChip(
                                selected = currentScenarioType == type,
                                onClick = { onScenarioTypeChanged(type) },
                                label = {
                                    Text(
                                        when (type) {
                                            ScenarioType.CONSERVATIVE -> "Conservative"
                                            ScenarioType.BASE -> "Base Case"
                                            ScenarioType.OPTIMISTIC -> "Optimistic"
                                        }
                                    )
                                },
                                modifier = Modifier.testTag("scenario_chip_${type.name}")
                            )
                        }
                    }
                    if (scenarioResult != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = scenarioResult.assumptionSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Key Results Metrics
            if (scenarioResult != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Month 12 Revenue",
                                value = "$${formatCurrency(scenarioResult.monthlyRevenueMonth12)}",
                                subtitle = "Run-rate / month",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                accentColor = ProfitGreen,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Year 1 Net Profit",
                                value = "$${formatCurrency(scenarioResult.totalFirstYearNetProfit)}",
                                subtitle = if (scenarioResult.totalFirstYearNetProfit >= 0) "Net Positive" else "Burn Period",
                                icon = Icons.Default.AttachMoney,
                                accentColor = if (scenarioResult.totalFirstYearNetProfit >= 0) ProfitGreen else RiskRed,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Break-Even Units",
                                value = "${scenarioResult.breakEvenUnitsPerMonth} /mo",
                                subtitle = scenarioResult.breakEvenMonth?.let { "Month $it projected" } ?: "Beyond Year 1",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                accentColor = TechTeal,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Cash Runway",
                                value = if (scenarioResult.runwayMonthsRemaining.isInfinite()) "Profitable" else "${scenarioResult.runwayMonthsRemaining.toInt()} Mo",
                                subtitle = "Reserve: $${formatCurrency(scenarioResult.inputs.runwayCashReserve)}",
                                icon = Icons.Default.HourglassBottom,
                                accentColor = if (scenarioResult.runwayMonthsRemaining.isInfinite() || scenarioResult.runwayMonthsRemaining > 12) ProfitGreen else WarningOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Financial Visual Chart
                item {
                    FinancialChart(metrics = scenarioResult.projections)
                }

                // Unit Economics Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Unit Economics & Customer Dynamics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Customer Acquisition Cost (CAC est.):", style = MaterialTheme.typography.bodyMedium)
                                Text("$${formatCurrency(scenarioResult.customerAcquisitionCostEstimated)}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Lifetime Value (LTV):", style = MaterialTheme.typography.bodyMedium)
                                Text("$${formatCurrency(scenarioResult.estimatedCustomerLifetimeValue)}", fontWeight = FontWeight.Bold, color = ProfitGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val ltvCacRatio = if (scenarioResult.customerAcquisitionCostEstimated > 0) {
                                scenarioResult.estimatedCustomerLifetimeValue / scenarioResult.customerAcquisitionCostEstimated
                            } else 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("LTV / CAC Ratio:", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    String.format(Locale.US, "%.1fx", ltvCacRatio),
                                    fontWeight = FontWeight.Bold,
                                    color = if (ltvCacRatio >= 3.0) ProfitGreen else WarningOrange
                                )
                            }
                        }
                    }
                }
            }

            // Editable Assumptions Inputs Section
            item {
                Text(
                    text = "Financial Modeling Assumptions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentInputs.unitPrice.toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.unitPrice
                            onInputsChanged(currentInputs.copy(unitPrice = v))
                        },
                        label = { Text("Price Per Unit ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentInputs.variableCostPerUnit.toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.variableCostPerUnit
                            onInputsChanged(currentInputs.copy(variableCostPerUnit = v))
                        },
                        label = { Text("Unit Variable Cost ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentInputs.initialCustomers.toString(),
                        onValueChange = {
                            val v = it.toIntOrNull() ?: currentInputs.initialCustomers
                            onInputsChanged(currentInputs.copy(initialCustomers = v))
                        },
                        label = { Text("Initial Customers") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentInputs.monthlyGrowthRatePercent.toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.monthlyGrowthRatePercent
                            onInputsChanged(currentInputs.copy(monthlyGrowthRatePercent = v))
                        },
                        label = { Text("Monthly Growth (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentInputs.churnRatePercent.toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.churnRatePercent
                            onInputsChanged(currentInputs.copy(churnRatePercent = v))
                        },
                        label = { Text("Monthly Churn (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentInputs.marketingBudgetMonthly.toInt().toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.marketingBudgetMonthly
                            onInputsChanged(currentInputs.copy(marketingBudgetMonthly = v))
                        },
                        label = { Text("Marketing / Mo ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentInputs.monthlyFixedCosts.toInt().toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.monthlyFixedCosts
                            onInputsChanged(currentInputs.copy(monthlyFixedCosts = v))
                        },
                        label = { Text("Fixed Overhead / Mo ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentInputs.runwayCashReserve.toInt().toString(),
                        onValueChange = {
                            val v = it.toDoubleOrNull() ?: currentInputs.runwayCashReserve
                            onInputsChanged(currentInputs.copy(runwayCashReserve = v))
                        },
                        label = { Text("Cash Reserve ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SafetyDisclaimerCard()
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format(Locale.US, "%,.0f", amount)
}
