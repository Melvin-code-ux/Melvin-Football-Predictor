package com.melvin.predictor.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.the-odds-api.com/"

    // 🔑 YOUR KEY HERE!
    private const val API_KEY  = "a25180dd6ffe4d59871a5e45b0c7fae2"

    val oddsService: OddsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val url = chain.request().url
                            .newBuilder()
                            .addQueryParameter("apiKey", API_KEY)
                            .build()
                        chain.proceed(
                            chain.request()
                                .newBuilder()
                                .url(url)
                                .build()
                        )
                    }
                    .addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OddsApiService::class.java)
    }
}
