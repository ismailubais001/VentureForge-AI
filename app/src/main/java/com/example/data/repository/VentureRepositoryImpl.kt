package com.example.data.repository

import com.example.data.database.VentureDatabase
import com.example.data.database.converters.Converters
import com.example.data.database.entity.ActionTaskEntity
import com.example.data.database.entity.ContentReportEntity
import com.example.data.database.entity.VentureAnalysisEntity
import com.example.data.database.entity.VentureEntity
import com.example.domain.model.ActionTask
import com.example.domain.model.ContentReport
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis
import com.example.domain.repository.VentureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VentureRepositoryImpl(
    private val database: VentureDatabase
) : VentureRepository {

    private val ventureDao = database.ventureDao()
    private val analysisDao = database.ventureAnalysisDao()
    private val taskDao = database.actionTaskDao()
    private val reportDao = database.contentReportDao()

    override fun getAllVentures(): Flow<List<Venture>> {
        return ventureDao.getAllVentures().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVentureById(id: Long): Flow<Venture?> {
        return ventureDao.getVentureById(id).map { it?.toDomain() }
    }

    override suspend fun getVentureByIdSync(id: Long): Venture? = withContext(Dispatchers.IO) {
        ventureDao.getVentureByIdSync(id)?.toDomain()
    }

    override suspend fun saveVenture(venture: Venture): Long = withContext(Dispatchers.IO) {
        val entity = VentureEntity.fromDomain(venture)
        ventureDao.insertVenture(entity)
    }

    override suspend fun updateVenture(venture: Venture) = withContext(Dispatchers.IO) {
        val entity = VentureEntity.fromDomain(venture.copy(updatedAt = System.currentTimeMillis()))
        ventureDao.updateVenture(entity)
    }

    override suspend fun deleteVenture(id: Long) = withContext(Dispatchers.IO) {
        ventureDao.deleteVenture(id)
    }

    override suspend fun duplicateVenture(id: Long): Long = withContext(Dispatchers.IO) {
        val existing = ventureDao.getVentureByIdSync(id) ?: return@withContext -1L
        val duplicated = existing.copy(
            id = 0,
            name = "${existing.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newId = ventureDao.insertVenture(duplicated)

        // Copy analysis if exists
        val existingAnalysis = analysisDao.getAnalysisForVentureSync(id)
        if (existingAnalysis != null) {
            val duplicatedAnalysis = existingAnalysis.copy(
                id = 0,
                ventureId = newId,
                generatedAt = System.currentTimeMillis()
            )
            analysisDao.insertAnalysis(duplicatedAnalysis)
        }
        newId
    }

    override suspend fun deleteAllVentures() = withContext(Dispatchers.IO) {
        ventureDao.deleteAllVentures()
    }

    override fun getAnalysisForVenture(ventureId: Long): Flow<VentureAnalysis?> {
        return analysisDao.getAnalysisForVenture(ventureId).map { entity ->
            if (entity == null) null else {
                VentureAnalysis(
                    id = entity.id,
                    ventureId = entity.ventureId,
                    executiveSummary = entity.executiveSummary,
                    problemAnalysis = entity.problemAnalysis,
                    solutionEvaluation = entity.solutionEvaluation,
                    targetMarketValidation = entity.targetMarketValidation,
                    businessModelFit = entity.businessModelFit,
                    valuePropositionAssessment = entity.valuePropositionAssessment,
                    strengths = Converters.deserializeStringList(entity.strengthsJson),
                    weaknesses = Converters.deserializeStringList(entity.weaknessesJson),
                    opportunities = Converters.deserializeStringList(entity.opportunitiesJson),
                    threats = Converters.deserializeStringList(entity.threatsJson),
                    competitors = Converters.deserializeCompetitors(entity.competitorsJson),
                    marketRisks = Converters.deserializeStringList(entity.marketRisksJson),
                    recommendations = Converters.deserializeStringList(entity.recommendationsJson),
                    actionPlan = Converters.deserializeActionPlan(entity.actionPlanJson),
                    feasibilityScore = Converters.deserializeFeasibilityScore(entity.feasibilityScoreJson),
                    confidenceNotes = Converters.deserializeStringList(entity.confidenceNotesJson),
                    generatedAt = entity.generatedAt,
                    isAiGenerated = entity.isAiGenerated
                )
            }
        }
    }

    override suspend fun saveAnalysis(analysis: VentureAnalysis): Long = withContext(Dispatchers.IO) {
        val entity = VentureAnalysisEntity(
            id = analysis.id,
            ventureId = analysis.ventureId,
            executiveSummary = analysis.executiveSummary,
            problemAnalysis = analysis.problemAnalysis,
            solutionEvaluation = analysis.solutionEvaluation,
            targetMarketValidation = analysis.targetMarketValidation,
            businessModelFit = analysis.businessModelFit,
            valuePropositionAssessment = analysis.valuePropositionAssessment,
            strengthsJson = Converters.serializeStringList(analysis.strengths),
            weaknessesJson = Converters.serializeStringList(analysis.weaknesses),
            opportunitiesJson = Converters.serializeStringList(analysis.opportunities),
            threatsJson = Converters.serializeStringList(analysis.threats),
            competitorsJson = Converters.serializeCompetitors(analysis.competitors),
            marketRisksJson = Converters.serializeStringList(analysis.marketRisks),
            recommendationsJson = Converters.serializeStringList(analysis.recommendations),
            actionPlanJson = Converters.serializeActionPlan(analysis.actionPlan),
            feasibilityScoreJson = Converters.serializeFeasibilityScore(analysis.feasibilityScore),
            confidenceNotesJson = Converters.serializeStringList(analysis.confidenceNotes),
            generatedAt = analysis.generatedAt,
            isAiGenerated = analysis.isAiGenerated
        )
        analysisDao.insertAnalysis(entity)
    }

    override suspend fun deleteAnalysisForVenture(ventureId: Long) = withContext(Dispatchers.IO) {
        analysisDao.deleteAnalysisForVenture(ventureId)
    }

    override fun getTasksForVenture(ventureId: Long): Flow<List<ActionTask>> {
        return taskDao.getTasksForVenture(ventureId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTask(task: ActionTask): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(ActionTaskEntity.fromDomain(task))
    }

    override suspend fun updateTask(task: ActionTask) = withContext(Dispatchers.IO) {
        taskDao.updateTask(ActionTaskEntity.fromDomain(task))
    }

    override suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(id)
    }

    override suspend fun saveInitialTasks(tasks: List<ActionTask>) = withContext(Dispatchers.IO) {
        taskDao.insertTasks(tasks.map { ActionTaskEntity.fromDomain(it) })
    }

    override fun getAllContentReports(): Flow<List<ContentReport>> {
        return reportDao.getAllReports().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun submitContentReport(report: ContentReport): Long = withContext(Dispatchers.IO) {
        reportDao.insertReport(ContentReportEntity.fromDomain(report))
    }
}
