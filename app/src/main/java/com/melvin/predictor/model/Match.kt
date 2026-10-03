package com.melvin.predictor.model

import com.google.gson.annotations.SerializedName

data class Match(
    @SerializedName("id")           val id: String,
    @SerializedName("sport_key")    val sportKey: String,
    @SerializedName("sport_title")  val sportTitle: String,
    @SerializedName("commence_time")val commenceTime: String,
    @SerializedName("home_team")    val homeTeam: String,
    @SerializedName("away_team")    val awayTeam: String,
    @SerializedName("bookmakers")   val bookmakers: List<Bookmaker>
) {
    val bestOdds: Outcomes?
        get() = bookmakers.firstOrNull()?.markets?.firstOrNull()?.outcomes

    val homeWinProbability: Int
        get() {
            val home = bestOdds?.find { it.name == homeTeam }?.price ?: return 0
            val away = bestOdds?.find { it.name == awayTeam }?.price ?: return 0
            val draw = bestOdds?.find { it.name == "Draw" }?.price
            return calculateProbability(home, away, draw)
        }

    val awayWinProbability: Int
        get() {
            val home = bestOdds?.find { it.name == homeTeam }?.price ?: return 0
            val away = bestOdds?.find { it.name == awayTeam }?.price ?: return 0
            val draw = bestOdds?.find { it.name == "Draw" }?.price
            return calculateProbability(away, home, draw)
        }

    val drawProbability: Int
        get() = 100 - homeWinProbability - awayWinProbability

    private fun calculateProbability(
        targetOdds: Double,
        otherOdds: Double,
        drawOdds: Double?
    ): Int {
        val impliedProb = 1.0 / targetOdds
        val otherProb   = 1.0 / otherOdds
        val drawProb    = drawOdds?.let { 1.0 / it } ?: 0.0
        val total       = impliedProb + otherProb + drawProb
        return ((impliedProb / total) * 100).toInt()
    }
}

data class Bookmaker(
    @SerializedName("key")     val key: String,
    @SerializedName("title")   val title: String,
    @SerializedName("markets") val markets: List<Market>
)

data class Market(
    @SerializedName("key")      val key: String,
    @SerializedName("outcomes") val outcomes: List<Outcome>
)

typealias Outcomes = List<Outcome>

data class Outcome(
    @SerializedName("name")  val name: String,
    @SerializedName("price") val price: Double
)
