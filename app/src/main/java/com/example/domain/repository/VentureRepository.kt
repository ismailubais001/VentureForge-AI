package com.example.domain.repository

import com.example.domain.model.ActionTask
import com.example.domain.model.ContentReport
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis
import kotlinx.coroutines.flow.Flow

interface VentureRepository {
    fun getAllVentures(): Flow<List<Venture>>
    fun getVentureById(id: Long): Flow<Venture?>
    suspend fun getVentureByIdSync(id: Long): Venture?
    suspend fun saveVenture(venture: Venture): Long
    suspend fun updateVenture(venture: Venture)
    suspend fun deleteVenture(id: Long)
    suspend fun duplicateVenture(id: Long): Long
    suspend fun deleteAllVentures()

    fun getAnalysisForVenture(ventureId: Long): Flow<VentureAnalysis?>
    suspend fun saveAnalysis(analysis: VentureAnalysis): Long
    suspend fun deleteAnalysisForVenture(ventureId: Long)

    fun getTasksForVenture(ventureId: Long): Flow<List<ActionTask>>
    suspend fun saveTask(task: ActionTask): Long
    suspend fun updateTask(task: ActionTask)
    suspend fun deleteTask(id: Long)
    suspend fun saveInitialTasks(tasks: List<ActionTask>)

    fun getAllContentReports(): Flow<List<ContentReport>>
    suspend fun submitContentReport(report: ContentReport): Long
}
