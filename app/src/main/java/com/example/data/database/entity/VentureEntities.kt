package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.ActionMilestone
import com.example.domain.model.ActionTask
import com.example.domain.model.CompetitorInsight
import com.example.domain.model.ContentReport
import com.example.domain.model.FeasibilityScore
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis

@Entity(tableName = "ventures")
data class VentureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val businessIdea: String,
    val industry: String,
    val targetMarket: String,
    val targetAudience: String,
    val customerProblem: String,
    val proposedSolution: String,
    val businessModel: String,
    val estimatedStartupBudget: Double,
    val monthlyOperatingBudget: Double,
    val teamSize: Int,
    val founderExperience: String,
    val expectedLaunchTimeframe: String,
    val revenueModel: String,
    val pricingAssumptions: String,
    val customerAcquisitionAssumptions: String,
    val competitors: String,
    val uniqueSellingProposition: String,
    val additionalNotes: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Venture = Venture(
        id = id,
        name = name,
        businessIdea = businessIdea,
        industry = industry,
        targetMarket = targetMarket,
        targetAudience = targetAudience,
        customerProblem = customerProblem,
        proposedSolution = proposedSolution,
        businessModel = businessModel,
        estimatedStartupBudget = estimatedStartupBudget,
        monthlyOperatingBudget = monthlyOperatingBudget,
        teamSize = teamSize,
        founderExperience = founderExperience,
        expectedLaunchTimeframe = expectedLaunchTimeframe,
        revenueModel = revenueModel,
        pricingAssumptions = pricingAssumptions,
        customerAcquisitionAssumptions = customerAcquisitionAssumptions,
        competitors = competitors,
        uniqueSellingProposition = uniqueSellingProposition,
        additionalNotes = additionalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: Venture): VentureEntity = VentureEntity(
            id = domain.id,
            name = domain.name,
            businessIdea = domain.businessIdea,
            industry = domain.industry,
            targetMarket = domain.targetMarket,
            targetAudience = domain.targetAudience,
            customerProblem = domain.customerProblem,
            proposedSolution = domain.proposedSolution,
            businessModel = domain.businessModel,
            estimatedStartupBudget = domain.estimatedStartupBudget,
            monthlyOperatingBudget = domain.monthlyOperatingBudget,
            teamSize = domain.teamSize,
            founderExperience = domain.founderExperience,
            expectedLaunchTimeframe = domain.expectedLaunchTimeframe,
            revenueModel = domain.revenueModel,
            pricingAssumptions = domain.pricingAssumptions,
            customerAcquisitionAssumptions = domain.customerAcquisitionAssumptions,
            competitors = domain.competitors,
            uniqueSellingProposition = domain.uniqueSellingProposition,
            additionalNotes = domain.additionalNotes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}

@Entity(
    tableName = "venture_analyses",
    foreignKeys = [
        ForeignKey(
            entity = VentureEntity::class,
            parentColumns = ["id"],
            childColumns = ["ventureId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ventureId"], unique = true)]
)
data class VentureAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ventureId: Long,
    val executiveSummary: String,
    val problemAnalysis: String,
    val solutionEvaluation: String,
    val targetMarketValidation: String,
    val businessModelFit: String,
    val valuePropositionAssessment: String,
    val strengthsJson: String,
    val weaknessesJson: String,
    val opportunitiesJson: String,
    val threatsJson: String,
    val competitorsJson: String,
    val marketRisksJson: String,
    val recommendationsJson: String,
    val actionPlanJson: String,
    val feasibilityScoreJson: String,
    val confidenceNotesJson: String,
    val generatedAt: Long,
    val isAiGenerated: Boolean
)

@Entity(
    tableName = "action_tasks",
    foreignKeys = [
        ForeignKey(
            entity = VentureEntity::class,
            parentColumns = ["id"],
            childColumns = ["ventureId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ventureId"])]
)
data class ActionTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ventureId: Long,
    val phase: String,
    val taskName: String,
    val description: String,
    val priority: String,
    val estimatedDays: Int,
    val isCompleted: Boolean
) {
    fun toDomain(): ActionTask = ActionTask(
        id = id,
        ventureId = ventureId,
        phase = phase,
        taskName = taskName,
        description = description,
        priority = priority,
        estimatedDays = estimatedDays,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomain(domain: ActionTask): ActionTaskEntity = ActionTaskEntity(
            id = domain.id,
            ventureId = domain.ventureId,
            phase = domain.phase,
            taskName = domain.taskName,
            description = domain.description,
            priority = domain.priority,
            estimatedDays = domain.estimatedDays,
            isCompleted = domain.isCompleted
        )
    }
}

@Entity(tableName = "content_reports")
data class ContentReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ventureId: Long,
    val responseSnippet: String,
    val category: String,
    val userComment: String,
    val timestamp: Long,
    val status: String
) {
    fun toDomain(): ContentReport = ContentReport(
        id = id,
        ventureId = ventureId,
        responseSnippet = responseSnippet,
        category = category,
        userComment = userComment,
        timestamp = timestamp,
        status = status
    )

    companion object {
        fun fromDomain(domain: ContentReport): ContentReportEntity = ContentReportEntity(
            id = domain.id,
            ventureId = domain.ventureId,
            responseSnippet = domain.responseSnippet,
            category = domain.category,
            userComment = domain.userComment,
            timestamp = domain.timestamp,
            status = domain.status
        )
    }
}
