package com.example.trady.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.trady.data.network.models.TickerInfo
import com.example.trady.data.util.Resource
import com.example.trady.ui.navigation.Screen
import com.example.trady.ui.theme.viewmodel.ExploreViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllScreen(
    isGainers: Boolean,
    navController: NavHostController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    // 1. Collect appropriate list from the ViewModel
    val itemsRes by remember {
        if (isGainers) viewModel.gainers else viewModel.losers
    }.collectAsState()

    // 2. UI scaffold with TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isGainers) "All Top Gainers" else "All Top Losers")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Content
            when (itemsRes) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = "Error: ${(itemsRes as Resource.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is Resource.Success -> {
                    val list = (itemsRes as Resource.Success<List<TickerInfo>>).data
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { ticker ->
                            GainerLoserRow(
                                ticker    = ticker,
                                isGainers = isGainers,
                                onClick   = {
                                    navController.navigate(Screen.Product.createRoute(ticker.symbol))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GainerLoserRow(
    ticker: TickerInfo,
    isGainers: Boolean,
    onClick: () -> Unit
) {
    // Determine arrow color and icon
    val displayColor = if (isGainers) Color(0xFF4CAF50) else Color(0xFFF44336)
    val arrowIcon    = if (isGainers) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo (or fallback circle)
            if (!ticker.logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ticker.logoUrl,
                    contentDescription = "${ticker.companyName} logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(displayColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ticker.symbol.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = displayColor
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Name and symbol
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticker.companyName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = ticker.symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            // Price and percent change + arrow
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${"%.2f".format(ticker.price.toDoubleOrNull() ?: 0.0)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = arrowIcon,
                        contentDescription = null,
                        tint = displayColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = ticker.percentChange,
                        style = MaterialTheme.typography.bodySmall,
                        color = displayColor
                    )
                }
            }
        }
    }
}