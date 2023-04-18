package com.example.favoriteplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.favoriteplaces.models.FavoritePlaceModel

class DatabaseHandler(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

            companion object {
                private const val DATABASE_VERSION = 1
                private const val DATABASE_NAME = "FavoritePlacesDatabase"
                private const val TABLE_FAVORITE_PLACE = "FavoritePlacesTable"
                private const val KEY_ID = "_id"
                private const val KEY_TITLE = "title"
                private const val KEY_IMAGE = "image"
                private const val KEY_DESCRIPTION = "description"
                private const val KEY_DATE = "date"
                private const val KEY_LOCATION = "location"
                private const val KEY_LATITUDE = "latitude"
                private const val KEY_LONGITUDE = "longitude"
            }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_FAVORITE_PLACE_TABLE = ("CREATE TABLE " + TABLE_FAVORITE_PLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_FAVORITE_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITE_PLACE")
        onCreate(db)
    }

    fun addFavoritePlace(favoritePlace: FavoritePlaceModel): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, favoritePlace.title)
        contentValues.put(KEY_IMAGE, favoritePlace.image)
        contentValues.put(KEY_DESCRIPTION, favoritePlace.description)
        contentValues.put(KEY_DATE, favoritePlace.date)
        contentValues.put(KEY_LOCATION, favoritePlace.location)
        contentValues.put(KEY_LATITUDE, favoritePlace.latitude)
        contentValues.put(KEY_LONGITUDE, favoritePlace.longitude)

        val result = db.insert(TABLE_FAVORITE_PLACE, null, contentValues)

        db.close()
        return result

    }

    fun deleteFavoritePlace(favoritePlace: FavoritePlaceModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_FAVORITE_PLACE,
            KEY_ID + "=" + favoritePlace.id, null)
        db.close()
        return success
    }

    fun getFavoritePlacesList(): ArrayList<FavoritePlaceModel>{
        val favoritePlaceList= ArrayList<FavoritePlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_FAVORITE_PLACE"
        val db = this.readableDatabase

        try{
            val cursor =db.rawQuery(selectQuery, null)

            if(cursor.moveToFirst()){
                do {
                    val place = FavoritePlaceModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE))
                    )
                    favoritePlaceList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return favoritePlaceList
    }

    fun updateFavoritePlace(favoritePlace: FavoritePlaceModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, favoritePlace.title)
        contentValues.put(KEY_IMAGE, favoritePlace.image)
        contentValues.put(KEY_DESCRIPTION, favoritePlace.description)
        contentValues.put(KEY_DATE, favoritePlace.date)
        contentValues.put(KEY_LOCATION, favoritePlace.location)
        contentValues.put(KEY_LATITUDE, favoritePlace.latitude)
        contentValues.put(KEY_LONGITUDE, favoritePlace.longitude)

        val success = db.update(
            TABLE_FAVORITE_PLACE,
            contentValues,
            KEY_ID + "=" + favoritePlace.id, null)

        db.close()
        return success

    }

        }