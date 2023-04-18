package com.example.favoriteplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.favoriteplaces.R
import com.example.favoriteplaces.databinding.ActivityMapBinding
import com.example.favoriteplaces.models.FavoritePlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mFavoritePlaceDetails : FavoritePlaceModel? = null
    var binding: ActivityMapBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mFavoritePlaceDetails =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        if (mFavoritePlaceDetails != null) {
            setSupportActionBar(binding?.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mFavoritePlaceDetails!!.title
            binding?.toolbarMap?.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)


        }
    }

    override fun onMapReady(p0: GoogleMap) {
        val position = LatLng(mFavoritePlaceDetails!!.latitude, mFavoritePlaceDetails!!.longitude)
        p0.addMarker(MarkerOptions().position(position).title(mFavoritePlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 8f)
        p0.animateCamera(newLatLngZoom)

    }


}