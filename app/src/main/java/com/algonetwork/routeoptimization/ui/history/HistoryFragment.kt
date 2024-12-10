package com.algonetwork.routeoptimization.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.adapter.TripHistoryAdapter
import com.algonetwork.routeoptimization.database.TripHistory
import com.algonetwork.routeoptimization.database.TripHistoryRoomDatabase
import com.algonetwork.routeoptimization.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var tripHistoryAdapter: TripHistoryAdapter
    private lateinit var database: TripHistoryRoomDatabase

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
        database = TripHistoryRoomDatabase.getDatabase(requireContext())

        tripHistoryAdapter = TripHistoryAdapter(arrayListOf()) { tripHistory ->
            deleteHistory(tripHistory)
        }

        binding.rvTripHistory.layoutManager = LinearLayoutManager(context)
        binding.rvTripHistory.adapter = tripHistoryAdapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            database.tripHistoryDao().getAll().collect { tripHistories ->
                tripHistoryAdapter.updateData(ArrayList(tripHistories))
            }
        }
    }

    private fun deleteHistory(tripHistory: TripHistory) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.tripHistoryDao().delete(tripHistory)
            withContext(Dispatchers.Main) {
                loadHistory()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}