package com.algonetwork.routeoptimization.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.algonetwork.routeoptimization.data.TripHistory
import com.algonetwork.routeoptimization.databinding.ItemHistoryTripBinding

class TripHistoryAdapter(private val tripHistory: ArrayList<TripHistory>) :
    RecyclerView.Adapter<TripHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, status, from, destination, vehicle) = tripHistory[position]
        holder.binding.tvHistoryDate.text = date
        holder.binding.tvHistoryStatus.text = status
        holder.binding.tvHistoryFrom.text = from
        holder.binding.tvHistoryDestination.text = destination
        holder.binding.ivVehicle.setImageResource(vehicle)
    }

    override fun getItemCount() = tripHistory.size

    class ViewHolder(var binding: ItemHistoryTripBinding) : RecyclerView.ViewHolder(binding.root)
}