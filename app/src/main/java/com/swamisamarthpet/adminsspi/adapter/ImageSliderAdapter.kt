package com.swamisamarthpet.adminsspi.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.smarteist.autoimageslider.SliderViewAdapter
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.databinding.SliderImageItemBinding

class ImageSliderAdapter(private val imagesArrayList:ArrayList<ByteArray>,private val context:Context):SliderViewAdapter<ImageSliderAdapter.ViewHolder>(){

    override fun getCount(): Int {
        return imagesArrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        return ViewHolder(SliderImageItemBinding.inflate(LayoutInflater.from(parent?.context),parent,false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, position: Int) {
        viewHolder?.bind(position,imagesArrayList,context)
    }

    class ViewHolder(val binding:SliderImageItemBinding): SliderViewAdapter.ViewHolder(binding.root) {
        fun bind(position:Int,images:ArrayList<ByteArray>,context: Context) {
            binding.apply {
                sliderImageView.visibility = View.VISIBLE
                btnPlayVideo.visibility = View.GONE

                sliderImageView.setOnClickListener {
                    ImagePicker.with(context as ComponentActivity)
                        .crop()
                        .compress(512)
                        .maxResultSize(512,512)
                        .createIntent { intent ->
                            Constants.sliderChangeImagePosition = position
                            Constants.startForSliderImageResult?.launch(intent)
                        }
                }

                Glide.with(context).load(images[position]).fitCenter().into(sliderImageView)
            }
        }
    }



}