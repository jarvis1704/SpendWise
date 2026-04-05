package com.biprangshu.spendwise.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.spendwise.data.preferences.StreakPreferencesManager
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.model.ResidueDistributionMethod
import com.biprangshu.spendwise.domain.model.StreakData
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import com.biprangshu.spendwise.domain.usecase.GetFinancialSummaryUseCase
import com.biprangshu.spendwise.domain.usecase.UpdateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.max

data class HomeUiState(
    val dailyBudget: Double = 2000.0,
    val todayExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val currencySymbol: String = "₹",
    val isLoading: Boolean = true,
    val totalBudget: Double = 0.0,
    val periodEndDate: Long? = null,
    val periodExpense: Double = 0.0,
    val showResidueDialog: Boolean = false,
    val yesterdayResidue: Double = 0.0,
    val streakData: StreakData = StreakData()
) {
    val remainingToday: Double
        get() = (dailyBudget - todayExpense).coerceAtLeast(0.0)

    val dailyProgress: Float
        get() = (todayExpense / dailyBudget.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()

    val isOverspent: Boolean
        get() = todayExpense > dailyBudget

    val balance: Double
        get() = totalIncome - totalExpense
}

private data class CorePrefs(
    val totalBudget: Double,
    val startMs: Long?,
    val finishMs: Long?,
    val currencySymbol: String
)

private data class BudgetSnapshot(
    val totalBudget: Double,
    val startMs: Long?,
    val finishMs: Long?,
    val currencySymbol: String,
    val residueMethod: ResidueDistributionMethod,
    val lastResidueDialogDay: Long
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val preferencesManager: UserPreferencesManager,
    private val transactionRepository: TransactionRepository,
    private val streakPreferencesManager: StreakPreferencesManager,
    private val updateStreakUseCase: UpdateStreakUseCase
) : ViewModel() {

    private val _todayResidueOverride = MutableStateFlow<ResidueDistributionMethod?>(null)

    init {
        // Check and update streaks on app open
        viewModelScope.launch {
            updateStreakUseCase()
        }
    }

    val uiState = combine(
        preferencesManager.totalBudget,
        preferencesManager.startPeriodDate,
        preferencesManager.finishPeriodDate,
        preferencesManager.currencySymbol
    ) { totalBudget, startMs, finishMs, currencySymbol ->
        CorePrefs(totalBudget, startMs, finishMs, currencySymbol)
    }.combine(
        combine(
            preferencesManager.residueDistributionMethod,
            preferencesManager.lastResidueDialogDay
        ) { method, lastDay -> method to lastDay }
    ) { core, (method, lastDay) ->
        BudgetSnapshot(
            totalBudget = core.totalBudget,
            startMs = core.startMs,
            finishMs = core.finishMs,
            currencySymbol = core.currencySymbol,
            residueMethod = method,
            lastResidueDialogDay = lastDay
        )
    }.flatMapLatest { snapshot ->
        val expenseFlow = snapshot.startMs
            ?.let { transactionRepository.getExpenseSinceDate(it) }
            ?: flowOf(0.0)

        val yesterdayStart = LocalDate.now().minusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayEnd = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val yesterdayExpenseFlow = transactionRepository.getExpenseForDateRange(yesterdayStart, yesterdayEnd)

        combine(
            getFinancialSummaryUseCase(),
            expenseFlow,
            yesterdayExpenseFlow,
            _todayResidueOverride,
            streakPreferencesManager.streakData
        ) { summary, periodExpense, yesterdayExpense, todayOverride, streakData ->
            val totalPeriodDays = snapshot.startMs?.let { startMs ->
                snapshot.finishMs?.let { finishMs ->
                    ChronoUnit.DAYS.between(
                        Instant.ofEpochMilli(startMs).atZone(ZoneId.systemDefault()).toLocalDate(),
                        Instant.ofEpochMilli(finishMs).atZone(ZoneId.systemDefault()).toLocalDate()
                    ) + 1
                }
            }?.coerceAtLeast(1) ?: 1L

            val baseDailyBudget = if (snapshot.totalBudget > 0 && totalPeriodDays > 0) {
                snapshot.totalBudget / totalPeriodDays
            } else {
                UserPreferencesManager.DEFAULT_DAILY_BUDGET
            }

            val remainingDays = snapshot.finishMs?.let { finishMs ->
                val endDate = Instant.ofEpochMilli(finishMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                ChronoUnit.DAYS.between(LocalDate.now(), endDate).coerceAtLeast(0) + 1
            } ?: 1L

            val distributeDailyAllowance = if (snapshot.totalBudget > 0 && snapshot.startMs != null) {
                ((snapshot.totalBudget - periodExpense + summary.totalExpense) / remainingDays.coerceAtLeast(1))
                    .coerceAtLeast(0.0)
            } else {
                UserPreferencesManager.DEFAULT_DAILY_BUDGET
            }

            val addToCurrentAllowance = (baseDailyBudget + max(0.0, baseDailyBudget - yesterdayExpense))
                .coerceAtLeast(0.0)

            val todayEpochDay = LocalDate.now().toEpochDay()
            val hasResidue = yesterdayExpense < baseDailyBudget && baseDailyBudget > 0
            val showResidueDialog = snapshot.residueMethod == ResidueDistributionMethod.ALWAYS_ASK
                && hasResidue
                && todayEpochDay > snapshot.lastResidueDialogDay
                && todayOverride == null
                && snapshot.startMs != null

            val dailyAllowance = when {
                snapshot.totalBudget <= 0 || snapshot.startMs == null -> UserPreferencesManager.DEFAULT_DAILY_BUDGET
                todayOverride == ResidueDistributionMethod.ADD_TO_CURRENT -> addToCurrentAllowance
                todayOverride != null -> distributeDailyAllowance
                snapshot.residueMethod == ResidueDistributionMethod.ADD_TO_CURRENT -> addToCurrentAllowance
                else -> distributeDailyAllowance
            }

            HomeUiState(
                dailyBudget = dailyAllowance,
                todayExpense = summary.todayExpense,
                totalIncome = summary.totalIncome,
                totalExpense = summary.totalExpense,
                currencySymbol = snapshot.currencySymbol,
                totalBudget = snapshot.totalBudget,
                periodEndDate = snapshot.finishMs,
                periodExpense = periodExpense,
                isLoading = false,
                showResidueDialog = showResidueDialog,
                yesterdayResidue = max(0.0, baseDailyBudget - yesterdayExpense),
                streakData = streakData
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HomeUiState()
    )

    val isNotificationsEnabled: StateFlow<Boolean> = preferencesManager.isNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val notifiedThreshold50: StateFlow<Boolean> = preferencesManager.notifiedThreshold50
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val notifiedThreshold90: StateFlow<Boolean> = preferencesManager.notifiedThreshold90
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val notifiedThreshold100: StateFlow<Boolean> = preferencesManager.notifiedThreshold100
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    fun markThresholdNotified(threshold: Int) {
        viewModelScope.launch { preferencesManager.setThresholdNotified(threshold) }
    }

    fun dismissResidueDialog(chosenMethod: ResidueDistributionMethod) {
        viewModelScope.launch {
            preferencesManager.setLastResidueDialogDay(LocalDate.now().toEpochDay())
            _todayResidueOverride.value = chosenMethod
        }
    }
}
