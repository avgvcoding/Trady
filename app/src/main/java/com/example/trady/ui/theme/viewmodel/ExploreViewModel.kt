package com.example.trady.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.trady.data.network.models.TickerInfo
import com.example.trady.data.repository.StockRepository
import com.example.trady.data.util.Resource
import com.example.trady.BuildConfig

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repo: StockRepository
) : ViewModel() {

    // UI-state holders
    private val _gainers = MutableStateFlow<Resource<List<TickerInfo>>>(Resource.Loading)
    val gainers: StateFlow<Resource<List<TickerInfo>>> = _gainers

    private val _losers = MutableStateFlow<Resource<List<TickerInfo>>>(Resource.Loading)
    val losers: StateFlow<Resource<List<TickerInfo>>> = _losers

    init {
        loadGainersLosers()
    }

    fun loadGainersLosers() {
        viewModelScope.launch {
            // collect the combined response
            repo.getTopGainersLosers(BuildConfig.ALPHA_VANTAGE_API_KEY)
                .collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            _gainers.value = Resource.Loading
                            _losers.value  = Resource.Loading
                        }
                        is Resource.Success -> {
                            // split into two lists
                            _gainers.value = Resource.Success(res.data.gainers ?: emptyList())
                            _losers.value  = Resource.Success(res.data.losers  ?: emptyList())
                        }
                        is Resource.Error -> {
                            _gainers.value = Resource.Error(res.message)
                            _losers.value  = Resource.Error(res.message)
                        }
                    }
                }
        }
    }
}