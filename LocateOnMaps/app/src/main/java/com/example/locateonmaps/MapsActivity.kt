package com.example.locateonmaps

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.locateonmaps.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var currentLocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode=101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        getCLocation()
    }

    private fun getCLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }
        val getLocation = fusedLocationProviderClient.lastLocation
        getLocation.addOnSuccessListener {
            if (it != null) {
//                val textLatitude = "Latitude:"+it.latitude.toString()
//                val textLong = "Longitude:"+it.longitude.toString()
//                Lat.text=textLatitude
//                long.text=textLong
                currentLocation = it

                Toast.makeText(applicationContext, currentLocation.longitude.toString()+""+
                currentLocation.latitude.toString(), Toast.LENGTH_LONG).show()

                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                getCLocation()
    }
}

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        val LatLng = LatLng(currentLocation.latitude,currentLocation.longitude)
        val MarkerOp = MarkerOptions().position(LatLng).title("Current Location")

        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(LatLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng, 12f))
        googleMap?.addMarker(MarkerOp)
    }


}