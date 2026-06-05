package com.biprangshu.spendwise.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.biprangshu.spendwise.MainActivity
import com.biprangshu.spendwise.R
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.di.WidgetEntryPoint
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import com.biprangshu.spendwise.ui.theme.DarkColorScheme
import com.biprangshu.spendwise.ui.theme.LightColorScheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent


class AddSpendWidget : GlanceAppWidget() {
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
                val todayExpense by repository.getTodayExpenseTotal().collectAsState(initial = 0.0)
                val currencySymbol by userPreferences.currencySymbol.collectAsState(initial = "₹")
                WidgetContent(
                    context = context,
                    todayExpense = todayExpense,
                    currency = currencySymbol
                )
            }
        }
    }
}

@Composable
fun WidgetContent(
    context: Context,
    todayExpense: Double,
    currency: String
) {
    val size = LocalSize.current
    val intent = Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_ADD_TRANSACTION
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(MainActivity.EXTRA_OPEN_ADD_TRANSACTION, true)
    }

    if (size.width < 130.dp || size.height < 90.dp) {
        // Compact Quick Add button
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxSize().padding(4.dp)
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_add),
                    contentDescription = "Add Spend",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Add Spend",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    } else {
        // Beautiful Medium card widget
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Spend",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = String.format("%s%.2f", currency, todayExpense),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .cornerRadius(24.dp) // Round button
                        .clickable(actionStartActivity(intent)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_add),
                        contentDescription = "Add Spend",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                        modifier = GlanceModifier.size(24.dp)
                    )
                }
            }
        }
    }
}
