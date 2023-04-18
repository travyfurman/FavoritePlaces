package com.example.favoriteplaces.models

import android.os.Parcel
import android.os.Parcelable

data class FavoritePlaceModel(
    val id: Int,
    val title: String?,
    val image: String?,
    val description: String?,
    val date: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FavoritePlaceModel> {
        override fun createFromParcel(parcel: Parcel): FavoritePlaceModel {
            return FavoritePlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<FavoritePlaceModel?> {
            return arrayOfNulls(size)
        }
    }
}