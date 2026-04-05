package com.biprangshu.spendwise.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.biprangshu.spendwise.MainActivity
import com.biprangshu.spendwise.R

object NotificationHelper {

    const val CHANNEL_BUDGET = "spendwise_budget"
    const val CHANNEL_STREAK = "spendwise_streak"
    const val ID_BUDGET_50 = 1001
    const val ID_BUDGET_90 = 1002
    const val ID_BUDGET_100 = 1003
    const val ID_STREAK = 1004

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val budgetChannel = NotificationChannel(
            CHANNEL_BUDGET,
            "Budget Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you reach budget thresholds"
        }

        val streakChannel = NotificationChannel(
            CHANNEL_STREAK,
            "Streak Achievements",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when you complete a streak challenge"
        }

        notificationManager.createNotificationChannel(budgetChannel)
        notificationManager.createNotificationChannel(streakChannel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun postBudgetThresholdNotification(
        context: Context,
        threshold: Int,
        currencySymbol: String,
        spent: Double,
        total: Double
    ) {
        if (!hasNotificationPermission(context)) return

        val notificationId = when (threshold) {
            50 -> ID_BUDGET_50
            90 -> ID_BUDGET_90
            else -> ID_BUDGET_100
        }

        val title = when (threshold) {
            50 -> "Halfway through your budget"
            90 -> "Almost out of budget!"
            else -> "Budget exhausted"
        }

        val body = when (threshold) {
            100 -> "You've spent your entire period budget of $currencySymbol${formatAmount(total)}."
            else -> "You've used $threshold% of your period budget. $currencySymbol${formatAmount(spent)} of $currencySymbol${formatAmount(total)} spent."
        }

        val pendingIntent = buildMainActivityIntent(context, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun postStreakNotification(context: Context, targetStreak: Int, totalAchieved: Int) {
        if (!hasNotificationPermission(context)) return

        val title = "Streak challenge complete!"
        val body = "You completed a $targetStreak-day streak! " +
                "That's $totalAchieved total challenge${if (totalAchieved != 1) "s" else ""} finished."

        val pendingIntent = buildMainActivityIntent(context, ID_STREAK)

        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ID_STREAK, notification)
    }

    private fun buildMainActivityIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            "%.2f".format(amount)
        }
    }
}
