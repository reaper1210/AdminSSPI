package com.swamisamarthpet.adminsspi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter
import com.swamisamarthpet.adminsspi.databinding.SliderImageItemBinding

class ImageSliderAdapter(private val imagesArrayList:ArrayList<Any>,private val context:Context):SliderViewAdapter<ImageSliderAdapter.ViewHolder>(){
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
        fun bind(position:Int,images:ArrayList<Any>,context:Context) {
            if (images[position] is ByteArray) {
                binding.apply {
                    sliderImageView.visibility = View.VISIBLE
                    btnPlayVideo.visibility = View.GONE
                    Glide.with(binding.root).load(images[position]).fitCenter()
                        .into(sliderImageView)
                }
            }
        }
    }
}