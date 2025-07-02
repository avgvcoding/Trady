package com.example.trady.di

import com.example.trady.data.network.AlphaVantageApi
import com.example.trady.data.network.FinnhubApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const val ALPHA_BASE_URL   = "https://www.alphavantage.co/"
private const val FINNHUB_BASE_URL = "https://finnhub.io/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides @Singleton
    fun provideOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    //---- AlphaVantage Retrofit ----
    @Provides @Singleton @Named("alphaRetrofit")
    fun provideAlphaRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ALPHA_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideAlphaVantageApi(
        @Named("alphaRetrofit") retrofit: Retrofit
    ): AlphaVantageApi =
        retrofit.create(AlphaVantageApi::class.java)

    //---- Finnhub Retrofit ----
    @Provides @Singleton @Named("finnhubRetrofit")
    fun provideFinnhubRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(FINNHUB_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides @Singleton
    fun provideFinnhubApi(
        @Named("finnhubRetrofit") retrofit: Retrofit
    ): FinnhubApi =
        retrofit.create(FinnhubApi::class.java)
}
