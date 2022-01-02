package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.CategoriesRepository
import com.swamisamarthpet.adminsspi.data.util.CategoryApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

}