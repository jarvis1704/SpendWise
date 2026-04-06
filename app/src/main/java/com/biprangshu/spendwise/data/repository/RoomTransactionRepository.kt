package com.biprangshu.spendwise.data.repository

import com.biprangshu.spendwise.data.local.dao.TransactionDao
import com.biprangshu.spendwise.data.local.entity.TransactionEntity
import com.biprangshu.spendwise.domain.model.FinancialSummary
import com.biprangshu.spendwise.domain.model.Transaction
import com.biprangshu.spendwise.domain.model.TransactionType
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class RoomTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTodayExpenseTotal(): Flow<Double> {
        val (startOfDay, endOfDay) = getTodayRange()
        return transactionDao.getTodayExpenseTotal(startOfDay, endOfDay)
    }

    override fun getTotalIncome(): Flow<Double> {
        return transactionDao.getTotalIncome()
    }

    override fun getTotalExpense(): Flow<Double> {
        return transactionDao.getTotalExpense()
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        val (startOfDay, endOfDay) = getTodayRange()
        return transactionDao.getFinancialSummaryTuple(startOfDay, endOfDay).map { tuple ->
            FinancialSummary(
                todayExpense = tuple.todayExpense,
                totalIncome = tuple.totalIncome,
                totalExpense = tuple.totalExpense
            )
        }
    }

    override fun getExpenseSinceDate(startDate: Long): Flow<Double> {
        return transactionDao.getExpenseSinceDate(startDate)
    }

    override fun getExpenseForDateRange(start: Long, end: Long): Flow<Double> {
        return transactionDao.getTodayExpenseTotal(start, end)
    }
    
    // Analytics methods for Budget End Screen
    
    override fun getExpenseTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getExpenseTransactionsForPeriod(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMinExpenseTransaction(startDate: Long, endDate: Long): Flow<Transaction?> {
        return transactionDao.getMinExpenseTransaction(startDate, endDate).map { entity ->
            entity?.toDomain()
        }
    }
    
    override fun getMaxExpenseTransaction(startDate: Long, endDate: Long): Flow<Transaction?> {
        return transactionDao.getMaxExpenseTransaction(startDate, endDate).map { entity ->
            entity?.toDomain()
        }
    }
    
    override fun getExpenseCountForPeriod(startDate: Long, endDate: Long): Flow<Int> {
        return transactionDao.getExpenseCountForPeriod(startDate, endDate)
    }
    
    override fun getTotalExpenseForPeriod(startDate: Long, endDate: Long): Flow<Double> {
        return transactionDao.getTotalExpenseForPeriod(startDate, endDate)
    }

    override suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        // Start of day (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // End of day (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return startOfDay to endOfDay
    }
}

// Mapper extensions
private fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        type = TransactionType.valueOf(type),
        category = category,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        date = date,
        note = note
    )
}

private fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type.name,
        category = category,
        tags = tags.joinToString(","),
        date = date,
        note = note
    )
}
