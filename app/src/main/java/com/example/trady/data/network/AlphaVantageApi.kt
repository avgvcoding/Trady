package com.example.trady.data.network

import com.example.trady.data.network.models.CompanyOverviewResponse
import com.example.trady.data.network.models.SymbolSearchResponse
import com.example.trady.data.network.models.TimeSeriesDailyResponse
import com.example.trady.data.network.models.TopGainersLosersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApi {
    @GET("query")
    suspend fun getTopGainersLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey") apiKey: String
    ): Response<TopGainersLosersResponse>

    @GET("query")
    suspend fun getTimeSeriesDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("outputsize") outputSize: String = "compact",
        @Query("apikey") apiKey: String
    ): Response<TimeSeriesDailyResponse>

    @GET("query")
    suspend fun getCompanyOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<CompanyOverviewResponse>

    @GET("query")
    suspend fun searchSymbol(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): Response<SymbolSearchResponse>
}