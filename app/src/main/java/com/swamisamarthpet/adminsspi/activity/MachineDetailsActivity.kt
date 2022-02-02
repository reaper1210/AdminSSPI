package com.swamisamarthpet.adminsspi.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
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
import java.io.File
import java.util.*
import java.util.zip.Inflater
import javax.inject.Inject


@AndroidEntryPoint
class MachineDetailsActivity : AppCompatActivity() {

    private val partViewModel: PartViewModel by viewModels()
    private val machineViewModel: MachineViewModel by viewModels()
    @Inject
    lateinit var machineDetailsAdapter: MachineDetailsAdapter
    @Inject
    lateinit var partsAdapter: PartsAdapter
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private lateinit var binding: ActivityMachineDetailsBinding
    private lateinit var machine: Machine
    private var parts: List<HashMap<String, String>> = ArrayList()
    private lateinit var imagesArrayList: ArrayList<ByteArray>
    private lateinit var resultLauncherPdf: ActivityResultLauncher<Intent>
    var machinePdf: ByteArray? = null

    @SuppressLint("Range")
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
                imagesArrayList.add(Constants.sliderChangeImagePosition,File(uri.path!!).readBytes())
                imageSliderAdapter.notifyDataSetChanged()
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        resultLauncherPdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->

            val data = result.data
            if(data!=null){
                val uri = data.data
                val inputStream = contentResolver.openInputStream(uri!!)
                machinePdf = inputStream?.readBytes()

            }
        }

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (binding.pdfViewMachineDetailsAct.visibility == View.VISIBLE) {
            binding.pdfViewMachineDetailsAct.visibility = View.GONE
            binding.topLayoutChipMachineDetailsActivity.text = "Delete Machine"
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
                        machinePdf = getPdfDecompressedByteArray(machine.machinePdf)
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
                                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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

    private fun handleUpdateMachineResponse() {
        lifecycleScope.launchWhenStarted {
            machineViewModel.machineApiStateFlow.collect { machineApiState ->
                when (machineApiState) {
                    is MachineApiState.LoadingUpdateMachine -> {
                        binding.machineDetailsActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is MachineApiState.SuccessUpdateMachine -> {
                        if(machineApiState.data == 1){
                            binding.machineDetailsActProgressBarLayout.visibility = View.GONE
                            Toast.makeText(this@MachineDetailsActivity, "Machine Updated", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MachineDetailsActivity,MachineActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }
                    }
                    is MachineApiState.FailureUpdateMachine -> {
                        println("Update Failed ${machineApiState.msg}")
                    }
                    is MachineApiState.EmptyUpdateMachine -> {

                    }
                    else -> {

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

        imagesArrayList = ArrayList<ByteArray>()
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
        imageSliderAdapter = ImageSliderAdapter(imagesArrayList,this)
        binding.apply {

            topLayoutChipMachineDetailsActivity.setOnClickListener {
                if(topLayoutChipMachineDetailsActivity.text == "Delete Machine"){
                    machineViewModel.deleteMachine(machine.machineId,Constants.currentCategory!!.categoryName)
                    handleDeleteMachineResponse()
                }
                else{
                    pdfViewMachineDetailsAct.visibility = View.GONE
                    topLayoutChipMachineDetailsActivity.text = "Delete Machine"
                }
            }

            txtProductNameMachineDetails.text = machine.machineName
            imageMachineDetailsAct.apply {
                setSliderAdapter(imageSliderAdapter)
                setIndicatorAnimation(IndicatorAnimationType.WORM)
                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
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
                edtTxtYoutubeLink.performClick()
            }

            btnViewPdf.setOnClickListener {
                if(machinePdf != null){
                    pdfViewMachineDetailsAct.fromBytes(machinePdf!!)
                        .onError {
                            println("Pdf Error: ${it.message}")
                        }
                        .enableSwipe(true)
                        .load()
                    pdfViewMachineDetailsAct.visibility = View.VISIBLE
                    topLayoutChipMachineDetailsActivity.text = "X Close PDF"
                }
                else{
                    pdfViewMachineDetailsAct.fromBytes(getPdfDecompressedByteArray(machine.machinePdf))
                        .onError {
                            println("Pdf Error: ${it.message}")
                        }
                        .enableSwipe(true)
                        .load()

                    pdfViewMachineDetailsAct.visibility = View.VISIBLE
                    topLayoutChipMachineDetailsActivity.text = "X Close PDF"
                }
            }

            pdfEditIcon.setOnClickListener {
                if(ActivityCompat.checkSelfPermission(this@MachineDetailsActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this@MachineDetailsActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1
                    )
                }
                else{
                    selectPDF()
                }
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

            btnUpdateMachineDetailsAct.setOnClickListener {
                val allKeyArray = ArrayList<String>()
                val allValuesArray = ArrayList<String>()
                val keysAndValuesMergedArray = ArrayList<String>()
                for(detail in Constants.currentMachineDetails){
                    allKeyArray.add(detail.key)
                    allValuesArray.add(detail.value)
                }
                for(i in 0..allKeyArray.lastIndex){
                    keysAndValuesMergedArray.add(allKeyArray[i]+":"+allValuesArray[i])
                }
                val machineDetailsString = keysAndValuesMergedArray.joinToString(";")

                machineViewModel.updateMachine(Constants.currentCategory!!.categoryName,
                machine.machineId,machine.machineName,machineDetailsString,
                    machinePdf!!,imagesArrayList,edtTxtYoutubeLink.text.toString())
                handleUpdateMachineResponse()
            }
        }

    }

    private fun selectPDF() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        resultLauncherPdf.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPDF()
        }
        else{
            Toast.makeText(applicationContext,"Permission Denied",Toast.LENGTH_SHORT).show()
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
