package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.PopularProductRepository
import com.swamisamarthpet.adminsspi.data.util.PopularProductApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularProductViewModel
@Inject constructor(private val popularProductRepository: PopularProductRepository): ViewModel(){

    private val _popularProduct_apiStateFlow: MutableStateFlow<PopularProductApiState> = MutableStateFlow(PopularProductApiState.Empty)
    val popularProductApiStateFlow: StateFlow<PopularProductApiState> = _popularProduct_apiStateFlow

    fun getAllPopularProducts() = viewModelScope.launch {
        popularProductRepository.getAllPopularProducts()
            .onStart {
                _popularProduct_apiStateFlow.value = PopularProductApiState.LoadingGetAllPopularProducts
            }
            .catch { e ->
                _popularProduct_apiStateFlow.value = PopularProductApiState.FailureGetAllPopularProducts(e)
            }
            .collect {response->
                _popularProduct_apiStateFlow.value = PopularProductApiState.SuccessGetAllPopularProducts(response)
            }
    }

    fun getPopularProductById(productId: Int) = viewModelScope.launch{
        popularProductRepository.getPopularProductById(productId)
            .onStart {
                _popularProduct_apiStateFlow.value = PopularProductApiState.LoadingGetPopularProductById
            }
            .catch { e->
                _popularProduct_apiStateFlow.value = PopularProductApiState.FailureGetPopularProductById(e)
            }
            .collect{ response->
                _popularProduct_apiStateFlow.value = PopularProductApiState.SuccessGetPopularProductById(response)
            }
    }

    fun updatePopularProduct(productId: Int, productPopularity: Int,productName: String, productType: String, productDetails: String, productImages: ArrayList<ByteArray>,productPdf: ByteArray, youtubeVideoLink: String) = viewModelScope.launch{
        popularProductRepository.updatePopularProduct(productId, productPopularity, productName, productType, productDetails, productImages, productPdf, youtubeVideoLink)
            .onStart {
                _popularProduct_apiStateFlow.value = PopularProductApiState.LoadingUpdatePopularProduct
            }
            .catch { e->
                _popularProduct_apiStateFlow.value = PopularProductApiState.FailureUpdatePopularProduct(e)
            }
            .collect{ response->
                _popularProduct_apiStateFlow.value = PopularProductApiState.SuccessUpdatePopularProduct(response)
            }
    }

    fun deletePopularProduct(productId: Int) = viewModelScope.launch{
        popularProductRepository.deletePopularProduct(productId)
            .onStart {
                _popularProduct_apiStateFlow.value = PopularProductApiState.LoadingDeletePopularProduct
            }
            .catch { e->
                _popularProduct_apiStateFlow.value = PopularProductApiState.FailureDeletePopularProduct(e)
            }
            .collect{ response->
                _popularProduct_apiStateFlow.value = PopularProductApiState.SuccessDeletePopularProduct(response)
            }
    }

}