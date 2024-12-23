package com.algonetwork.routeoptimization.ui.destination

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.databinding.ActivityDestinationBinding
import com.algonetwork.routeoptimization.adapter.Destination
import com.algonetwork.routeoptimization.adapter.DestinationAdapter
import com.algonetwork.routeoptimization.data.AddLocationsResponse
import com.algonetwork.routeoptimization.data.GetLocationsResponse
import com.algonetwork.routeoptimization.data.Location
import com.algonetwork.routeoptimization.data.LocationCoordinates
import com.algonetwork.routeoptimization.data.LocationsRequest
import com.algonetwork.routeoptimization.data.RoutePoint
import com.algonetwork.routeoptimization.data.retrofit.ApiConfig
import com.algonetwork.routeoptimization.ui.result.ResultActivity
import com.algonetwork.routeoptimization.ui.selectlocation.SelectLocationActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDestinationBinding
    private lateinit var adapter: DestinationAdapter
    private val destinationList = mutableListOf<Destination>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

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
            if (!isGpsEnabled()) {
                promptEnableGps()
            } else {
                navigateToResultActivity()
            }
        }

        isLoading.observe(this) { loading ->
            showLoading(loading)
        }
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
                detail = null
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
                        binding.tvLocationDetail.tag = it
                    }
                    REQUEST_CODE_FIRST_DESTINATION -> {
                        binding.tvDestinationDetail.text = it.name
                        binding.tvDestinationDetail.tag = it
                    }
                    REQUEST_CODE_NEXT_DESTINATION -> {
                        val position = data?.getIntExtra("position", -1) ?: -1
                        if (position != -1) {
                            destinationList[position].detail = it
                            adapter.notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun promptEnableGps() {
        AlertDialog.Builder(this)
            .setTitle("Enable GPS")
            .setMessage("GPS is required to optimize the route. Please enable GPS and try again.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToResultActivity() {
        val firstLocation = binding.tvLocationDetail.text.toString()
        val firstDestination = binding.tvDestinationDetail.text.toString()
        val otherDestinations = destinationList.map { it.detail?.name }
        val vehicleType = intent.getStringExtra("vehicleType") ?: "car"

        val firstLocationObj = binding.tvLocationDetail.tag as? Location
        val firstDestinationObj = binding.tvDestinationDetail.tag as? Location
        val otherDestinationsObj = destinationList.mapNotNull { it.detail }

        val otherDestinationsCoordinates = otherDestinationsObj.map {
            RoutePoint(it.latitude, it.longitude)
        }

        if (firstLocationObj == null || firstDestinationObj == null) {
            Toast.makeText(
                this,
                "Please select both first location and destination.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val incompleteDestination = destinationList.find { it.detail == null }
        if (incompleteDestination != null) {
            Toast.makeText(
                this,
                "Please complete all destination details.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val locations = mutableListOf(
            LocationCoordinates(firstLocationObj.latitude, firstLocationObj.longitude),
            LocationCoordinates(firstDestinationObj.latitude, firstDestinationObj.longitude)
        )
        locations.addAll(otherDestinationsCoordinates.map {
            LocationCoordinates(it.latitude, it.longitude)
        })

        val apiService = ApiConfig.getApiService()
        val locationsRequest = LocationsRequest(locations)

        _isLoading.value = true
        apiService.addLocations(locationsRequest).enqueue(object : Callback<AddLocationsResponse> {
            override fun onResponse(
                call: Call<AddLocationsResponse>,
                response: Response<AddLocationsResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(
                        this@DestinationActivity,
                        "Optimizing Route",
                        Toast.LENGTH_SHORT
                    ).show()

                    _isLoading.value = true
                    apiService.getLocations().enqueue(object : Callback<GetLocationsResponse> {
                        override fun onResponse(
                            call: Call<GetLocationsResponse>,
                            response: Response<GetLocationsResponse>
                        ) {
                            _isLoading.value = false
                            if (response.isSuccessful && response.body()?.status == "success") {
                                val routeDataList = response.body()?.routes ?: emptyList()

                                val moveIntent = Intent(this@DestinationActivity, ResultActivity::class.java)
                                moveIntent.putExtra("firstLocation", firstLocation)
                                moveIntent.putExtra("firstDestination", firstDestination)
                                moveIntent.putStringArrayListExtra("otherDestinations", ArrayList(otherDestinations))
                                moveIntent.putParcelableArrayListExtra("routeDataList", ArrayList(routeDataList.flatten()))
                                moveIntent.putExtra("vehicleType", vehicleType)

                                startActivity(moveIntent)
                            } else {
                                Toast.makeText(
                                    this@DestinationActivity,
                                    "Error fetch locations: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<GetLocationsResponse>, t: Throwable) {
                            _isLoading.value = false
                            Toast.makeText(
                                this@DestinationActivity,
                                "Error fetching locations: ${t.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(
                        this@DestinationActivity,
                        "Error uploading locations: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AddLocationsResponse>, t: Throwable) {
                _isLoading.value = false
                Toast.makeText(
                    this@DestinationActivity,
                    "Error uploading locations: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        private const val SELECT_LOCATION_REQUEST_CODE = 1001
        private const val REQUEST_CODE_FIRST_LOCATION = 1
        private const val REQUEST_CODE_FIRST_DESTINATION = 2
        private const val REQUEST_CODE_NEXT_DESTINATION = 3
    }
}