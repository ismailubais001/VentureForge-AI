package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.Result
import com.example.domain.financial.FinancialEngine
import com.example.domain.model.ActionMilestone
import com.example.domain.model.ActionTask
import com.example.domain.model.ContentReport
import com.example.domain.model.FinancialInputs
import com.example.domain.model.FinancialScenarioResult
import com.example.domain.model.ScenarioType
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis
import com.example.domain.repository.AiRepository
import com.example.domain.repository.VentureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VentureViewModel(
    private val repository: VentureRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    val allVentures: StateFlow<List<Venture>> = repository.getAllVentures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVenture = MutableStateFlow<Venture?>(null)
    val selectedVenture: StateFlow<Venture?> = _selectedVenture.asStateFlow()

    private val _currentAnalysis = MutableStateFlow<VentureAnalysis?>(null)
    val currentAnalysis: StateFlow<VentureAnalysis?> = _currentAnalysis.asStateFlow()

    private val _analysisState = MutableStateFlow<Result<VentureAnalysis>?>(null)
    val analysisState: StateFlow<Result<VentureAnalysis>?> = _analysisState.asStateFlow()

    private val _tasks = MutableStateFlow<List<ActionTask>>(emptyList())
    val tasks: StateFlow<List<ActionTask>> = _tasks.asStateFlow()

    private val _financialInputs = MutableStateFlow(FinancialInputs())
    val financialInputs: StateFlow<FinancialInputs> = _financialInputs.asStateFlow()

    private val _selectedScenarioType = MutableStateFlow(ScenarioType.BASE)
    val selectedScenarioType: StateFlow<ScenarioType> = _selectedScenarioType.asStateFlow()

    private val _financialResult = MutableStateFlow<FinancialScenarioResult?>(null)
    val financialResult: StateFlow<FinancialScenarioResult?> = _financialResult.asStateFlow()

    init {
        // Initial base calculation
        recalculateFinancials(_financialInputs.value, _selectedScenarioType.value)
    }

    fun selectVenture(id: Long) {
        viewModelScope.launch {
            repository.getVentureById(id).collect { venture ->
                _selectedVenture.value = venture
                if (venture != null) {
                    // Update initial financial inputs from venture data if available
                    val updatedInputs = _financialInputs.value.copy(
                        startupCost = if (venture.estimatedStartupBudget > 0) venture.estimatedStartupBudget else _financialInputs.value.startupCost,
                        monthlyFixedCosts = if (venture.monthlyOperatingBudget > 0) venture.monthlyOperatingBudget else _financialInputs.value.monthlyFixedCosts
                    )
                    _financialInputs.value = updatedInputs
                    recalculateFinancials(updatedInputs, _selectedScenarioType.value)
                }
            }
        }

        viewModelScope.launch {
            repository.getAnalysisForVenture(id).collect { analysis ->
                _currentAnalysis.value = analysis
                if (analysis != null) {
                    _analysisState.value = Result.Success(analysis)
                }
            }
        }

        viewModelScope.launch {
            repository.getTasksForVenture(id).collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun saveVenture(venture: Venture, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val id = if (venture.id == 0L) {
                repository.saveVenture(venture)
            } else {
                repository.updateVenture(venture)
                venture.id
            }
            onSaved(id)
        }
    }

    fun deleteVenture(id: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteVenture(id)
            onDeleted()
        }
    }

    fun duplicateVenture(id: Long) {
        viewModelScope.launch {
            repository.duplicateVenture(id)
        }
    }

    fun runAiAnalysis(venture: Venture) {
        viewModelScope.launch {
            _analysisState.value = Result.Loading
            val result = aiRepository.generateAnalysis(venture)
            _analysisState.value = result

            if (result is Result.Success) {
                val analysis = result.data
                repository.saveAnalysis(analysis)
                _currentAnalysis.value = analysis

                // Convert action milestones into action tasks if tasks don't exist yet
                if (_tasks.value.isEmpty() && analysis.actionPlan.isNotEmpty()) {
                    val newTasks = analysis.actionPlan.map { milestone ->
                        ActionTask(
                            ventureId = venture.id,
                            phase = milestone.phase,
                            taskName = milestone.taskName,
                            description = milestone.description,
                            priority = milestone.priority,
                            estimatedDays = milestone.estimatedDays,
                            isCompleted = false
                        )
                    }
                    repository.saveInitialTasks(newTasks)
                }
            }
        }
    }

    fun setScenarioType(type: ScenarioType) {
        _selectedScenarioType.value = type
        recalculateFinancials(_financialInputs.value, type)
    }

    fun updateFinancialInputs(inputs: FinancialInputs) {
        _financialInputs.value = inputs
        recalculateFinancials(inputs, _selectedScenarioType.value)
    }

    private fun recalculateFinancials(inputs: FinancialInputs, scenarioType: ScenarioType) {
        val result = FinancialEngine.calculateScenario(inputs, scenarioType)
        _financialResult.value = result
    }

    fun toggleTaskCompletion(task: ActionTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun submitReport(report: ContentReport, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.submitContentReport(report)
            aiRepository.reportContent(report)
            onComplete()
        }
    }

    fun exportVentureReport(context: Context, venture: Venture, analysis: VentureAnalysis?) {
        val textBuilder = StringBuilder()
        textBuilder.append("=== VENTUREFORGE AI REPORT ===\n\n")
        textBuilder.append("Venture Name: ${venture.name}\n")
        textBuilder.append("Industry: ${venture.industry}\n")
        textBuilder.append("Target Market: ${venture.targetMarket}\n")
        textBuilder.append("Business Model: ${venture.businessModel}\n")
        textBuilder.append("Initial Budget: $${venture.estimatedStartupBudget}\n")
        textBuilder.append("Monthly Budget: $${venture.monthlyOperatingBudget}\n\n")

        textBuilder.append("Problem Statement:\n${venture.customerProblem}\n\n")
        textBuilder.append("Proposed Solution:\n${venture.proposedSolution}\n\n")

        if (analysis != null) {
            textBuilder.append("--- AI EXECUTIVE EVALUATION ---\n")
            textBuilder.append("${analysis.executiveSummary}\n\n")
            textBuilder.append("Feasibility Score: ${analysis.feasibilityScore.overallScore}/100 (${analysis.feasibilityScore.riskLevel} Risk)\n")
            textBuilder.append("Note: ${analysis.feasibilityScore.disclaimerNote}\n\n")

            textBuilder.append("STRENGTHS:\n")
            analysis.strengths.forEach { textBuilder.append("- $it\n") }
            textBuilder.append("\nWEAKNESSES:\n")
            analysis.weaknesses.forEach { textBuilder.append("- $it\n") }
            textBuilder.append("\nOPPORTUNITIES:\n")
            analysis.opportunities.forEach { textBuilder.append("- $it\n") }
            textBuilder.append("\nTHREATS:\n")
            analysis.threats.forEach { textBuilder.append("- $it\n") }
            textBuilder.append("\n")

            if (analysis.recommendations.isNotEmpty()) {
                textBuilder.append("KEY RECOMMENDATIONS:\n")
                analysis.recommendations.forEach { textBuilder.append("• $it\n") }
                textBuilder.append("\n")
            }
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textBuilder.toString())
            putExtra(Intent.EXTRA_SUBJECT, "VentureForge AI Plan: ${venture.name}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Venture Report")
        context.startActivity(shareIntent)
    }
}
