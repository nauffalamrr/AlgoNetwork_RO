package com.algonetwork.routeoptimization.ui.selectlocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.adapter.LocationSuggestionAdapter
import com.algonetwork.routeoptimization.data.Location
import com.algonetwork.routeoptimization.data.LocationSuggestion
import com.algonetwork.routeoptimization.data.retrofit.NominatimApiConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import retrofit2.HttpException
import java.util.*

class SelectLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchView: SearchView
    private lateinit var geocoder: Geocoder
    private lateinit var locationManager: LocationManager
    private lateinit var selectLocationButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationSuggestionsAdapter: LocationSuggestionAdapter
    private val locationCache = mutableMapOf<String, List<LocationSuggestion>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        supportActionBar?.hide()

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = findViewById(R.id.map_view)
        searchView = findViewById(R.id.search_view)

        selectLocationButton = findViewById(R.id.select_location_button)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        geocoder = Geocoder(this, Locale.getDefault())

        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        recyclerView = findViewById(R.id.rv_suggestion)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationSuggestionsAdapter = LocationSuggestionAdapter { selectedSuggestion ->
            val geoPoint = GeoPoint(selectedSuggestion.latitude, selectedSuggestion.longitude)
            updateLocationOnMap(geoPoint, selectedSuggestion.displayName)
            recyclerView.visibility = View.GONE
        }
        recyclerView.adapter = locationSuggestionsAdapter

        val lastKnownLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }

        val initialPoint = if (lastKnownLocation != null) {
            GeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude)
        } else {
            GeoPoint(-8.109125, -247.077650)
        }

        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(initialPoint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val markerPresent = mapView.overlays.any { it is Marker }
                selectLocationButton.isEnabled = markerPresent || !newText.isNullOrEmpty()
                if (!newText.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val cachedSuggestions = locationCache[newText]
                        if (cachedSuggestions != null) {
                            locationSuggestionsAdapter.updateSuggestions(cachedSuggestions)
                            recyclerView.visibility = View.VISIBLE
                        } else {
                            try {
                                val nominatimApi = NominatimApiConfig.getNominatimApiService()
                                val suggestions = nominatimApi.searchLocations(newText)

                                locationCache[newText] = suggestions

                                locationSuggestionsAdapter.updateSuggestions(suggestions)
                                recyclerView.visibility = View.VISIBLE
                            } catch (e: HttpException) {
                                if (e.code() == 429) {
                                    Toast.makeText(this@SelectLocationActivity, "Too many requests, please wait", Toast.LENGTH_SHORT).show()
                                } else {
                                    e.printStackTrace()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    recyclerView.visibility = View.GONE
                }
                return false
            }
        })

        selectLocationButton.setOnClickListener {
            val marker = mapView.overlays.find { it is Marker } as? Marker
            if (marker != null) {
                val locationName = marker.title
                val geoPoint = marker.position
                val location = Location(locationName, geoPoint.latitude, geoPoint.longitude)

                val requestCode = intent.getIntExtra("requestCode", 0)
                val position = intent.getIntExtra("position", -1)

                val resultIntent = Intent().apply {
                    putExtra("location", location)
                    putExtra("requestCode", requestCode)
                    putExtra("position", position)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Select Location First!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<CardView>(R.id.button_current_location).setOnClickListener {
            getCurrentLocation()
        }

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(it.x.toInt(), it.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        val locationName = getLocationName(point) ?: "Unknown Location"
                        updateLocationOnMap(point, locationName)
                    }
                }
                return true
            }
        })
    }

    private fun updateLocationOnMap(geoPoint: GeoPoint, locationName: String) {
        mapView.overlays.clear()
        addMapClickListener()

        val marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = locationName
        }
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint)
    }

    private fun getLocationName(geoPoint: GeoPoint): String? {
        return try {
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addMapClickListener() {
        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(it.x.toInt(), it.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        val locationName = getLocationName(point) ?: "Unknown Location"
                        updateLocationOnMap(point, locationName)
                    }
                }
                return true
            }
        })
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    val locationName = getLocationName(geoPoint) ?: "Unknown Location"
                    updateLocationOnMap(geoPoint, locationName)
                    mapView.controller.animateTo(geoPoint)
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting current location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

