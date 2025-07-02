package com.example.trady.data.network.models

import com.squareup.moshi.Json

data class TopGainersLosersResponse(
    @Json(name = "metadata")            val metadata: String?,
    @Json(name = "last_updated")        val lastUpdated: String?,
    @Json(name = "most_actively_traded")val mostActively: List<TickerInfo>?,
    @Json(name = "top_gainers")         val gainers:        List<TickerInfo>?,
    @Json(name = "top_losers")          val losers:         List<TickerInfo>?
)

data class TickerInfo(
    @Json(name = "ticker")            val symbol:        String,
    @Json(name = "price")             val price:         String,
    @Json(name = "change_amount")     val change:        String,
    @Json(name = "change_percentage") val percentChange: String,

    // new fields for Finnhub
    var companyName: String = symbol,
    var logoUrl:      String? = null
)