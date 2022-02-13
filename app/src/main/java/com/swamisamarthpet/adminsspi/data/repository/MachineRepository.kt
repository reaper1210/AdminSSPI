package com.swamisamarthpet.adminsspi.data.repository

import com.swamisamarthpet.adminsspi.data.model.Machine
import com.swamisamarthpet.adminsspi.data.network.MachineApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MachineRepository
@Inject constructor(private val machineApiService: MachineApiService)
{

    fun getAllMachines(categoryName:String): Flow<List<HashMap<String, String>>> = flow {
        emit(machineApiService.getAllMachines(categoryName))
    }.flowOn(Dispatchers.IO)

    fun getMachineById(machineId: Int,categoryName: String): Flow<Machine> = flow{
        emit(machineApiService.getMachineById(machineId,categoryName))
    }.flowOn(Dispatchers.IO)

    fun deleteMachine(machineId: Int,categoryName: String): Flow<Int> = flow {
        emit(machineApiService.deleteMachine(machineId,categoryName))
    }.flowOn(Dispatchers.IO)

    fun updateMachine(categoryName:String, machineId:Int,machineName:String, machineDetails:String,
                      machinePDF:ByteArray, machineImages:ArrayList<ByteArray>, youtubeVideoLink:String): Flow<Int> = flow {
        emit(machineApiService.updateMachine(categoryName, machineId, machineName, machineDetails, machinePDF, machineImages, youtubeVideoLink))
    }.flowOn(Dispatchers.IO)

    fun insertMachine(categoryName:String, machineName:String, machineDetails:String,
                      machinePDF:ByteArray, machineImages:ArrayList<ByteArray>, youtubeVideoLink:String): Flow<Int> = flow {
        emit(machineApiService.insertMachine(categoryName, machineName, machineDetails, machinePDF, machineImages, youtubeVideoLink))
    }.flowOn(Dispatchers.IO)

    fun markMachineAsPopular(machineName: String,machineDetails: String, machinePopularity:Int, machinePDF:ByteArray, machineImages: ArrayList<ByteArray>, youtubeVideoLink: String):Flow<Int> = flow {
        emit(machineApiService.markMachineAsPopular(machineName, machineDetails, machinePopularity, machinePDF, machineImages, youtubeVideoLink))
    }.flowOn(Dispatchers.IO)

}