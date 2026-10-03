package com.melvin.predictor.api

import com.melvin.predictor.model.Match
import com.melvin.predictor.model.Sport
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OddsApiService {

    // 🌍 Get all available sports/leagues
    @GET("v4/sports")
    suspend fun getAllSports(
        @Query("all") all: Boolean = true
    ): Response<List<Sport>>

    // ⚽ Get odds for specific sport + ALL markets
    @GET("v4/sports/{sport}/odds")
    suspend fun getOddsForSport(
        @Path("sport")        sport: String,
        @Query("regions")     regions: String    = "uk,us,eu,au",
        @Query("markets")     markets: String    = "h2h,totals,spreads,btts",
        @Query("oddsFormat")  oddsFormat: String = "decimal",
        @Query("dateFormat")  dateFormat: String = "iso"
    ): Response<List<Match>>
}
