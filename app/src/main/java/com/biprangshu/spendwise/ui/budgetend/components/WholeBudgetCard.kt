package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * A card showing the whole budget amount with a timeline visualization.
 * Displays start date → arrow → end date with the period duration.
 */
@Composable
fun WholeBudgetCard(
    totalBudget: Double,
    currencySymbol: String,
    startDate: Long,
    finishDate: Long,
    actualFinishDate: Long? = null,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM")
    
    val startLocalDate = Instant.ofEpochMilli(startDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val finishLocalDate = Instant.ofEpochMilli(finishDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val actualFinishLocalDate = actualFinishDate?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    
    val originalDays = ChronoUnit.DAYS.between(startLocalDate, finishLocalDate) + 1
    val actualDays = actualFinishLocalDate?.let {
        ChronoUnit.DAYS.between(startLocalDate, it) + 1
    }
    
    val finishedEarly = actualFinishLocalDate != null && actualFinishLocalDate.isBefore(finishLocalDate)
    
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Budget amount
            Text(
                text = "Total Budget",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(totalBudget, currencySymbol),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Date timeline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start date
                DateLabel(
                    date = startLocalDate.format(dateFormatter),
                    label = "Start"
                )
                
                // Arrow with days chip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Arrow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                    
                    // Days chip
                    if (finishedEarly && actualDays != null) {
                        // Show crossed out original days and actual days
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DaysChip(
                                days = actualDays,
                                modifier = Modifier.rotate(6f)
                            )
                            CrossedOutDaysChip(
                                days = originalDays
                            )
                        }
                    } else {
                        DaysChip(days = originalDays)
                    }
                }
                
                // End date
                if (finishedEarly && actualFinishLocalDate != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DateLabel(
                            date = actualFinishLocalDate.format(dateFormatter),
                            label = "Ended"
                        )
                        Text(
                            text = finishLocalDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                } else {
                    DateLabel(
                        date = finishLocalDate.format(dateFormatter),
                        label = "End"
                    )
                }
            }
        }
    }
}

@Composable
private fun DateLabel(
    date: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DaysChip(
    days: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = "$days days",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CrossedOutDaysChip(
    days: Long,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Text(
            text = "$days days",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textDecoration = TextDecoration.LineThrough
        )
    }
}

@Composable
private fun Arrow(
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.outlineVariant
    
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val arrowHeadSize = 8.dp.toPx()
        val centerY = size.height / 2
        
        // Draw line
        drawLine(
            color = color,
            start = Offset(0f, centerY),
            end = Offset(size.width - arrowHeadSize, centerY),
            strokeWidth = strokeWidth
        )
        
        // Draw arrow head
        val path = Path().apply {
            moveTo(size.width, centerY)
            lineTo(size.width - arrowHeadSize, centerY - arrowHeadSize / 2)
            lineTo(size.width - arrowHeadSize, centerY + arrowHeadSize / 2)
            close()
        }
        drawPath(path, color = color)
    }
}

private fun formatCurrency(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault())
    return when {
        amount >= 10_000_000 -> "$symbol${formatter.format(amount / 10_000_000)}Cr"
        amount >= 100_000 -> "$symbol${formatter.format(amount / 100_000)}L"
        amount >= 1_000 -> "$symbol${formatter.format(amount / 1_000)}K"
        else -> "$symbol${formatter.format(amount.toLong())}"
    }
}
