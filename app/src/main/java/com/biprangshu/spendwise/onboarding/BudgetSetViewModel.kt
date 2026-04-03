package com.biprangshu.spendwise.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.model.ResidueDistributionMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class BudgetSetViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    val currencySymbol: StateFlow<String> = userPreferencesManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesManager.DEFAULT_CURRENCY_SYMBOL)

    val residueDistributionMethod: StateFlow<ResidueDistributionMethod> =
        userPreferencesManager.residueDistributionMethod
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResidueDistributionMethod.DISTRIBUTE)

    var budgetInput by mutableStateOf("")
        private set

    var selectedEndDate by mutableStateOf<LocalDate?>(null)
        private set

    var showResidueSheet by mutableStateOf(false)
        private set

    val parsedAmount: Double
        get() = budgetInput.toDoubleOrNull() ?: 0.0

    val dailyAllowancePreview: Double?
        get() {
            val amount = parsedAmount
            val endDate = selectedEndDate ?: return null
            if (amount <= 0) return null
            val days = ChronoUnit.DAYS.between(LocalDate.now(), endDate) + 1
            return amount / days.coerceAtLeast(1)
        }

    val canConfirm: Boolean
        get() = parsedAmount > 0 && selectedEndDate != null

    fun onBudgetAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val dotCount = filtered.count { it == '.' }
        if (dotCount <= 1) {
            budgetInput = filtered
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedEndDate = date
    }

    fun openResidueSheet() {
        showResidueSheet = true
    }

    fun closeResidueSheet() {
        showResidueSheet = false
    }

    fun onResidueMethodSelected(method: ResidueDistributionMethod) {
        viewModelScope.launch {
            userPreferencesManager.setResidueDistributionMethod(method)
            showResidueSheet = false
        }
    }

    fun restorePreviousValues() {
        viewModelScope.launch {
            val total = userPreferencesManager.totalBudget.first()
            val finishMs = userPreferencesManager.finishPeriodDate.first()
            if (total > 0) budgetInput = total.toLong().toString()
            if (finishMs != null) {
                selectedEndDate = Instant.ofEpochMilli(finishMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }
    }

    fun confirmBudget(onSuccess: () -> Unit) {
        val amount = parsedAmount
        val endDate = selectedEndDate ?: return

        viewModelScope.launch {
            val today = LocalDate.now()
            val startMs = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val finishMs = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            userPreferencesManager.setBudgetPeriod(amount, startMs, finishMs)
            userPreferencesManager.setOnboardingComplete()

            onSuccess()
        }
    }
}
