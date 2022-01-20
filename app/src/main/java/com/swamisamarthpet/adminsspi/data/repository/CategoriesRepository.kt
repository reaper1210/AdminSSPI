package com.swamisamarthpet.adminsspi.data.repository

import com.swamisamarthpet.adminsspi.data.model.Category
import com.swamisamarthpet.adminsspi.data.model.User
import com.swamisamarthpet.adminsspi.data.network.CategoriesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CategoriesRepository
@Inject constructor(private val categoriesApiService: CategoriesApiService){

    fun getAllCategories(): Flow<List<Category>> = flow{
        emit(categoriesApiService.getAllCategories())
    }.flowOn(Dispatchers.IO)

    fun deleteCategory(categoryId:Int): Flow<Int> = flow{
        emit(categoriesApiService.deleteCategory(categoryId))
    }.flowOn(Dispatchers.IO)

}