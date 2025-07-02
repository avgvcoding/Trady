package com.example.trady.data.network

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// 1) Data class for the company profile response:
data class CompanyProfileResponse(
    @Json(name="name") val name: String?,
    @Json(name="logo") val logo: String?,
    @Json(name="ticker") val symbol: String?
    // add any other fields you care about
)

// 2) Retrofit interface for Finnhub:
interface FinnhubApi {
    @GET("api/v1/stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Query("token")  apiKey: String
    ): Response<CompanyProfileResponse>
}
