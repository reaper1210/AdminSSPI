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

    fun insertPart(machineName: String, partName: String, partDetails: String, partImages: ArrayList<ByteArray>):Flow<Int> = flow {
        emit(partApiService.insertPart(machineName, partName, partDetails, partImages))
    }.flowOn(Dispatchers.IO)

    fun updatePart(machineName: String, partId: Int, partName: String, partDetails: String, partImages: ArrayList<ByteArray>):Flow<Int> = flow {
        emit(partApiService.updatePart(machineName, partId, partName, partDetails, partImages))
    }.flowOn(Dispatchers.IO)

    fun deletePart(machineName: String,partId: Int):Flow<Int> = flow {
        emit(partApiService.deletePart(machineName, partId))
    }.flowOn(Dispatchers.IO)

    fun markPartAsPopular(partName: String,partDetails: String,partPopularity: Int,partImages: ArrayList<ByteArray>):Flow<Int> = flow {
        emit(partApiService.markPartAsPopular(partName, partDetails, partPopularity, partImages))
    }.flowOn(Dispatchers.IO)

}