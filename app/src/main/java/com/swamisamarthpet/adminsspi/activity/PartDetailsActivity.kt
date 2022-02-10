package com.swamisamarthpet.adminsspi.activity

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
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.ImageSliderAdapter
import com.swamisamarthpet.adminsspi.adapter.PartDetailsAdapter
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.data.model.Part
import com.swamisamarthpet.adminsspi.data.util.PartApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityPartDetailsBinding
import com.swamisamarthpet.adminsspi.ui.PartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import javax.inject.Inject

@AndroidEntryPoint
class PartDetailsActivity : AppCompatActivity() {
    private val partViewModel: PartViewModel by viewModels()
    @Inject
    lateinit var partDetailsAdapter: PartDetailsAdapter
    lateinit var binding: ActivityPartDetailsBinding
    private lateinit var part: Part

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartDetailsBinding.inflate(layoutInflater)

        partDetailsAdapter = PartDetailsAdapter()
        partDetailsAdapter.activityContext = this
        val partId = intent.getIntExtra("partId",0)

        if(Constants.currentMachine != null && partId!=0){
            partViewModel.getPartById(partId, Constants.currentMachine?.machineName!!)
            handleResponse()
        }

        binding.btnBackPartDetailsActivity.setOnClickListener {
            onBackPressed()
        }

        setContentView(binding.root)
    }

    private fun handleResponse(){
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


    private fun setValues(){
        val partDetails = part.partDetails.split(";")
        val detailsList = ArrayList<Details>()
        for(detail in partDetails){
            val keyValue = detail.split(":")
            if(keyValue.size > 1){
                detailsList.add(Details(keyValue[0],keyValue[1]))
            }
        }
        partDetailsAdapter.submitList(detailsList)

        val imagesArrayList = ArrayList<ByteArray>()
        val imagesStringList = part.partImages.split(";")
        for(imageString in imagesStringList){
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
        val imageSliderAdapter = ImageSliderAdapter(imagesArrayList,this@PartDetailsActivity)

        binding.apply {
            txtPartNamePartDetails.text = part.partName
            imagePartDetailsAct.apply {
                setSliderAdapter(imageSliderAdapter)
                setIndicatorAnimation(IndicatorAnimationType.WORM)
                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            }
            btnInterestedMachineDetailsAct.setOnClickListener {
                val intent = Intent(this@PartDetailsActivity,MainActivity::class.java)
                intent.putExtra("supportFragmentRedirect",true)
                intent.putExtra("productName",part.partName)
                startActivity(intent)
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
        }
    }
}