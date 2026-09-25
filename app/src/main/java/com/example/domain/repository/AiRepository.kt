package com.example.domain.repository

import com.example.core.model.Result
import com.example.domain.model.ContentReport
import com.example.domain.model.Venture
import com.example.domain.model.VentureAnalysis

interface AiRepository {
    suspend fun generateAnalysis(venture: Venture): Result<VentureAnalysis>
    suspend fun reportContent(report: ContentReport): Result<Boolean>
}
