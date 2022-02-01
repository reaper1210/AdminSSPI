package com.swamisamarthpet.adminsspi.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.MachinesAdapter
import com.swamisamarthpet.adminsspi.data.util.MachineApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityMachineBinding
import com.swamisamarthpet.adminsspi.ui.CategoriesViewModel
import com.swamisamarthpet.adminsspi.ui.MachineViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.util.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.github.dhaval2404.imagepicker.ImagePicker
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.data.util.CategoryApiState
import java.io.File

@AndroidEntryPoint
class MachineActivity : AppCompatActivity() {
    private val machinesViewModel: MachineViewModel by viewModels()
    private val categoryViewModel: CategoriesViewModel by viewModels()
    private lateinit var binding: ActivityMachineBinding
    private var machinesList: List<HashMap<String,String>> = ArrayList()
    private lateinit var automaticMachinesList: List<HashMap<String,String>>
    private lateinit var semiAutomaticMachinesList: List<HashMap<String,String>>
    @Inject
    lateinit var machinesAdapter: MachinesAdapter
    var uri: Uri? = null
    var updated = false

    @OptIn(InternalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val categoryName = intent.getStringExtra("categoryName")
//        val categoryImg = intent.getByteArrayExtra("categoryImg")
//        val categoryId = intent.getIntExtra("categoryId",0)

        if((Constants.currentCategory?.categoryName!=null) and (Constants.currentCategory?.categoryImage!=null)){
            machinesAdapter = MachinesAdapter()
            binding.apply {
                txtCategoryNameMachineActivity.text = Constants.currentCategory?.categoryName
                Glide.with(binding.root).load(Constants.currentCategoryImageByteArray).into(imgCategoryImageMachineActivity)
                noMachinesLayout.visibility = View.GONE

                recyclerMachinesActivity.apply {
                    layoutManager = GridLayoutManager(this@MachineActivity,2)
                    adapter = machinesAdapter
                }

                btnDeleteCategoryMachineActivity.setOnClickListener {
                    val dialog = AlertDialog.Builder(this@MachineActivity)
                        dialog.apply {
                            setTitle("Delete Category")
                            setMessage("Are you sure to delete category?")
                            setNegativeButton("Cancel", null)
                            setPositiveButton("Delete") { dialog, which ->
                                categoryViewModel.deleteCategory(Constants.currentCategory?.categoryId!!)
                                handleDeleteResponse()
                                dialog?.cancel()
                            }
                        }
                    dialog.create()
                    dialog.show()

                }

                val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                    val resultCode = result.resultCode
                    val data = result.data

                    if (resultCode == Activity.RESULT_OK) {
                        uri = data?.data!!
                        Glide.with(this@MachineActivity).load(uri).error(R.drawable.ic_launcher_foreground).into(imgCategoryImageMachineActivity)
                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(this@MachineActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MachineActivity, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }

                imgCategoryImageMachineActivity.setOnClickListener {
                    ImagePicker.with(this@MachineActivity)
                        .crop()
                        .compress(512)
                        .maxResultSize(512,512)
                        .createIntent { intent ->
                            startForProfileImageResult.launch(intent)
                        }
                }

                btnUpdateCategoryMachineActivity.setOnClickListener {
                    if(uri!=null){
                        categoryViewModel.updateCategory(Constants.currentCategory?.categoryId!!,File(uri?.path!!))
                        handleUpdateResponse()
                    }
                    else{
                        Toast.makeText(this@MachineActivity,"Please change the image first",Toast.LENGTH_SHORT).show()
                    }
                }

                edtTxtSearchBarCategoriesFrag.addTextChangedListener(object: TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        when {
                            allMachines.tag == "selected" -> {
                                searchMachines(s.toString(),
                                    machinesList as MutableList<HashMap<String, String>>
                                )
                            }
                            automaticMachines.tag == "selected" -> {
                                searchMachines(s.toString(),
                                    automaticMachinesList as MutableList<HashMap<String, String>>
                                )
                            }
                            else -> {
                                searchMachines(s.toString(),
                                    semiAutomaticMachinesList as MutableList<HashMap<String, String>>
                                )
                            }
                        }
                    }

                })

                allMachines.setOnClickListener {
                    if(allMachines.tag == "unselected"){
//                        chipMachineActivity.text = "All Machines"
                        filterMachines("")
                        allMachines.tag = "selected"
                        automaticMachines.tag = "unselected"
                        semiAutomaticMachines.tag = "unselected"
                        allMachines.setImageResource(R.drawable.all_machines_selected)
                        automaticMachines.setImageResource(R.drawable.automatic_machines_unselected)
                        semiAutomaticMachines.setImageResource(R.drawable.semi_auto_unselected)
                    }
                }

                automaticMachines.setOnClickListener {
                    if(automaticMachines.tag == "unselected"){
//                        chipMachineActivity.text = "Automatic Machines"
                        automaticMachinesList = filterMachines("Automatic")
                        automaticMachines.tag = "selected"
                        allMachines.tag = "unselected"
                        semiAutomaticMachines.tag = "unselected"
                        automaticMachines.setImageResource(R.drawable.automatic_machines_selected)
                        allMachines.setImageResource(R.drawable.all_machines_unselected)
                        semiAutomaticMachines.setImageResource(R.drawable.semi_auto_unselected)
                    }
                }

                semiAutomaticMachines.setOnClickListener {
//                    chipMachineActivity.text = "Semi-Automatic Machines"
                    if (semiAutomaticMachines.tag == "unselected") {
                        semiAutomaticMachinesList = filterMachines("Semi-Automatic")
                        semiAutomaticMachines.tag = "selected"
                        automaticMachines.tag = "unselected"
                        allMachines.tag = "unselected"
                        semiAutomaticMachines.setImageResource(R.drawable.semi_auto_selected)
                        automaticMachines.setImageResource(R.drawable.automatic_machines_unselected)
                        allMachines.setImageResource(R.drawable.all_machines_unselected)
                    }
                }


            }
            machinesViewModel.getAllMachines(Constants.currentCategory?.categoryName!!)

            handleResponse()

        }


    }

    fun handleResponse() {
        lifecycleScope.launchWhenStarted {
            machinesViewModel.machineApiStateFlow.collect {
                when (it) {
                    is MachineApiState.LoadingGetAllMachines -> {
                        binding.machineActProgressBarLayout.visibility = View.VISIBLE
                    }
                    is MachineApiState.SuccessGetAllMachines -> {
                        binding.apply{
                            machineActProgressBarLayout.visibility = View.GONE
                            machinesList = it.data
                            if(it.data.isNullOrEmpty()){
                                txtNoMachines.text = "No Machines Found"
                                noMachinesLayout.visibility = View.VISIBLE
                                recyclerMachinesActivity.visibility = View.GONE
                            }
                            machinesAdapter.submitList(it.data)
                        }
                    }
                    is MachineApiState.FailureGetAllMachines -> {
                            binding.machineActProgressBarLayout.visibility = View.GONE
                    }
                    is MachineApiState.EmptyGetAllMachines -> {
                            binding.machineActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun handleDeleteResponse(){
        lifecycleScope.launchWhenStarted {
            categoryViewModel.categoryApiStateFlow.collect {
                when (it) {
                    is CategoryApiState.SuccessDeleteCategory -> {
                        if( it.data == 1 ){
                            Toast.makeText(this@MachineActivity,"Category Deleted SuccessFully",Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MachineActivity,MainActivity::class.java)
                            intent.putExtra("redirect",1)
                            startActivity(intent)
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun handleUpdateResponse(){
        lifecycleScope.launchWhenStarted {
            categoryViewModel.categoryApiStateFlow.collect { categoryApiState ->
                when(categoryApiState){
                    is CategoryApiState.LoadingUpdateCategory -> {
                        binding.btnUpdateCategoryMachineActivity.isClickable = false
                    }
                    is CategoryApiState.SuccessUpdateCategory -> {
                        if(categoryApiState.data==1){
                            binding.btnUpdateCategoryMachineActivity.isClickable = true
                            Toast.makeText(this@MachineActivity,"Category Updated Successfully",Toast.LENGTH_SHORT).show()
                            updated = true
                        }
                        else{
                            binding.btnUpdateCategoryMachineActivity.isClickable = true
                            Toast.makeText(this@MachineActivity,"Failed to update category",Toast.LENGTH_SHORT).show()
                        }
                    }
                    is CategoryApiState.FailureUpdateCategory -> {
                        binding.btnUpdateCategoryMachineActivity.isClickable = true
                        Toast.makeText(this@MachineActivity,"Failed to update category",Toast.LENGTH_SHORT).show()
                        println("failed update category ${categoryApiState.msg}")
                    }
                    else -> {}
                }
            }
        }
    }

    @InternalAPI
    fun filterMachines(text: String?): MutableList<HashMap<String, String>> {
        val temp: MutableList<HashMap<String,String>> = mutableListOf()
        for (hashmap in machinesList) {
            if(text == "Automatic"){
                if ((hashmap["machineName"].toString().toLowerCasePreservingASCIIRules()).contains(text.toString().toLowerCasePreservingASCIIRules())
                    and
                    (!(hashmap["machineName"].toString().toLowerCasePreservingASCIIRules()).contains("semi")))
                {
                    temp.add(hashmap)
                }
            }
            else{
                if ((hashmap["machineName"].toString().toLowerCasePreservingASCIIRules()).contains(text.toString().toLowerCasePreservingASCIIRules())) {
                    temp.add(hashmap)
                }
            }
        }
        if(temp.isEmpty()) {
            binding.apply{
                txtNoMachines.text = "No $text Machines Found"
                noMachinesLayout.visibility = View.VISIBLE
                recyclerMachinesActivity.visibility = View.GONE
            }
        }
        else{
            binding.apply{
                noMachinesLayout.visibility = View.GONE
                recyclerMachinesActivity.visibility = View.VISIBLE
            }
            machinesAdapter.submitList(temp)
        }

        return temp

    }

    @InternalAPI
    fun searchMachines(text: String?,list:MutableList<HashMap<String,String>>){
        val temp: MutableList<HashMap<String,String>> = mutableListOf()
        for (hashmap in list) {
            if ((hashmap["machineName"].toString().toLowerCasePreservingASCIIRules()).contains(text.toString().toLowerCasePreservingASCIIRules())) {
                temp.add(hashmap)
            }
        }
        machinesAdapter.submitList(temp)
    }

    override fun onBackPressed() {
        if(updated) {
            Intent(this@MachineActivity, MainActivity::class.java).also {
                it.putExtra("redirect", 1)
                startActivity(it)
            }
        }
        else {
            super.onBackPressed()
        }
    }

}