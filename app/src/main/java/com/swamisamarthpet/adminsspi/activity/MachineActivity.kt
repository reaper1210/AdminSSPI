package com.swamisamarthpet.adminsspi.activity

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.swamisamarthpet.adminsspi.data.util.CategoryApiState


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

    @OptIn(InternalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val categoryName = intent.getStringExtra("categoryName")
        val categoryImg = intent.getByteArrayExtra("categoryImg")
        val categoryId = intent.getIntExtra("categoryId",0)

        if((categoryName!=null) and (categoryImg!=null)){
            machinesAdapter = MachinesAdapter()
            binding.apply {
                txtCategoryNameMachineActivity.setText(categoryName)
                Glide.with(binding.root).load(categoryImg).into(imgCategoryImageMachineActivity)
                noMachinesLayout.visibility = View.GONE

                recyclerMachinesActivity.apply {
                    layoutManager = GridLayoutManager(this@MachineActivity,2)
                    adapter = machinesAdapter
                }

                btnDeleteCategoryMachineActivity.setOnClickListener {
                    println("Chip Clicked")
                    val dialog = AlertDialog.Builder(this@MachineActivity)
                        dialog.apply {
                            setTitle("Delete Category")
                            setMessage("Are you sure to delete category?")
                            setNegativeButton("Cancel", null)
                            setPositiveButton("Delete",object :DialogInterface.OnClickListener{
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    categoryViewModel.deleteCategory(categoryId)
                                    handleDeleteResponse()
                                    dialog?.cancel()
                                }
                            })
                        }
                    dialog.create()
                    dialog.show()

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
            machinesViewModel.getAllMachines(categoryName!!)

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
//                            binding.machineActProgressBarLayout.visibility = View.GONE
                    }
                    is MachineApiState.EmptyGetAllMachines -> {
//                            binding.machineActProgressBarLayout.visibility = View.GONE
                    }
                    else -> {
//                            binding.machineActProgressBarLayout.visibility = View.GONE
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
                        if( it.data ==1 ){
                            Toast.makeText(this@MachineActivity,"Category Deleted SuccessFully",Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MachineActivity,MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    else -> {
                    }
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

}