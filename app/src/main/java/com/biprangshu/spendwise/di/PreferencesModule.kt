package com.biprangshu.spendwise.di

import android.content.Context
import com.biprangshu.spendwise.data.preferences.StreakPreferencesManager
import com.biprangshu.spendwise.data.preferences.UserPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager {
        return UserPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideStreakPreferencesManager(@ApplicationContext context: Context): StreakPreferencesManager {
        return StreakPreferencesManager(context)
    }
}
