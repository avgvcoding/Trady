package com.example.trady.data.network.models

import com.squareup.moshi.Json

data class TimeSeriesDailyResponse(
    @Json(name = "Meta Data")          val meta: MetaData,
    @Json(name = "Time Series (Daily)")val series: Map<String, DailyData>
)

data class MetaData(
    @Json(name = "1. Information")   val info: String,
    @Json(name = "2. Symbol")        val symbol: String,
    @Json(name = "3. Last Refreshed")val lastRefreshed: String,
    @Json(name = "4. Output Size")   val outputSize: String,
    @Json(name = "5. Time Zone")     val timeZone: String
)

data class DailyData(
    @Json(name = "1. open")  val open: String,
    @Json(name = "2. high")  val high: String,
    @Json(name = "3. low")   val low: String,
    @Json(name = "4. close") val close: String,
    @Json(name = "5. volume")val volume: String
)