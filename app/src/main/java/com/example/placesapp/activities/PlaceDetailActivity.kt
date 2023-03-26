package com.example.placesapp.activities

import android.R

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.placesapp.databinding.ActivityPlaceDetailBinding
import com.example.placesapp.models.PlaceModel
import java.io.Serializable

class PlaceDetailActivity : AppCompatActivity() {

    private var model:PlaceModel?=null
    private var binding:ActivityPlaceDetailBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val bar = supportActionBar
        bar?.setDisplayHomeAsUpEnabled(true)
        if(intent.hasExtra("EXTRA_PLACE_DETAILS")){
            model= intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }
        model?.let {
            Toast.makeText(this,"${model?.title.toString()}",Toast.LENGTH_SHORT).show()
            binding?.ivPlaceImage?.setImageURI(Uri.parse(it.image))
            binding?.tvDescription?.text=it.description
            binding?.tvLocation?.text=it.location
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