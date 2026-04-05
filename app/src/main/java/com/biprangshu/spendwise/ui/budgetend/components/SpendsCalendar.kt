package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.biprangshu.spendwise.util.BudgetHealthColors
import com.biprangshu.spendwise.util.combineColors
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.pow
import androidx.compose.ui.platform.LocalLocale


private data class CalendarDay(
    val date: LocalDate,
    val spending: Double,
    val dailyBudget: Double
) {
    val spentPercent: Float
        get() = if (dailyBudget > 0) (spending / dailyBudget).toFloat() else 0f
    
    val isOverBudget: Boolean
        get() = spending > dailyBudget
}


@Composable
fun SpendsCalendar(
    dailySpending: Map<LocalDate, Double>,
    startDate: Long,
    finishDate: Long,
    totalBudget: Double,
    modifier: Modifier = Modifier
) {
    val startLocalDate = Instant.ofEpochMilli(startDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val finishLocalDate = Instant.ofEpochMilli(finishDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    
    // Calculate daily budget
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startLocalDate, finishLocalDate) + 1
    val dailyBudget = if (totalDays > 0) totalBudget / totalDays else 0.0
    
    // Generate all days in the period
    val calendarDays = remember(startLocalDate, finishLocalDate, dailySpending, dailyBudget) {
        generateSequence(startLocalDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(finishLocalDate) }
            .map { date ->
                CalendarDay(
                    date = date,
                    spending = dailySpending[date] ?: 0.0,
                    dailyBudget = dailyBudget
                )
            }
            .toList()
    }
    
    // Group days by week
    val weeks = remember(calendarDays) {
        calendarDays.groupBy { day ->
            // Get week number relative to start
            java.time.temporal.ChronoUnit.WEEKS.between(
                startLocalDate.with(DayOfWeek.MONDAY),
                day.date.with(DayOfWeek.MONDAY)
            )
        }.toSortedMap()
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Daily Spending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Day of week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DayOfWeek.entries.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            weeks.values.forEach { weekDays ->
                CalendarWeekRow(
                    days = weekDays,
                    startDate = startLocalDate
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legend
            CalendarLegend()
        }
    }
}

@Composable
private fun CalendarWeekRow(
    days: List<CalendarDay>,
    startDate: LocalDate,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        val firstDay = days.firstOrNull() ?: return
        val dayOfWeek = firstDay.date.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
        

        repeat(dayOfWeek - 1) {
            EmptyDayCell(modifier = Modifier.weight(1f))
        }
        

        days.forEach { day ->
            DayCell(
                day = day,
                modifier = Modifier.weight(1f)
            )
        }
        

        val lastDayOfWeek = days.lastOrNull()?.date?.dayOfWeek?.value ?: 7
        repeat(7 - lastDayOfWeek) {
            EmptyDayCell(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    modifier: Modifier = Modifier
) {
    val cellSize = 32.dp
    val spentPercent = day.spentPercent.coerceIn(0f, 2f)
    
    // Calculate color based on spending
    val color = if (day.isOverBudget) {
        BudgetHealthColors.colorBad
    } else {
        BudgetHealthColors.getHealthColor(spentPercent)
    }
    
    // Inner box size proportional to spending (minimum 30% if any spending)
    val fillPercent = if (day.spending > 0) {
        (spentPercent.coerceIn(0.3f, 1f))
    } else {
        0f
    }
    
    // Corner radius based on spending
    val cornerRadius = (10.dp * fillPercent.pow(1.8f)).coerceAtLeast(4.dp)
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = fillPercent * 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        
        // Fill based on spending
        if (day.spending > 0) {
            Box(
                modifier = Modifier
                    .size(cellSize * fillPercent)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(color.copy(alpha = 0.7f))
                    .zIndex(fillPercent) // Higher spending appears on top
            )
        }
        
        // Day number
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = if (day.spending > 0) {
                color
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun EmptyDayCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
    )
}

@Composable
private fun CalendarLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Under budget",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        
        // Gradient legend
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(0.2f, 0.5f, 0.8f, 1.0f).forEach { percent ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BudgetHealthColors.getHealthColor(percent))
                )
            }
        }
        
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        
        Text(
            text = "Over budget",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
