package com.melvin.predictor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.melvin.predictor.databinding.ItemMatchBinding
import com.melvin.predictor.model.Match
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MatchAdapter : ListAdapter<Match,
    MatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MatchViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(
        private val binding: ItemMatchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            binding.tvSportTitle.text =
                match.sportTitle.uppercase()
            binding.tvHomeTeam.text = match.homeTeam
            binding.tvAwayTeam.text = match.awayTeam
            binding.tvMatchTime.text =
                formatTime(match.commenceTime)
            binding.tvHomeProb.text =
                "${match.homeWinProbability}%"
            binding.tvDrawProb.text =
                "${match.drawProbability}%"
            binding.tvAwayProb.text =
                "${match.awayWinProbability}%"
            binding.tvPrediction.text =
                match.homeTeam
                    .takeIf {
                        match.homeWinProbability >
                        match.awayWinProbability
                    } ?: match.awayTeam
            binding.tvConfidence.text = "🔥"
            binding.tvOverUnder.text =
                match.bestOverUnder
            binding.tvBttsYes.text =
                match.bttsYes?.let {
                    "%.2f".format(it)
                } ?: "N/A"
            binding.tvBttsNo.text =
                match.bttsNo?.let {
                    "%.2f".format(it)
                } ?: "N/A"
        }

        private fun formatTime(isoTime: String): String {
            return try {
                val input = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.getDefault()
                ).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val output = SimpleDateFormat(
                    "EEE dd MMM HH:mm",
                    Locale.getDefault()
                )
                "📅 ${output.format(input.parse(isoTime)!!)}"
            } catch (e: Exception) {
                isoTime
            }
        }
    }

    class MatchDiffCallback :
        DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(
            oldItem: Match,
            newItem: Match
        ) = oldItem.id == newItem.id
        override fun areContentsTheSame(
            oldItem: Match,
            newItem: Match
        ) = oldItem == newItem
    }
}
