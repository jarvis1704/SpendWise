package com.biprangshu.spendwise.domain.usecase

import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
}
