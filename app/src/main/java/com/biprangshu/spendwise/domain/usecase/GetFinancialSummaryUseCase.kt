package com.biprangshu.spendwise.domain.usecase

import com.biprangshu.spendwise.domain.model.FinancialSummary
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetFinancialSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<FinancialSummary> {
        return combine(
            repository.getTodayExpenseTotal(),
            repository.getTotalIncome(),
            repository.getTotalExpense()
        ) { todayExpense, totalIncome, totalExpense ->
            FinancialSummary(
                todayExpense = todayExpense,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )
        }
    }
}
