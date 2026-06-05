package com.biprangshu.spendwise.di

import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {

    fun transactionRepository(): TransactionRepository
    fun userPreferencesManager(): UserPreferencesManager

}