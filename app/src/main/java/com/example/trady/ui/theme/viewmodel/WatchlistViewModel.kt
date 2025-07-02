package com.example.trady.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trady.data.util.Resource
import com.example.trady.data.watchlist.Watchlist
import com.example.trady.data.watchlist.WatchlistItem
import com.example.trady.data.watchlist.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repo: WatchlistRepository
) : ViewModel() {

    private val _watchlists = MutableStateFlow<Resource<List<Watchlist>>>(Resource.Loading)
    val watchlists: StateFlow<Resource<List<Watchlist>>> = _watchlists

    private val _selectedId = MutableStateFlow<Long?>(null)
    val selectedId: StateFlow<Long?> = _selectedId

    private val _items = MutableStateFlow<Resource<List<WatchlistItem>>>(Resource.Loading)
    val items: StateFlow<Resource<List<WatchlistItem>>> = _items

    // New StateFlow to hold all items across all watchlists
    private val _allItems = MutableStateFlow<Resource<List<WatchlistItem>>>(Resource.Loading)
    val allItems: StateFlow<Resource<List<WatchlistItem>>> = _allItems

    init {
        loadWatchlists()
        loadAllItems()
    }

    fun loadWatchlists() {
        viewModelScope.launch {
            repo.getAllWatchlists()
                .collect { list ->
                    _watchlists.value = Resource.Success(list)
                }
        }
    }

    fun selectWatchlist(id: Long) {
        _selectedId.value = id
        loadItems(id)
    }

    // Add this method for clearing selection and showing all items
    fun clearSelection() {
        _selectedId.value = null
        loadAllItems()
    }

    private fun loadItems(watchlistId: Long) {
        viewModelScope.launch {
            repo.getItemsFor(watchlistId)
                .collect { list ->
                    _items.value = Resource.Success(list)
                }
        }
    }

    // New method to load ALL items from all watchlists
    fun loadAllItems() {
        viewModelScope.launch {
            try {
                // Get all watchlists first
                val allWatchlists = repo.getAllWatchlists().first()
                val allItemsList = mutableListOf<WatchlistItem>()

                // Collect items from all watchlists
                allWatchlists.forEach { watchlist ->
                    val items = repo.getItemsFor(watchlist.id).first()
                    allItemsList.addAll(items)
                }

                _allItems.value = Resource.Success(allItemsList)

                // If no specific watchlist is selected, show all items in the main items flow
                if (_selectedId.value == null) {
                    _items.value = Resource.Success(allItemsList)
                }
            } catch (e: Exception) {
                _allItems.value = Resource.Error(e.message ?: "Unknown error")
                if (_selectedId.value == null) {
                    _items.value = Resource.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun createWatchlist(name: String) {
        viewModelScope.launch {
            repo.createWatchlist(name)
            // watchlists Flow will update automatically
        }
    }

    fun deleteWatchlist(watchlist: Watchlist) {
        viewModelScope.launch {
            repo.deleteWatchlist(watchlist)
            if (_selectedId.value == watchlist.id) {
                _selectedId.value = null
                _items.value = Resource.Loading
            }
            // Reload all items after deletion
            loadAllItems()
        }
    }

    fun addItem(symbol: String) {
        val wlId = _selectedId.value ?: return
        viewModelScope.launch {
            repo.addItemToWatchlist(wlId, symbol)
            loadItems(wlId)
            // Also update all items
            loadAllItems()
        }
    }

    fun removeItem(item: WatchlistItem) {
        viewModelScope.launch {
            repo.removeItem(item)
            _selectedId.value?.let { loadItems(it) }
            // Also update all items
            loadAllItems()
        }
    }

    fun addItemToWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            repo.addItemToWatchlist(watchlistId, symbol)
            // If this list is currently selected, reload its items
            if (_selectedId.value == watchlistId) {
                loadItems(watchlistId)
            }
            // Always update all items
            loadAllItems()
        }
    }

    /**
     * Remove the given symbol from the specified watchlist.
     * Fixed implementation that properly gets items from the repository.
     */
    fun removeItemFromWatchlist(watchlistId: Long, symbol: String) {
        viewModelScope.launch {
            try {
                // Get all items for this specific watchlist directly from repository
                val watchlistItems = repo.getItemsFor(watchlistId).first()
                watchlistItems
                    .firstOrNull { it.symbol == symbol }
                    ?.let { itemToRemove ->
                        repo.removeItem(itemToRemove)
                    }

                // Reload if this watchlist is currently selected
                if (_selectedId.value == watchlistId) {
                    loadItems(watchlistId)
                }

                // Always update all items
                loadAllItems()
            } catch (e: Exception) {
                // Handle error if needed
                e.printStackTrace()
            }
        }
    }

    /**
     * Check if a symbol exists in a specific watchlist
     */
    fun isSymbolInWatchlist(watchlistId: Long, symbol: String): Boolean {
        val currentAllItems = (_allItems.value as? Resource.Success<List<WatchlistItem>>)?.data
            ?: return false

        return currentAllItems.any {
            it.watchlistId == watchlistId && it.symbol.equals(symbol, ignoreCase = true)
        }
    }

    /**
     * Get all watchlists that contain a specific symbol
     */
    fun getWatchlistsContainingSymbol(symbol: String): List<Long> {
        val currentAllItems = (_allItems.value as? Resource.Success<List<WatchlistItem>>)?.data
            ?: return emptyList()

        return currentAllItems
            .filter { it.symbol.equals(symbol, ignoreCase = true) }
            .map { it.watchlistId }
            .distinct()
    }

    /**
     * Get all unique symbols across all watchlists
     */
    fun getAllUniqueSymbols(): List<String> {
        val currentAllItems = (_allItems.value as? Resource.Success<List<WatchlistItem>>)?.data
            ?: return emptyList()

        return currentAllItems
            .map { it.symbol }
            .distinct()
            .sorted()
    }
}