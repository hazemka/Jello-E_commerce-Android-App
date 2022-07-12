package com.example.jello.user

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jello.R
import com.example.jello.databinding.ActivityMapUserBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapUserActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapUserBinding
    private var productLatitude = 0.0
    private var  productLongitude = 0.0
    private var userLatitude = 0.0
    private var userLongitude = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        productLatitude = intent.getDoubleExtra("productLatitude",0.0)
        productLongitude = intent.getDoubleExtra("productLongitude",0.0)
        userLatitude = intent.getDoubleExtra("userLatitude",0.0)
        userLongitude = intent.getDoubleExtra("userLongitude",0.0)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val productLocation = LatLng(productLatitude,productLongitude)
        val userLocation = LatLng(userLatitude,userLongitude)
        mMap.addMarker(MarkerOptions().position(productLocation).title("Product here"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(productLocation,15f))

        mMap.addMarker(MarkerOptions().position(userLocation).title("You here"))

        mMap.addPolyline(PolylineOptions()
            .add(productLocation)
            .add(userLocation)
            .color(Color.BLUE)
        )
    }
}