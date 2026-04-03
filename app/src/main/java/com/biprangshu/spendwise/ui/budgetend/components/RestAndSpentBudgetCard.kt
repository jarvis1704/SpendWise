package com.biprangshu.spendwise.ui.budgetend.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.util.BudgetHealthColors
import com.biprangshu.spendwise.util.WavyShape
import com.biprangshu.spendwise.util.clamp
import com.biprangshu.spendwise.util.combineColors
import java.text.NumberFormat
import java.util.Locale

/**
 * A card showing remaining vs spent budget with an animated wavy progress indicator.
 * Clicking toggles between "Remaining" and "Spent" views.
 * 
 * Based on Buckwheat's RestAndSpentBudgetCard implementation.
 */
@Composable
fun RestAndSpentBudgetCard(
    totalBudget: Double,
    totalSpent: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showRest by remember { mutableStateOf(true) }
    
    val remaining = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val spentPercent = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val remainingPercent = if (totalBudget > 0) (remaining / totalBudget).toFloat() else 0f
    
    // Current display values based on toggle
    val displayAmount = if (showRest) remaining else totalSpent
    val displayPercent = if (showRest) remainingPercent else spentPercent
    val displayLabel = if (showRest) "Remaining" else "Spent"
    
    // Wave animation
    val waveShift = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            waveShift.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
            )
            waveShift.snapTo(0f)
        }
    }
    
    // Dynamic color based on budget health
    val healthColor = if (showRest) {
        BudgetHealthColors.getHealthColorFromRemaining(remainingPercent)
    } else {
        BudgetHealthColors.getHealthColor(spentPercent)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                showRest = !showRest
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wavy fill background
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(displayPercent.clamp(0.01f, 1f))
                    .background(
                        color = healthColor.copy(alpha = 0.3f),
                        shape = WavyShape(
                            period = 40.dp,
                            amplitude = 2.dp * displayPercent.clamp(0.96f, 1f),
                            shift = waveShift.value
                        )
                    )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with label and page indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Page indicator dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PageDot(isActive = showRest)
                        PageDot(isActive = !showRest)
                    }
                }
                
                // Amount
                Text(
                    text = formatCurrency(displayAmount, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Percentage
                Text(
                    text = formatPercent(displayPercent),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = healthColor
                )
            }
        }
    }
}

@Composable
private fun PageDot(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(6.dp)
            .background(
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape
            )
    )
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

private fun formatPercent(percent: Float): String {
    return when {
        percent > 10f -> ">1000%"
        percent >= 1f -> "${(percent * 100).toInt()}%"
        else -> "${String.format(Locale.getDefault(), "%.1f", percent * 100)}%"
    }
}
