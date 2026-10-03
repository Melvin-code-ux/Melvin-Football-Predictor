package com.melvin.predictor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.melvin.predictor.R
import com.melvin.predictor.databinding.ItemMatchBinding
import com.melvin.predictor.model.BetSuggestion
import com.melvin.predictor.model.Match
import com.melvin.predictor.utils.PredictionEngine
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MatchAdapter : ListAdapter<Match, MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(
        private val binding: ItemMatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            val prediction = PredictionEngine.predict(match)

            with(binding) {
                // 🏆 Sport Title
                tvSportTitle.text = match.sportTitle.uppercase()

                // ⚽ Teams
                tvHomeTeam.text  = match.homeTeam
                tvAwayTeam.text  = match.awayTeam
                tvMatchTime.text = formatMatchTime(match.commenceTime)

                // 📊 1X2 Probabilities
                tvHomeProb.text = "${match.homeWinProbability}%"
                tvDrawProb.text = "${match.drawProbability}%"
                tvAwayProb.text = "${match.awayWinProbability}%"

                // 🎯 Actual Odds
                val h2h = match.h2hMarket?.outcomes
                tvHomeOdds.text = h2h?.find { it.name == match.homeTeam }
                    ?.price?.let { "@${"%.2f".format(it)}" } ?: "--"
                tvDrawOdds.text = h2h?.find { it.name == "Draw" }
                    ?.price?.let { "@${"%.2f".format(it)}" } ?: "--"
                tvAwayOdds.text = h2h?.find { it.name == match.awayTeam }
                    ?.price?.let { "@${"%.2f".format(it)}" } ?: "--"

                // ⚽ Over/Under
                tvOverUnder.text = match.bestOverUnder

                // 🎯 BTTS
                tvBttsYes.text = match.bttsYes?.let { "${"%.2f".format(it)}" } ?: "N/A"
                tvBttsNo.text  = match.bttsNo?.let { "${"%.2f".format(it)}" } ?: "N/A"

                // 🏆 Top 5 Best Bets
                buildTopBets(match.topBets)

                // 🔮 Overall Prediction
                tvPrediction.text  = prediction.predictedWinner
                tvConfidence.text  = prediction.confidenceLabel
            }
        }

        // 🏆 Build Top 5 Bets dynamically
        private fun buildTopBets(bets: List<BetSuggestion>) {
            binding.topBetsContainer.removeAllViews()

            if (bets.isEmpty()) {
                val tv = TextView(binding.root.context)
                tv.text      = "No markets available"
                tv.textSize  = 11f
                tv.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.text_secondary)
                )
                binding.topBetsContainer.addView(tv)
                return
            }

            bets.forEachIndexed { index, bet ->
                val row = LinearLayout(binding.root.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                }

                // Number badge
                val numTv = TextView(binding.root.context).apply {
                    text      = "${index + 1}."
                    textSize  = 11f
                    setTextColor(ContextCompat.getColor(context, R.color.accent))
                    setPadding(0, 0, 8, 0)
                }

                // Market name
                val marketTv = TextView(binding.root.context).apply {
                    text      = "${bet.market}: "
                    textSize  = 11f
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }

                // Pick
                val pickTv = TextView(binding.root.context).apply {
                    text      = bet.pick
                    textSize  = 11f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                }

                // Odds
                val oddsTv = TextView(binding.root.context).apply {
                    text      = " @${"%.2f".format(bet.odds)}"
                    textSize  = 11f
                    setTextColor(ContextCompat.getColor(context, R.color.accent))
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }

                // Confidence
                val confTv = TextView(binding.root.context).apply {
                    text      = bet.confidence
                    textSize  = 10f
                    setTextColor(ContextCompat.getColor(context, R.color.quota_good))
                }

                row.addView(numTv)
                row.addView(marketTv)
                row.addView(pickTv)
                row.addView(oddsTv)
                row.addView(confTv)
                binding.topBetsContainer.addView(row)
            }
        }

        private fun formatMatchTime(isoTime: String): String {
            return try {
                val inputSdf = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()
                ).apply { timeZone = TimeZone.getTimeZone("UTC") }
                val outputSdf = SimpleDateFormat("EEE dd MMM • HH:mm", Locale.getDefault())
                "📅 ${outputSdf.format(inputSdf.parse(isoTime)!!)}"
            } catch (e: Exception) { isoTime }
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Match, newItem: Match) =
            oldItem == newItem
    }
}
