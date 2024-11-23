package com.algonetwork.routeoptimization.ui.destination

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.databinding.ActivityDestinationBinding
import com.algonetwork.routeoptimization.ui.Destination
import com.algonetwork.routeoptimization.ui.DestinationAdapter

class DestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDestinationBinding
    private lateinit var adapter: DestinationAdapter
    private val destinationList = mutableListOf<Destination>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.cardRemoveDestination.visibility = View.GONE
        binding.textRemoveDestination.visibility = View.GONE

        setupRecyclerView()

        binding.buttonAddDestination.setOnClickListener {
            addNewDestination()
        }

        binding.buttonRemoveDestination.setOnClickListener {
            removeLastDestination()
        }

        val vehicleType = intent.getStringExtra("vehicleType")

        if (vehicleType == "motorcycle") {
            binding.optionVehicle.setImageResource(R.drawable.ic_motorcycle)
            binding.textVehicleOption.text = "Motorcycle"
        } else {
            binding.optionVehicle.setImageResource(R.drawable.ic_car)
            binding.textVehicleOption.text = "Car"
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = DestinationAdapter(destinationList)
        binding.rvAddDestination.layoutManager = LinearLayoutManager(this)
        binding.rvAddDestination.adapter = adapter
    }

    private fun addNewDestination() {
        val maxDestinations = 9
        if (destinationList.size < maxDestinations) {
            val newDestination = Destination(
                title = "Your Destination",
                detail = "Detail of destination"
            )
            destinationList.size + 1
            adapter.addDestination(newDestination)

            if (destinationList.size == maxDestinations) {
                binding.cardAddDestination.visibility = View.GONE
                binding.textAddDestination.visibility = View.GONE
            }

            if (destinationList.size == 1) {
                binding.cardRemoveDestination.visibility = View.VISIBLE
                binding.textRemoveDestination.visibility = View.VISIBLE
            }
        }
    }


    private fun removeLastDestination() {
        if (destinationList.isNotEmpty()) {
            // Hapus item terakhir
            destinationList.removeAt(destinationList.size - 1)
            adapter.notifyItemRemoved(destinationList.size)

            if (destinationList.size < 9) {
                binding.cardAddDestination.visibility = View.VISIBLE
                binding.textAddDestination.visibility = View.VISIBLE
            }

            if (destinationList.isEmpty()) {
                binding.cardRemoveDestination.visibility = View.GONE
                binding.textRemoveDestination.visibility = View.GONE
            }
        }
    }
}