package com.algonetwork.routeoptimization.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.databinding.ItemAddDestinationBinding

data class Destination(val title: String, val detail: String)

class DestinationAdapter(
    private val destinations: MutableList<Destination>
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    inner class DestinationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemAddDestinationBinding.bind(view)
        fun bind(destination: Destination) {
            binding.tvDestination.text = destination.title
            binding.tvDestinationDetail.text = destination.detail
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount(): Int = destinations.size

    fun addDestination(destination: Destination) {
        destinations.add(destination)
        notifyItemInserted(destinations.size - 1)
    }
}
