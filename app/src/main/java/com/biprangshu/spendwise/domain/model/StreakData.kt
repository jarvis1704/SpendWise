package com.biprangshu.spendwise.domain.model

/**
 * Represents the user's streak tracking data for staying under daily budget.
 *
 * @param currentStreak Current consecutive days under budget
 * @param targetStreak Current challenge target (3, 5, or 7 days)
 * @param totalStreaksAchieved Total milestones completed this budget period
 * @param lastCheckDate Epoch day when streak was last checked/updated
 * @param periodStartDate Budget period start date (for reset detection)
 * @param pendingCelebration Flag to trigger confetti animation on Insights screen
 */
data class StreakData(
    val currentStreak: Int = 0,
    val targetStreak: Int = 3,
    val totalStreaksAchieved: Int = 0,
    val lastCheckDate: Long = 0L,
    val periodStartDate: Long = 0L,
    val pendingCelebration: Boolean = false
) {

    val daysRemaining: Int
        get() = (targetStreak - currentStreak).coerceAtLeast(0)


    val progress: Float
        get() = if (targetStreak > 0) {
            (currentStreak.toFloat() / targetStreak).coerceIn(0f, 1f)
        } else 0f


    val isChallengeComplete: Boolean
        get() = currentStreak >= targetStreak


    fun getNextTarget(): Int = when {
        targetStreak < 5 -> 5
        targetStreak < 7 -> 7
        else -> 7 // Max is 7
    }

    companion object {
        const val MIN_TARGET = 3
        const val MAX_TARGET = 7

        val TARGETS = listOf(3, 5, 7)
    }
}
