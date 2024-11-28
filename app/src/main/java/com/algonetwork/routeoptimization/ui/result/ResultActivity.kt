package com.algonetwork.routeoptimization.ui.result

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.algonetwork.routeoptimization.databinding.ActivityResultBinding
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = binding.mapView

        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(15.0)

        val startPoint = GeoPoint(-6.200000, 106.816666) // Contoh koordinat Jakarta
        mapController.setCenter(startPoint)

        binding.btnGo.setOnClickListener {
            binding.popupCard.visibility = View.GONE
        }

        binding.btnClose.setOnClickListener {
            binding.popupCard.visibility = View.GONE
        }

        mapView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == android.view.MotionEvent.ACTION_UP) {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(
                    motionEvent.x.toInt(),
                    motionEvent.y.toInt()
                ) as GeoPoint

                val placeName = getPlaceName(geoPoint.latitude, geoPoint.longitude)

                addMarker(geoPoint, placeName)

                mapView.performClick()
            }
            false
        }
    }

    private fun getPlaceName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                return addresses[0].getAddressLine(0) ?: "Tidak diketahui"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Nama Tempat Tidak Ditemukan"
    }

    private fun addMarker(geoPoint: GeoPoint, placeName: String) {
        val marker = org.osmdroid.views.overlay.Marker(mapView)
        marker.position = geoPoint
        marker.title = placeName
        mapView.overlays.add(marker)
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