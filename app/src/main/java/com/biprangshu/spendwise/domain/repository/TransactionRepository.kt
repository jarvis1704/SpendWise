package com.biprangshu.spendwise.domain.repository

import com.biprangshu.spendwise.domain.model.FinancialSummary
import com.biprangshu.spendwise.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getTodayExpenseTotal(): Flow<Double>
    fun getTotalIncome(): Flow<Double>
    fun getTotalExpense(): Flow<Double>
    fun getFinancialSummary(): Flow<FinancialSummary>
    fun getExpenseSinceDate(startDate: Long): Flow<Double>
    fun getExpenseForDateRange(start: Long, end: Long): Flow<Double>
    
    // Analytics methods for Budget End Screen
    fun getExpenseTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getMinExpenseTransaction(startDate: Long, endDate: Long): Flow<Transaction?>
    fun getMaxExpenseTransaction(startDate: Long, endDate: Long): Flow<Transaction?>
    fun getExpenseCountForPeriod(startDate: Long, endDate: Long): Flow<Int>
    fun getTotalExpenseForPeriod(startDate: Long, endDate: Long): Flow<Double>
    
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}
