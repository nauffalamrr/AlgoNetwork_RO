package com.algonetwork.routeoptimization.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.databinding.FragmentHomeBinding
import com.algonetwork.routeoptimization.ui.destination.DestinationActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.optionMotorcycle.setOnClickListener{
            navigateToDestinationActivity("motorcycle")
        }

        binding.optionCar.setOnClickListener{
            navigateToDestinationActivity("car")
        }
    }

    private fun navigateToDestinationActivity(vehicleType: String) {
        val intent = Intent(requireContext(), DestinationActivity::class.java)
        intent.putExtra("vehicleType", vehicleType)
        startActivity(intent)
    }
}