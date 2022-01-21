package com.swamisamarthpet.adminsspi.data.repository

import com.swamisamarthpet.adminsspi.data.model.Part
import com.swamisamarthpet.adminsspi.data.network.PartApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class PartRepository
@Inject constructor(private val partApiService: PartApiService)
{

    fun getAllParts(machineName: String): Flow<List<HashMap<String,String>>> = flow {
        emit(partApiService.getAllParts(machineName))
    }.flowOn(Dispatchers.IO)

    fun getPartById(partId: Int,machineName: String):Flow<Part> = flow {
        emit(partApiService.getPartById(partId, machineName))
    }.flowOn(Dispatchers.IO)

}