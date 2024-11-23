package com.algonetwork.routeoptimization.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.adapter.TripHistoryAdapter
import com.algonetwork.routeoptimization.data.TripHistory
import com.algonetwork.routeoptimization.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var tripHistoryAdapter: TripHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tripHistoryList = getTripHistory()
        tripHistoryAdapter = TripHistoryAdapter(tripHistoryList)

        binding.rvTripHistory.layoutManager = LinearLayoutManager(context)
        binding.rvTripHistory.adapter = tripHistoryAdapter
    }

    private fun getTripHistory(): ArrayList<TripHistory> {
        val dataDate = resources.getStringArray(R.array.data_date)
        val dataStatus = resources.getStringArray(R.array.data_status)
        val dataFrom = resources.getStringArray(R.array.data_from)
        val dataDestination = resources.getStringArray(R.array.data_destination)
        val dataVehicle = resources.obtainTypedArray(R.array.data_vehicle)

        val tripHistoryList = ArrayList<TripHistory>()
        for (i in dataDate.indices) {
            val trip = TripHistory(
                date = dataDate[i],
                status = dataStatus[i],
                from = dataFrom[i],
                destination = dataDestination[i],
                vehicle = dataVehicle.getResourceId(i, -1)
            )
            tripHistoryList.add(trip)
        }
        dataVehicle.recycle()
        return tripHistoryList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}