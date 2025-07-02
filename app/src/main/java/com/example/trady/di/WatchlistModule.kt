package com.example.trady.di

import com.example.trady.data.watchlist.WatchlistDao
import com.example.trady.data.watchlist.WatchlistItemDao
import com.example.trady.data.watchlist.WatchlistRepository
import com.example.trady.data.watchlist.WatchlistRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WatchlistModule {
    @Provides @Singleton
    fun provideWatchlistRepository(
        watchlistDao: WatchlistDao,
        itemDao: WatchlistItemDao
    ): WatchlistRepository = WatchlistRepositoryImpl(watchlistDao, itemDao)
}
