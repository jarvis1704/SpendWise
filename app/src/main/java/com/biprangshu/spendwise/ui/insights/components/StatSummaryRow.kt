package com.biprangshu.spendwise.ui.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.ui.theme.colorExpense
import com.biprangshu.spendwise.ui.theme.colorIncome
import java.util.Locale

@Composable
fun StatSummaryRow(
    totalIncome: Double,
    totalExpense: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val netBalance = totalIncome - totalExpense

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Income",
            value = "$currency${formatStat(totalIncome)}",
            icon = Icons.Default.TrendingUp,
            iconTint = colorIncome,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Expense",
            value = "$currency${formatStat(totalExpense)}",
            icon = Icons.Default.TrendingDown,
            iconTint = colorExpense,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Balance",
            value = "${if (netBalance < 0) "-" else ""}$currency${formatStat(kotlin.math.abs(netBalance))}",
            icon = Icons.Default.AccountBalance,
            iconTint = if (netBalance >= 0) colorIncome else colorExpense,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatStat(value: Double): String {
    return when {
        value >= 100_000 -> String.format(Locale.getDefault(), "%.1fL", value / 100_000)
        value >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", value / 1_000)
        else -> String.format(Locale.getDefault(), "%.0f", value)
    }
}
