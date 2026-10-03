package com.melvin.predictor.utils

import com.melvin.predictor.model.Match

object PredictionEngine {

    fun predict(match: Match): PredictionResult {
        val homeProb = match.homeWinProbability
        val awayProb = match.awayWinProbability
        val drawProb = match.drawProbability.coerceAtLeast(0)

        val winner = when {
            homeProb > awayProb && homeProb > drawProb -> match.homeTeam
            awayProb > homeProb && awayProb > drawProb -> match.awayTeam
            else                                        -> "Draw"
        }

        val confidence = maxOf(homeProb, awayProb, drawProb)

        val confidenceLabel = when {
            confidence >= 70 -> "🔥 High Confidence"
            confidence >= 50 -> "✅ Moderate"
            confidence >= 35 -> "⚠️ Low Confidence"
            else             -> "❓ Very Uncertain"
        }

        return PredictionResult(
            predictedWinner = winner,
            homeWinProb     = homeProb,
            awayWinProb     = awayProb,
            drawProb        = drawProb,
            confidence      = confidence,
            confidenceLabel = confidenceLabel
        )
    }
}

data class PredictionResult(
    val predictedWinner : String,
    val homeWinProb     : Int,
    val awayWinProb     : Int,
    val drawProb        : Int,
    val confidence      : Int,
    val confidenceLabel : String
)
