package com.example.placesapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.placesapp.activities.AddHappyPlaceActivity
import com.example.placesapp.activities.MainActivity
import com.example.placesapp.database.DatabaseHandler
import com.example.placesapp.databinding.ItemPlaceBinding
import com.example.placesapp.models.PlaceModel

open class PlacesAdapter(private val context: Context,private val list:ArrayList<PlaceModel>):RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {
    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesAdapter.ViewHolder {
        return ViewHolder(ItemPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place= list[position]

        holder.ivPlaceImage.setImageURI(Uri.parse(place.image))
        holder.tvTitle.text=place.title
        holder.tvDescription.text=place.description

        holder.itemView.setOnClickListener{
            if(onClickListener!=null){
                onClickListener!!.onClick(position , place)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }
    class ViewHolder(binding: ItemPlaceBinding):RecyclerView.ViewHolder(binding.root){
        val ivPlaceImage=binding.ivPlaceImage
        val tvTitle=binding.tvTitle
        val tvDescription=binding.tvDescription
    }
    interface OnClickListener{
        fun onClick(position: Int,model:PlaceModel)
    }
    fun notifyEditItem(activity: Activity,position:Int,requestCode:Int){
        val intent= Intent(context,AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
        activity.startActivity(intent)
        notifyItemChanged(position)
    }

    fun notifyRemoveAt(position : Int){
        val dbHandler =DatabaseHandler(context)
        val isDeleted =dbHandler.deletePlace(list[position])
        if(isDeleted>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }else{
            Toast.makeText(context,"Error Occurred",Toast.LENGTH_SHORT).show()
        }
    }
}