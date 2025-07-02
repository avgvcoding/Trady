package com.example.trady.ui.screens

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.trady.data.network.models.CompanyOverviewResponse
import com.example.trady.data.util.Resource
import com.example.trady.ui.theme.viewmodel.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import com.example.trady.R
import com.example.trady.data.watchlist.Watchlist
import com.example.trady.data.watchlist.WatchlistItem
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductScreen(
    symbol: String,
    onBack: () -> Unit,
    productVM: ProductViewModel = hiltViewModel(),
    watchVM:  WatchlistViewModel = hiltViewModel()
) {
    /* ------------------------------------------------------------ */
    LaunchedEffect(symbol) {
        productVM.load(symbol)
        // Set default range to 1D if not already set
        if (productVM.range.value == null) {
            productVM.selectRange(Range.D1) // Changed default to 1D
        }
    }

    val range         by productVM.range.collectAsState()
    val logoUrl       by productVM.logo.collectAsState()
    val tsRes         by productVM.timeSeries.collectAsState()
    val ovRes         by productVM.overview.collectAsState()
    val itemsRes      by watchVM.items.collectAsState()

    // 1) All existing watchlists
    val watchlistsRes by watchVM.watchlists.collectAsState()

    // 2) Name for a brand‐new watchlist
    var newListName by remember { mutableStateOf("") }

    // 3) Which watchlist IDs this symbol is currently inside
    var selectedListIds by remember { mutableStateOf(setOf<Long>()) }

    // 4) State for popup visibility
    var showWatchlistDialog by remember { mutableStateOf(false) }

    // 5) Whenever the saved items or watchlists change, recompute selectedListIds
    LaunchedEffect(itemsRes, watchlistsRes) {
        val currentItems = (itemsRes as? Resource.Success<List<WatchlistItem>>)?.data ?: emptyList()
        val allLists    = (watchlistsRes as? Resource.Success<List<Watchlist>>)?.data ?: emptyList()
        selectedListIds = allLists
            .filter { wl -> currentItems.any { it.watchlistId == wl.id && it.symbol == symbol } }
            .map { it.id }
            .toSet()
    }

    val watchList : List<WatchlistItem> =
        (itemsRes as? Resource.Success)?.data ?: emptyList()
    val inWatch = watchList.any { it.symbol == symbol }

    val dfPrice = remember { DecimalFormat("#,##0.00") }
    val dfPct   = remember { DecimalFormat("#0.00")  }

    // Calculate dynamic percentage based on selected range
    val (currentPrice, percentageChange) = remember(tsRes, range) {
        if (tsRes is Resource.Success) {
            val candles = productVM.filteredSeries()
            if (candles.isNotEmpty()) {
                val latest = candles.lastOrNull()?.second ?: 0f
                val firstInRange = candles.firstOrNull()?.second ?: latest
                val pctChange = if (firstInRange == 0f) 0f else (latest - firstInRange) / firstInRange * 100f
                Pair(latest, pctChange)
            } else {
                Pair(0f, 0f)
            }
        } else {
            Pair(0f, 0f)
        }
    }

    /* --------------------------- UI ----------------------------- */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details Screen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showWatchlistDialog = true }  // Changed to show dialog
                    ) {
                        Icon(
                            if (inWatch) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            /* ============================================================
                              HEADER : name • price • logo
               ============================================================ */
            if (ovRes is Resource.Success && tsRes is Resource.Success) {
                val d = (ovRes as Resource.Success<CompanyOverviewResponse>).data

                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (!logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                d.name.first().uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            d.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text("${d.symbol} • ${d.assetType} • ${d.exchange}", style = MaterialTheme.typography.bodySmall)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$${dfPrice.format(currentPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${if (percentageChange >= 0) "+" else ""}${dfPct.format(percentageChange)}%",
                            color = if (percentageChange >= 0) Color(0xFF00C853) else Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                CircularProgressIndicator()
            }

            /* ============================================================
                                   PRICE   C H A R T
               ============================================================ */
            Card(
                modifier = Modifier.fillMaxWidth().height(320.dp),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                when (tsRes) {
                    is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    is Resource.Error   -> Text(
                        (tsRes as Resource.Error).message,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    is Resource.Success -> {
                        val series = productVM.filteredSeries()
                        if (series.isNotEmpty()) {
                            val entries = series.mapIndexed { idx, p -> Entry(idx.toFloat(), p.second) }

                            // Format date labels based on range for better display
                            val dateLabels = series.map { dateString ->
                                try {
                                    when (range) {
                                        Range.D1 -> {
                                            // For 1 day, show time (assuming intraday data)
                                            // This assumes your dateString contains time information
                                            // You might need to adjust based on your actual data format
                                            if (dateString.first.contains("T") || dateString.first.contains(" ")) {
                                                // If it's a datetime string
                                                val dateTime = LocalDateTime.parse(dateString.first.replace(" ", "T"))
                                                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                            } else {
                                                // If it's just a date, show abbreviated format
                                                val date = LocalDate.parse(dateString.first)
                                                date.format(DateTimeFormatter.ofPattern("HH:mm"))
                                            }
                                        }
                                        Range.W1 -> {
                                            // For weekly, show day name
                                            val date = LocalDate.parse(dateString.first)
                                            date.format(DateTimeFormatter.ofPattern("EEE"))
                                        }
                                        Range.M1 -> {
                                            // For monthly, show day
                                            val date = LocalDate.parse(dateString.first)
                                            date.format(DateTimeFormatter.ofPattern("dd"))
                                        }
                                        Range.M6 -> {
                                            // For 6 months, show month/day
                                            val date = LocalDate.parse(dateString.first)
                                            date.format(DateTimeFormatter.ofPattern("MM/dd"))
                                        }
                                        else -> {
                                            // Fallback
                                            val date = LocalDate.parse(dateString.first)
                                            date.format(DateTimeFormatter.ofPattern("MM/dd"))
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Fallback for 1D if datetime parsing fails
                                    if (range == Range.D1) {
                                        dateString.first.takeLast(5) // Show last 5 characters (likely time)
                                    } else {
                                        dateString.first // Fallback to original string
                                    }
                                }
                            }

                            // Create formatted dates for marker display
                            val markerDates = series.map { dateString ->
                                try {
                                    when (range) {
                                        Range.D1 -> {
                                            // For 1D, show full datetime if available
                                            if (dateString.first.contains("T") || dateString.first.contains(" ")) {
                                                val dateTime = LocalDateTime.parse(dateString.first.replace(" ", "T"))
                                                dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                                            } else {
                                                dateString.first
                                            }
                                        }
                                        else -> {
                                            val date = LocalDate.parse(dateString.first)
                                            date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                        }
                                    }
                                } catch (e: Exception) {
                                    dateString.first
                                }
                            }

                            AndroidView(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                factory  = { ctx ->
                                    LineChart(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                                        // Remove description
                                        description.isEnabled = false

                                        // Hide grids and axes styling
                                        xAxis.apply {
                                            position = XAxis.XAxisPosition.BOTTOM
                                            setDrawGridLines(false)
                                            setDrawAxisLine(false)
                                            granularity = 1f
                                            textColor = android.graphics.Color.GRAY
                                            textSize = 10f
                                            // Adjust label count based on range
                                            labelCount = when (range) {
                                                Range.D1 -> 6 // More labels for intraday
                                                else -> 4
                                            }
                                        }

                                        axisLeft.apply {
                                            setDrawGridLines(false)
                                            setDrawAxisLine(false)
                                            textColor = android.graphics.Color.GRAY
                                            textSize = 10f
                                            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                                        }

                                        axisRight.isEnabled = false
                                        legend.isEnabled = false

                                        // Touch and zoom settings
                                        setTouchEnabled(true)
                                        isDragEnabled = true
                                        setScaleEnabled(true)
                                        setPinchZoom(true)

                                        // Styling for modern look
                                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                        setDrawBorders(false)

                                        // Custom marker with formatted dates for proper display
                                        marker = PriceMarker(ctx, R.layout.marker_view, markerDates)
                                    }
                                },
                                update = { chart ->
                                    val isPositive = percentageChange >= 0
                                    val lineColor = if (isPositive) android.graphics.Color.parseColor("#00C853")
                                    else android.graphics.Color.parseColor("#D32F2F")

                                    val set = LineDataSet(entries, "Price").apply {
                                        color = lineColor
                                        setDrawCircles(false)
                                        setDrawValues(false)
                                        lineWidth = 3f
                                        mode = LineDataSet.Mode.CUBIC_BEZIER

                                        // Gradient fill
                                        setDrawFilled(true)
                                        fillColor = lineColor
                                        fillAlpha = 30

                                        // Highlight settings
                                        highLightColor = lineColor
                                        setDrawHighlightIndicators(true)
                                        setDrawHorizontalHighlightIndicator(false)
                                        setDrawVerticalHighlightIndicator(true)
                                        highlightLineWidth = 2f
                                    }

                                    // Set the formatted date labels for X-axis
                                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels.toTypedArray())
                                    chart.data = LineData(set)

                                    // Update marker with new formatted dates
                                    chart.marker = PriceMarker(chart.context, R.layout.marker_view, markerDates)

                                    // Animation
                                    chart.animateX(500)
                                    chart.invalidate()
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No data available for selected range",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            /* --------------- Range Selector (Now includes 1D) ------------------------- */
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Now includes D1 (1 day) as the first option
                listOf(Range.D1, Range.W1, Range.M1, Range.M6).forEach { r ->
                    FilterChip(
                        selected = range == r,
                        onClick  = { productVM.selectRange(r) },
                        label    = {
                            Text(when(r) {
                                Range.D1 -> "1D"
                                Range.W1 -> "1W"
                                Range.M1 -> "1M"
                                Range.M6 -> "6M"
                                else -> r.name
                            })
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            /* ============================================================
                                   A  B  O  U  T
               ============================================================ */
            if (ovRes is Resource.Success) {
                val d = (ovRes as Resource.Success<CompanyOverviewResponse>).data

                Text("About", style = MaterialTheme.typography.titleMedium)
                Text(d.description, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                // Improved sector and industry boxes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sector Box
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sector",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                d.sector,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Industry Box
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Industry",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                d.industry,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* -------- 52-Week Strip -------------------------------- */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column { Subtitle("52-Week Low");  ValueText(d.week52Low) }
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Subtitle("Current") ; ValueText("$${dfPrice.format(currentPrice)}")
                    }
                    Spacer(Modifier.weight(1f))
                    Column { Subtitle("52-Week High"); ValueText(d.week52High) }
                }

                Divider(Modifier.padding(vertical = 16.dp))

                /* -------- Metrics Grid (rectangular boxes) ------------- */
                FlowRow(
                    maxItemsInEachRow     = 2,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricBox("Market Cap",     "$${d.marketCap ?: "--"}")
                    MetricBox("P/E Ratio",      d.peRatio)
                    MetricBox("Beta",           d.beta)
                    MetricBox("Dividend Yield", d.dividendYield)
                    MetricBox("Profit Margin",  d.profitMargin)
                }
            }
        }
    }

    // Watchlist Dialog
    if (showWatchlistDialog) {
        AlertDialog(
            onDismissRequest = { showWatchlistDialog = false },
            title = { Text("Manage Watchlists") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Create new watchlist section
                    Text(
                        "Create New Watchlist",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newListName,
                            onValueChange = { newListName = it },
                            placeholder = { Text("Watchlist name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newListName.isNotBlank()) {
                                    watchVM.createWatchlist(newListName.trim())
                                    newListName = ""
                                }
                            },
                            enabled = newListName.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }

                    Divider()

                    // Existing watchlists section
                    Text(
                        "Add to Existing Watchlists",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    val lists = (watchlistsRes as? Resource.Success<List<Watchlist>>)?.data.orEmpty()

                    if (lists.isEmpty()) {
                        Text(
                            "No watchlists available. Create one above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        lists.forEach { wl ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val isNowSelected = !selectedListIds.contains(wl.id)
                                        if (isNowSelected) {
                                            watchVM.addItemToWatchlist(wl.id, symbol)
                                        } else {
                                            watchVM.removeItemFromWatchlist(wl.id, symbol)
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = selectedListIds.contains(wl.id),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            watchVM.addItemToWatchlist(wl.id, symbol)
                                        } else {
                                            watchVM.removeItemFromWatchlist(wl.id, symbol)
                                        }
                                    }
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    wl.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWatchlistDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    BackHandler(onBack = onBack)
}

/* ===============================================================
                         H  E  L  P  E  R  S
   =============================================================== */
@Composable private fun MetricBox(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width  = 1.dp,
                color  = MaterialTheme.colorScheme.outline,
                shape  = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(value ?: "--", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun Subtitle(text: String) =
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable private fun ValueText(v: String?) =
    Text(v ?: "--", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)