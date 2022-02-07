package com.swamisamarthpet.adminsspi.activity

import android.Manifest
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.MachineDetailsAdapter
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.databinding.ActivityAddMachineBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddMachineActivity : AppCompatActivity() {

    @Inject
    lateinit var machineDetailsAdapter: MachineDetailsAdapter
    private lateinit var binding: ActivityAddMachineBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Constants.currentMachineDetails.clear()
        machineDetailsAdapter = MachineDetailsAdapter()
        machineDetailsAdapter.activityContext = this
        machineDetailsAdapter.submitList(Constants.currentMachineDetails)

        Constants.currentMachineDetails.clear()
        binding.apply{

            machineDetailsRecycler.apply{
                adapter = machineDetailsAdapter
                layoutManager = LinearLayoutManager(this@AddMachineActivity)
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
                        println("details: ${Constants.currentMachineDetails}")
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

            btnViewPdf.setOnClickListener {
                if(machinePdf != null){
                    pdfViewMachineDetailsAct.fromBytes(machinePdf!!)
                        .onError {
                            println("Pdf Error: ${it.message}")
                        }
                        .enableSwipe(true)
                        .load()
                    pdfViewMachineDetailsAct.visibility = View.VISIBLE
                    chipClosePdfAddMachineActivity.visibility = View.VISIBLE
                }
                else{
                    Toast.makeText(this@AddMachineActivity,"Please add a pdf first",Toast.LENGTH_SHORT).show()
                }
            }

            chipClosePdfAddMachineActivity.setOnClickListener {
                pdfViewMachineDetailsAct.visibility = View.GONE
                chipClosePdfAddMachineActivity.visibility = View.GONE
            }

            btnBackAddMachineActivity.setOnClickListener {
                onBackPressed()
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

        if(binding.pdfViewMachineDetailsAct.visibility == View.VISIBLE){
            binding.pdfViewMachineDetailsAct.visibility = View.GONE
            binding.chipClosePdfAddMachineActivity.visibility = View.GONE
        }
        super.onBackPressed()

    }

}