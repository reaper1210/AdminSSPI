package com.swamisamarthpet.adminsspi.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.data.util.MachineApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityAddMachineBinding
import com.swamisamarthpet.adminsspi.ui.MachineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AddMachineActivity : AppCompatActivity() {

    private val machineViewModel: MachineViewModel by viewModels()
    @Inject
    lateinit var machineDetailsAdapter: MachineDetailsAdapter
    private lateinit var binding: ActivityAddMachineBinding
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private lateinit var imagesArrayList: ArrayList<ByteArray>

    private var machinePdf: ByteArray? = null
    private var resultLauncherPdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val data = result.data
        if(data!=null){
            val uri = data.data
            val inputStream = contentResolver.openInputStream(uri!!)
            machinePdf = inputStream?.readBytes()
            binding.btnAddPdf.setImageResource(R.drawable.ic_edit)
        }
    }

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
        binding = ActivityAddMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Constants.currentMachineDetails.clear()
        machineDetailsAdapter = MachineDetailsAdapter()
        machineDetailsAdapter.activityContext = this
        machineDetailsAdapter.submitList(Constants.currentMachineDetails)

        Constants.startForSliderImageResult = updateStartForSliderImageResult

        binding.apply{

            machineDetailsRecycler.apply{
                adapter = machineDetailsAdapter
                layoutManager = LinearLayoutManager(this@AddMachineActivity)
            }

            imagesArrayList = ArrayList()
            imageSliderAdapter = ImageSliderAdapter(imagesArrayList,this@AddMachineActivity)
            machineImagesSliderAddMachineAct.apply {
                setSliderAdapter(imageSliderAdapter)
                setIndicatorAnimation(IndicatorAnimationType.WORM)
                setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            }

            btnAddImageAddMachineActivity.setOnClickListener {
                ImagePicker.with(this@AddMachineActivity)
                    .crop()
                    .compress(512)
                    .maxResultSize(512,512)
                    .createIntent { intent ->
                        insertStartForSliderImageResult?.launch(intent)
                    }

            }

            btnRemoveImageAddMachineActivity.setOnClickListener {
                imagesArrayList.removeAt(machineImagesSliderAddMachineAct.currentPagePosition)
                imageSliderAdapter.notifyDataSetChanged()
            }

            btnMachineDetailActivityAddDetail.setOnClickListener {
                val builder = AlertDialog.Builder(this@AddMachineActivity)
                val activityInflater = this@AddMachineActivity.layoutInflater
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
                        Toast.makeText(this@AddMachineActivity,"Please fill the fields", Toast.LENGTH_SHORT).show()
                    }

                }

                dialog.show()
            }

            btnAddPdf.setOnClickListener {
                if(ActivityCompat.checkSelfPermission(this@AddMachineActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this@AddMachineActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1
                    )
                }
                else{
                    selectPDF()
                }
            }

            btnAddMachineAddMachineAct.setOnClickListener {
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
                        Toast.makeText(this@AddMachineActivity, "Details Empty", Toast.LENGTH_SHORT).show()
                    }
                    edtTxtMachineNameMachineDetails.text.isEmpty() -> {
                        edtTxtMachineNameMachineDetails.error = "Cannot be Empty"
                    }
                    edtTxtYoutubeLink.text.isEmpty() -> {
                        edtTxtYoutubeLink.error = "Cannot be Empty"
                    }
                    imagesArrayList.isNullOrEmpty() -> {
                        Toast.makeText(this@AddMachineActivity, "Add Atleast one Image", Toast.LENGTH_SHORT).show()
                    }
                    machinePdf!!.isEmpty() -> {
                        Toast.makeText(this@AddMachineActivity, "Add PDF", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        machineViewModel.insertMachine(Constants.currentCategory!!.categoryName,edtTxtMachineNameMachineDetails.text.toString(),
                            detailsKeysAndValuesMerged, machinePdf!!, imagesArrayList, edtTxtYoutubeLink.text.toString())
                        handleInsertMachineResponse()
                    }
                }
            }

            btnViewPdf.setOnClickListener {
                if(machinePdf != null){
                    pdfViewAddMachineAct.fromBytes(machinePdf!!)
                        .onError {
                            println("Pdf Error: ${it.message}")
                        }
                        .enableSwipe(true)
                        .load()
                    pdfViewAddMachineAct.visibility = View.VISIBLE
                    chipClosePdfAddMachineActivity.visibility = View.VISIBLE
                }
                else{
                    Toast.makeText(this@AddMachineActivity,"Please add a pdf first",Toast.LENGTH_SHORT).show()
                }
            }

            chipClosePdfAddMachineActivity.setOnClickListener {
                pdfViewAddMachineAct.visibility = View.GONE
                chipClosePdfAddMachineActivity.visibility = View.GONE
            }

            btnBackAddMachineActivity.setOnClickListener {
                onBackPressed()
            }

        }

    }

    private fun handleInsertMachineResponse() {
        lifecycleScope.launchWhenStarted {
            machineViewModel.machineApiStateFlow.collect { machineApiState ->
                when (machineApiState) {
                    is MachineApiState.LoadingInsertMachine -> {
                        binding.addMachineActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is MachineApiState.SuccessInsertMachine -> {

                        if(machineApiState.data == 1){
                            binding.addMachineActProgressBarLayout.visibility = View.GONE
                            Toast.makeText(this@AddMachineActivity, "Machine Inserted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AddMachineActivity,MachineActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }
                    }
                    is MachineApiState.FailureInsertMachine -> {
                        println("Insert Failed ${machineApiState.msg}")
                    }
                    is MachineApiState.EmptyInsertMachine -> {

                    }
                    else -> {

                    }
                }
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
        if(requestCode==1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPDF()
        }
        else{
            Toast.makeText(applicationContext,"Permission Denied",Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {

        if(binding.pdfViewAddMachineAct.visibility == View.VISIBLE){
            binding.pdfViewAddMachineAct.visibility = View.GONE
            binding.chipClosePdfAddMachineActivity.visibility = View.GONE
        }
        else{
            super.onBackPressed()
        }

    }


    override fun onResume() {
        Constants.startForSliderImageResult = updateStartForSliderImageResult
        super.onResume()
    }

}