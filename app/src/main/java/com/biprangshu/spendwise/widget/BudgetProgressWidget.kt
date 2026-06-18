package com.biprangshu.spendwise.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.biprangshu.spendwise.MainActivity
import com.biprangshu.spendwise.di.WidgetEntryPoint
import com.biprangshu.spendwise.ui.theme.DarkColorScheme
import com.biprangshu.spendwise.ui.theme.LightColorScheme
import dagger.hilt.android.EntryPointAccessors

class BudgetProgressWidget: GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.transactionRepository()
        val userPreferences = entryPoint.userPreferencesManager()

        val spendWiseWidgetColors = ColorProviders(
            light = LightColorScheme,
            dark = DarkColorScheme
        )

        provideContent {
            GlanceTheme(colors = spendWiseWidgetColors) {
                val totalBudget by userPreferences.totalBudget.collectAsState(initial = 0.0)
                val currencySymbol by userPreferences.currencySymbol.collectAsState(initial = "₹")
                val startMs by userPreferences.startPeriodDate.collectAsState(initial = null)
                val periodExpense by androidx.compose.runtime.remember(startMs) {
                    if (startMs != null) {
                        repository.getExpenseSinceDate(startMs!!)
                    } else {
                        kotlinx.coroutines.flow.flowOf(0.0)
                    }
                }.collectAsState(initial = 0.0)

                BudgetProgressWidgetComposable(
                    context = context,
                    totalBudget = totalBudget,
                    periodExpense = periodExpense,
                    currency = currencySymbol
                )
            }
        }
    }
}

@Composable
fun BudgetProgressWidgetComposable(
    context: Context,
    totalBudget: Double,
    periodExpense: Double,
    currency: String
) {
    val size = LocalSize.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val hasBudget = totalBudget > 0.0
    val remaining = totalBudget - periodExpense
    val isOverBudget = remaining < 0.0
    val absRemaining = kotlin.math.abs(remaining)
    val progress = if (hasBudget) (periodExpense / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
    val percentSpent = if (hasBudget) ((periodExpense / totalBudget) * 100).toInt() else 0

    val statusColor = when {
        !hasBudget -> GlanceTheme.colors.onSurfaceVariant
        isOverBudget -> GlanceTheme.colors.error
        progress > 0.8f -> GlanceTheme.colors.secondary
        else -> GlanceTheme.colors.primary
    }

    val progressIndicatorColor = when {
        isOverBudget -> GlanceTheme.colors.error
        progress > 0.8f -> GlanceTheme.colors.secondary
        else -> GlanceTheme.colors.primary
    }

    if (size.width < 130.dp || size.height < 90.dp) {
        // UI of the compact version of the Widget
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxSize().padding(6.dp)
            ) {
                if (!hasBudget) {
                    Text(
                        text = "Set Budget",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    Text(
                        text = "$percentSpent%",
                        style = TextStyle(
                            color = statusColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = if (isOverBudget) "Over" else "${currency}${absRemaining.toInt()}",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    } else {
        // Beautiful Medium card widget
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity(intent))
                .padding(12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Budget",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (hasBudget) {
                        Text(
                            text = "$percentSpent%",
                            style = TextStyle(
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                if (!hasBudget) {
                    Box(
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap to set monthly budget",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    Text(
                        text = String.format("Spent %s%.0f of %s%.0f", currency, periodExpense, currency, totalBudget),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    androidx.glance.appwidget.LinearProgressIndicator(
                        progress = progress,
                        modifier = GlanceModifier.fillMaxWidth().height(8.dp).cornerRadius(4.dp),
                        color = progressIndicatorColor,
                        backgroundColor = GlanceTheme.colors.surfaceVariant
                    )

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    val statusText = when {
                        isOverBudget -> String.format("Over limit by %s%.2f! ⚠️", currency, absRemaining)
                        progress > 0.8f -> String.format("%s%.2f left (warning)", currency, remaining)
                        else -> String.format("%s%.2f remaining", currency, remaining)
                    }

                    Text(
                        text = statusText,
                        style = TextStyle(
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}