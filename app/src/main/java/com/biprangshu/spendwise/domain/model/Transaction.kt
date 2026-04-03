package com.biprangshu.spendwise.domain.model

data class Transaction(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val tags: List<String> = emptyList(),
    val date: Long,
    val note: String = ""
)
