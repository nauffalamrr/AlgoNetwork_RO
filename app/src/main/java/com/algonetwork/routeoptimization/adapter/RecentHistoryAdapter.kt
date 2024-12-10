package com.algonetwork.routeoptimization.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.algonetwork.routeoptimization.database.TripHistory
import com.algonetwork.routeoptimization.databinding.ItemRecentHistoryBinding

class RecentHistoryAdapter(
    private var tripHistory: MutableList<TripHistory>) :
    RecyclerView.Adapter<RecentHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = tripHistory[position]
        holder.bind(historyItem)
    }

    override fun getItemCount() = tripHistory.size

    fun updateData(newData: List<TripHistory>) {
        tripHistory.clear()
        tripHistory.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var binding: ItemRecentHistoryBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(history: TripHistory) {
            with(binding) {
                tvHistoryDate.text = history.date
                tvHistoryStatus.text = history.status
                tvHistoryFrom.text = history.from
                tvHistoryDestination1.text = history.destination1
                if (history.destination2.isNullOrEmpty()) {
                    layoutDestination2.visibility = View.GONE
                } else {
                    tvHistoryDestination2.text = history.destination2
                }
                if (history.destination3.isNullOrEmpty()) {
                    layoutDestination3.visibility = View.GONE
                } else {
                    tvHistoryDestination3.text = history.destination3
                }
                ivVehicle.setImageResource(history.vehicle)
            }
        }
    }
}