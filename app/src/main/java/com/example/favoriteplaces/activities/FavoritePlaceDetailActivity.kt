package com.example.favoriteplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.favoriteplaces.databinding.ActivityFavoritePlaceDetailBinding
import com.example.favoriteplaces.models.FavoritePlaceModel


class FavoritePlaceDetailActivity : AppCompatActivity() {
    private var binding: ActivityFavoritePlaceDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritePlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var favoritePlaceDetailModel: FavoritePlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            favoritePlaceDetailModel =
                intent.getParcelableExtra(
                    MainActivity.EXTRA_PLACE_DETAILS)
        }

        if (favoritePlaceDetailModel != null){
            setSupportActionBar(binding?.toolbarFavoritePlaceDetails)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = favoritePlaceDetailModel.title
            binding?.toolbarFavoritePlaceDetails?.setNavigationOnClickListener{
                onBackPressed()
            }

            binding?.ivPlaceImage?.setImageURI(Uri.parse(favoritePlaceDetailModel.image))
            binding?.tvDescription?.text = favoritePlaceDetailModel.description
            binding?.tvLocation?.text = favoritePlaceDetailModel.location

            binding?.btnViewOnMap?.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, favoritePlaceDetailModel)
                startActivity(intent)
            }

        }

    }
}