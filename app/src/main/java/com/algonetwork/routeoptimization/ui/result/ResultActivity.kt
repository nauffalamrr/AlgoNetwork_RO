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
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.algonetwork.routeoptimization.data.RoutePoint
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.BoundingBox

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var mapView: MapView
    private lateinit var adapter: ResultAdapter
    private lateinit var userMarker: Marker
    private lateinit var database: TripHistoryRoomDatabase
    private val locations = mutableListOf<Result>()
    private val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val markers = mutableListOf<Marker>()
    private val geoPoints = mutableListOf<GeoPoint>()
    private var routeDataList: List<RoutePoint> = emptyList()
    private var isFollowingUser = false
    private var lastUserGeoPoint: GeoPoint? = null
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        database = TripHistoryRoomDatabase.getDatabase(this)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = binding.mapView
        mapView.setMultiTouchControls(true)

        mapView.setMapListener(object : org.osmdroid.events.MapListener {
            override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                if (isFollowingUser) {
                    isFollowingUser = false
                }
                return true
            }

            override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                return true
            }
        })

        if (checkLocationPermission()) {
            setupUserLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(LOCATION_PERMISSION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        val firstLocation = intent.getStringExtra("firstLocation")
        val firstDestination = intent.getStringExtra("firstDestination")
        val otherDestinations = intent.getStringArrayListExtra("otherDestinations")
        val vehicleType = intent.getStringExtra("vehicleType") ?: "Car"
        routeDataList = intent.getParcelableArrayListExtra<RoutePoint>("routeDataList") ?: emptyList()
        val inputLocations = intent.getStringArrayListExtra("inputLocations") ?: arrayListOf()

        CoroutineScope(Dispatchers.Main).launch {
            addLocationData(routeDataList, inputLocations)
            setupRecyclerView()
        }

        setupRecyclerView()

        displayMarkersAndRoute(routeDataList)

        binding.btnGo.setOnClickListener {
            saveTripToDatabase(firstLocation, firstDestination, otherDestinations, vehicleType)
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

        binding.btnCenterOnUser.setOnClickListener {
            if (::userMarker.isInitialized && lastUserGeoPoint != null) {
                isFollowingUser = true
                mapView.controller.animateTo(lastUserGeoPoint)
            } else {
                Toast.makeText(this, "User location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    private fun navigateToHistoryFragment() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openFragment", "HistoryFragment")
        startActivity(intent)
        finish()
    }

    private fun saveTripToDatabase(from: String?, destination: String?, otherDestinations: ArrayList<String>?, vehicleType: String) {
        val date = DateHelper.getCurrentDate()

        val destination1 = destination
        val destination2 = otherDestinations?.getOrNull(0) ?: ""
        val destination3 = otherDestinations?.getOrNull(1) ?: ""

        val tripHistory = TripHistory(
            date = date,
            status = "Completed",
            from = from ?: "Unknown",
            destination1 = destination1,
            destination2 = destination2,
            destination3 = destination3,
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
        return when (vehicleType.lowercase()) {
            "motorcycle" -> R.drawable.ic_motorcycle
            else -> R.drawable.ic_car
        }
    }

    private suspend fun addLocationData(routeDataList: List<RoutePoint>, inputLocations: List<String>) {
        locations.clear()
        val filteredRouteDataList = routeDataList.drop(1)
        filteredRouteDataList.forEachIndexed { index, routePoint ->
            val locationName = getLocationNameUsingNominatim(routePoint.latitude, routePoint.longitude)
            val location = inputLocations.getOrNull(index) ?: "Unknown Location"
            locations.add(Result(index + 1, locationName ?: location))
        }
    }

    private fun setupRecyclerView() {
        adapter = ResultAdapter(locations.filter { it.destination.isNotBlank() })
        binding.rvResult.layoutManager = LinearLayoutManager(this)
        binding.rvResult.adapter = adapter
    }

    private fun displayMarkersAndRoute(locationDataList: List<RoutePoint>) {
        locationDataList.forEachIndexed { index, routeData ->
            val geoPoint = GeoPoint(routeData.latitude, routeData.longitude)
            geoPoints.add(geoPoint)
            if (index > 0) {
                val title = "Route $index"
                addMarker(geoPoint, title)
            }
        }

        if (geoPoints.isNotEmpty()) {
            drawRoute(geoPoints)
        }
    }

    private fun addMarker(geoPoint: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = title
        markers.add(marker)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun drawRoute(geoPoints: List<GeoPoint>) {
        val roadManager = OSRMRoadManager(this, "RouteOptimizationUserAgent")
        roadManager.setMean(OSRMRoadManager.MEAN_BY_CAR)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allRouteOverlays = mutableListOf<Polyline>()
                val routeGeoPoints = mutableListOf<GeoPoint>()

                for (i in 0 until geoPoints.size - 1) {
                    val segmentPoints = listOf(geoPoints[i], geoPoints[i + 1])

                    val road: Road = roadManager.getRoad(ArrayList(segmentPoints))

                    val segmentColor = when (i) {
                        0 -> resources.getColor(android.R.color.holo_blue_bright)
                        1 -> resources.getColor(android.R.color.holo_blue_dark)
                        else -> resources.getColor(android.R.color.holo_purple)
                    }

                    val segmentOverlay = Polyline().apply {
                        setPoints(road.mRouteHigh)
                        color = segmentColor
                    }

                    allRouteOverlays.add(segmentOverlay)
                    routeGeoPoints.addAll(segmentPoints)
                }

                withContext(Dispatchers.Main) {
                    allRouteOverlays.forEach {
                        mapView.overlays.add(it)
                    }

                    val bounds = BoundingBox.fromGeoPoints(routeGeoPoints)
                    mapView.zoomToBoundingBox(bounds, true)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    binding.errorMessage.text = getString(R.string.error_loading_route, e.localizedMessage)
                    binding.errorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                interval = 10000
                priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                        lastUserGeoPoint = userGeoPoint
                        updateUserMarker(userGeoPoint)

                        if (isFollowingUser) {
                            mapView.controller.animateTo(userGeoPoint)
                        }
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)

        } catch (e: SecurityException) {
            Toast.makeText(this, "Error accessing location: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserMarker(geoPoint: GeoPoint) {
        userMarker = Marker(mapView).apply {
            position = geoPoint
            icon = resources.getDrawable(R.drawable.ic_user_location)
            title = "Your Location"
        }
        mapView.overlays.add(userMarker)
        mapView.invalidate()
    }

    private fun updateUserMarker(geoPoint: GeoPoint) {
        if (::userMarker.isInitialized) {
            userMarker.position = geoPoint
            mapView.invalidate()
        } else {
            addUserMarker(geoPoint)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private suspend fun getLocationNameUsingNominatim(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string())
                    return@withContext jsonResponse.optString("display_name", "Unknown Location")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }
}
