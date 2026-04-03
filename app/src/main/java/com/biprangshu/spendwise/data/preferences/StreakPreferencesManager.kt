package com.biprangshu.spendwise.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biprangshu.spendwise.domain.model.StreakData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore(name = "streak_preferences")

/**
 * Manages streak-related preferences using DataStore.
 * Tracks user's progress in staying under daily budget.
 */
class StreakPreferencesManager(private val context: Context) {

    companion object {
        val CURRENT_STREAK_KEY = intPreferencesKey("current_streak")
        val TARGET_STREAK_KEY = intPreferencesKey("target_streak")
        val TOTAL_STREAKS_ACHIEVED_KEY = intPreferencesKey("total_streaks_achieved")
        val LAST_CHECK_DATE_KEY = longPreferencesKey("last_check_date")
        val STREAK_PERIOD_START_KEY = longPreferencesKey("streak_period_start")
        val PENDING_CELEBRATION_KEY = booleanPreferencesKey("pending_celebration")
    }

    /**
     * Flow of the complete streak data
     */
    val streakData: Flow<StreakData> = context.streakDataStore.data.map { preferences ->
        StreakData(
            currentStreak = preferences[CURRENT_STREAK_KEY] ?: 0,
            targetStreak = preferences[TARGET_STREAK_KEY] ?: StreakData.MIN_TARGET,
            totalStreaksAchieved = preferences[TOTAL_STREAKS_ACHIEVED_KEY] ?: 0,
            lastCheckDate = preferences[LAST_CHECK_DATE_KEY] ?: 0L,
            periodStartDate = preferences[STREAK_PERIOD_START_KEY] ?: 0L,
            pendingCelebration = preferences[PENDING_CELEBRATION_KEY] ?: false
        )
    }

    /**
     * Flow for just the pending celebration flag (used by InsightsScreen)
     */
    val pendingCelebration: Flow<Boolean> = context.streakDataStore.data.map { preferences ->
        preferences[PENDING_CELEBRATION_KEY] ?: false
    }

    /**
     * Update the current streak count
     */
    suspend fun setCurrentStreak(streak: Int) {
        context.streakDataStore.edit { preferences ->
            preferences[CURRENT_STREAK_KEY] = streak
        }
    }

    /**
     * Update the target streak (challenge goal)
     */
    suspend fun setTargetStreak(target: Int) {
        context.streakDataStore.edit { preferences ->
            preferences[TARGET_STREAK_KEY] = target.coerceIn(StreakData.MIN_TARGET, StreakData.MAX_TARGET)
        }
    }

    /**
     * Update total streaks achieved this period
     */
    suspend fun setTotalStreaksAchieved(total: Int) {
        context.streakDataStore.edit { preferences ->
            preferences[TOTAL_STREAKS_ACHIEVED_KEY] = total
        }
    }

    /**
     * Update the last check date (epoch day)
     */
    suspend fun setLastCheckDate(epochDay: Long) {
        context.streakDataStore.edit { preferences ->
            preferences[LAST_CHECK_DATE_KEY] = epochDay
        }
    }

    /**
     * Update the period start date for reset detection
     */
    suspend fun setPeriodStartDate(epochMs: Long) {
        context.streakDataStore.edit { preferences ->
            preferences[STREAK_PERIOD_START_KEY] = epochMs
        }
    }

    /**
     * Set the pending celebration flag
     */
    suspend fun setPendingCelebration(pending: Boolean) {
        context.streakDataStore.edit { preferences ->
            preferences[PENDING_CELEBRATION_KEY] = pending
        }
    }

    /**
     * Update multiple streak values atomically
     */
    suspend fun updateStreakData(
        currentStreak: Int? = null,
        targetStreak: Int? = null,
        totalStreaksAchieved: Int? = null,
        lastCheckDate: Long? = null,
        periodStartDate: Long? = null,
        pendingCelebration: Boolean? = null
    ) {
        context.streakDataStore.edit { preferences ->
            currentStreak?.let { preferences[CURRENT_STREAK_KEY] = it }
            targetStreak?.let { preferences[TARGET_STREAK_KEY] = it.coerceIn(StreakData.MIN_TARGET, StreakData.MAX_TARGET) }
            totalStreaksAchieved?.let { preferences[TOTAL_STREAKS_ACHIEVED_KEY] = it }
            lastCheckDate?.let { preferences[LAST_CHECK_DATE_KEY] = it }
            periodStartDate?.let { preferences[STREAK_PERIOD_START_KEY] = it }
            pendingCelebration?.let { preferences[PENDING_CELEBRATION_KEY] = it }
        }
    }

    /**
     * Reset streak data for a new budget period
     */
    suspend fun resetForNewPeriod(newPeriodStartMs: Long) {
        context.streakDataStore.edit { preferences ->
            preferences[CURRENT_STREAK_KEY] = 0
            preferences[TARGET_STREAK_KEY] = StreakData.MIN_TARGET
            preferences[TOTAL_STREAKS_ACHIEVED_KEY] = 0
            preferences[LAST_CHECK_DATE_KEY] = 0L
            preferences[STREAK_PERIOD_START_KEY] = newPeriodStartMs
            preferences[PENDING_CELEBRATION_KEY] = false
        }
    }

    /**
     * Reset current streak (when user goes over budget)
     */
    suspend fun resetCurrentStreak() {
        context.streakDataStore.edit { preferences ->
            preferences[CURRENT_STREAK_KEY] = 0
            preferences[TARGET_STREAK_KEY] = StreakData.MIN_TARGET
        }
    }

    /**
     * Mark a challenge as complete and advance to next target
     */
    suspend fun completeChallengeAndAdvance(currentTotal: Int, currentTarget: Int) {
        val nextTarget = when {
            currentTarget < 5 -> 5
            currentTarget < 7 -> 7
            else -> 7
        }
        context.streakDataStore.edit { preferences ->
            preferences[CURRENT_STREAK_KEY] = 0
            preferences[TARGET_STREAK_KEY] = nextTarget
            preferences[TOTAL_STREAKS_ACHIEVED_KEY] = currentTotal + 1
            preferences[PENDING_CELEBRATION_KEY] = true
        }
    }
}
