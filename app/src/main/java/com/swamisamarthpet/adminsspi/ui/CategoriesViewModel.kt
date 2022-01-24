package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.CategoriesRepository
import com.swamisamarthpet.adminsspi.data.util.CategoryApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel
@Inject constructor(private val categoriesRepository: CategoriesRepository): ViewModel() {

    private val _Category_apiStateFlow: MutableStateFlow<CategoryApiState> = MutableStateFlow(CategoryApiState.EmptyGetAllMachines)
    val categoryApiStateFlow: StateFlow<CategoryApiState> = _Category_apiStateFlow

    fun getAllCategories() = viewModelScope.launch {
        categoriesRepository.getAllCategories()
            .onStart {
                _Category_apiStateFlow.value = CategoryApiState.LoadingGetAllMachines
            }
            .catch { e ->
                _Category_apiStateFlow.value = CategoryApiState.FailureGetAllMachines(e)
            }
            .collect {response->
                _Category_apiStateFlow.value = CategoryApiState.SuccessGetAllMachines(response)
            }
    }

    fun insertCategory(categoryName: String, categoryImage: File) = viewModelScope.launch {
        categoriesRepository.insertCategory(categoryName, categoryImage)
            .onStart {
                _Category_apiStateFlow.value = CategoryApiState.LoadingInsertCategory
            }
            .catch {e->
                _Category_apiStateFlow.value = CategoryApiState.FailureInsertCategory(e)
            }
            .collect { response->
                _Category_apiStateFlow.value = CategoryApiState.SuccessInsertCategory(response)
            }
    }

    fun updateCategory(categoryId:Int, categoryImage: File) = viewModelScope.launch {
        categoriesRepository.updateCategory(categoryId,categoryImage)
            .onStart {
                _Category_apiStateFlow.value = CategoryApiState.LoadingUpdateCategory
            }
            .catch {e->
                _Category_apiStateFlow.value = CategoryApiState.FailureUpdateCategory(e)
            }
            .collect { response->
                _Category_apiStateFlow.value = CategoryApiState.SuccessUpdateCategory(response)
            }
    }

    fun deleteCategory(categoryId:Int) = viewModelScope.launch {
        categoriesRepository.deleteCategory(categoryId)
            .onStart {
                _Category_apiStateFlow.value = CategoryApiState.LoadingDeleteCategory
            }
            .catch {e->
                _Category_apiStateFlow.value = CategoryApiState.FailureDeleteCategory(e)
            }
            .collect {response->
                _Category_apiStateFlow.value = CategoryApiState.SuccessDeleteCategory(response)
            }
    }

}