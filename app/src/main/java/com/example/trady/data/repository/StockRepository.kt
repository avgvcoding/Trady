package com.example.trady.data.repository

import com.example.trady.data.network.models.*
import com.example.trady.data.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getTopGainersLosers(apiKey: String): Flow<Resource<TopGainersLosersResponse>>
    fun getDailyTimeSeries(symbol: String, apiKey: String): Flow<Resource<TimeSeriesDailyResponse>>
    fun getCompanyOverview(symbol: String, apiKey: String): Flow<Resource<CompanyOverviewResponse>>
    fun searchSymbol(keywords: String, apiKey: String): Flow<Resource<SymbolSearchResponse>>
}