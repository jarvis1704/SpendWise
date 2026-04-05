package com.biprangshu.spendwise.domain.usecase

import com.biprangshu.spendwise.data.preferences.StreakPreferencesManager
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.model.StreakData
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject


class UpdateStreakUseCase @Inject constructor(
    private val streakPreferencesManager: StreakPreferencesManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(): StreakData {
        val currentStreakData = streakPreferencesManager.streakData.first()
        val budgetPeriodStart = userPreferencesManager.startPeriodDate.first()
        val totalBudget = userPreferencesManager.totalBudget.first()
        val finishDate = userPreferencesManager.finishPeriodDate.first()


        if (budgetPeriodStart == null || totalBudget <= 0 || finishDate == null) {
            return currentStreakData
        }


        if (currentStreakData.periodStartDate != budgetPeriodStart) {
            streakPreferencesManager.resetForNewPeriod(budgetPeriodStart)
            return streakPreferencesManager.streakData.first()
        }

        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val lastCheckEpochDay = currentStreakData.lastCheckDate


        if (lastCheckEpochDay >= todayEpochDay - 1) {
            return currentStreakData
        }


        val dailyBudget = calculateDailyBudget(totalBudget, budgetPeriodStart, finishDate)

        // Check days since last check (up to yesterday)
        val startCheckDay = if (lastCheckEpochDay == 0L) {
            // First time - check yesterday only
            todayEpochDay - 1
        } else {
            // Check from day after last check
            lastCheckEpochDay + 1
        }

        var newStreak = currentStreakData.currentStreak
        var newTarget = currentStreakData.targetStreak
        var newTotal = currentStreakData.totalStreaksAchieved
        var pendingCelebration = currentStreakData.pendingCelebration

        // Check each day from startCheckDay to yesterday
        for (epochDay in startCheckDay until todayEpochDay) {
            val checkDate = LocalDate.ofEpochDay(epochDay)
            
            // Skip if the day is before the budget period started
            val periodStartDate = Instant.ofEpochMilli(budgetPeriodStart)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            if (checkDate.isBefore(periodStartDate)) {
                continue
            }

            val dayExpense = getExpenseForDay(checkDate)
            
            if (dayExpense <= dailyBudget) {
                // User stayed under budget - increment streak
                newStreak++
                
                // Check if milestone reached
                if (newStreak >= newTarget) {
                    newTotal++
                    pendingCelebration = true
                    // Advance to next target
                    newTarget = when {
                        newTarget < 5 -> 5
                        newTarget < 7 -> 7
                        else -> 7 // Max is 7
                    }
                    newStreak = 0 // Reset for next challenge
                }
            } else {
                // User went over budget - break streak
                newStreak = 0
                newTarget = StreakData.MIN_TARGET // Reset to 3-day challenge
            }
        }

        // Update preferences
        streakPreferencesManager.updateStreakData(
            currentStreak = newStreak,
            targetStreak = newTarget,
            totalStreaksAchieved = newTotal,
            lastCheckDate = todayEpochDay - 1, // We checked up to yesterday
            pendingCelebration = pendingCelebration
        )

        return streakPreferencesManager.streakData.first()
    }


    suspend fun incrementStreak() {
        val current = streakPreferencesManager.streakData.first()
        val newStreak = current.currentStreak + 1
        
        if (newStreak >= current.targetStreak) {
            streakPreferencesManager.completeChallengeAndAdvance(
                currentTotal = current.totalStreaksAchieved,
                currentTarget = current.targetStreak
            )
        } else {
            streakPreferencesManager.setCurrentStreak(newStreak)
        }
    }

    /**
     * Reset streak (when user breaks the streak)
     */
    suspend fun resetStreak() {
        streakPreferencesManager.resetCurrentStreak()
    }


    suspend fun clearCelebration() {
        streakPreferencesManager.setPendingCelebration(false)
    }

    private fun calculateDailyBudget(totalBudget: Double, startMs: Long, finishMs: Long): Double {
        val startDate = Instant.ofEpochMilli(startMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val endDate = Instant.ofEpochMilli(finishMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        return if (totalDays > 0) totalBudget / totalDays else totalBudget
    }

    private suspend fun getExpenseForDay(date: LocalDate): Double {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return transactionRepository.getExpenseForDateRange(startOfDay, endOfDay).first()
    }
}
