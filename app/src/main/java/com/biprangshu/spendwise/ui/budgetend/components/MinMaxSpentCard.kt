package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.util.combineColors
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Colors for min/max visualization
private val colorMin = Color(0xFF34A853)  // Green
private val colorMax = Color(0xFFEA4335)  // Red

/**
 * A card showing either the minimum or maximum spend transaction.
 * Shows the amount, date, and transaction title.
 * 
 * Based on Buckwheat's MinMaxSpentCard implementation.
 */
@Composable
fun MinMaxSpentCard(
    transaction: Transaction?,
    isMin: Boolean,
    minAmount: Double,
    maxAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm")
    
    // Calculate color interpolation between min and max colors
    val cardColor = if (transaction != null && maxAmount > minAmount) {
        val ratio = ((transaction.amount - minAmount) / (maxAmount - minAmount)).toFloat()
        combineColors(colorMin, colorMax, ratio)
    } else {
        if (isMin) colorMin else colorMax
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (transaction != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header with icon and label
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMin) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = cardColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMin) "Minimum" else "Maximum",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount
                Text(
                    text = formatCurrency(transaction.amount, currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = cardColor
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Date and title
                val dateText = Instant.ofEpochMilli(transaction.date)
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter)
                
                Text(
                    text = if (transaction.title.isNotBlank()) {
                        "$dateText • ${transaction.title}"
                    } else {
                        dateText
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isMin) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isMin) "No minimum" else "No maximum",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * A row containing both Min and Max spend cards.
 */
@Composable
fun MinMaxSpendRow(
    minTransaction: Transaction?,
    maxTransaction: Transaction?,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val minAmount = minTransaction?.amount ?: 0.0
    val maxAmount = maxTransaction?.amount ?: 0.0
    
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        MinMaxSpentCard(
            transaction = minTransaction,
            isMin = true,
            minAmount = minAmount,
            maxAmount = maxAmount,
            currencySymbol = currencySymbol,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        MinMaxSpentCard(
            transaction = maxTransaction,
            isMin = false,
            minAmount = minAmount,
            maxAmount = maxAmount,
            currencySymbol = currencySymbol,
            modifier = Modifier.weight(1f)
        )
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
