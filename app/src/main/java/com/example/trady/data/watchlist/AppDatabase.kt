package com.example.trady.data.watchlist

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Watchlist::class, WatchlistItem::class],
    version  = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchlistItemDao(): WatchlistItemDao
}