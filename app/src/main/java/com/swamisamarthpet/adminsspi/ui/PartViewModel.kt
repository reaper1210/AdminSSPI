package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.PartRepository
import com.swamisamarthpet.adminsspi.data.util.PartApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartViewModel
@Inject constructor(private val partRepository: PartRepository): ViewModel() {

    private val _part_apiStateFlow: MutableStateFlow<PartApiState> = MutableStateFlow(PartApiState.Empty)
    val partApiStateFlow: StateFlow<PartApiState> = _part_apiStateFlow

    fun getAllParts(machineName: String) = viewModelScope.launch {
        partRepository.getAllParts(machineName)
            .onStart {
                _part_apiStateFlow.value = PartApiState.LoadingGetAllParts
            }
            .catch { e->
                _part_apiStateFlow.value = PartApiState.FailureGetAllParts(e)
            }
            .collect { response->
                _part_apiStateFlow.value = PartApiState.SuccessGetAllParts(response)
            }
    }

    fun getPartById(partId: Int, machineName: String) = viewModelScope.launch {
        partRepository.getPartById(partId,machineName)
            .onStart {
                _part_apiStateFlow.value = PartApiState.LoadingGetPartById
            }
            .catch { e->
                _part_apiStateFlow.value = PartApiState.FailureGetPartById(e)
            }
            .collect { response->
                _part_apiStateFlow.value = PartApiState.SuccessGetPartById(response)
            }
    }

}