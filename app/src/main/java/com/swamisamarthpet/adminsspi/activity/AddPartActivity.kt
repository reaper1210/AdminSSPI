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
    private var machineId = 0

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

    private val updateStartForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!
            imagesArrayList.removeAt(Constants.sliderChangeImagePosition)
            imagesArrayList.add(Constants.sliderChangeImagePosition,File(uri.path!!).readBytes())
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

        machineId = Constants.currentMachine?.machineId!!

        Constants.currentPartDetails.clear()
        partDetailsAdapter = PartDetailsAdapter()
        partDetailsAdapter.activityContext = this
        partDetailsAdapter.submitList(Constants.currentPartDetails)

        Constants.startForSliderImageResult = updateStartForSliderImageResult

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
                        insertStartForSliderImageResult.launch(intent)
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
                        Constants.currentPartDetails.add(Details(feature, detail))
                        partDetailsAdapter.notifyItemInserted(Constants.currentPartDetails.size)
                        dialog.cancel()
                    }
                    else{
                        Toast.makeText(this@AddPartActivity,"Please fill the fields", Toast.LENGTH_SHORT).show()
                    }

                }

                dialog.show()
            }

            btnAddPartAddPartAct.setOnClickListener {
                val detailsFeaturesList = ArrayList<String>()
                val detailsDescList = ArrayList<String>()

                for(i in 0..Constants.currentPartDetails.lastIndex){
                    detailsFeaturesList.add(Constants.currentPartDetails[i].key)
                    detailsDescList.add(Constants.currentPartDetails[i].value)
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
                        Intent(this@AddPartActivity,MachineDetailsActivity::class.java).also{
                            it.putExtra("machineId",machineId)
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(it)
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

    override fun onResume() {
        Constants.startForSliderImageResult = updateStartForSliderImageResult
        super.onResume()
    }

}