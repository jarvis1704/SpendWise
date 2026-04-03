package com.biprangshu.spendwise.di

import com.biprangshu.spendwise.data.repository.RoomTransactionRepository
import com.biprangshu.spendwise.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        roomTransactionRepository: RoomTransactionRepository
    ): TransactionRepository
}
