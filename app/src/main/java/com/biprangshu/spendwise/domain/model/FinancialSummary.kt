package com.biprangshu.spendwise.domain.model

data class FinancialSummary(
    val todayExpense: Double,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double get() = totalIncome - totalExpense
}
