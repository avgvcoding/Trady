package com.example.trady.data.watchlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlists")
    fun getAllWatchlists(): Flow<List<Watchlist>>

    @Insert
    fun insertWatchlist(w: Watchlist): Long

    @Delete
    fun deleteWatchlist(w: Watchlist)
}