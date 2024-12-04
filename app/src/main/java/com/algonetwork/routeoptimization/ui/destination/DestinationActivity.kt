package com.algonetwork.routeoptimization.ui.destination

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.databinding.ActivityDestinationBinding
import com.algonetwork.routeoptimization.adapter.Destination
import com.algonetwork.routeoptimization.adapter.DestinationAdapter
import com.algonetwork.routeoptimization.data.Location
import com.algonetwork.routeoptimization.ui.result.ResultActivity
import com.algonetwork.routeoptimization.ui.selectlocation.SelectLocationActivity

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

        binding.firstLocation.setOnClickListener{
            openSelectLocationActivity(REQUEST_CODE_FIRST_LOCATION)
        }

        binding.firstDestination.setOnClickListener {
            openSelectLocationActivity(REQUEST_CODE_FIRST_DESTINATION)
        }

        val vehicleType = intent.getStringExtra("vehicleType")

        if (vehicleType == "motorcycle") {
            binding.optionVehicle.setImageResource(R.drawable.ic_motorcycle)
            binding.textVehicleOption.text = getString(R.string.vehicle_motorcycle)
        } else {
            binding.optionVehicle.setImageResource(R.drawable.ic_car)
            binding.textVehicleOption.text = getString(R.string.vehicle_car)
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.buttonOptimize.setOnClickListener {
            navigateToResultActivity()
        }
    }

    private fun navigateToResultActivity() {
        val moveIntent = Intent(this@DestinationActivity, ResultActivity::class.java)
        moveIntent.putExtra("firstLocation", binding.tvLocationDetail.text.toString())
        moveIntent.putExtra("firstDestination", binding.tvDestinationDetail.text.toString())

        val destinationDetails = destinationList.map { it.detail }
        moveIntent.putStringArrayListExtra("otherDestinations", ArrayList(destinationDetails))

        val vehicleType = intent.getStringExtra("vehicleType")
        moveIntent.putExtra("vehicleType", vehicleType)

        startActivity(moveIntent)
    }

    private fun setupRecyclerView() {
        adapter = DestinationAdapter(destinationList) { position ->
            openSelectLocationActivity(REQUEST_CODE_NEXT_DESTINATION, position)
        }
        binding.rvAddDestination.layoutManager = LinearLayoutManager(this)
        binding.rvAddDestination.adapter = adapter
    }

    private fun addNewDestination() {
        val maxDestinations = 2
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

    private fun openSelectLocationActivity(requestCode: Int, position: Int? = null) {
        val intent = Intent(this, SelectLocationActivity::class.java)
        intent.putExtra("requestCode", requestCode)
        position?.let { intent.putExtra("position", it) }
        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val location = data?.getParcelableExtra<Location>("location")
            val reqCode = data?.getIntExtra("requestCode", 0) ?: 0

            location?.let {
                when (reqCode) {
                    REQUEST_CODE_FIRST_LOCATION -> {
                        binding.tvLocationDetail.text = it.name
                    }
                    REQUEST_CODE_FIRST_DESTINATION -> {
                        binding.tvDestinationDetail.text = it.name
                    }
                    REQUEST_CODE_NEXT_DESTINATION -> {
                        val position = data?.getIntExtra("position", -1) ?: -1
                        if (position != -1) {
                            destinationList[position].detail = it.name
                            adapter.notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val SELECT_LOCATION_REQUEST_CODE = 1001
        private const val REQUEST_CODE_FIRST_LOCATION = 1
        private const val REQUEST_CODE_FIRST_DESTINATION = 2
        private const val REQUEST_CODE_NEXT_DESTINATION = 3
    }
}