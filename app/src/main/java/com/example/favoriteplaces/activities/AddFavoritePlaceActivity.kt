package com.example.favoriteplaces.activities


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.favoriteplaces.R
import com.example.favoriteplaces.database.DatabaseHandler
import com.example.favoriteplaces.databinding.ActivityAddFavoritePlaceBinding
import com.example.favoriteplaces.models.FavoritePlaceModel
import com.example.favoriteplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddFavoritePlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var binding: ActivityAddFavoritePlaceBinding? = null
    private var mFavoritePlaceDetails: FavoritePlaceModel? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFavoritePlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddFavoritePlaceActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mFavoritePlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        dateSetListener = OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if (mFavoritePlaceDetails != null) {
            supportActionBar?.title = "Edit Place Details"
            binding?.etPlaceName?.setText(mFavoritePlaceDetails!!.title)
            binding?.etPlaceDescription?.setText(mFavoritePlaceDetails!!.description)
            binding?.etDate?.setText(mFavoritePlaceDetails!!.date)
            binding?.etLocation?.setText(mFavoritePlaceDetails!!.location)
            mLatitude = mFavoritePlaceDetails!!.latitude
            mLongitude = mFavoritePlaceDetails!!.longitude
            saveImageToInternalStorage = Uri.parse(mFavoritePlaceDetails!!.image)
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
            binding?.btnSave?.text = "UPDATE"

        }

        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.btnSelectCurrentLocation?.setOnClickListener(this)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        mFusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    mLatitude = location.latitude
                    mLongitude = location.longitude
                }

            }

        val addressTask = GetAddressFromLatLng(
            this@AddFavoritePlaceActivity,
            lat = mLatitude,
            lng = mLongitude
        )

        addressTask.setCustomAddressListener(object : GetAddressFromLatLng.AddressListener {
            override fun onAddressFound(address: String) {
                binding?.etLocation?.setText(address)
            }

            override fun onError() {
                Log.e("Get address:: ", "onError: Something went wrong",)
            }

        })

        lifecycleScope.launch(Dispatchers.IO) {
            addressTask.launchBackgroundProcessForRequest()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.etDate -> {
                DatePickerDialog(
                    this@AddFavoritePlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tvAddImage -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from Gallery",
                    "Take photo with Camera"
                )
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoWithCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btnSave -> {
                when {
                    binding?.etPlaceName?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a Place Name!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    binding?.etPlaceDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a Description!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a Location!", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select an Image!", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val favoritePlaceModel = FavoritePlaceModel(
                            if(mFavoritePlaceDetails == null) 0 else mFavoritePlaceDetails!!.id,
                            binding?.etPlaceName?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etPlaceDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if (mFavoritePlaceDetails == null){
                            val addFavoritePlace = dbHandler.addFavoritePlace(favoritePlaceModel)
                            if (addFavoritePlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateFavoritePlace = dbHandler.updateFavoritePlace(favoritePlaceModel)
                            if (updateFavoritePlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }

            R.id.etLocation ->{
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME,
                        Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddFavoritePlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            R.id.btnSelectCurrentLocation -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location is permission is disabled. Please enable from settings",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                    requestNewLocationData()
                            }
                        }
                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ){
                            showRationaleDialogForPermissions()
                        }
                    }).onSameThread().check()
                }
            }

        }
    }

        public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GALLERY) {
                    if (data != null) {
                        val contentUri = data.data
                        try {
                            val selectedImageBitmap =
                                MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                            binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                            saveImageToInternalStorage =
                                saveImageToInternalStorage(selectedImageBitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@AddFavoritePlaceActivity,
                                "Failed to load image from the Gallery!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (requestCode == CAMERA) {
                    val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                    var ivPlaceImage: ImageView = findViewById(R.id.ivPlaceImage)
                    saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                    ivPlaceImage.setImageBitmap(thumbnail)

                }else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    binding?.etLocation?.setText(place.address)
                    mLatitude = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
                }
            }
        }

        private fun takePhotoWithCamera() {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread().check();
        }

        private fun choosePhotoFromGallery() {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent: Intent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread().check();
        }

        private fun showRationaleDialogForPermissions() {
            AlertDialog.Builder(this)
                .setMessage("It looks like you have disabled permissions required for this feature. They can be enabled under application settings.")
                .setPositiveButton("Go to Settings")
                { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        private fun updateDateInView() {
            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            binding?.etDate?.setText(sdf.format(cal.time).toString())
        }

        private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
            val wrapper = ContextWrapper(applicationContext)
            var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
            file = File(file, "S${UUID.randomUUID()}.jpg")
            try {
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return Uri.parse(file.absolutePath)
        }

        companion object {
            private const val GALLERY = 1
            private const val CAMERA = 2
            private const val IMAGE_DIRECTORY = "FavoritePlacesImages"
            private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        }

    }
