package com.biprangshu.spendwise.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biprangshu.spendwise.ui.components.confetti.ConfettiOverlay
import com.biprangshu.spendwise.ui.components.confetti.rememberConfettiController
import com.biprangshu.spendwise.ui.insights.components.BudgetProgressCard
import com.biprangshu.spendwise.ui.insights.components.CategorySpendingChart
import com.biprangshu.spendwise.ui.insights.components.DailyTrendChart
import com.biprangshu.spendwise.ui.insights.components.InsightCard
import com.biprangshu.spendwise.ui.insights.components.SpendingAssistantSheet
import com.biprangshu.spendwise.ui.insights.components.StatSummaryRow
import com.biprangshu.spendwise.ui.insights.components.WeeklyComparisonChart
import com.biprangshu.spendwise.util.NotificationHelper
import com.biprangshu.spendwise.ui.theme.colorExpense
import com.biprangshu.spendwise.ui.theme.colorIncome
import com.biprangshu.spendwise.ui.theme.robotoFlexTopBarStyle
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    innerPadding: PaddingValues,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val thisWeekExpense by viewModel.thisWeekExpense.collectAsStateWithLifecycle()
    val lastWeekExpense by viewModel.lastWeekExpense.collectAsStateWithLifecycle()
    val dailyTrend by viewModel.dailyTrend.collectAsStateWithLifecycle()
    val topCategory by viewModel.topCategory.collectAsStateWithLifecycle()
    val totalBudget by viewModel.totalBudget.collectAsStateWithLifecycle()
    val periodExpense by viewModel.periodExpense.collectAsStateWithLifecycle()
    val finishPeriodDate by viewModel.finishPeriodDate.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val hasTransactions by viewModel.hasTransactions.collectAsStateWithLifecycle()
    val chatState by viewModel.chatState.collectAsStateWithLifecycle()
    val showConfetti by viewModel.showConfetti.collectAsStateWithLifecycle()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsStateWithLifecycle()
    val streakData by viewModel.streakData.collectAsStateWithLifecycle()

    var showChatSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val confettiController = rememberConfettiController()
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }


    LaunchedEffect(showConfetti) {
        if (showConfetti && screenWidth > 0 && screenHeight > 0) {
            confettiController.spawnCelebration(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                particleCount = 120
            )
            if (isNotificationsEnabled) {
                NotificationHelper.postStreakNotification(
                    context = context,
                    targetStreak = streakData.targetStreak,
                    totalAchieved = streakData.totalStreaksAchieved
                )
            }
        }
    }

    val categoryModelProducer = remember { CartesianChartModelProducer() }
    val weeklyModelProducer = remember { CartesianChartModelProducer() }
    val trendModelProducer = remember { CartesianChartModelProducer() }

    // Update category chart
    LaunchedEffect(categoryBreakdown) {
        if (categoryBreakdown.isNotEmpty()) {
            val values = categoryBreakdown.values.toList()
            categoryModelProducer.runTransaction {
                columnSeries { series(values) }
            }
        }
    }

    // Update weekly comparison chart
    LaunchedEffect(thisWeekExpense, lastWeekExpense) {
        weeklyModelProducer.runTransaction {
            columnSeries { series(listOf(thisWeekExpense, lastWeekExpense)) }
        }
    }

    // Update daily trend chart
    LaunchedEffect(dailyTrend) {
        if (dailyTrend.isNotEmpty()) {
            val values = dailyTrend.map { it.second }
            trendModelProducer.runTransaction {
                lineSeries { series(values) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                screenHeight = size.height.toFloat()
            }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    )
            ) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Insights",
                        style = robotoFlexTopBarStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { showChatSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!hasTransactions) {
                    EmptyInsightsState()
                } else {
                    // Budget progress card
                    if (totalBudget > 0) {
                        BudgetProgressCard(
                            spent = periodExpense.toFloat(),
                            budget = totalBudget.toFloat(),
                            currency = currencySymbol,
                            periodEndDate = finishPeriodDate
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Stats summary row
                    StatSummaryRow(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        currency = currencySymbol
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Category spending chart
                    if (categoryBreakdown.isNotEmpty()) {
                        ChartCard(title = "Spending by Category") {
                            CategorySpendingChart(
                                modelProducer = categoryModelProducer,
                                categoryLabels = categoryBreakdown.keys.toList()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Weekly comparison chart
                    ChartCard(title = "This Week vs Last Week") {
                        WeeklyComparisonChart(modelProducer = weeklyModelProducer)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily trend chart
                    if (dailyTrend.isNotEmpty()) {
                        ChartCard(title = "Daily Trend (14 days)") {
                            DailyTrendChart(
                                modelProducer = trendModelProducer,
                                dayLabels = dailyTrend.map { it.first }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Insight cards section
                    Text(
                        text = "Key Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Top category insight
                        topCategory?.let { cat ->
                            val catAmount = categoryBreakdown[cat] ?: 0.0
                            InsightCard(
                                icon = Icons.Default.Category,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = "Top Spending Category",
                                body = "You've spent the most on $cat this period.",
                                highlightValue = "$currencySymbol${formatAmount(catAmount)}"
                            )
                        }

                        // Week comparison insight
                        if (lastWeekExpense > 0) {
                            val diff = thisWeekExpense - lastWeekExpense
                            val pct = abs(diff / lastWeekExpense * 100).toInt()
                            val isLess = diff < 0
                            InsightCard(
                                icon = Icons.Default.CompareArrows,
                                iconTint = if (isLess) colorIncome else colorExpense,
                                title = "Weekly Comparison",
                                body = if (isLess)
                                    "Great! You're spending $pct% less than last week."
                                else
                                    "You're spending $pct% more than last week."
                            )
                        }

                        // Budget pace insight
                        val startPeriodDate by viewModel.startPeriodDate.collectAsStateWithLifecycle()
                        if (totalBudget > 0 && finishPeriodDate != null) {
                            val endDate = Instant.ofEpochMilli(finishPeriodDate!!)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), endDate).coerceAtLeast(0)
                            val totalPeriodDays = if (startPeriodDate != null){
                                val start = Instant.ofEpochMilli(startPeriodDate!!)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                ChronoUnit.DAYS.between(start, endDate) + 1
                            } else daysLeft+1
                            val budgetRemaining = totalBudget - periodExpense
                            val isOnTrack = budgetRemaining >= 0
                            val dailyAllowance = if (totalPeriodDays > 0) (totalBudget / totalPeriodDays).roundToInt() else 0
                            InsightCard(
                                icon = if (isOnTrack) Icons.Default.CheckCircle else Icons.Default.Warning,
                                iconTint = if (isOnTrack) colorIncome else colorExpense,
                                title = if (isOnTrack) "On Track" else "Over Budget",
                                body = if (isOnTrack)
                                    "$daysLeft days left — $currencySymbol${dailyAllowance}/day available."
                                else
                                    "You've exceeded your period budget by $currencySymbol${formatAmount(abs(budgetRemaining))}."
                            )
                        }
                    }
                }
            }
        }

        // Confetti overlay for streak celebrations (renders on top)
        ConfettiOverlay(
            controller = confettiController,
            modifier = Modifier.fillMaxSize(),
            onAnimationComplete = { viewModel.onCelebrationComplete() }
        )
    }

    if (showChatSheet) {
        SpendingAssistantSheet(
            chatState = chatState,
            onSendMessage = { viewModel.sendMessage(it) },
            onDismiss = { showChatSheet = false }
        )
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyInsightsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No insights yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Add some transactions to see your spending analytics here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 100_000 -> String.format(Locale.getDefault(), "%.1fL", amount / 100_000)
        amount >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", amount / 1_000)
        else -> String.format(Locale.getDefault(), "%.0f", amount)
    }
}
