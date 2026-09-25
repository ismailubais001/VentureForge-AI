package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.ActionTaskEntity
import com.example.data.database.entity.ContentReportEntity
import com.example.data.database.entity.VentureAnalysisEntity
import com.example.data.database.entity.VentureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentureDao {
    @Query("SELECT * FROM ventures ORDER BY updatedAt DESC")
    fun getAllVentures(): Flow<List<VentureEntity>>

    @Query("SELECT * FROM ventures WHERE id = :id")
    fun getVentureById(id: Long): Flow<VentureEntity?>

    @Query("SELECT * FROM ventures WHERE id = :id")
    suspend fun getVentureByIdSync(id: Long): VentureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenture(venture: VentureEntity): Long

    @Update
    suspend fun updateVenture(venture: VentureEntity)

    @Query("DELETE FROM ventures WHERE id = :id")
    suspend fun deleteVenture(id: Long)

    @Query("DELETE FROM ventures")
    suspend fun deleteAllVentures()
}

@Dao
interface VentureAnalysisDao {
    @Query("SELECT * FROM venture_analyses WHERE ventureId = :ventureId")
    fun getAnalysisForVenture(ventureId: Long): Flow<VentureAnalysisEntity?>

    @Query("SELECT * FROM venture_analyses WHERE ventureId = :ventureId")
    suspend fun getAnalysisForVentureSync(ventureId: Long): VentureAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: VentureAnalysisEntity): Long

    @Query("DELETE FROM venture_analyses WHERE ventureId = :ventureId")
    suspend fun deleteAnalysisForVenture(ventureId: Long)
}

@Dao
interface ActionTaskDao {
    @Query("SELECT * FROM action_tasks WHERE ventureId = :ventureId ORDER BY id ASC")
    fun getTasksForVenture(ventureId: Long): Flow<List<ActionTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ActionTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ActionTaskEntity>)

    @Update
    suspend fun updateTask(task: ActionTaskEntity)

    @Query("DELETE FROM action_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM action_tasks WHERE ventureId = :ventureId")
    suspend fun deleteTasksForVenture(ventureId: Long)
}

@Dao
interface ContentReportDao {
    @Query("SELECT * FROM content_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ContentReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ContentReportEntity): Long
}
