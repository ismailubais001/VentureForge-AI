package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.database.converters.Converters
import com.example.data.database.dao.ActionTaskDao
import com.example.data.database.dao.ContentReportDao
import com.example.data.database.dao.VentureAnalysisDao
import com.example.data.database.dao.VentureDao
import com.example.data.database.entity.ActionTaskEntity
import com.example.data.database.entity.ContentReportEntity
import com.example.data.database.entity.VentureAnalysisEntity
import com.example.data.database.entity.VentureEntity

@Database(
    entities = [
        VentureEntity::class,
        VentureAnalysisEntity::class,
        ActionTaskEntity::class,
        ContentReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VentureDatabase : RoomDatabase() {
    abstract fun ventureDao(): VentureDao
    abstract fun ventureAnalysisDao(): VentureAnalysisDao
    abstract fun actionTaskDao(): ActionTaskDao
    abstract fun contentReportDao(): ContentReportDao

    companion object {
        @Volatile
        private var INSTANCE: VentureDatabase? = null

        fun getInstance(context: Context): VentureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VentureDatabase::class.java,
                    "ventureforge.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
