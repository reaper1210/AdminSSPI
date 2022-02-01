package com.swamisamarthpet.adminsspi

import android.content.Intent
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import com.swamisamarthpet.adminsspi.data.model.*

object Constants {
    var currentCategory: Category? = null
    var currentCategoryImageByteArray: ByteArray? = null
    var currentMachineDetails = ArrayList<Details>()
    var startForSliderImageResult: ActivityResultLauncher<Intent>? = null
    var sliderImageViewList = ArrayList<ImageView>()
    var sliderChangeImagePosition: Int = 0
}