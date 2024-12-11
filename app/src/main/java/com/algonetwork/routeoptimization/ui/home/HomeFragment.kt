package com.algonetwork.routeoptimization.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.adapter.RecentHistoryAdapter
import com.algonetwork.routeoptimization.database.TripHistoryRoomDatabase
import com.algonetwork.routeoptimization.databinding.FragmentHomeBinding
import com.algonetwork.routeoptimization.ui.destination.DestinationActivity
import com.algonetwork.routeoptimization.ui.result.ResultActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recentHistoryAdapter: RecentHistoryAdapter
    private lateinit var database: TripHistoryRoomDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = TripHistoryRoomDatabase.getDatabase(requireContext())

        recentHistoryAdapter = RecentHistoryAdapter(mutableListOf())
        binding.rvRecentHistory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentHistory.adapter = recentHistoryAdapter

        loadHistory()

        binding.optionMotorcycle.setOnClickListener{
            navigateToDestinationActivity("motorcycle")
        }

        binding.optionCar.setOnClickListener{
            navigateToDestinationActivity("car")
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            database.tripHistoryDao().getAll().collect { tripHistories ->
                val limitedTripHistories = tripHistories.take(1)
                recentHistoryAdapter.updateData(ArrayList(limitedTripHistories))
            }
        }
    }

    private fun navigateToDestinationActivity(vehicleType: String) {
        val intent = Intent(requireContext(), DestinationActivity::class.java)
        intent.putExtra("vehicleType", vehicleType)
        startActivity(intent)
    }
}