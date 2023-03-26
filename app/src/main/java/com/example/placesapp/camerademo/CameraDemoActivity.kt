package com.example.placesapp.camerademo


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.placesapp.R


class CameraDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_demo)

    }

    fun onCameraBtnClock(view: View) {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent,2)
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//                    result ->
//                onActivityResult(2, result)
//            }.launch(intent)
////            }
//            resultLauncher.launch(intent)
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 2)
            } else {
                Toast.makeText(this, "OPPs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==2){
                if(data!=null){
                val bitmap: Bitmap=data?.extras?.get("data") as Bitmap
                findViewById<ImageView>(R.id.iv_immage).setImageBitmap(bitmap)
            }}
        }
    }

}