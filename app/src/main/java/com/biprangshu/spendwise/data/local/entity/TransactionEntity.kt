package com.biprangshu.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String,        // "INCOME" or "EXPENSE"
    val category: String,    // predefined category tag
    val tags: String = "",   // comma-separated custom tags
    val date: Long,          // Unix millis
    val note: String = ""
)
