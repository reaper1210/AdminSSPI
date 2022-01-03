package com.swamisamarthpet.adminsspi.data.repository

import com.swamisamarthpet.adminsspi.data.model.SupportMessage
import com.swamisamarthpet.adminsspi.data.model.User
import com.swamisamarthpet.adminsspi.data.network.SupportApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SupportRepository
@Inject constructor(private val supportApiService: SupportApiService){

    fun getAllUsers(): Flow<List<User>> = flow{
        emit(supportApiService.getAllUsers())
    }.flowOn(Dispatchers.IO)

    fun getAllMessages(userId:String): Flow<List<SupportMessage>> = flow {
        emit(supportApiService.getAllMessages(userId))
    }.flowOn(Dispatchers.IO)

    fun sendMessage(userId:String,message: String): Flow<SupportMessage> = flow {
        emit(supportApiService.sendMessage(userId,message))
    }.flowOn(Dispatchers.IO)

    fun updateLastMessageTime(userId:String,time:String): Flow<Int> = flow {
        emit(supportApiService.updateLastMessageTime(userId,time))
    }.flowOn(Dispatchers.IO)

}