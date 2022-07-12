package com.example.jello.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import com.example.jello.R
import com.example.jello.admin.fragments.AddProductFragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.jello.databinding.ActivityMapAdminBinding
import com.google.android.gms.location.*

class MapAdminActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapAdminBinding
    private var latitudeUser = 0.0
    private var longitudeUser = 0.0
    private var latitudeProduct = 0.0
    private var longitudeProduct = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /// user location :
        latitudeUser = intent.getDoubleExtra("latitude",0.0)
        longitudeUser = intent.getDoubleExtra("longitude",0.0)

        binding.btnSaveLocation.setOnClickListener {
            if (latitudeProduct == 0.0 || longitudeProduct == 0.0){
                latitudeProduct = latitudeUser
                longitudeProduct = longitudeUser
            }
            AddProductFragment.setProductLocation(latitudeProduct,longitudeProduct)
            super.onBackPressed()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.addMarker(MarkerOptions().position(LatLng(latitudeUser,longitudeUser)).title("Your location"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitudeUser,longitudeUser),15f))

        mMap.setOnMapClickListener { latlng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latlng).title("Product here"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
            latitudeProduct = latlng.latitude
            longitudeProduct = latlng.longitude
        }
    }

}