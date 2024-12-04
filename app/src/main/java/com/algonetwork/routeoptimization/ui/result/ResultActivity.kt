package com.algonetwork.routeoptimization.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.algonetwork.routeoptimization.MainActivity
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.adapter.ResultAdapter
import com.algonetwork.routeoptimization.data.Result
import com.algonetwork.routeoptimization.database.TripHistory
import com.algonetwork.routeoptimization.database.TripHistoryRoomDatabase
import com.algonetwork.routeoptimization.databinding.ActivityResultBinding
import com.algonetwork.routeoptimization.helper.DateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var mapView: MapView
    private lateinit var adapter: ResultAdapter
    private lateinit var database: TripHistoryRoomDatabase
    private val locations = mutableListOf<Result>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        database = TripHistoryRoomDatabase.getDatabase(this)

        // Load map configuration
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = binding.mapView
        mapView.setMultiTouchControls(true)

        // Get data from DestinationActivity
        val firstLocation = intent.getStringExtra("firstLocation")
        val firstDestination = intent.getStringExtra("firstDestination")
        val otherDestinations = intent.getStringArrayListExtra("otherDestinations")
        val vehicleType = intent.getStringExtra("vehicleType") ?: "Car"

        // Populate RecyclerView data
        addLocationData(firstLocation, firstDestination, otherDestinations)

        // Initialize RecyclerView
        setupRecyclerView()

        // Display markers and route
        displayMarkersAndRoute()

        binding.btnGo.setOnClickListener {
            saveTripToDatabase(firstLocation, firstDestination, vehicleType)
            navigateToHistoryFragment()
        }

        binding.btnClose.setOnClickListener {
            binding.popupCard.visibility = View.GONE
            binding.btnShowPopup.visibility = View.VISIBLE
        }

        binding.btnShowPopup.setOnClickListener {
            binding.popupCard.visibility = View.VISIBLE
            binding.btnShowPopup.visibility = View.GONE
        }
    }

    private fun navigateToHistoryFragment() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openFragment", "HistoryFragment")
        startActivity(intent)
        finish()
    }

    private fun saveTripToDatabase(from: String?, destination: String?, vehicleType: String) {
        val date = DateHelper.getCurrentDate()

        val lastDestination = if (locations.isNotEmpty()) {
            locations.last().destination
        } else {
            "Unknown"
        }
        val tripHistory = TripHistory(
            date = date,
            status = "Completed",
            from = from ?: "Unknown",
            destination = lastDestination,
            vehicle = getVehicleIcon(vehicleType)
        )

        CoroutineScope(Dispatchers.IO).launch {
            database.tripHistoryDao().insert(tripHistory)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ResultActivity, "Trip saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getVehicleIcon(vehicleType: String): Int {
        return when (vehicleType.lowercase(Locale.getDefault())) {
            "motorcycle" -> R.drawable.ic_motorcycle
            else -> R.drawable.ic_car
        }
    }

    private fun addLocationData(firstLocation: String?, firstDestination: String?, otherDestinations: ArrayList<String>?) {
        if (!firstLocation.isNullOrEmpty()) {
            val geoPoint = getGeoPointFromAddress(firstLocation)
            if (geoPoint != null) {
                addMarker(geoPoint, firstLocation)
            }
        }

        if (!firstDestination.isNullOrEmpty()) {
            locations.add(Result(1, firstDestination))
        }

        otherDestinations?.take(2)?.forEachIndexed { index, destination ->
            locations.add(Result(index + 2, destination))
        }

        otherDestinations?.drop(2)?.forEachIndexed { index, destination ->
            locations.add(Result(index + 4, destination))
        }
    }

    private fun setupRecyclerView() {
        adapter = ResultAdapter(locations)
        binding.rvResult.layoutManager = LinearLayoutManager(this)
        binding.rvResult.adapter = adapter
    }

    private fun getGeoPointFromAddress(address: String?): GeoPoint? {
        val geocoder = android.location.Geocoder(this, Locale.getDefault())
        return try {
            val addressList = geocoder.getFromLocationName(address ?: "", 1)
            if (!addressList.isNullOrEmpty()) {
                val latLng = addressList[0]
                GeoPoint(latLng.latitude, latLng.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayMarkersAndRoute() {
        val geoPoints = mutableListOf<GeoPoint>()

        val firstLocation = intent.getStringExtra("firstLocation")
        val geoPoint = getGeoPointFromAddress(firstLocation)
        if (geoPoint != null) {
            geoPoints.add(geoPoint)
        }

        locations.forEach { result ->
            try {
                val address = getGeoPointFromAddress(result.destination)
                if (address != null) {
                    geoPoints.add(address)
                    addMarker(address, result.destination)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (geoPoints.size > 1) {
            drawRoute(ArrayList(geoPoints))
        }
    }

    private fun addMarker(geoPoint: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = title
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint) // Center the map on the marker
    }

    private fun drawRoute(geoPoints: ArrayList<GeoPoint>) {
        val roadManager = OSRMRoadManager(this, "YOUR_USER_AGENT")
        roadManager.setMean(OSRMRoadManager.MEAN_BY_CAR)

        // Launch a coroutine for network operation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Perform the network request
                val road: Road = roadManager.getRoad(geoPoints)

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    val roadOverlay = Polyline()
                    roadOverlay.setPoints(road.mRouteHigh)
                    roadOverlay.color = resources.getColor(android.R.color.holo_blue_dark)
                    mapView.overlays.add(roadOverlay)

                    // Zoom to fit the entire route
                    mapView.zoomToBoundingBox(roadOverlay.bounds, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Optionally, show an error message to the user
                    binding.errorMessage.text = getString(R.string.error_loading_route, e.localizedMessage)
                    binding.errorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
