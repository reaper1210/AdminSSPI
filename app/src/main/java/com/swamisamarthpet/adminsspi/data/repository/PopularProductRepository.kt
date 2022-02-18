package com.swamisamarthpet.adminsspi.data.repository

import com.swamisamarthpet.adminsspi.data.model.PopularProduct
import com.swamisamarthpet.adminsspi.data.network.PopularProductApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PopularProductRepository
@Inject constructor(private val popularProductApiService: PopularProductApiService){

    fun getAllPopularProducts(): Flow<List<HashMap<String, String>>> = flow {
        emit(popularProductApiService.getAllPopularProducts())
    }.flowOn(Dispatchers.IO)

    fun getPopularProductById(productId: Int): Flow<PopularProduct> = flow{
        emit(popularProductApiService.getPopularProductById(productId))
    }.flowOn(Dispatchers.IO)

    fun updatePopularProduct(productId: Int, productPopularity: Int,productName: String, productType: String, productDetails: String, productImages: ArrayList<ByteArray>,productPdf: ByteArray, youtubeVideoLink: String): Flow<Int> = flow{
        emit(popularProductApiService.updatePopularProduct(productId, productPopularity, productName, productType, productDetails, productImages, productPdf, youtubeVideoLink))
    }.flowOn(Dispatchers.IO)

    fun deletePopularProduct(productId: Int): Flow<Int> = flow{
        emit(popularProductApiService.deletePopularProduct(productId))
    }.flowOn(Dispatchers.IO)

}