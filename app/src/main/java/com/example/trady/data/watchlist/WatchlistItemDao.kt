package com.example.trady.data.watchlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistItemDao {
    @Query("SELECT * FROM watchlist_items WHERE watchlistId = :wlId")
    fun getItemsForWatchlist(wlId: Long): Flow<List<WatchlistItem>>

    @Insert
    fun insertItem(item: WatchlistItem): Long

    @Delete
    fun deleteItem(item: WatchlistItem)

    @Query("""
        DELETE FROM watchlist_items
        WHERE watchlistId = :watchlistId AND symbol = :symbol
    """)
    fun deleteItemBySymbol(
        watchlistId: Long,
        symbol: String
    ): Int
}