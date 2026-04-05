package com.biprangshu.spendwise.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biprangshu.spendwise.domain.model.ResidueDistributionMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val DAILY_BUDGET_KEY = doublePreferencesKey("daily_budget")
        val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
        val IS_ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("is_onboarding_complete")
        val TOTAL_BUDGET_KEY = doublePreferencesKey("total_budget")
        val START_PERIOD_DATE_KEY = longPreferencesKey("start_period_date")
        val FINISH_PERIOD_DATE_KEY = longPreferencesKey("finish_period_date")
        val FINISH_PERIOD_ACTUAL_DATE_KEY = longPreferencesKey("finish_period_actual_date")
        val RESIDUE_DISTRIBUTION_METHOD_KEY = stringPreferencesKey("residue_distribution_method")
        val LAST_RESIDUE_DIALOG_DAY_KEY = longPreferencesKey("last_residue_dialog_day")
        val IS_BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("is_biometric_enabled")

        const val DEFAULT_DAILY_BUDGET = 2000.0
        const val DEFAULT_CURRENCY_SYMBOL = "₹"
    }

    val dailyBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[DAILY_BUDGET_KEY] ?: DEFAULT_DAILY_BUDGET
    }

    val currencySymbol: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_SYMBOL_KEY] ?: DEFAULT_CURRENCY_SYMBOL
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDING_COMPLETE_KEY] ?: false
    }

    val totalBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_BUDGET_KEY] ?: 0.0
    }

    val startPeriodDate: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[START_PERIOD_DATE_KEY]
    }

    val finishPeriodDate: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[FINISH_PERIOD_DATE_KEY]
    }

    val finishPeriodActualDate: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[FINISH_PERIOD_ACTUAL_DATE_KEY]
    }

    val residueDistributionMethod: Flow<ResidueDistributionMethod> = context.dataStore.data.map { preferences ->
        val name = preferences[RESIDUE_DISTRIBUTION_METHOD_KEY] ?: ResidueDistributionMethod.DISTRIBUTE.name
        ResidueDistributionMethod.valueOf(name)
    }

    val lastResidueDialogDay: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_RESIDUE_DIALOG_DAY_KEY] ?: -1L
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED_KEY] ?: false
    }

    suspend fun setDailyBudget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_BUDGET_KEY] = amount
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] = symbol
        }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETE_KEY] = true
        }
    }

    suspend fun setBudgetPeriod(total: Double, startMs: Long, finishMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_BUDGET_KEY] = total
            preferences[START_PERIOD_DATE_KEY] = startMs
            preferences[FINISH_PERIOD_DATE_KEY] = finishMs
        }
    }

    suspend fun setResidueDistributionMethod(method: ResidueDistributionMethod) {
        context.dataStore.edit { preferences ->
            preferences[RESIDUE_DISTRIBUTION_METHOD_KEY] = method.name
        }
    }

    suspend fun setLastResidueDialogDay(epochDay: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_RESIDUE_DIALOG_DAY_KEY] = epochDay
        }
    }

    suspend fun finishBudgetEarly(actualFinishDate: Long) {
        context.dataStore.edit { preferences ->
            preferences[FINISH_PERIOD_ACTUAL_DATE_KEY] = actualFinishDate
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun clearBudgetPeriod() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOTAL_BUDGET_KEY)
            preferences.remove(START_PERIOD_DATE_KEY)
            preferences.remove(FINISH_PERIOD_DATE_KEY)
            preferences.remove(FINISH_PERIOD_ACTUAL_DATE_KEY)
            preferences.remove(LAST_RESIDUE_DIALOG_DAY_KEY)
        }
    }


    suspend fun nukeAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
