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
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.ImageSliderAdapter
import com.swamisamarthpet.adminsspi.adapter.MachineDetailsAdapter
import com.swamisamarthpet.adminsspi.adapter.PartsAdapter
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.data.model.Machine
import com.swamisamarthpet.adminsspi.data.util.MachineApiState
import com.swamisamarthpet.adminsspi.data.util.PartApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityMachineDetailsBinding
import com.swamisamarthpet.adminsspi.ui.MachineViewModel
import com.swamisamarthpet.adminsspi.ui.PartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.Inflater
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class MachineDetailsActivity : AppCompatActivity() {

    private val partViewModel: PartViewModel by viewModels()
    private val machineViewModel: MachineViewModel by viewModels()
    @Inject
    lateinit var machineDetailsAdapter: MachineDetailsAdapter
    @Inject
    lateinit var partsAdapter: PartsAdapter
    private lateinit var binding: ActivityMachineDetailsBinding
    private lateinit var machine: Machine
    private var parts: List<HashMap<String, String>> = ArrayList()
    private lateinit var imagesArrayList: ArrayList<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineDetailsBinding.inflate(layoutInflater)

        binding.txtNoSpareParts.visibility = View.GONE
        machineDetailsAdapter = MachineDetailsAdapter()
        machineDetailsAdapter.activityContext = this
        partsAdapter = PartsAdapter()
        val machineId = intent.getIntExtra("machineId", 0)
        if (Constants.currentCategory?.categoryName != null) {
            machineViewModel.getMachineById(machineId, Constants.currentCategory!!.categoryName)
            handleMachineDetailsResponse()
        }

        Constants.startForSliderImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.data!!
                imagesArrayList.removeAt(Constants.sliderChangeImagePosition)
                imagesArrayList.add(Constants.sliderChangeImagePosition,uri)
                Glide.with(this).load(uri).error(R.drawable.ic_launcher_foreground).into(Constants.sliderImageViewList[Constants.sliderChangeImagePosition])
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (binding.pdfViewMachineDetailsAct.visibility == View.VISIBLE) {
            binding.pdfViewMachineDetailsAct.visibility = View.GONE
            binding.topLayoutChipMachineDetailsActivity.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun handleMachineDetailsResponse() {
        lifecycleScope.launchWhenStarted {
            machineViewModel.machineApiStateFlow.collect { machineApiState ->
                when (machineApiState) {
                    is MachineApiState.LoadingGetMachineById -> {
                        binding.apply{
                            machineDetailsActProgressBarLayout.visibility = View.VISIBLE
                        }
                    }
                    is MachineApiState.SuccessGetMachineById -> {
                        machine = machineApiState.data
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                        partViewModel.getAllParts(machine.machineName)
                        handlePartsResponse()
                        setValues()
                    }
                    is MachineApiState.FailureGetMachineById -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    is MachineApiState.EmptyGetMachineById -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {}
                }
            }

        }
    }

    private fun handleDeleteMachineResponse() {
        lifecycleScope.launchWhenStarted {
            machineViewModel.machineApiStateFlow.collect { machineApiState ->
                when (machineApiState) {
                    is MachineApiState.LoadingDeleteMachine -> {
                        binding.apply{
                            machineDetailsActProgressBarLayout.visibility = View.VISIBLE
                        }
                    }
                    is MachineApiState.SuccessDeleteMachine-> {
                        if(machineApiState.data == 1){
                            binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                            Intent(this@MachineDetailsActivity,MachineActivity::class.java).also{
                                startActivity(it)
                            }

                        }
                    }
                    is MachineApiState.FailureDeleteMachine -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    is MachineApiState.EmptyDeleteMachine -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {}
                }
            }

        }
    }

    private fun handlePartsResponse() {
        lifecycleScope.launchWhenStarted {
            partViewModel.partApiStateFlow.collect { partApiState ->
                when (partApiState) {
                    is PartApiState.LoadingGetAllParts -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.VISIBLE
                    }
                    is PartApiState.SuccessGetAllParts -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        parts = partApiState.data
                        if (parts.isEmpty()) {
                            binding.txtNoSpareParts.visibility = View.VISIBLE
                        }
                        setPartRecycler()
                    }
                    is PartApiState.FailureGetAllParts -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        binding.txtNoSpareParts.visibility = View.VISIBLE
                    }
                    is PartApiState.Empty -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        binding.txtNoSpareParts.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.partsRecyclerViewProgressBarLayout.visibility = View.GONE
                        binding.txtNoSpareParts.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setValues() {

        Constants.currentMachineDetails.clear()
        val machineDetails = machine.machineDetails.split(";")
        for (detail in machineDetails) {
            val keyValue = detail.split(":")
            Constants.currentMachineDetails.add(Details(keyValue[0], keyValue[1]))
        }
        machineDetailsAdapter.submitList(Constants.currentMachineDetails)

        imagesArrayList = ArrayList<Any>()
        val imagesStringList = machine.machineImages.split(";")
        for (imageString in imagesStringList) {
            val decompressor = Inflater()
            val stringArray = imageString.replace("[", "").replace("]", "").replace(" ", "").split(",")
            val bytes = ByteArray(imageString.length)
            for (i in 0..stringArray.lastIndex) {
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

        Constants.sliderImageViewList.clear()
        val imageSliderAdapter = ImageSliderAdapter(imagesArrayList,this)

        binding.apply {

            topLayoutChipMachineDetailsActivity.setOnClickListener {
                if(binding.topLayoutChipMachineDetailsActivity.text == "Delete Machine"){
                    machineViewModel.deleteMachine(machine.machineId,Constants.currentCategory!!.categoryName)
                    handleDeleteMachineResponse()
                }
            }

            txtProductNameMachineDetails.text = machine.machineName
            imageMachineDetailsAct.apply {
                setSliderAdapter(imageSliderAdapter)
                setIndicatorAnimation(IndicatorAnimationType.WORM)
                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            }
            btnInterestedMachineDetailsAct.setOnClickListener {
                val intent = Intent(this@MachineDetailsActivity, MainActivity::class.java)
                intent.putExtra("supportFragmentRedirect", true)
                intent.putExtra("productName", machine.machineName)
                startActivity(intent)
            }

            machineDetailsRecycler.apply {
                layoutManager = LinearLayoutManager(this@MachineDetailsActivity)
                adapter = machineDetailsAdapter
            }

            if (machine.machinePdf == "") {
                btnViewPdf.visibility = View.INVISIBLE
            }

            edtTxtYoutubeLink.apply{
                setText(machine.youtubeVideoLink)
                isEnabled = false
            }

            youtubeLinkEditIcon.setOnClickListener {
                edtTxtYoutubeLink.isEnabled = true
            }

            btnViewPdf.setOnClickListener {
                pdfViewMachineDetailsAct.fromBytes(getPdfDecompressedByteArray(machine.machinePdf))
                    .onError {
                        println("Pdf Error: ${it.message}")
                    }
                    .enableSwipe(true)
                    .load()

                pdfViewMachineDetailsAct.visibility = View.VISIBLE
                topLayoutChipMachineDetailsActivity.visibility = View.VISIBLE
            }

//            topLayoutChipMachineDetailsActivity.setOnClickListener {
//                pdfViewMachineDetailsAct.visibility = View.GONE
//                topLayoutChipMachineDetailsActivity.visibility = View.GONE
//            }

            btnMachineDetailActivityAddDetail.setOnClickListener {
                val builder = AlertDialog.Builder(this@MachineDetailsActivity)
                val activityInflater = this@MachineDetailsActivity.layoutInflater
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
                        machineDetailsAdapter.notifyItemInserted(Constants.currentMachineDetails.size)
                        dialog.cancel()
                    }
                    else{
                        Toast.makeText(this@MachineDetailsActivity,"Please fill the fields",Toast.LENGTH_SHORT).show()
                    }

                }

                dialog.show()
            }

            btnBackMachineDetailsActivity.setOnClickListener {
                onBackPressed()
            }
        }

    }

    private fun setPartRecycler() {
        partsAdapter.submitList(parts)
        binding.apply {
            partsRecycler.apply {
                layoutManager = LinearLayoutManager(
                    this@MachineDetailsActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = partsAdapter
            }
        }
    }

    private fun getPdfDecompressedByteArray(pdfString: String): ByteArray {
        val decompressor = Inflater()
        val stringArray = pdfString.replace("[", "").replace("]", "").replace(" ", "").split(",")
        val bytes = ByteArray(pdfString.length)
        for (i in 0..stringArray.lastIndex) {
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
        return bos.toByteArray()
    }

}
