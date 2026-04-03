package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Data for a single segment in the donut chart.
 */
data class DonutSegment(
    val value: Double,
    val color: Color,
    val label: String
)

/**
 * A donut chart composable that displays segments with minimum angle support.
 * Small segments are guaranteed a minimum visibility.
 * 
 * Based on Buckwheat's DonutChart implementation.
 */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    minAngle: Float = 28f, // Minimum angle in degrees for small segments
    gapAngle: Float = 2f   // Gap between segments
) {
    if (segments.isEmpty()) return
    
    val total = segments.sumOf { it.value }
    if (total <= 0) return
    
    // Calculate angles with minimum angle guarantee
    val angles = remember(segments, minAngle) {
        calculateSegmentAngles(segments, total, minAngle, gapAngle)
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Butt
            )
            
            var startAngle = -90f // Start from top
            
            angles.forEachIndexed { index, angle ->
                drawArc(
                    color = segments[index].color,
                    startAngle = startAngle,
                    sweepAngle = angle - gapAngle, // Subtract gap
                    useCenter = false,
                    style = stroke
                )
                startAngle += angle
            }
        }
    }
}

/**
 * Calculates segment angles ensuring each segment has at least minAngle.
 * "Steals" from larger segments to give minimum to smaller ones.
 */
private fun calculateSegmentAngles(
    segments: List<DonutSegment>,
    total: Double,
    minAngle: Float,
    gapAngle: Float
): List<Float> {
    val totalGap = gapAngle * segments.size
    val availableAngle = 360f - totalGap
    
    // Calculate natural angles
    val naturalAngles = segments.map { 
        (it.value / total * availableAngle).toFloat() 
    }
    
    // Find segments that need minimum angle
    val needsMin = naturalAngles.map { it < minAngle }
    val smallSegmentCount = needsMin.count { it }
    
    if (smallSegmentCount == 0) {
        return naturalAngles.map { it + gapAngle }
    }
    
    // Calculate how much we need to steal from larger segments
    val deficit = needsMin.mapIndexed { i, needs ->
        if (needs) minAngle - naturalAngles[i] else 0f
    }.sum()
    
    // Distribute deficit proportionally from larger segments
    val largeSegmentsTotal = naturalAngles.filterIndexed { i, _ -> !needsMin[i] }.sum()
    
    return naturalAngles.mapIndexed { i, angle ->
        if (needsMin[i]) {
            minAngle + gapAngle
        } else if (largeSegmentsTotal > 0) {
            val stolen = deficit * (angle / largeSegmentsTotal)
            (angle - stolen + gapAngle).coerceAtLeast(gapAngle)
        } else {
            angle + gapAngle
        }
    }
}
