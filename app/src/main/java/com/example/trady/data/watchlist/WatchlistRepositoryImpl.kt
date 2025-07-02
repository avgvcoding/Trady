package com.example.trady.data.watchlist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchlistRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao,
    private val itemDao: WatchlistItemDao
) : WatchlistRepository {

    override fun getAllWatchlists(): Flow<List<Watchlist>> =
        dao.getAllWatchlists()

    override suspend fun createWatchlist(name: String): Long =
        withContext(Dispatchers.IO) {
            dao.insertWatchlist(Watchlist(name = name))
        }

    override suspend fun deleteWatchlist(watchlist: Watchlist) =
        withContext(Dispatchers.IO) {
            dao.deleteWatchlist(watchlist)
        }


    override fun getItemsFor(watchlistId: Long): Flow<List<WatchlistItem>> =
        itemDao.getItemsForWatchlist(watchlistId)

    override suspend fun addItemToWatchlist(watchlistId: Long, symbol: String): Long =
        withContext(Dispatchers.IO) {
            itemDao.insertItem(WatchlistItem(watchlistId = watchlistId, symbol = symbol))
        }

    override suspend fun removeItem(item: WatchlistItem) =
        withContext(Dispatchers.IO) {
            itemDao.deleteItem(item)
        }

    override suspend fun deleteItemBySymbol(watchlistId: Long, symbol: String): Int =
        withContext(Dispatchers.IO) {
            itemDao.deleteItemBySymbol(watchlistId, symbol)   // returns Int
        }
}
