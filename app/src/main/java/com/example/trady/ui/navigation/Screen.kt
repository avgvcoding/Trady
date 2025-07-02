package com.example.trady.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Explore : Screen("explore", "Explore", Icons.Filled.ShowChart)
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Filled.List)

    // New: Product screen, param is the ticker symbol
    object Product : Screen(
        "product/{symbol}",           // route pattern
        "Details",                    // label that shows up in the bottom bar (won’t be visible because we hide it)
        Icons.Filled.ShowChart        // any icon — not actually displayed, but must be supplied
    ) {
        // Helper to build a concrete route for a given ticker
        fun createRoute(symbol: String) = "product/$symbol"
    }

    object ViewAllGainers : Screen("viewall/gainers", "Gainers",   Icons.Default.ArrowUpward)
    object ViewAllLosers  : Screen("viewall/losers",  "Losers",    Icons.Default.ArrowDownward)
}