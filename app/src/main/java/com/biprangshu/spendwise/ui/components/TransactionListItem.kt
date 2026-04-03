package com.biprangshu.spendwise.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.model.TransactionType
import com.biprangshu.spendwise.ui.theme.colorExpense
import com.biprangshu.spendwise.ui.theme.colorIncome
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ListItemPosition {
    TOP, MIDDLE, BOTTOM, SINGLE
}

@Composable
fun TransactionListItem(
    transaction: Transaction,
    currencySymbol: String,
    position: ListItemPosition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = when (position) {
        ListItemPosition.TOP -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        ListItemPosition.MIDDLE -> RoundedCornerShape(4.dp)
        ListItemPosition.BOTTOM -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
        ListItemPosition.SINGLE -> MaterialTheme.shapes.extraLarge
    }

    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) colorIncome else colorExpense
    val amountPrefix = if (isIncome) "+" else "-"

    ListItem(
        headlineContent = {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = "${transaction.category} • ${formatDate(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = if (isIncome) "Income" else "Expense",
                tint = amountColor,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Text(
                text = "$amountPrefix$currencySymbol${formatAmount(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier
            .clip(shape)
            .clickable { onClick() }
    )
}

private fun formatAmount(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(amount)
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
