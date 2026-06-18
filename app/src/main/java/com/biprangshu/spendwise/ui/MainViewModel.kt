package com.biprangshu.spendwise.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.spendwise.AppStartState
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject



@HiltViewModel
class MainViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    val currencySymbol = userPreferencesManager.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = "₹"
        )

    val isBiometricEnabled: StateFlow<Boolean> = userPreferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isNotificationsEnabled: StateFlow<Boolean> = userPreferencesManager.isNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _openAddTransactionTrigger = MutableStateFlow(false)
    val openAddTransactionTrigger: StateFlow<Boolean> = _openAddTransactionTrigger.asStateFlow()


    private val _openInsightsScreen = MutableStateFlow(false)
    val openInsightsScreen: StateFlow<Boolean> = _openInsightsScreen.asStateFlow()


    fun triggerAddTransaction(trigger: Boolean) {
        _openAddTransactionTrigger.value = trigger
    }

    fun triggerOpenInsightsScreen(trigger: Boolean){
        _openInsightsScreen.value = trigger
    }

    fun onAuthenticationSuccess() {
        _isAuthenticated.value = true
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch { userPreferencesManager.setBiometricEnabled(enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { userPreferencesManager.setNotificationsEnabled(enabled) }
    }

    // Track if user has dismissed the budget end screen
    private val _budgetEndScreenDismissed = MutableStateFlow(false)
    val budgetEndScreenDismissed: StateFlow<Boolean> = _budgetEndScreenDismissed.asStateFlow()

    val startState = combine(
        userPreferencesManager.isOnboardingComplete,
        userPreferencesManager.finishPeriodDate,
        _budgetEndScreenDismissed
    ) { complete, finishMs, dismissed ->
        when {
            !complete -> AppStartState.ONBOARDING
            finishMs == null -> AppStartState.BUDGET_SET
            isPeriodExpired(finishMs) && !dismissed -> AppStartState.PERIOD_ENDED
            isPeriodExpired(finishMs) -> AppStartState.BUDGET_SET
            else -> AppStartState.HOME
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppStartState.LOADING
    )

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            addTransactionUseCase(transaction)
        }
    }


    fun onBudgetEndScreenDismissed() {
        _budgetEndScreenDismissed.value = true
    }


    fun resetBudgetEndState() {
        _budgetEndScreenDismissed.value = false
    }

    private fun isPeriodExpired(finishMs: Long): Boolean {
        val endDate = Instant.ofEpochMilli(finishMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return LocalDate.now().isAfter(endDate)
    }
}
