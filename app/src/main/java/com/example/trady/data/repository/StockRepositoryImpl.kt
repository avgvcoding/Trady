package com.example.trady.data.repository

import com.example.trady.BuildConfig
import com.example.trady.data.network.AlphaVantageApi
import com.example.trady.data.network.FinnhubApi
import com.example.trady.data.network.models.*
import com.example.trady.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val api: AlphaVantageApi,
    private val finnhubApi: FinnhubApi
) : StockRepository {

    override fun getTopGainersLosers(apiKey: String) = flow {
        emit(Resource.Loading)
        try {
            val resp = api.getTopGainersLosers(apiKey = apiKey)
            if (!resp.isSuccessful || resp.body() == null) {
                emit(Resource.Error("Empty or error response"))
                return@flow
            }
            val raw = resp.body()!!

            // 1) Enrich gainers
            val enrichedGainers = raw.gainers.orEmpty().map { ti ->
                try {
                    val prof = finnhubApi.getCompanyProfile(
                        symbol = ti.symbol,
                        apiKey = BuildConfig.FINNHUB_API_KEY
                    ).body()
                    ti.copy(
                        companyName = prof?.name ?: ti.symbol,
                        logoUrl      = prof?.logo
                    )
                } catch (_: Exception) {
                    ti
                }
            }

            // 2) Enrich losers
            val enrichedLosers = raw.losers.orEmpty().map { ti ->
                try {
                    val prof = finnhubApi.getCompanyProfile(
                        symbol = ti.symbol,
                        apiKey = BuildConfig.FINNHUB_API_KEY
                    ).body()
                    ti.copy(
                        companyName = prof?.name ?: ti.symbol,
                        logoUrl      = prof?.logo
                    )
                } catch (_: Exception) {
                    ti
                }
            }

            // 3) Emit enriched response
            emit(Resource.Success(
                raw.copy(
                    gainers = enrichedGainers,
                    losers  = enrichedLosers
                )
            ))

        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)


    override fun getDailyTimeSeries(symbol: String, apiKey: String): Flow<Resource<TimeSeriesDailyResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resp = api.getTimeSeriesDaily(symbol = symbol, apiKey = apiKey)
            if (resp.isSuccessful) {
                resp.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error("Error ${resp.code()}: ${resp.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getCompanyOverview(symbol: String, apiKey: String): Flow<Resource<CompanyOverviewResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resp = api.getCompanyOverview(symbol = symbol, apiKey = apiKey)
            if (resp.isSuccessful) {
                resp.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error("Error ${resp.code()}: ${resp.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchSymbol(keywords: String, apiKey: String): Flow<Resource<SymbolSearchResponse>> = flow {
        emit(Resource.Loading)
        try {
            val resp = api.searchSymbol(keywords = keywords, apiKey = apiKey)
            if (resp.isSuccessful) {
                resp.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error("Error ${resp.code()}: ${resp.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
}
