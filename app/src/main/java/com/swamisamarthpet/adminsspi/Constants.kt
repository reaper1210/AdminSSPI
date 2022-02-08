package com.swamisamarthpet.adminsspi

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.swamisamarthpet.adminsspi.data.model.*

object Constants {
    var currentCategory: Category? = null
    var currentCategoryImageByteArray: ByteArray? = null
    var currentMachineDetails = ArrayList<Details>()
    var startForSliderImageResult: ActivityResultLauncher<Intent>? = null
    var sliderChangeImagePosition: Int = 0
    var currentMachine: Machine? = null
    var currentPartDetails = ArrayList<Details>()
}