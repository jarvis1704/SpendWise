package com.biprangshu.spendwise.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.usecase.AddTransactionUseCase
import com.biprangshu.spendwise.domain.usecase.DeleteTransactionUseCase
import com.biprangshu.spendwise.domain.usecase.GetAllTransactionsUseCase
import com.biprangshu.spendwise.domain.usecase.SearchTransactionsUseCase
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val searchTransactionsUseCase: SearchTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactions = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            getAllTransactionsUseCase()
        } else {
            searchTransactionsUseCase(query)
        }
    }

    val uiState = combine(
        transactions,
        _searchQuery
    ) { transactions, searchQuery ->
        HistoryUiState(
            transactions = transactions,
            searchQuery = searchQuery,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { deleteTransactionUseCase(transaction) }
    }

    fun restoreTransaction(transaction: Transaction) {
        viewModelScope.launch { addTransactionUseCase(transaction) }
    }
}
