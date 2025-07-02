package com.example.trady.di

import android.content.Context
import androidx.room.Room
import com.example.trady.data.watchlist.AppDatabase
import com.example.trady.data.watchlist.WatchlistDao
import com.example.trady.data.watchlist.WatchlistItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "trady-db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao =
        db.watchlistDao()

    @Provides
    fun provideWatchlistItemDao(db: AppDatabase): WatchlistItemDao =
        db.watchlistItemDao()
}