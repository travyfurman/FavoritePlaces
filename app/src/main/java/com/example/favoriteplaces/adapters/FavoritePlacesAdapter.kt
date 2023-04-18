package com.example.favoriteplaces.adapters


import android.app.Activity
import android.content.*
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.favoriteplaces.activities.AddFavoritePlaceActivity
import com.example.favoriteplaces.activities.MainActivity
import com.example.favoriteplaces.database.DatabaseHandler
import com.example.favoriteplaces.databinding.ItemFavoritePlaceBinding
import com.example.favoriteplaces.models.FavoritePlaceModel



class FavoritePlacesAdapter(private var list: ArrayList<FavoritePlaceModel>) :
    RecyclerView.Adapter<FavoritePlacesAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

            inner class ViewHolder(binding: ItemFavoritePlaceBinding) :
                    RecyclerView.ViewHolder(binding.root) {
                        val tvPlaceName = binding.tvPlaceName
                        val tvDescription = binding.tvDescription
                        val ivPlaceImage = binding.ivPlaceImage
                    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFavoritePlaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: FavoritePlaceModel = list[position]
        holder.tvPlaceName.text = model.title
        holder.tvDescription.text = model.description
        holder.ivPlaceImage.setImageURI(Uri.parse(model.image))
        holder.itemView.setOnClickListener{
            if (onClickListener != null){
                onClickListener!!.onClick(position, model)
            }
        }
    }

    fun removeAt(context: Context, position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteFavoritePlace(list[position])
        if (isDeleted > 0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun notifyEditItem(context: Context, activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddFavoritePlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: FavoritePlaceModel)
    }

}