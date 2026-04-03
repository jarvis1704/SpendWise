package com.biprangshu.spendwise.di

import android.content.Context
import androidx.room.Room
import com.biprangshu.spendwise.data.local.dao.TransactionDao
import com.biprangshu.spendwise.data.local.database.SpendWiseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpendWiseDatabase {
        return Room.databaseBuilder(
            context,
            SpendWiseDatabase::class.java,
            "spendwise_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: SpendWiseDatabase): TransactionDao {
        return database.transactionDao()
    }
}
