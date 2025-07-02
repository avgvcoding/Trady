package com.example.trady.data.network.models

import com.squareup.moshi.Json

data class CompanyOverviewResponse(
    @Json(name = "Symbol")            val symbol:            String,
    @Json(name = "AssetType")         val assetType:         String,
    @Json(name = "Name")              val name:              String,
    @Json(name = "Description")       val description:       String,
    @Json(name = "Industry")          val industry:          String,
    @Json(name = "Sector")            val sector:            String,

    /*  -----------  new fields we show on the screen  ------------------ */
    @Json(name = "MarketCapitalization") val marketCap:      String?,
    @Json(name = "PERatio")             val peRatio:         String?,
    @Json(name = "DividendYield")       val dividendYield:   String?,
    @Json(name = "Beta")                val beta:            String?,
    @Json(name = "ProfitMargin")        val profitMargin:    String?,
    @Json(name = "52WeekLow")           val week52Low:       String?,
    @Json(name = "52WeekHigh")          val week52High:      String?,
    @Json(name = "Exchange")            val exchange:        String?
)
