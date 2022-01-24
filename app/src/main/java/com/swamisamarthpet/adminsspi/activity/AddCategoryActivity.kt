package com.swamisamarthpet.adminsspi.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.data.util.CategoryApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityAddCategoryBinding
import com.swamisamarthpet.adminsspi.ui.CategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class AddCategoryActivity : AppCompatActivity() {

    val categoriesViewModel: CategoriesViewModel by viewModels()
    private lateinit var binding: ActivityAddCategoryBinding
    var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply{

            val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == Activity.RESULT_OK) {
                    uri = data?.data

                    Glide.with(this@AddCategoryActivity).load(uri).error(R.drawable.ic_launcher_foreground).into(imgCategoryAddCategoryAct)
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this@AddCategoryActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddCategoryActivity, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

            imgCategoryAddCategoryAct.setOnClickListener {
                ImagePicker.with(this@AddCategoryActivity)
                    .crop()
                    .compress(512)
                    .maxResultSize(512,512)
                    .createIntent { intent ->
                        startForProfileImageResult.launch(intent)
                    }

            }

            btnAddCategoryAddCategoryActivity.setOnClickListener {
                val categoryName = txtCategoryNameAddCategoryAct.text.toString()
                if(uri!=null){
                    if(!TextUtils.isEmpty(categoryName)){
                            categoriesViewModel.insertCategory(categoryName, File(uri?.path!!))
                            handleAddCategoryResponse()
                    }
                    else{
                        txtCategoryNameAddCategoryAct.error = "Category Name missing"
                    }
                }
                else{
                    Toast.makeText(this@AddCategoryActivity,"Category Image missing", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    private fun handleAddCategoryResponse() {
        lifecycleScope.launchWhenStarted {
            categoriesViewModel.categoryApiStateFlow.collect { categoryApiState ->
                when(categoryApiState){
                    is CategoryApiState.LoadingInsertCategory -> {
                        binding.apply {
                            btnAddCategoryText.visibility = View.GONE
                            btnAddCategoryProgressBar.visibility = View.VISIBLE
                            btnAddCategoryAddCategoryActivity.isClickable = false
                        }
                    }
                    is CategoryApiState.SuccessInsertCategory -> {
                        binding.apply {
                            btnAddCategoryText.visibility = View.VISIBLE
                            btnAddCategoryProgressBar.visibility = View.GONE
                            btnAddCategoryAddCategoryActivity.isClickable = true
                        }
                        val intent = Intent(this@AddCategoryActivity,MainActivity::class.java)
                        intent.putExtra("redirect",1)
                        startActivity(intent)
                    }
                    is CategoryApiState.FailureInsertCategory -> {
                        Toast.makeText(this@AddCategoryActivity,"Failed to add category",Toast.LENGTH_SHORT).show()
                        binding.apply {
                            btnAddCategoryText.visibility = View.VISIBLE
                            btnAddCategoryProgressBar.visibility = View.GONE
                            btnAddCategoryAddCategoryActivity.isClickable = true
                        }
                    }
                    else -> {}
                }
            }
        }
    }

}