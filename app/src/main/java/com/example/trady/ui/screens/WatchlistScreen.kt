package com.example.trady.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.trady.data.watchlist.Watchlist
import com.example.trady.data.watchlist.WatchlistItem
import com.example.trady.data.util.Resource
import com.example.trady.ui.navigation.Screen
import com.example.trady.ui.theme.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(
    navController: NavHostController,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val watchlistsRes by viewModel.watchlists.collectAsState()
    val selectedId   by viewModel.selectedId.collectAsState()
    val itemsRes     by viewModel.items.collectAsState()

    var showNewDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newName       by remember { mutableStateOf("") }
    var newSymbol     by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Watchlists Row ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Watchlists",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showNewDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New list")
            }
        }
        Spacer(Modifier.height(8.dp))

        when (watchlistsRes) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Error   -> Text(
                "Error: ${(watchlistsRes as Resource.Error).message}",
                color = MaterialTheme.colorScheme.error
            )
            is Resource.Success -> {
                val lists = (watchlistsRes as Resource.Success<List<Watchlist>>).data
                if (lists.isEmpty()) {
                    Text("No watchlists. Tap + to create one.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(lists) { wl ->
                            val isSelected = wl.id == selectedId
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectWatchlist(wl.id) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    wl.name,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.deleteWatchlist(wl) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete list")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Items in Selected List ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Items",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                enabled = selectedId != null,
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add symbol")
            }
        }
        Spacer(Modifier.height(8.dp))

        when (itemsRes) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Error   -> Text(
                "Error: ${(itemsRes as Resource.Error).message}",
                color = MaterialTheme.colorScheme.error
            )
            is Resource.Success -> {
                val items = (itemsRes as Resource.Success<List<WatchlistItem>>).data
                if (items.isEmpty()) {
                    Text(
                        if (selectedId == null)
                            "Select a watchlist above"
                        else
                            "No symbols. Tap + to add."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items) { item ->
                            WatchlistRow(
                                symbol      = item.symbol,
                                onClick     = {
                                    navController.navigate(Screen.Product.createRoute(item.symbol))
                                },
                                onRemove    = { viewModel.removeItem(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Dialog: Create new watchlist ---
    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title   = { Text("New Watchlist") },
            text    = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createWatchlist(newName.trim())
                    newName = ""
                    showNewDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Dialog: Add symbol ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title   = { Text("Add Symbol") },
            text    = {
                OutlinedTextField(
                    value = newSymbol,
                    onValueChange = { newSymbol = it },
                    label = { Text("Ticker symbol (e.g. AAPL)") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addItem(newSymbol.trim().uppercase())
                    newSymbol = ""
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WatchlistRow(
    symbol: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo placeholder circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            // Symbol text
            Text(
                text = symbol,
                style    = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove symbol")
            }
        }
    }
}
