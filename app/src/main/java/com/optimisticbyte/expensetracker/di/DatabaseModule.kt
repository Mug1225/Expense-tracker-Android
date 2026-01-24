package com.optimisticbyte.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.optimisticbyte.expensetracker.data.*
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "expense_tracker_db"
        ).addMigrations(AppDatabase.MIGRATION_3_4).build()
    }

    @Provides
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    fun provideMerchantMappingDao(appDatabase: AppDatabase): MerchantMappingDao {
        return appDatabase.merchantMappingDao()
    }

    @Provides
    fun provideSpendingLimitDao(appDatabase: AppDatabase): SpendingLimitDao {
        return appDatabase.spendingLimitDao()
    }
}
