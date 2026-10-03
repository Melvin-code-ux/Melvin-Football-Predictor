package com.melvin.predictor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.melvin.predictor.databinding.ItemMatchBinding
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
                tvSportTitle.text = match.sportTitle.uppercase()
                tvHomeTeam.text   = match.homeTeam
                tvAwayTeam.text   = match.awayTeam
                tvMatchTime.text  = formatMatchTime(match.commenceTime)
                tvHomeProb.text   = "${prediction.homeWinProb}%"
                tvDrawProb.text   = "${prediction.drawProb}%"
                tvAwayProb.text   = "${prediction.awayWinProb}%"
                tvPrediction.text = prediction.predictedWinner
                tvConfidence.text = prediction.confidenceLabel
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
