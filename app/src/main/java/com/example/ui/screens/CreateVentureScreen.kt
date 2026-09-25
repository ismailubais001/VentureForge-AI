package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Venture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVentureScreen(
    existingVenture: Venture? = null,
    onSaveClick: (Venture) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember(existingVenture) { mutableStateOf(existingVenture?.name ?: "") }
    var businessIdea by remember(existingVenture) { mutableStateOf(existingVenture?.businessIdea ?: "") }
    var industry by remember(existingVenture) { mutableStateOf(existingVenture?.industry ?: "") }
    var targetMarket by remember(existingVenture) { mutableStateOf(existingVenture?.targetMarket ?: "") }
    var targetAudience by remember(existingVenture) { mutableStateOf(existingVenture?.targetAudience ?: "") }
    var customerProblem by remember(existingVenture) { mutableStateOf(existingVenture?.customerProblem ?: "") }
    var proposedSolution by remember(existingVenture) { mutableStateOf(existingVenture?.proposedSolution ?: "") }
    var businessModel by remember(existingVenture) { mutableStateOf(existingVenture?.businessModel ?: "B2B SaaS") }
    var startupBudget by remember(existingVenture) {
        mutableStateOf(existingVenture?.estimatedStartupBudget?.takeIf { it > 0 }?.toInt()?.toString() ?: "10000")
    }
    var monthlyBudget by remember(existingVenture) {
        mutableStateOf(existingVenture?.monthlyOperatingBudget?.takeIf { it > 0 }?.toInt()?.toString() ?: "2500")
    }
    var teamSize by remember(existingVenture) {
        mutableStateOf(existingVenture?.teamSize?.toString() ?: "2")
    }
    var founderExperience by remember(existingVenture) { mutableStateOf(existingVenture?.founderExperience ?: "5 years in software & operations") }
    var launchTimeframe by remember(existingVenture) { mutableStateOf(existingVenture?.expectedLaunchTimeframe ?: "3-6 months") }
    var revenueModel by remember(existingVenture) { mutableStateOf(existingVenture?.revenueModel ?: "Monthly subscription tiers") }
    var pricingAssumptions by remember(existingVenture) { mutableStateOf(existingVenture?.pricingAssumptions ?: "$49/month per active user") }
    var acquisitionAssumptions by remember(existingVenture) { mutableStateOf(existingVenture?.customerAcquisitionAssumptions ?: "Direct outbound sales & content marketing") }
    var competitors by remember(existingVenture) { mutableStateOf(existingVenture?.competitors ?: "") }
    var uniqueSellingProposition by remember(existingVenture) { mutableStateOf(existingVenture?.uniqueSellingProposition ?: "") }
    var additionalNotes by remember(existingVenture) { mutableStateOf(existingVenture?.additionalNotes ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var ideaError by remember { mutableStateOf<String?>(null) }
    var problemError by remember { mutableStateOf<String?>(null) }
    var solutionError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (name.trim().isBlank()) {
            nameError = "Venture name is required"
            isValid = false
        } else {
            nameError = null
        }

        if (businessIdea.trim().length < 10) {
            ideaError = "Please describe the business idea (min 10 characters)"
            isValid = false
        } else {
            ideaError = null
        }

        if (customerProblem.trim().length < 5) {
            problemError = "Customer problem statement is required"
            isValid = false
        } else {
            problemError = null
        }

        if (proposedSolution.trim().length < 5) {
            solutionError = "Proposed solution is required"
            isValid = false
        } else {
            solutionError = null
        }

        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingVenture != null) "Edit Venture" else "New Venture Plan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("create_venture_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validate()) {
                                onSaveClick(
                                    Venture(
                                        id = existingVenture?.id ?: 0L,
                                        name = name.trim(),
                                        businessIdea = businessIdea.trim(),
                                        industry = industry.trim().ifEmpty { "General Tech / Service" },
                                        targetMarket = targetMarket.trim().ifEmpty { "North America / Global" },
                                        targetAudience = targetAudience.trim(),
                                        customerProblem = customerProblem.trim(),
                                        proposedSolution = proposedSolution.trim(),
                                        businessModel = businessModel.trim(),
                                        estimatedStartupBudget = startupBudget.toDoubleOrNull() ?: 10000.0,
                                        monthlyOperatingBudget = monthlyBudget.toDoubleOrNull() ?: 2500.0,
                                        teamSize = teamSize.toIntOrNull() ?: 1,
                                        founderExperience = founderExperience.trim(),
                                        expectedLaunchTimeframe = launchTimeframe.trim(),
                                        revenueModel = revenueModel.trim(),
                                        pricingAssumptions = pricingAssumptions.trim(),
                                        customerAcquisitionAssumptions = acquisitionAssumptions.trim(),
                                        competitors = competitors.trim(),
                                        uniqueSellingProposition = uniqueSellingProposition.trim(),
                                        additionalNotes = additionalNotes.trim(),
                                        createdAt = existingVenture?.createdAt ?: System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("create_venture_top_save_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save Venture")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "1. Core Identity & Concept",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Venture / Startup Name *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_venture_name")
            )

            OutlinedTextField(
                value = businessIdea,
                onValueChange = { businessIdea = it; ideaError = null },
                label = { Text("Business Idea & Value Proposition *") },
                isError = ideaError != null,
                supportingText = ideaError?.let { { Text(it) } },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_business_idea")
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Industry / Sector") },
                    placeholder = { Text("e.g. Fintech, HealthTech, B2B SaaS") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = targetMarket,
                    onValueChange = { targetMarket = it },
                    label = { Text("Target Market / Region") },
                    placeholder = { Text("e.g. US, MENA, Global") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = targetAudience,
                onValueChange = { targetAudience = it },
                label = { Text("Target Customer Profile") },
                placeholder = { Text("e.g. SMB owners, Marketing agencies, Gen Z creators") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "2. Problem & Solution Dynamics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = customerProblem,
                onValueChange = { customerProblem = it; problemError = null },
                label = { Text("Customer Problem Statement *") },
                placeholder = { Text("What acute pain point are customers experiencing?") },
                isError = problemError != null,
                supportingText = problemError?.let { { Text(it) } },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = proposedSolution,
                onValueChange = { proposedSolution = it; solutionError = null },
                label = { Text("Proposed Solution & Differentiation *") },
                placeholder = { Text("How does your product solve this uniquely?") },
                isError = solutionError != null,
                supportingText = solutionError?.let { { Text(it) } },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uniqueSellingProposition,
                onValueChange = { uniqueSellingProposition = it },
                label = { Text("Unique Selling Proposition (USP)") },
                placeholder = { Text("What is your unfair advantage or proprietary moat?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "3. Business Model & Financial Baseline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = businessModel,
                onValueChange = { businessModel = it },
                label = { Text("Business Model Type") },
                placeholder = { Text("Subscription, Marketplace, E-commerce, Freemium") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startupBudget,
                    onValueChange = { startupBudget = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Startup Budget ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monthly Budget ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = teamSize,
                    onValueChange = { teamSize = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Team Size") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = launchTimeframe,
                    onValueChange = { launchTimeframe = it },
                    label = { Text("Launch Timeframe") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = pricingAssumptions,
                onValueChange = { pricingAssumptions = it },
                label = { Text("Pricing Assumptions") },
                placeholder = { Text("e.g. $49/mo starter, $199/mo enterprise") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = acquisitionAssumptions,
                onValueChange = { acquisitionAssumptions = it },
                label = { Text("Customer Acquisition Strategy") },
                placeholder = { Text("e.g. SEO, Paid Ads, Cold Outreach, Strategic Partnerships") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = competitors,
                onValueChange = { competitors = it },
                label = { Text("Known Competitors (comma separated)") },
                placeholder = { Text("e.g. CompetitorA, CompetitorB, Legacy Excel") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                label = { Text("Additional Assumptions & Strategic Notes") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validate()) {
                        onSaveClick(
                            Venture(
                                id = existingVenture?.id ?: 0L,
                                name = name.trim(),
                                businessIdea = businessIdea.trim(),
                                industry = industry.trim().ifEmpty { "General Tech / Service" },
                                targetMarket = targetMarket.trim().ifEmpty { "North America / Global" },
                                targetAudience = targetAudience.trim(),
                                customerProblem = customerProblem.trim(),
                                proposedSolution = proposedSolution.trim(),
                                businessModel = businessModel.trim(),
                                estimatedStartupBudget = startupBudget.toDoubleOrNull() ?: 10000.0,
                                monthlyOperatingBudget = monthlyBudget.toDoubleOrNull() ?: 2500.0,
                                teamSize = teamSize.toIntOrNull() ?: 1,
                                founderExperience = founderExperience.trim(),
                                expectedLaunchTimeframe = launchTimeframe.trim(),
                                revenueModel = revenueModel.trim(),
                                pricingAssumptions = pricingAssumptions.trim(),
                                customerAcquisitionAssumptions = acquisitionAssumptions.trim(),
                                competitors = competitors.trim(),
                                uniqueSellingProposition = uniqueSellingProposition.trim(),
                                additionalNotes = additionalNotes.trim(),
                                createdAt = existingVenture?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_venture_save_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(if (existingVenture != null) "Update Venture Plan" else "Save Venture Plan")
            }
        }
    }
}
