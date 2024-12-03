package com.algonetwork.routeoptimization.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.algonetwork.routeoptimization.data.Result
import com.algonetwork.routeoptimization.databinding.ItemResultBinding

class ResultAdapter(
    private val results: List<Result>
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(val binding: ItemResultBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.binding.tvDestinationNumber.text = "Rute ${result.number}"
        holder.binding.tvDestination.text = result.destination
    }

    override fun getItemCount() = results.size
}
