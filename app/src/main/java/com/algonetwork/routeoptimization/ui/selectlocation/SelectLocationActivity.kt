package com.algonetwork.routeoptimization.ui.selectlocation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.algonetwork.routeoptimization.R
import com.algonetwork.routeoptimization.data.Location
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.*

class SelectLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchView: SearchView
    private lateinit var geocoder: Geocoder
    private lateinit var locationManager: LocationManager
    private lateinit var selectLocationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        supportActionBar?.hide()

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = findViewById(R.id.map_view)
        searchView = findViewById(R.id.search_view)

        selectLocationButton = findViewById(R.id.select_location_button)
        selectLocationButton.isEnabled = false

        geocoder = Geocoder(this, Locale.getDefault())

        mapView.setMultiTouchControls(true)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
                if (!query.isNullOrEmpty()) {
                    searchLocation(query)
                    selectLocationButton.isEnabled = true
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                selectLocationButton.isEnabled = !newText.isNullOrEmpty()
                return false
            }
        })

        mapView.addOnFirstLayoutListener { _, _, _, _, _ ->
            mapView.mapCenter.also {
                if (it.latitude != -7.797068 && it.longitude != 110.370529) {
                    selectLocationButton.isEnabled = true
                }
            }
        }

        selectLocationButton.setOnClickListener {
            if (searchView.query.isNullOrEmpty()) {
                Toast.makeText(this, "Pilih Titik Terlebih Dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                val locationName = searchView.query.toString()
                val geoPoint = mapView.mapCenter as GeoPoint
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
            }
        }

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(it.x.toInt(), it.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        updateLocationOnMap(point)
                        updateSearchViewWithLocationName(point)
                    }
                }
                return true
            }
        })
    }

    private fun searchLocation(query: String) {
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val geoPoint = GeoPoint(address.latitude, address.longitude)
                updateLocationOnMap(geoPoint)
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationOnMap(geoPoint: GeoPoint) {
        mapView.overlays.clear()
        addMapClickListener()

        val marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
        mapView.controller.animateTo(geoPoint)
    }

    private fun addMapClickListener() {
        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(it.x.toInt(), it.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        updateLocationOnMap(point)
                        updateSearchViewWithLocationName(point)
                    }
                }
                return true
            }
        })
    }

    private fun updateSearchViewWithLocationName(geoPoint: GeoPoint) {
        try {
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                searchView.setQuery(address.getAddressLine(0), false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
