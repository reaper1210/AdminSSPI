package com.swamisamarthpet.adminsspi.activity

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import com.swamisamarthpet.adminsspi.data.util.PartApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityAddPartBinding
import com.swamisamarthpet.adminsspi.ui.PartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AddPartActivity : AppCompatActivity() {

    @Inject
    lateinit var partDetailsAdapter: PartDetailsAdapter
    private val partViewModel: PartViewModel by viewModels()
    private lateinit var binding: ActivityAddPartBinding
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private lateinit var imagesArrayList: ArrayList<ByteArray>

    private val startForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Constants.currentPartDetails.clear()
        partDetailsAdapter = PartDetailsAdapter()
        partDetailsAdapter.activityContext = this
        partDetailsAdapter.submitList(Constants.currentPartDetails)

        binding.apply{

            partDetailsRecyclerAddPartAct.apply{
                adapter = partDetailsAdapter
                layoutManager = LinearLayoutManager(this@AddPartActivity)
            }

            imagesArrayList = ArrayList()
            imageSliderAdapter = ImageSliderAdapter(imagesArrayList,this@AddPartActivity)
            partImagesSliderAddPartAct.apply {
                setSliderAdapter(imageSliderAdapter)
                setIndicatorAnimation(IndicatorAnimationType.WORM)
                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            }

            btnAddImageAddPartAct.setOnClickListener {
                ImagePicker.with(this@AddPartActivity)
                    .crop()
                    .compress(512)
                    .maxResultSize(512,512)
                    .createIntent { intent ->
                        startForSliderImageResult.launch(intent)
                    }

            }

            btnRemoveImageAddPartAct.setOnClickListener {
                imagesArrayList.removeAt(partImagesSliderAddPartAct.currentPagePosition)
                imageSliderAdapter.notifyDataSetChanged()
            }

            btnAddDetailAddPartAct.setOnClickListener {
                val builder = AlertDialog.Builder(this@AddPartActivity)
                val activityInflater = this@AddPartActivity.layoutInflater
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
                        Constants.currentMachineDetails.add(Details(feature, detail))
                        println("details: ${Constants.currentMachineDetails}")
                        partDetailsAdapter.notifyItemInserted(Constants.currentMachineDetails.size)
                        dialog.cancel()
                    }
                    else{
                        Toast.makeText(this@AddPartActivity,"Please fill the fields", Toast.LENGTH_SHORT).show()
                    }

                }

                dialog.show()
            }

            btnAddPartAddPartAct.setOnClickListener { it: View? ->
                val detailsFeaturesList = ArrayList<String>()
                val detailsDescList = ArrayList<String>()

                for(i in 0..Constants.currentMachineDetails.lastIndex){
                    detailsFeaturesList.add(Constants.currentMachineDetails[i].key)
                    detailsDescList.add(Constants.currentMachineDetails[i].value)
                }

                var detailsKeysAndValuesMerged = String()

                for(i in 0..detailsFeaturesList.lastIndex){
                    if(i!=detailsFeaturesList.lastIndex){
                        detailsKeysAndValuesMerged+=detailsFeaturesList[i]+":"+detailsDescList[i]+";"
                    }
                    else{
                        detailsKeysAndValuesMerged+=detailsFeaturesList[i]+":"+detailsDescList[i]
                    }

                }
                when {
                    detailsKeysAndValuesMerged.isEmpty() -> {
                        Toast.makeText(this@AddPartActivity, "Details Empty", Toast.LENGTH_SHORT).show()
                    }
                    edtTxtPartNameAddPartAct.text.isEmpty() -> {
                        edtTxtPartNameAddPartAct.error = "Cannot be Empty"
                    }
                    imagesArrayList.isNullOrEmpty() -> {
                        Toast.makeText(this@AddPartActivity, "Add Atleast one Image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        partViewModel.insertPart(Constants.currentMachine?.machineName!!,edtTxtPartNameAddPartAct.text.toString(), detailsKeysAndValuesMerged, imagesArrayList)
                        handleInsertPartResponse()
                    }
                }
            }

            btnBackAddPartAct.setOnClickListener {
                onBackPressed()
            }

        }


    }

    private fun handleInsertPartResponse() {
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState ->
                when (partApiState) {
                    is PartApiState.LoadingInsertPart -> {
                        binding.apply {
                            btnAddPartProgressBarLayout.visibility = View.VISIBLE
                            txtAddPartAddPartAct.visibility = View.GONE
                            btnAddPartAddPartAct.isClickable = false
                        }

                    }
                    is PartApiState.SuccessInsertPart -> {
                        binding.apply {
                            btnAddPartProgressBarLayout.visibility = View.GONE
                            txtAddPartAddPartAct.visibility = View.VISIBLE
                            btnAddPartAddPartAct.isClickable = true
                        }
                    }
                    is PartApiState.FailureInsertPart -> {
                        binding.apply {
                            btnAddPartProgressBarLayout.visibility = View.GONE
                            txtAddPartAddPartAct.visibility = View.VISIBLE
                            btnAddPartAddPartAct.isClickable = true
                        }
                        Toast.makeText(this@AddPartActivity,"Failed to Add Part",Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

}