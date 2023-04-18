package com.example.favoriteplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.favoriteplaces.adapters.FavoritePlacesAdapter
import com.example.favoriteplaces.database.DatabaseHandler
import com.example.favoriteplaces.databinding.ActivityMainBinding
import com.example.favoriteplaces.models.FavoritePlaceModel
import com.example.favoriteplaces.utils.SwipeToDeleteCallback
import com.example.favoriteplaces.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddFavorite?.setOnClickListener{
            val intent = Intent(this, AddFavoritePlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getFavoritePlacesFromLocalDB()

    }

    private fun setUpFavoritePlacesRecyclerView(favoritePlaceList: ArrayList<FavoritePlaceModel>){
        binding?.rvPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.rvPlacesList?.setHasFixedSize(true)
        val placesAdapter = FavoritePlacesAdapter(favoritePlaceList)
        binding?.rvPlacesList?.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : FavoritePlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: FavoritePlaceModel) {
                val intent = Intent(this@MainActivity, FavoritePlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvPlacesList?.adapter as FavoritePlacesAdapter
                adapter.notifyEditItem(this@MainActivity, this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvPlacesList?.adapter as FavoritePlacesAdapter
                adapter.removeAt(this@MainActivity, viewHolder.adapterPosition)
                getFavoritePlacesFromLocalDB()
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvPlacesList)

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvPlacesList)

    }


    private fun getFavoritePlacesFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getFavoritePlaceList: ArrayList<FavoritePlaceModel> = dbHandler.getFavoritePlacesList()

        if(getFavoritePlaceList.size > 0){
            binding?.rvPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setUpFavoritePlacesRecyclerView(getFavoritePlaceList)
            }else{
            binding?.rvPlacesList?.visibility = View.INVISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getFavoritePlacesFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }

    companion object{
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}