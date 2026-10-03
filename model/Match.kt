package com.melvin.predictor.model

import com.google.gson.annotations.SerializedName

data class Match(
    @SerializedName("id")            val id: String,
    @SerializedName("sport_key")     val sportKey: String,
    @SerializedName("sport_title")   val sportTitle: String,
    @SerializedName("commence_time") val commenceTime: String,
    @SerializedName("home_team")     val homeTeam: String,
    @SerializedName("away_team")     val awayTeam: String,
    @SerializedName("bookmakers")    val bookmakers: List<Bookmaker>
) {
    // 🎯 Get all available markets
    val allMarkets: List<Market>
        get() = bookmakers.firstOrNull()?.markets ?: emptyList()

    // 1️⃣ H2H Market (Match Winner 1X2)
    val h2hMarket: Market?
        get() = allMarkets.find { it.key == "h2h" }

    // 2️⃣ Totals Market (Over/Under)
    val totalsMarket: Market?
        get() = allMarkets.find { it.key == "totals" }

    // 3️⃣ Spreads Market (Asian Handicap)
    val spreadsMarket: Market?
        get() = allMarkets.find { it.key == "spreads" }

    // 4️⃣ BTTS Market (Both Teams To Score)
    val bttsMarket: Market?
        get() = allMarkets.find { it.key == "btts" }

    // 📊 H2H Probabilities
    val homeWinProbability: Int
        get() {
            val outcomes = h2hMarket?.outcomes ?: return 0
            val home = outcomes.find { it.name == homeTeam }?.price ?: return 0
            val away = outcomes.find { it.name == awayTeam }?.price ?: return 0
            val draw = outcomes.find { it.name == "Draw" }?.price
            return calculateProb(home, away, draw)
        }

    val awayWinProbability: Int
        get() {
            val outcomes = h2hMarket?.outcomes ?: return 0
            val home = outcomes.find { it.name == homeTeam }?.price ?: return 0
            val away = outcomes.find { it.name == awayTeam }?.price ?: return 0
            val draw = outcomes.find { it.name == "Draw" }?.price
            return calculateProb(away, home, draw)
        }

    val drawProbability: Int
        get() = (100 - homeWinProbability - awayWinProbability).coerceAtLeast(0)

    // 📊 Best Over/Under line
    val bestOverUnder: String
        get() {
            val outcomes = totalsMarket?.outcomes ?: return "N/A"
            val over = outcomes.find { it.name == "Over" }
            val under = outcomes.find { it.name == "Under" }
            return if (over != null && under != null)
                "O${over.point ?: "2.5"} @${over.price} / U${under.point ?: "2.5"} @${under.price}"
            else "N/A"
        }

    // 📊 BTTS odds
    val bttsYes: Double?
        get() = bttsMarket?.outcomes?.find {
            it.name.contains("Yes", ignoreCase = true)
        }?.price

    val bttsNo: Double?
        get() = bttsMarket?.outcomes?.find {
            it.name.contains("No", ignoreCase = true)
        }?.price

    // 🔮 Top 5 Best Bets
    val topBets: List<BetSuggestion>
        get() {
            val bets = mutableListOf<BetSuggestion>()

            // Bet 1 - Match Winner
            h2hMarket?.let {
                val outcomes = it.outcomes
                val best = outcomes.minByOrNull { o -> o.price }
                best?.let { o ->
                    bets.add(BetSuggestion(
                        market     = "Match Winner",
                        pick       = o.name,
                        odds       = o.price,
                        confidence = calculateConfidence(o.price)
                    ))
                }
            }

            // Bet 2 - Over/Under
            totalsMarket?.let {
                val over  = it.outcomes.find { o -> o.name == "Over" }
                val under = it.outcomes.find { o -> o.name == "Under" }
                val best  = listOfNotNull(over, under).minByOrNull { o -> o.price }
                best?.let { o ->
                    bets.add(BetSuggestion(
                        market     = "Over/Under ${o.point ?: "2.5"}",
                        pick       = "${o.name} ${o.point ?: "2.5"}",
                        odds       = o.price,
                        confidence = calculateConfidence(o.price)
                    ))
                }
            }

            // Bet 3 - BTTS
            bttsMarket?.let {
                val yes = it.outcomes.find { o ->
                    o.name.contains("Yes", ignoreCase = true)
                }
                yes?.let { o ->
                    bets.add(BetSuggestion(
                        market     = "Both Teams Score",
                        pick       = "BTTS Yes",
                        odds       = o.price,
                        confidence = calculateConfidence(o.price)
                    ))
                }
            }

            // Bet 4 - Spreads/Handicap
            spreadsMarket?.let {
                val best = it.outcomes.minByOrNull { o -> o.price }
                best?.let { o ->
                    bets.add(BetSuggestion(
                        market     = "Asian Handicap",
                        pick       = "${o.name} ${o.point ?: "0"}",
                        odds       = o.price,
                        confidence = calculateConfidence(o.price)
                    ))
                }
            }

            // Bet 5 - Draw No Bet
            h2hMarket?.let {
                val home = it.outcomes.find { o -> o.name == homeTeam }
                val away = it.outcomes.find { o -> o.name == awayTeam }
                val best = listOfNotNull(home, away).minByOrNull { o -> o.price }
                best?.let { o ->
                    bets.add(BetSuggestion(
                        market     = "Draw No Bet",
                        pick       = o.name,
                        odds       = o.price,
                        confidence = calculateConfidence(o.price)
                    ))
                }
            }

            return bets.take(5)
        }

    private fun calculateProb(
        targetOdds: Double,
        otherOdds: Double,
        drawOdds: Double?
    ): Int {
        val t = 1.0 / targetOdds
        val o = 1.0 / otherOdds
        val d = drawOdds?.let { 1.0 / it } ?: 0.0
        return ((t / (t + o + d)) * 100).toInt()
    }

    private fun calculateConfidence(odds: Double): String {
        return when {
            odds <= 1.5  -> "🔥 Very High"
            odds <= 2.0  -> "✅ High"
            odds <= 3.0  -> "⚠️ Medium"
            odds <= 5.0  -> "❓ Low"
            else         -> "🎲 Very Low"
        }
    }
}

// 💡 Bet Suggestion Model
data class BetSuggestion(
    val market     : String,
    val pick       : String,
    val odds       : Double,
    val confidence : String
)

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
    @SerializedName("price") val price: Double,
    @SerializedName("point") val point: Double? = null
)
