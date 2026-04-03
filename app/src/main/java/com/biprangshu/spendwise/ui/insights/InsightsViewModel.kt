package com.biprangshu.spendwise.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.spendwise.data.preferences.StreakPreferencesManager
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.model.TransactionType
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import com.biprangshu.spendwise.ui.insights.state.ChatMessage
import com.biprangshu.spendwise.ui.insights.state.ChatUiState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val preferencesManager: UserPreferencesManager,
    private val streakPreferencesManager: StreakPreferencesManager
) : ViewModel() {

    private val allTransactionsFlow = repository.getAllTransactions()

    val categoryBreakdown = allTransactionsFlow
        .map { transactions ->
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val thisWeekExpense = allTransactionsFlow
        .map { transactions ->
            val weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            transactions
                .filter { it.type == TransactionType.EXPENSE && it.date >= weekStart }
                .sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val lastWeekExpense = allTransactionsFlow
        .map { transactions ->
            val thisWeekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val lastWeekStart = LocalDate.now()
                .minusWeeks(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            transactions
                .filter { it.type == TransactionType.EXPENSE && it.date >= lastWeekStart && it.date < thisWeekStart }
                .sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val dailyTrend = allTransactionsFlow
        .map { transactions ->
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEE d")
            (13 downTo 0).map { daysAgo ->
                val date = today.minusDays(daysAgo.toLong())
                val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                val label = date.format(formatter)
                val total = transactions
                    .filter { it.type == TransactionType.EXPENSE && it.date in dayStart..dayEnd }
                    .sumOf { it.amount }
                label to total
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val topCategory = categoryBreakdown
        .map { breakdown -> breakdown.maxByOrNull { it.value }?.key }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val totalBudget = preferencesManager.totalBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val finishPeriodDate = preferencesManager.finishPeriodDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val startPeriodDate = preferencesManager.startPeriodDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val currencySymbol = preferencesManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "₹")

    val totalIncome = allTransactionsFlow
        .map { transactions ->
            transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val totalExpense = allTransactionsFlow
        .map { transactions ->
            transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val periodExpense = preferencesManager.startPeriodDate
        .flatMapLatest { startMs ->
            startMs?.let { repository.getExpenseSinceDate(it) } ?: flowOf(0.0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val hasTransactions = allTransactionsFlow
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    // Streak celebration state
    val pendingCelebration = streakPreferencesManager.pendingCelebration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti = _showConfetti.asStateFlow()

    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState = _chatState.asStateFlow()

    init {
        // Check for pending celebration when screen is loaded
        viewModelScope.launch {
            pendingCelebration.collect { isPending ->
                if (isPending) {
                    triggerCelebration()
                }
            }
        }
    }

    /**
     * Trigger confetti celebration and clear the pending flag
     */
    private fun triggerCelebration() {
        viewModelScope.launch {
            _showConfetti.value = true
            // Clear the pending celebration flag so it doesn't trigger again
            streakPreferencesManager.setPendingCelebration(false)
        }
    }

    /**
     * Called when confetti animation completes
     */
    fun onCelebrationComplete() {
        _showConfetti.value = false
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = userMessage,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
            _chatState.update { it.copy(messages = it.messages + userMsg, isLoading = true, error = null) }

            try {
                val prompt = buildPrompt(userMessage)
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-3.1-flash-lite-preview")
                val response = model.generateContent(prompt)
                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.text ?: "Sorry, I couldn't generate a response.",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _chatState.update { it.copy(messages = it.messages + aiMsg, isLoading = false) }
            } catch (e: Exception) {
                _chatState.update { it.copy(isLoading = false, error = e.message ?: "Failed to get response") }
            }
        }
    }

    fun clearError() {
        _chatState.update { it.copy(error = null) }
    }

    private fun buildPrompt(userMessage: String): String {
        val currency = currencySymbol.value
        val budget = totalBudget.value
        val expense = periodExpense.value
        val remaining = (budget - expense).coerceAtLeast(0.0)
        val categoryData = categoryBreakdown.value.entries
            .sortedByDescending { it.value }
            .take(5)
            .joinToString(", ") { "${it.key}: $currency${String.format("%.0f", it.value)}" }
        val thisWeek = thisWeekExpense.value
        val lastWeek = lastWeekExpense.value
        val endDate = finishPeriodDate.value?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val daysRemaining = endDate?.let {
            ChronoUnit.DAYS.between(LocalDate.now(), it).coerceAtLeast(0)
        } ?: 0L

        return """You are a personal finance assistant for SpendWise. Here is the user's financial data:

Budget period: $currency${String.format("%.0f", budget)} total, $currency${String.format("%.0f", expense)} spent, $currency${String.format("%.0f", remaining)} remaining
Days remaining in period: $daysRemaining
This week's expenses: $currency${String.format("%.0f", thisWeek)}
Last week's expenses: $currency${String.format("%.0f", lastWeek)}
Top spending categories: $categoryData

User's question: $userMessage

Provide a helpful, concise response based on this data. Be encouraging but honest. Keep your response under 150 words."""
    }
}
