package com.biprangshu.spendwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biprangshu.spendwise.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class FinancialSummaryTuple(
    val todayExpense: Double,
    val totalIncome: Double,
    val totalExpense: Double
)

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE title LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%' 
        ORDER BY date DESC
        """
    )
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :start 
        AND date <= :end
        """
    )
    fun getTodayExpenseTotal(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE' AND date >= :startDate")
    fun getExpenseSinceDate(startDate: Long): Flow<Double>

    // Analytics queries for Budget End Screen
    
    @Query("""
        SELECT * FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY date DESC
    """)
    fun getExpenseTransactionsForPeriod(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY amount ASC 
        LIMIT 1
    """)
    fun getMinExpenseTransaction(startDate: Long, endDate: Long): Flow<TransactionEntity?>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY amount DESC 
        LIMIT 1
    """)
    fun getMaxExpenseTransaction(startDate: Long, endDate: Long): Flow<TransactionEntity?>
    
    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    fun getExpenseCountForPeriod(startDate: Long, endDate: Long): Flow<Int>
    
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    fun getTotalExpenseForPeriod(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' AND date >= :todayStart AND date <= :todayEnd
                             THEN amount ELSE 0 END), 0.0) AS todayExpense,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0.0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0.0) AS totalExpense
        FROM transactions
    """)
    fun getFinancialSummaryTuple(todayStart: Long, todayEnd: Long): Flow<FinancialSummaryTuple>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
