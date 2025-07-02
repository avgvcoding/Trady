package com.example.trady.data.watchlist

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun   getAllWatchlists(): Flow<List<Watchlist>>
    suspend fun createWatchlist(name: String): Long
    suspend fun deleteWatchlist(watchlist: Watchlist)

    fun   getItemsFor(watchlistId: Long): Flow<List<WatchlistItem>>
    suspend fun addItemToWatchlist(watchlistId: Long, symbol: String): Long
    suspend fun removeItem(item: WatchlistItem)
    suspend fun deleteItemBySymbol(watchlistId: Long, symbol: String): Int
}
