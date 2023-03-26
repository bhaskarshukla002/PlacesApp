package com.example.placesapp.activities

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.placesapp.database.DatabaseHandler
import com.example.placesapp.databinding.ActivityAddHappyPlaceBinding
import com.example.placesapp.models.PlaceModel
import com.example.placesapp.utils.GetAddressFromLatLang
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity() {

    private val cal= Calendar.getInstance()
    private lateinit var datePickerDialog: DatePickerDialog
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mPlaceDetail: PlaceModel?=null
    private var mHappyPlaceDetails : PlaceModel?=null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY="HappyPlacesImages"
    }

    private var binding:ActivityAddHappyPlaceBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val bar = supportActionBar
        bar?.setDisplayHomeAsUpEnabled(true)

        updateDateInView()

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mPlaceDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mPlaceDetail?.let {
            supportActionBar?.title = "Edit Place"
            binding?.etTitle?.setText(it.title)

            binding?.etDescription?.setText(it.description)
            binding?.etDate?.setText(it.date)
            binding?.etLocation?.setText(it.location)
            mLatitude = it.latitude
            mLongitude = it.longitude
            saveImageToInternalStorage = Uri.parse(it.image)
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
            binding?.btnSave?.text = "UPDATE"
        }

        binding?.etDate?.setOnClickListener {
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)
            datePickerDialog = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    cal.set(Calendar.MONTH, monthOfYear)
                    val format = SimpleDateFormat("dd.MM.yyyy").format(cal.time).toString()
                    binding?.etDate?.setText(format)
                }, year, month, day
            )
            datePickerDialog.show()
        }

        binding?.tvAddImage?.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItem =
                arrayOf("Select photo from Gallery", "Capture photo from camera")
            pictureDialog.setItems(pictureDialogItem) { dailog, which ->
                when (which) {
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }

        binding?.tvSelectCurrentLocation?.setOnClickListener{
            if (!isLocationEnabled()) {
                Toast.makeText(
                    this,
                    "Your location provider is turned off. Please turn it on.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } else {
                Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object :MultiplePermissionsListener{

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report?.areAllPermissionsGranted() == true) {
                                createLocationRequest()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }

                    }).onSameThread().check()
            }
        }


        binding?.btnSave?.setOnClickListener {
            when {
                binding?.etTitle?.text.isNullOrEmpty() -> {
                    binding?.etTitle?.error = "This field is required"
//                    Toast.makeText(this,"please enter a Title",Toast.LENGTH_SHORT).show()
                }
                binding?.etDescription?.text.isNullOrEmpty() -> {
                    binding?.etDescription?.error = "This field is required"
//                    Toast.makeText(this,"please enter a Description",Toast.LENGTH_SHORT).show()
                }
                binding?.etLocation?.text.isNullOrEmpty() -> {
                    binding?.etLocation?.error = "This field is required"
//                    Toast.makeText(this,"please enter Location",Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val placeModel = PlaceModel(
                        if (mPlaceDetail == null) 0 else mPlaceDetail!!.id,
                        binding?.etTitle?.text.toString(),
                        saveImageToInternalStorage.toString(),
                        binding?.etDescription?.text.toString(),
                        binding?.etDate?.text.toString(),
                        binding?.etLocation?.text.toString(),
                        mLatitude,
                        mLongitude
                        )
                    val dbHandler = DatabaseHandler(this)
                    if (mPlaceDetail == null) {
                        val addPlace = dbHandler.addPlace(placeModel)
                        if (addPlace > 0) {
                            Toast.makeText(this, "the happy place details are inserted sucessfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        val updatePlace = dbHandler.updatePlace(placeModel)
                        if (updatePlace > 0) {
                            Toast.makeText(this, "the happy place details are inserted sucessfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval=1000
        mLocationRequest.numUpdates=1

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack,
            Looper.myLooper())

    }

    private val mLocationCallBack=object :LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val mLastLocation: Location? = p0?.lastLocation
            mLatitude= mLastLocation?.latitude!!
            mLongitude=mLastLocation?.longitude!!
            Log.e("lati: ","$mLatitude")
            Log.e("long: ","$mLongitude")

                val addressTask=GetAddressFromLatLang(this@AddHappyPlaceActivity,mLatitude,mLongitude)
                addressTask.setAddressListener(object : GetAddressFromLatLang.AddressListener{
                    override fun onAddressFound(address: String?){
                        binding?.etLocation?.setText(address)
                    }
                    override fun onError(){
                    }
                })
            try{
                addressTask.getAddress()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }


    private fun updateDateInView(){
        val format = SimpleDateFormat("dd.MM.yyyy").format(cal.time).toString()
        binding?.etDate?.setText(format)
    }

    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
//                        val intent= Intent(MediaStore.ACTION_PICK_IMAGES)
                        val galleryIntent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?, token: PermissionToken?) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?, token: PermissionToken?) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()

    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage(""+
                "It looks like you have turned off permission required"+
                "for this feature. It can be under the "+
                "Applications Settings")
            .setPositiveButton("Go To Settings"){ _,_->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data =uri
                    startActivity(intent)
                }catch(e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){
                    dialog,_->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper=ContextWrapper(applicationContext)
        var file=wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file= File(file,"${UUID.randomUUID()}.jpg")
        try{
            val stream:OutputStream=FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode== GALLERY){
                if(data!=null){
                    val contentUri=data.data
                    try{
                        val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                        saveImageToInternalStorage=saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("saved Image: ","Path :: $saveImageToInternalStorage")
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,"Failed to load image from gallery",Toast.LENGTH_SHORT).show()
                    }
                }
            }else if(requestCode== CAMERA){
                val bitmap: Bitmap=data!!.extras!!.get("data")as Bitmap
                saveImageToInternalStorage=saveImageToInternalStorage(bitmap)
                Log.e("saved Image: ","Path :: $saveImageToInternalStorage")
                binding?.ivPlaceImage?.setImageBitmap(bitmap)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                finish()
                return true } }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { return true }
    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}