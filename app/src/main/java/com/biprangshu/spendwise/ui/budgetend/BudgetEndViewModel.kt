package com.biprangshu.spendwise.ui.budgetend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

/**
 * UI state for the Budget End Screen.
 */
data class BudgetEndUiState(
    val isLoading: Boolean = true,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val currencySymbol: String = "₹",
    val startPeriodDate: Long? = null,
    val finishPeriodDate: Long? = null,
    val actualFinishDate: Long? = null,
    val spends: List<Transaction> = emptyList(),
    val minSpend: Transaction? = null,
    val maxSpend: Transaction? = null,
    val spendCount: Int = 0
) {
    val remaining: Double
        get() = (totalBudget - totalSpent).coerceAtLeast(0.0)
    
    val spentPercent: Float
        get() = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    
    val remainingPercent: Float
        get() = if (totalBudget > 0) (remaining / totalBudget).toFloat() else 0f
    
    val isOverBudget: Boolean
        get() = totalSpent > totalBudget
    
    val hasSpends: Boolean
        get() = spends.isNotEmpty()
    
    val periodDays: Long
        get() {
            if (startPeriodDate == null || finishPeriodDate == null) return 0
            val start = Instant.ofEpochMilli(startPeriodDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(actualFinishDate ?: finishPeriodDate).atZone(ZoneId.systemDefault()).toLocalDate()
            return ChronoUnit.DAYS.between(start, end) + 1
        }
    
    /**
     * Groups spends by category and returns a map of category to total amount.
     */
    val categoryBreakdown: Map<String, Double>
        get() = spends.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    
    /**
     * Groups spends by date (day) and returns a map of date to total amount.
     */
    val dailySpending: Map<LocalDate, Double>
        get() = spends.groupBy { transaction ->
            Instant.ofEpochMilli(transaction.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
}

/**
 * ViewModel for the Budget End Screen.
 * Provides analytics data for the completed budget period.
 */
@HiltViewModel
class BudgetEndViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {
    
    private val _showBudgetSetModal = MutableStateFlow(false)
    val showBudgetSetModal: StateFlow<Boolean> = _showBudgetSetModal.asStateFlow()
    
    private data class PeriodInfo(
        val totalBudget: Double,
        val startDate: Long?,
        val finishDate: Long?,
        val actualFinishDate: Long?,
        val currencySymbol: String
    )
    
    val uiState: StateFlow<BudgetEndUiState> = combine(
        preferencesManager.totalBudget,
        preferencesManager.startPeriodDate,
        preferencesManager.finishPeriodDate,
        preferencesManager.finishPeriodActualDate,
        preferencesManager.currencySymbol
    ) { totalBudget, startMs, finishMs, actualFinishMs, currency ->
        PeriodInfo(totalBudget, startMs, finishMs, actualFinishMs, currency)
    }.flatMapLatest { periodInfo ->
        val startMs = periodInfo.startDate ?: return@flatMapLatest flowOf(BudgetEndUiState())
        val endMs = periodInfo.actualFinishDate ?: periodInfo.finishDate ?: return@flatMapLatest flowOf(BudgetEndUiState())
        
        // Get end of day for the finish date
        val endOfDay = Instant.ofEpochMilli(endMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - 1
        
        combine(
            transactionRepository.getExpenseTransactionsForPeriod(startMs, endOfDay),
            transactionRepository.getTotalExpenseForPeriod(startMs, endOfDay),
            transactionRepository.getMinExpenseTransaction(startMs, endOfDay),
            transactionRepository.getMaxExpenseTransaction(startMs, endOfDay),
            transactionRepository.getExpenseCountForPeriod(startMs, endOfDay)
        ) { spends, totalSpent, minSpend, maxSpend, count ->
            BudgetEndUiState(
                isLoading = false,
                totalBudget = periodInfo.totalBudget,
                totalSpent = totalSpent,
                currencySymbol = periodInfo.currencySymbol,
                startPeriodDate = periodInfo.startDate,
                finishPeriodDate = periodInfo.finishDate,
                actualFinishDate = periodInfo.actualFinishDate,
                spends = spends,
                minSpend = minSpend,
                maxSpend = maxSpend,
                spendCount = count
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BudgetEndUiState()
    )
    
    /**
     * Called when user wants to set up a new budget.
     */
    fun onSetupNewBudget() {
        _showBudgetSetModal.value = true
    }
    
    /**
     * Called when budget set modal is dismissed.
     */
    fun onBudgetSetModalDismissed() {
        _showBudgetSetModal.value = false
    }
    
    /**
     * Clears the current budget period data.
     * Called after user confirms a new budget to reset the old period.
     */
    fun clearCurrentPeriod() {
        viewModelScope.launch {
            preferencesManager.clearBudgetPeriod()
        }
    }
}
