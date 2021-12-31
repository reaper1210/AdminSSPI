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

    private val _support_apiStateFlow: MutableStateFlow<SupportApiState> = MutableStateFlow(SupportApiState.Empty)
    val supportApiStateFlow: StateFlow<SupportApiState> = _support_apiStateFlow

    fun getAllUsers() = viewModelScope.launch {
        supportRepository.getAllUsers()
            .onStart {
                _support_apiStateFlow.value = SupportApiState.Loading
            }
            .catch {e->
                _support_apiStateFlow.value = SupportApiState.Failure(e)
            }
            .collect {usersList->
                _support_apiStateFlow.value = SupportApiState.SuccessGetAllUsers(usersList)
            }
    }

}