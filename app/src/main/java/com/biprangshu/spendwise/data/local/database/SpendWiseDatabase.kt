package com.biprangshu.spendwise.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.biprangshu.spendwise.data.local.dao.TransactionDao
import com.biprangshu.spendwise.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SpendWiseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
