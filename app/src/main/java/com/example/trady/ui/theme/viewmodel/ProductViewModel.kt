package com.example.trady.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trady.BuildConfig
import com.example.trady.data.network.FinnhubApi
import com.example.trady.data.network.models.CompanyOverviewResponse
import com.example.trady.data.network.models.TimeSeriesDailyResponse
import com.example.trady.data.repository.StockRepository
import com.example.trady.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.SortedMap
import javax.inject.Inject

enum class Range { D1, W1, M1, M6, Y1 }

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repo: StockRepository,
    private val finnhubApi: FinnhubApi            // ← NEW
) : ViewModel() {

    private val _timeSeries =
        MutableStateFlow<Resource<TimeSeriesDailyResponse>>(Resource.Loading)
    val timeSeries: StateFlow<Resource<TimeSeriesDailyResponse>> = _timeSeries

    private val _overview =
        MutableStateFlow<Resource<CompanyOverviewResponse>>(Resource.Loading)
    val overview: StateFlow<Resource<CompanyOverviewResponse>> = _overview

    private val _logo = MutableStateFlow<String?>(null)
    val logo: StateFlow<String?> = _logo

    private val _range = MutableStateFlow(Range.D1)
    val range: StateFlow<Range> = _range

    fun selectRange(r: Range) {
        _range.value = r
    }

    /** Call once per symbol */
    fun load(symbol: String) {
        /* --- Alpha Vantage daily candles -------------------------------- */
        viewModelScope.launch {
            repo.getDailyTimeSeries(symbol, BuildConfig.ALPHA_VANTAGE_API_KEY)
                .collect { result -> _timeSeries.value = result }
        }

        /* --- Alpha Vantage overview ------------------------------------- */
        viewModelScope.launch {
            repo.getCompanyOverview(symbol, BuildConfig.ALPHA_VANTAGE_API_KEY)
                .collect { result -> _overview.value = result }
        }

        /* --- Finnhub logo ------------------------------------------------ */
        viewModelScope.launch {
            try {
                val resp = finnhubApi.getCompanyProfile(
                    symbol = symbol,
                    apiKey = BuildConfig.FINNHUB_API_KEY
                )
                _logo.value = resp.body()?.logo
            } catch (_: Exception) {
                _logo.value = null
            }
        }
    }

    /* util: filter the candles based on selected range (MVVM keeps UI thin) */
    fun filteredSeries(): List<Pair<String, Float>> {
        val raw = (_timeSeries.value as? Resource.Success)
            ?.data?.series ?: return emptyList()

        val sorted = raw.toSortedMap()                      // ascending yyyy-MM-dd

        return when (range.value) {
            Range.D1 ->                                         // just the last candle
                listOf(sorted.entries.last()).map { it.key to (it.value.close.toFloat()) }

            Range.W1 ->
                sorted.filterKeysAfter(days = 7)

            Range.M1 ->
                sorted.filterKeysAfter(days = 30)

            Range.M6 ->
                sorted.filterKeysAfter(days = 180)

            Range.Y1 ->
                sorted.filterKeysAfter(days = 365)
        }
    }

    /* extension */
    private fun <K : String, V> SortedMap<K, V>.filterKeysAfter(days: Long)
            : List<Pair<K, Float>> {

        val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val cutoff = java.time.LocalDate.now().minusDays(days)

        return this
            .filterKeys { java.time.LocalDate.parse(it, fmt) >= cutoff }
            .map {
                it.key to ((it.value as com.example.trady.data.network.models.DailyData)
                    .close.toFloatOrNull() ?: 0f)
            }
    }
}

