package com.biprangshu.spendwise.domain.usecase

import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(query: String): Flow<List<Transaction>> {
        return repository.searchTransactions(query)
    }
}
