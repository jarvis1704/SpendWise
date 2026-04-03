package com.biprangshu.spendwise.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biprangshu.spendwise.domain.model.StreakData

/**
 * A card component that displays the user's streak progress
 * for staying under their daily budget.
 */
@Composable
fun StreaksCard(
    streakData: StreakData,
    hasBudgetSet: Boolean,
    modifier: Modifier = Modifier
) {
    // Don't show if no budget is set
    if (!hasBudgetSet) return

    val animatedProgress by animateFloatAsState(
        targetValue = streakData.progress,
        animationSpec = tween(durationMillis = 800),
        label = "streakProgress"
    )

    val streakColor = when {
        streakData.currentStreak >= 5 -> Color(0xFFFF6B35) // Hot orange for high streaks
        streakData.currentStreak >= 3 -> Color(0xFFFFAB40) // Warm orange
        streakData.currentStreak >= 1 -> Color(0xFFFFC107) // Yellow
        else -> MaterialTheme.colorScheme.outline // Gray when no streak
    }

    val animatedStreakColor by animateColorAsState(
        targetValue = streakColor,
        animationSpec = tween(durationMillis = 500),
        label = "streakColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with fire icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = animatedStreakColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Streak Challenge",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                // Total streaks badge
                if (streakData.totalStreaksAchieved > 0) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${streakData.totalStreaksAchieved} completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(streakData.targetStreak) { index ->
                    val isCompleted = index < streakData.currentStreak
                    val dotColor = if (isCompleted) {
                        animatedStreakColor
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(if (streakData.targetStreak <= 5) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
                
                // Show progress text
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${streakData.currentStreak} of ${streakData.targetStreak} days",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = animatedStreakColor,
                trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Motivational message
            Text(
                text = getMotivationalMessage(streakData),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Returns an encouraging message based on the current streak status.
 */
private fun getMotivationalMessage(streakData: StreakData): String {
    return when {
        streakData.currentStreak == 0 && streakData.totalStreaksAchieved == 0 -> 
            "Stay under your daily budget to start your streak!"
        streakData.currentStreak == 0 -> 
            "Start fresh! Stay under budget today to begin a new streak."
        streakData.daysRemaining == 1 -> 
            "Almost there! Just 1 more day to complete this challenge!"
        streakData.daysRemaining == 2 -> 
            "Great progress! Only 2 days left to reach your goal!"
        streakData.currentStreak == 1 -> 
            "Nice start! Keep going for ${streakData.daysRemaining} more days."
        streakData.currentStreak >= 5 -> 
            "You're on fire! ${streakData.daysRemaining} days to complete this challenge!"
        else -> 
            "Keep it up! ${streakData.daysRemaining} days left to complete this challenge."
    }
}
