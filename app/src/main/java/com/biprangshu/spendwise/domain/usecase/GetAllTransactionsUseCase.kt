package com.biprangshu.spendwise.domain.usecase

import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }
}
