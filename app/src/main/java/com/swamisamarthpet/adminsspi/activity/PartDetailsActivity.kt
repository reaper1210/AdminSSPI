package com.swamisamarthpet.adminsspi.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.ImageSliderAdapter
import com.swamisamarthpet.adminsspi.adapter.PartDetailsAdapter
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.data.model.Machine
import com.swamisamarthpet.adminsspi.data.model.Part
import com.swamisamarthpet.adminsspi.data.util.PartApiState
import com.swamisamarthpet.adminsspi.data.util.PopularProductApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityPartDetailsBinding
import com.swamisamarthpet.adminsspi.ui.PartViewModel
import com.swamisamarthpet.adminsspi.ui.PopularProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Inflater
import javax.inject.Inject

@AndroidEntryPoint
class PartDetailsActivity : AppCompatActivity() {
    private val partViewModel: PartViewModel by viewModels()
    private val popularProductViewModel: PopularProductViewModel by viewModels()
    @Inject
    lateinit var partDetailsAdapter: PartDetailsAdapter
    lateinit var binding: ActivityPartDetailsBinding
    private var part: Part? = null
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private lateinit var imagesArrayList: ArrayList<ByteArray>

    private val insertStartForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!
            imagesArrayList.add(File(uri.path!!).readBytes())
            imageSliderAdapter.notifyDataSetChanged()
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private val startForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!
            imagesArrayList.removeAt(Constants.sliderChangeImagePosition)
            imagesArrayList.add(Constants.sliderChangeImagePosition, File(uri.path!!).readBytes())
            imageSliderAdapter.notifyDataSetChanged()
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartDetailsBinding.inflate(layoutInflater)

        Constants.currentPartDetails.clear()
        partDetailsAdapter = PartDetailsAdapter()
        partDetailsAdapter.activityContext = this
        val partId = intent.getIntExtra("partId",0)

        Constants.startForSliderImageResult = startForSliderImageResult

        if(Constants.currentMachine != null && partId!=0){
            partViewModel.getPartById(partId, Constants.currentMachine?.machineName!!)
            handleGetPartByIdResponse()
        }
        else{
            val productId = intent.getIntExtra("productId", 0)
            if (productId != 0) {
                popularProductViewModel.getPopularProductById(productId)
                handleGetPopularProductByIdResponse()
            }
        }

        binding.btnBackPartDetailsActivity.setOnClickListener {
            onBackPressed()
        }

        setContentView(binding.root)
    }

    private fun handleGetPopularProductByIdResponse() {
        lifecycleScope.launchWhenStarted {
            popularProductViewModel.popularProductApiStateFlow.collect { popularProductApiState ->
                when (popularProductApiState) {
                    is PopularProductApiState.LoadingGetPopularProductById -> {
                        binding.apply{
                            partDetailsActProgressBarLayout.visibility = View.VISIBLE
                        }
                    }
                    is PopularProductApiState.SuccessGetPopularProductById -> {
                        val product = popularProductApiState.data
                        part = Part(product.productId,product.productName,product.productImages,product.productDetails)
                        binding.apply{
                            partDetailsActProgressBarLayout.visibility = View.GONE
                            btnAddToPopularPartDetailsAct.visibility = View.GONE
                            txtProductPopularityPartDetailsActivity.apply{
                                visibility = View.VISIBLE
                                text = product.productPopularity.toString()
                            }
                            btnDeletePartDetailsActivity.text = "Delete Product"
                        }
                        setValues()
                    }
                    is PopularProductApiState.FailureGetPopularProductById -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleUpdatePopularProductResponse() {
        lifecycleScope.launchWhenStarted {
            popularProductViewModel.popularProductApiStateFlow.collect { popularProductApiState ->
                when (popularProductApiState) {
                    is PopularProductApiState.LoadingUpdatePopularProduct -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is PopularProductApiState.SuccessUpdatePopularProduct -> {
                        if(popularProductApiState.data == 1){
                            binding.partDetailsActProgressBarLayout.visibility = View.GONE
                            Toast.makeText(this@PartDetailsActivity, "Popular Product Updated", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PartDetailsActivity,MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            intent.putExtra("redirect",2)
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(this@PartDetailsActivity, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is PopularProductApiState.FailureUpdatePopularProduct -> {
                        println("Update Failed ${popularProductApiState.msg}")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleDeletePopularProductResponse() {
        lifecycleScope.launchWhenStarted {
            popularProductViewModel.popularProductApiStateFlow.collect { popularProductApiState ->
                when (popularProductApiState) {
                    is PopularProductApiState.LoadingDeletePopularProduct -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is PopularProductApiState.SuccessDeletePopularProduct -> {
                        if(popularProductApiState.data == 1){
                            binding.partDetailsActProgressBarLayout.visibility = View.GONE
                            Toast.makeText(this@PartDetailsActivity, "Popular Product Deleted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@PartDetailsActivity,MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            intent.putExtra("redirect",2)
                            startActivity(intent)
                        }
                    }
                    is PopularProductApiState.FailureDeletePopularProduct -> {
                        println("Delete Failed ${popularProductApiState.msg}")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleGetPartByIdResponse(){
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState->
                when (partApiState) {
                    is PartApiState.LoadingGetPartById -> {
                        binding.apply{
                            partDetailsActProgressBarLayout.visibility = View.VISIBLE
                        }
                    }
                    is PartApiState.SuccessGetPartById -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.GONE
                        part = partApiState.data
                        setValues()
                    }
                    is PartApiState.FailureGetPartById -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleDeletePartResponse(){
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState->
                when (partApiState) {
                    is PartApiState.LoadingDeletePart -> {
                        binding.btnDeletePartDetailsActivity.visibility = View.GONE
                    }
                    is PartApiState.SuccessDeletePart -> {
                        Intent(this@PartDetailsActivity,MachineDetailsActivity::class.java).also{
                            it.putExtra("machineId",Constants.currentMachine?.machineId!!)
                            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(it)
                        }
                    }
                    is PartApiState.FailureDeletePart -> {
                        Toast.makeText(this@PartDetailsActivity,"Failed to Delete Part",Toast.LENGTH_SHORT).show()
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleUpdatePartResponse() {
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState->
                when (partApiState) {
                    is PartApiState.LoadingUpdatePart -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is PartApiState.SuccessUpdatePart -> {
                        Intent(this@PartDetailsActivity,MachineDetailsActivity::class.java).also{
                            it.putExtra("machineId",Constants.currentMachine?.machineId!!)
                            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(it)
                        }
                        binding.partDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    is PartApiState.FailureUpdatePart -> {
                        Toast.makeText(this@PartDetailsActivity,"Failed to Update Part",Toast.LENGTH_SHORT).show()
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleMarkPartAsPopularResponse() {
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState->
                when (partApiState) {
                    is PartApiState.LoadingMarkPartAsPopular -> {
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is PartApiState.SuccessMarkPartAsPopular -> {
                        Intent(this@PartDetailsActivity,MainActivity::class.java).also{
                            it.putExtra("redirect",2)
                            startActivity(it)
                        }
                        binding.partDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    is PartApiState.FailureMarkPartAsPopular -> {
                        println("failed mark as popular: ${partApiState.msg}")
                        Toast.makeText(this@PartDetailsActivity,"Failed to Mark Part As Popular",Toast.LENGTH_SHORT).show()
                        binding.partDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setValues(){
        if(part!=null){
            Constants.currentPartDetails.clear()
            val partDetails = part?.partDetails?.split(";")
            for(detail in partDetails!!){
                val keyValue = detail.split(":")
                if(keyValue.size > 1){
                    Constants.currentPartDetails.add(Details(keyValue[0],keyValue[1]))
                }
            }
            partDetailsAdapter.submitList(Constants.currentPartDetails)

            imagesArrayList = ArrayList()
            val imagesStringList = part?.partImages?.split(";")
            for(imageString in imagesStringList!!){
                val decompressor = Inflater()
                val stringArray = imageString.replace("[","").replace("]","").replace(" ","").split(",")
                val bytes = ByteArray(imageString.length)
                for(i in 0..stringArray.lastIndex){
                    val byte = stringArray[i].toByte()
                    bytes[i] = byte
                }
                decompressor.setInput(bytes)
                val bos = ByteArrayOutputStream(bytes.size)
                val buf = ByteArray(1024)
                while (!decompressor.finished()) {
                    val count = decompressor.inflate(buf)
                    bos.write(buf, 0, count)
                }
                bos.close()
                val decompressedImageByteArray = bos.toByteArray()
                imagesArrayList.add(decompressedImageByteArray)
            }
            imageSliderAdapter = ImageSliderAdapter(imagesArrayList,this@PartDetailsActivity)

            binding.apply {
                txtPartNamePartDetails.text = part?.partName
                imagePartDetailsAct.apply {
                    setSliderAdapter(imageSliderAdapter)
                    setIndicatorAnimation(IndicatorAnimationType.WORM)
                    setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
                }
                btnUpdatePartDetailsAct.setOnClickListener {
                    val allKeyArray = ArrayList<String>()
                    val allValuesArray = ArrayList<String>()
                    val keysAndValuesMergedArray = java.util.ArrayList<String>()
                    for(detail in Constants.currentPartDetails){
                        allKeyArray.add(detail.key)
                        allValuesArray.add(detail.value)
                    }
                    for(i in 0..allKeyArray.lastIndex){
                        keysAndValuesMergedArray.add(allKeyArray[i]+":"+allValuesArray[i])
                    }
                    val partDetailsString = keysAndValuesMergedArray.joinToString(";")

                    val productId = intent.getIntExtra("productId", 0)
                    if (productId != 0) {
                        popularProductViewModel.updatePopularProduct(productId,txtProductPopularityPartDetailsActivity.text.toString().toInt(),part?.partName!!,"part",partDetailsString,imagesArrayList,ByteArray(0),"")
                        handleUpdatePopularProductResponse()
                    }
                    else{
                        partViewModel.updatePart(Constants.currentMachine!!.machineName, part!!.partId, part!!.partName, partDetailsString, imagesArrayList)
                        handleUpdatePartResponse()
                    }


                }
                partDetailsRecycler.apply {
                    layoutManager = LinearLayoutManager(this@PartDetailsActivity)
                    adapter = partDetailsAdapter
                }
                btnAddDetailPartDetails.setOnClickListener {
                    val builder = AlertDialog.Builder(this@PartDetailsActivity)
                    val activityInflater = this@PartDetailsActivity.layoutInflater
                    val view = activityInflater.inflate(R.layout.dialog_add_detail,null)

                    builder.setView(view)
                    val dialog=builder.create()
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val edtTxtAddDetailFeature = view.findViewById<EditText>(R.id.edtTxtAddDetailFeature)
                    val edtTxtAddDetailDetail = view.findViewById<EditText>(R.id.edtTxtAddDetailDetail)
                    val btnAddDetail = view.findViewById<ImageView>(R.id.btnAddDetailConfirm)

                    btnAddDetail.setOnClickListener {
                        val feature = edtTxtAddDetailFeature.text.toString()
                        val detail = edtTxtAddDetailDetail.text.toString()
                        if(feature.isNotEmpty() && detail.isNotEmpty()){
                            Constants.currentPartDetails.add(Details(feature, detail))
                            partDetailsAdapter.notifyItemInserted(Constants.currentPartDetails.size)
                            dialog.cancel()
                        }
                        else{
                            Toast.makeText(this@PartDetailsActivity,"Please fill the fields", Toast.LENGTH_SHORT).show()
                        }

                    }

                    dialog.show()
                }

                btnAddImagePartDetailsAct.setOnClickListener {
                    ImagePicker.with(this@PartDetailsActivity)
                        .crop()
                        .compress(512)
                        .maxResultSize(512,512)
                        .createIntent { intent ->
                            insertStartForSliderImageResult.launch(intent)
                        }
                }

                btnRemoveImagePartDetailsAct.setOnClickListener {
                    imagesArrayList.removeAt(imagePartDetailsAct.currentPagePosition)
                    imageSliderAdapter.notifyDataSetChanged()
                }

                btnDeletePartDetailsActivity.setOnClickListener {
                    val productId = intent.getIntExtra("productId", 0)
                    if (productId != 0) {
                        popularProductViewModel.deletePopularProduct(productId)
                        handleDeletePopularProductResponse()
                    }
                    else{
                        if((Constants.currentMachine!=null) and (part!=null)){
                            partViewModel.deletePart(Constants.currentMachine?.machineName!!, part?.partId!!)
                            handleDeletePartResponse()
                        }
                        else{
                            Toast.makeText(this@PartDetailsActivity,"Failed to Delete Part",Toast.LENGTH_SHORT).show()
                        }
                    }

                }

                val builder = AlertDialog.Builder(this@PartDetailsActivity)
                val activityInflater = this@PartDetailsActivity.layoutInflater
                val view = activityInflater.inflate(R.layout.dialog_select_popularity,null)

                builder.setView(view)
                val dialog=builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val numPickerPopularity = view.findViewById<NumberPicker>(R.id.numPickerPopularity)
                numPickerPopularity.maxValue = 10
                numPickerPopularity.minValue = 1
                val btnConfirmPopularity = view.findViewById<ImageView>(R.id.btnConfirmPopularity)

                btnAddToPopularPartDetailsAct.setOnClickListener {

                    val allKeyArray = ArrayList<String>()
                    val allValuesArray = ArrayList<String>()
                    val keysAndValuesMergedArray = java.util.ArrayList<String>()
                    for(detail in Constants.currentPartDetails){
                        allKeyArray.add(detail.key)
                        allValuesArray.add(detail.value)
                    }
                    for(i in 0..allKeyArray.lastIndex){
                        keysAndValuesMergedArray.add(allKeyArray[i]+":"+allValuesArray[i])
                    }
                    val partDetailsString = keysAndValuesMergedArray.joinToString(";")

                    btnConfirmPopularity.setOnClickListener {
                        val popularity = numPickerPopularity.value
                        partViewModel.markPartAsPopular(part?.partName!!,partDetailsString,popularity,imagesArrayList)
                        handleMarkPartAsPopularResponse()
                        dialog.cancel()
                    }

                    dialog.show()
                }

                txtProductPopularityPartDetailsActivity.setOnClickListener {
                    numPickerPopularity.value = txtProductPopularityPartDetailsActivity.text.toString().toInt()
                    btnConfirmPopularity.setOnClickListener {
                        txtProductPopularityPartDetailsActivity.text = numPickerPopularity.value.toString()
                        dialog.cancel()
                    }
                    dialog.show()
                }

            }
        }

    }

    override fun onResume() {
        Constants.startForSliderImageResult = startForSliderImageResult
        super.onResume()
    }

}