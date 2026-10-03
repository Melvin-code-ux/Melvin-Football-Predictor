package com.melvin.predictor.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.the-odds-api.com/"
    private var currentApiKey: String = ""

    fun setApiKey(apiKey: String) {
        currentApiKey = apiKey
        resetClient()
    }

    private var _okHttpClient: OkHttpClient? = null
    private var _retrofit: Retrofit? = null
    private var _oddsService: OddsApiService? = null

    private fun resetClient() {
        _okHttpClient = null
        _retrofit = null
        _oddsService = null
    }

    private val okHttpClient: OkHttpClient
        get() {
            if (_okHttpClient == null) {
                _okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(ApiKeyInterceptor(currentApiKey))
                    .addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        }
                    )
                    .build()
            }
            return _okHttpClient!!
        }

    private val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                _retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return _retrofit!!
        }

    val oddsService: OddsApiService
        get() {
            if (_oddsService == null) {
                _oddsService = retrofit.create(OddsApiService::class.java)
            }
            return _oddsService!!
        }
}
