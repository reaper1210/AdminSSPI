package com.swamisamarthpet.adminsspi.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.activity.AddCategoryActivity
import com.swamisamarthpet.adminsspi.adapter.CategoriesAdapter
import com.swamisamarthpet.adminsspi.data.model.Category
import com.swamisamarthpet.adminsspi.data.util.CategoryApiState
import com.swamisamarthpet.adminsspi.databinding.FragmentCategoriesBinding
import com.swamisamarthpet.adminsspi.ui.CategoriesViewModel
import io.ktor.util.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@InternalAPI
class CategoriesFragment : Fragment() {
    private lateinit var binding: FragmentCategoriesBinding
    @Inject
    lateinit var categoriesAdapter: CategoriesAdapter
    private val categoryViewModel: CategoriesViewModel by activityViewModels()
    private var categoriesList:List<Category> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(layoutInflater)
        categoriesAdapter = CategoriesAdapter()
        initRecyclerview()
        categoryViewModel.getAllCategories()
        handleResponse()

        binding.apply {
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
                    searchCategories(s.toString())
                }

            })

            btnAddCategoryFragment.setOnClickListener {
                val intent = Intent(requireContext(),AddCategoryActivity::class.java)
                startActivity(intent)
            }
        }
        return binding.root
    }

    private fun handleResponse(){
        lifecycleScope.launchWhenStarted {
            categoryViewModel.categoryApiStateFlow.collect {
                when (it) {
                    is CategoryApiState.LoadingGetAllMachines -> {
                        binding.apply {
                            categoriesFragmentProgressBarLayout.visibility = View.VISIBLE
                        }
                    }
                    is CategoryApiState.SuccessGetAllMachines -> {
                        binding.apply {
                            categoriesFragmentProgressBarLayout.visibility = View.GONE
                            categoriesList = it.data
                            if(it.data.isNullOrEmpty()){
                                noCategoriesLayout.visibility = View.VISIBLE
                                recyclerViewCategoriesFragment.visibility = View.GONE
                            }
                            else{
                                categoriesAdapter.submitList(it.data)
                            }

                        }
                    }
                    is CategoryApiState.FailureGetAllMachines -> {
                        binding.categoriesFragmentProgressBarLayout.visibility = View.GONE
                    }
                    is CategoryApiState.EmptyGetAllMachines -> {
                        binding.categoriesFragmentProgressBarLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initRecyclerview() {
        binding.apply {
            recyclerViewCategoriesFragment.apply {
                layoutManager = GridLayoutManager(activity,2)
                adapter = categoriesAdapter
            }
        }
    }


    private fun searchCategories(text: String?){
        val temp: MutableList<Category> = mutableListOf()
        for (category in categoriesList) {
            if ((category.categoryName.toLowerCasePreservingASCIIRules()).contains(text.toString().toLowerCasePreservingASCIIRules())) {
                temp.add(category)
            }
        }
        categoriesAdapter.submitList(temp)
    }

}