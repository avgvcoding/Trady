package com.example.trady.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.trady.data.network.models.TickerInfo
import com.example.trady.ui.theme.viewmodel.ExploreViewModel
import com.example.trady.data.util.Resource
import coil.compose.AsyncImage
import com.example.trady.ui.navigation.Screen

@Composable
fun ExploreScreen(
    navController: NavHostController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val gainersRes by viewModel.gainers.collectAsState()
    val losersRes  by viewModel.losers.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Add this line
            .padding(16.dp)
    ) {
        // Top bar with app name and search
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Trady",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Search bar
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO: search functionality */ },
                placeholder = { Text("Search stocks...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
        }

        Spacer(Modifier.height(30.dp))

        // --- Gainers Section ---
        SectionHeader(title = "Top Gainers", onViewAll = {
            navController.navigate(Screen.ViewAllGainers.route)
        })
        Spacer(Modifier.height(8.dp))

        when (gainersRes) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
            is Resource.Error   -> {
                Text(
                    "Error loading gainers: ${(gainersRes as Resource.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            is Resource.Success -> {
                TickerGrid(
                    items     = (gainersRes as Resource.Success<List<TickerInfo>>).data.take(4),
                    isGainers = true,
                    onClick   = { symbol ->
                        navController.navigate(Screen.Product.createRoute(symbol))
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Losers Section ---
        SectionHeader(title = "Top Losers", onViewAll = {
            navController.navigate(Screen.ViewAllLosers.route)
        })
        Spacer(Modifier.height(8.dp))

        when (losersRes) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
            is Resource.Error   -> {
                Text(
                    "Error loading losers: ${(losersRes as Resource.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            is Resource.Success -> {
                TickerGrid(
                    items     = (losersRes as Resource.Success<List<TickerInfo>>).data.take(4),
                    isGainers = false,
                    onClick   = { symbol ->
                        navController.navigate(Screen.Product.createRoute(symbol))
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onViewAll) {
            Text("View All", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TickerGrid(items: List<TickerInfo>, isGainers: Boolean, onClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp), // Fixed height for the grid
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { t ->
            TickerCard(
                t        = t,
                isGainers= isGainers,
                onClick  = { onClick(t.symbol) }  // ← fire with this ticker’s symbol
            )
        }
    }
}

@Composable
private fun TickerCard(t: TickerInfo, isGainers: Boolean, onClick: () -> Unit) {
    // Determine if the change is positive based on the section and actual value
    val changeValue = t.percentChange.trimEnd('%')
        .trimStart('+','−','-')
        .toDoubleOrNull() ?: 0.0

    val isPositive = if (isGainers) true else false
    val displayColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo or fallback circle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!t.logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = t.logoUrl,
                        contentDescription = "${t.companyName} logo",
                        modifier = Modifier
                            .size(55.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    // Fallback circle with first letter or symbol
                    Box(
                        modifier = Modifier
                            .size(55.dp)
                            .background(
                                color = displayColor.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (t.symbol?.firstOrNull()?.toString() ?: t.companyName.firstOrNull()?.toString() ?: "?").uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = displayColor
                        )
                    }
                }

                // Arrow icon
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = displayColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Company Name
            Text(
                text = t.companyName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Symbol (if available)
            if (!t.symbol.isNullOrEmpty()) {
                Text(
                    text = t.symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Text(
                text = "$${"%.2f".format(t.price.toDoubleOrNull() ?: 0.0)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Percent change
            val percentValue = t.percentChange
                .trimEnd('%')
                .trimStart('+', '-', '−')
                .toDoubleOrNull() ?: 0.0

            val roundedPercent = String.format("%.2f", percentValue) // rounds to nearest whole number

            Text(
                text = "${if (isPositive) "+" else "-"}$roundedPercent%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = displayColor
            )
        }
    }
}