package com.swamisamarthpet.adminsspi

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import com.swamisamarthpet.adminsspi.data.model.*
import com.swamisamarthpet.adminsspi.databinding.SliderImageItemBinding

object Constants {
    var currentCategory: Category? = null
    var currentCategoryImageByteArray: ByteArray? = null
    var currentMachineDetails = ArrayList<Details>()
    var startForSliderImageResult: ActivityResultLauncher<Intent>? = null
    var sliderChangeImagePosition: Int = 0
    var isImageChanged:Boolean = false
}