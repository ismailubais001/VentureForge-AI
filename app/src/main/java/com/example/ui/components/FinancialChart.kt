package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MonthlyMetric
import com.example.ui.theme.ForgeGold
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.RiskRed
import com.example.ui.theme.TechTeal
import kotlin.math.max

@Composable
fun FinancialChart(
    metrics: List<MonthlyMetric>,
    modifier: Modifier = Modifier
) {
    if (metrics.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "12-Month Financial Trajectory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Deterministic simulation based on current scenario inputs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = ProfitGreen, label = "Revenue")
                LegendItem(color = RiskRed, label = "Expenses")
                LegendItem(color = TechTeal, label = "Cash Balance")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val maxVal = max(
                    1.0,
                    metrics.maxOf { max(it.revenue, max(it.totalExpenses, max(0.0, it.cashBalance))) }
                )
                val minVal = metrics.minOf { minOf(0.0, it.cashBalance, it.netProfit) }
                val valueRange = maxVal - minVal

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 24f
                    val paddingTop = 12f
                    val plotH = h - paddingBottom - paddingTop
                    val stepX = if (metrics.size > 1) w / (metrics.size - 1) else w

                    // Draw baseline grid lines
                    val zeroY = paddingTop + plotH * (maxVal / valueRange).toFloat()
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, zeroY),
                        end = Offset(w, zeroY),
                        strokeWidth = 1.5f
                    )

                    // Helper to compute Y coordinate
                    fun getY(v: Double): Float {
                        val normalized = (maxVal - v) / valueRange
                        return paddingTop + (normalized * plotH).toFloat()
                    }

                    // Paths for Revenue, Expenses, Cash
                    val revenuePath = Path()
                    val expensePath = Path()
                    val cashPath = Path()

                    metrics.forEachIndexed { i, metric ->
                        val x = i * stepX
                        val yRev = getY(metric.revenue)
                        val yExp = getY(metric.totalExpenses)
                        val yCash = getY(metric.cashBalance)

                        if (i == 0) {
                            revenuePath.moveTo(x, yRev)
                            expensePath.moveTo(x, yExp)
                            cashPath.moveTo(x, yCash)
                        } else {
                            revenuePath.lineTo(x, yRev)
                            expensePath.lineTo(x, yExp)
                            cashPath.lineTo(x, yCash)
                        }

                        // Draw month dots
                        drawCircle(ProfitGreen, radius = 3.5f, center = Offset(x, yRev))
                        drawCircle(RiskRed, radius = 3.5f, center = Offset(x, yExp))
                    }

                    // Draw lines
                    drawPath(
                        path = revenuePath,
                        color = ProfitGreen,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = expensePath,
                        color = RiskRed,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = cashPath,
                        color = TechTeal,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                }
            }

            // Month Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("M1", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("M3", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("M6", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("M9", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("M12", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
