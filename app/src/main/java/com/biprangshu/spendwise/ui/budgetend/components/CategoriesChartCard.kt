package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.util.combineColors
import java.text.NumberFormat
import java.util.Locale

// Category colors palette (7 colors, harmonized)
private val categoryColors = listOf(
    Color(0xFF4285F4), // Blue
    Color(0xFF34A853), // Green
    Color(0xFFFBBC04), // Yellow
    Color(0xFFEA4335), // Red
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFF9800), // Orange
)

//card showing spending breakdown by category with a donut chart
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesChartCard(
    categoryBreakdown: Map<String, Double>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    maxCategories: Int = 7
) {
    if (categoryBreakdown.isEmpty()) {
        EmptyCategoriesCard(modifier = modifier)
        return
    }
    

    val sortedCategories = categoryBreakdown.entries
        .sortedByDescending { it.value }
        .toList()
    
    val displayCategories: List<Pair<String, Double>>
    val hasOverflow: Boolean
    
    if (sortedCategories.size > maxCategories) {
        val visible = sortedCategories.take(maxCategories - 1)
        val rest = sortedCategories.drop(maxCategories - 1)
        val restTotal = rest.sumOf { it.value }
        
        displayCategories = visible.map { it.key to it.value } + ("Other" to restTotal)
        hasOverflow = true
    } else {
        displayCategories = sortedCategories.map { it.key to it.value }
        hasOverflow = false
    }
    

    val segments = remember(displayCategories) {
        displayCategories.mapIndexed { index, (label, value) ->
            DonutSegment(
                value = value,
                color = categoryColors[index % categoryColors.size],
                label = label
            )
        }
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
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Donut chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    segments = segments,
                    modifier = Modifier.size(140.dp),
                    strokeWidth = 24.dp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                segments.forEach { segment ->
                    CategoryChip(
                        label = segment.label,
                        amount = displayCategories.find { it.first == segment.label }?.second ?: 0.0,
                        color = segment.color,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    amount: Double,
    color: Color,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Amount
            Text(
                text = formatCompactCurrency(amount, currencySymbol),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCategoriesCard(
    modifier: Modifier = Modifier
) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "No categories yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Add categories to your transactions to see spending breakdown.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatCompactCurrency(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault())
    return when {
        amount >= 10_000_000 -> "$symbol${formatter.format(amount / 10_000_000)}Cr"
        amount >= 100_000 -> "$symbol${formatter.format(amount / 100_000)}L"
        amount >= 1_000 -> "$symbol${String.format(Locale.getDefault(), "%.1f", amount / 1_000)}K"
        else -> "$symbol${formatter.format(amount.toLong())}"
    }
}
