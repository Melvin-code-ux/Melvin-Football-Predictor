package com.melvin.predictor.api

import com.melvin.predictor.model.Match
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OddsApiService {

    @GET("v4/sports/soccer/odds")
    suspend fun getFootballOdds(
        @Query("regions")    regions: String    = "uk",
        @Query("markets")    markets: String    = "h2h",
        @Query("oddsFormat") oddsFormat: String = "decimal",
        @Query("dateFormat") dateFormat: String = "iso"
    ): Response<List<Match>>
}
