package com.melvin.predictor.api

import com.melvin.predictor.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.the-odds-api.com/"

    // 🔌 OkHttp client with timeout settings
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                // Add API key to every request automatically
                ApiKeyInterceptor(BuildConfig.ODDS_API_KEY)
            )
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS // ← Captures quota headers!
                }
            )
            .build()
    }

    // 🌐 Retrofit instance
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🎯 Service instance
    val oddsService: OddsApiService by lazy {
        retrofit.create(OddsApiService::class.java)
    }
}
