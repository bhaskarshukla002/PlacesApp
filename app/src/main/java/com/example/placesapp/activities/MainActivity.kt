 package com.example.placesapp.activities

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.placesapp.SwipeToDeleteCallback
import com.example.placesapp.SwipeToEditCallback
import com.example.placesapp.adapters.PlacesAdapter
import com.example.placesapp.database.DatabaseHandler
import com.example.placesapp.databinding.ActivityMainBinding
import com.example.placesapp.models.PlaceModel

 class MainActivity : AppCompatActivity() {

    private  var binding:ActivityMainBinding?=null
     companion object{
         val EXTRA_PLACE_DETAILS="EXTRA_PLACE_DETAILS"
         val ADD_PLACE_ACTIVITY_REQUEST_CODE=1
     }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent=Intent(this , AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }
        getPlacesListFromLocalDatabase()
    }

     private fun setUpPlaceRecyclerView(placeList:ArrayList<PlaceModel>){
         val adapter=PlacesAdapter(this,placeList)
         binding?.rvHappyPlacesList?.setHasFixedSize(true)
         binding?.rvHappyPlacesList?.layoutManager=LinearLayoutManager(this)
         binding?.rvHappyPlacesList?.adapter=adapter

         adapter.setOnClickListener(object:PlacesAdapter.OnClickListener{
             override fun onClick(position: Int,model: PlaceModel){
                 val intent =Intent(this@MainActivity,PlaceDetailActivity::class.java)
                 intent.putExtra(EXTRA_PLACE_DETAILS,model)
                 startActivity(intent)
             }
         })

         val editSwipeHandler =object : SwipeToEditCallback(this){
             override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 val adapter =binding?.rvHappyPlacesList?.adapter as PlacesAdapter
                 adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,ADD_PLACE_ACTIVITY_REQUEST_CODE)
             }
         }

         val editItemTouchHelper=ItemTouchHelper(editSwipeHandler)
         editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

         val deleteSwipeHandler =object : SwipeToDeleteCallback(this){
             override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 val adapter =binding?.rvHappyPlacesList?.adapter as PlacesAdapter
                 adapter.notifyRemoveAt(viewHolder.adapterPosition)
                 getPlacesListFromLocalDatabase()
             }
         }

         val deleteItemTouchHelper=ItemTouchHelper(deleteSwipeHandler)
         deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

     }
     private fun getPlacesListFromLocalDatabase(){
         val dbHandler =DatabaseHandler(this)
         val placeList:ArrayList<PlaceModel> =dbHandler.getPlacesList()
         if(placeList.isNotEmpty() ){
             binding?.tvNoRecordsAvailable?.visibility=View.GONE
             binding?.rvHappyPlacesList?.visibility= View.VISIBLE
             setUpPlaceRecyclerView(placeList)
         }else {
             binding?.tvNoRecordsAvailable?.visibility=View.VISIBLE
             binding?.rvHappyPlacesList?.visibility= View.GONE
         }
     }

     override fun onResume() {
         super.onResume()
         getPlacesListFromLocalDatabase()
     }

     override fun onDestroy() {
         super.onDestroy()
         binding=null
     }
     override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
             R.id.home -> {
                 finish()
                 return true } }
         return super.onOptionsItemSelected(item)
     }
 }