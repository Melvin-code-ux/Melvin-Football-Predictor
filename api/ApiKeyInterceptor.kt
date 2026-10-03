package com.melvin.predictor.api

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("apiKey", apiKey)  // ← Auto added to every call!
            .build()

        return chain.proceed(
            original.newBuilder().url(url).build()
        )
    }
}
