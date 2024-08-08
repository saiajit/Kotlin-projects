package com.example.locationapp

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class GeoLocaActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var long : TextView
    private lateinit var Lat : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_loca)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Lat = findViewById(R.id.latitude)
        long = findViewById(R.id.longitude)
        val btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener{
            getLocation()
        }
    }

    private fun getLocation() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100)
            return
        }
        val location = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener{
            if(it!=null){
                val textLatitude = "Latitude:"+it.latitude.toString()
                val textLong = "Longitude:"+it.longitude.toString()
                Lat.text=textLatitude
                long.text=textLong
            }
        }
    }
}