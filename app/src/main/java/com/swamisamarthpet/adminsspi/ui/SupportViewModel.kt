package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.SupportRepository
import com.swamisamarthpet.adminsspi.data.util.SupportApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportViewModel
@Inject constructor(private val supportRepository: SupportRepository):ViewModel(){

    private val _support_apiStateFlow: MutableStateFlow<SupportApiState> = MutableStateFlow(SupportApiState.EmptyGetAllUsers)
    val supportApiStateFlow: StateFlow<SupportApiState> = _support_apiStateFlow

    fun getAllUsers() = viewModelScope.launch {
        supportRepository.getAllUsers()
            .onStart {
                _support_apiStateFlow.value = SupportApiState.LoadingGetAllUsers
            }
            .catch {e->
                _support_apiStateFlow.value = SupportApiState.FailureGetAllUsers(e)
            }
            .collect {usersList->
                _support_apiStateFlow.value = SupportApiState.SuccessGetAllUsers(usersList)
            }
    }

    fun getAllMessages(userId: String) = viewModelScope.launch {
        supportRepository.getAllMessages(userId)
            .onStart {
                _support_apiStateFlow.value = SupportApiState.LoadingGetAllMessages
            }
            .catch { e->
                _support_apiStateFlow.value = SupportApiState.FailureGetAllMessages(e)
            }
            .collect { messageList ->
                _support_apiStateFlow.value = SupportApiState.SuccessGetAllMessages(messageList)
            }
    }

    fun sendMessage(userId: String,message: String) = viewModelScope.launch {
        supportRepository.sendMessage(userId,message)
            .onStart {
                _support_apiStateFlow.value = SupportApiState.LoadingSendMessage
            }
            .catch { e->
                _support_apiStateFlow.value = SupportApiState.FailureSendMessage(e)
            }
            .collect { supportMessage ->
                _support_apiStateFlow.value = SupportApiState.SuccessSendMessage(supportMessage)
            }
    }

    fun updateLastMessageTime(userId: String,time:String) = viewModelScope.launch {
        supportRepository.updateLastMessageTime(userId,time)
            .onStart {
                _support_apiStateFlow.value = SupportApiState.LoadingUpdateLastMessageTime
            }
            .catch { e->
                _support_apiStateFlow.value = SupportApiState.FailureUpdateLastMessageTime(e)
            }
            .collect { response ->
                _support_apiStateFlow.value = SupportApiState.SuccessUpdateLastMessageTime(response)
            }
    }

}