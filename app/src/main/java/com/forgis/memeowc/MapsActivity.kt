package com.forgis.memeowc

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// import com.forgis.memeowc.databinding.ActivityMapsBinding

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    // private lateinit var binding: ActivityMapsBinding

    // Entry points to APIs
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // Defaults
    private var locationPermissionGranted = false
    private var defaultLocation = getDefaultLocation()
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding = ActivityMapsBinding.inflate(layoutInflater)
        // setContentView(binding.root)

        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Construct clients
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val button: Button = findViewById(R.id.save_wc)
        button.setOnClickListener {
            saveWCPosition()
        }
    }

    private fun saveWCPosition() {
        // Popup to save a description of the place.
        val description = EditText(this)
        val descriptionInput: AlertDialog.Builder = AlertDialog.Builder(this)

        descriptionInput.setTitle("Notes")
        descriptionInput.setView(description)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(description)

        descriptionInput.setView(layout)

        // Dialog Logic
        descriptionInput.setPositiveButton(R.string.save_note) {view, _ ->
            storeNewWC(description.text.toString())
            view.dismiss()
        }

        descriptionInput.setNegativeButton(R.string.cancel_note) {view, _ ->
            view.cancel()
        }

        descriptionInput.show()
    }

    private fun storeNewWC(description: String) {
        Log.i(TAG, "User position: (Lat, Long) = (${lastKnownLocation!!.latitude}, ${lastKnownLocation!!.longitude}) | Description: $description")
    }

    // Prompts user for device GPS location
    private fun getLocationPermission() {
        val FINE_LOCATION: Boolean = ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        // val COARSE_LOCATION: Boolean = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // if (FINE_LOCATION && COARSE_LOCATION) {
        if (FINE_LOCATION) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            Log.i(TAG, "User position: (Lat, Long) = (${lastKnownLocation!!.latitude}, ${lastKnownLocation!!.longitude})")
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null.")
                        Log.e(TAG, "Exception: %s", task.exception)

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDefaultLocation(): LatLng {
        // else statement is "es" -> Spain
        return when (Locale.getDefault().displayLanguage) {
            "fr" -> LatLng(46.2276, 2.2137)
            "en" -> LatLng(52.3555, 1.1743)
            "us" -> LatLng(37.0902, 95.7129)
            else -> LatLng(40.4637, 3.7492)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }

        updateLocationUI()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // First we get GPS permissions
        getLocationPermission()

        updateLocationUI()

        // Gets the current location and centers the map
        getDeviceLocation()
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        // private const val PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15
    }
}
