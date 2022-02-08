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

    fun insertPart(machineName: String, partName: String, partDetails: String, partImages: ArrayList<ByteArray>) = viewModelScope.launch {
        partRepository.insertPart(machineName,partName,partDetails,partImages)
            .onStart {
                _part_apiStateFlow.value = PartApiState.LoadingInsertPart
            }
            .catch { e->
                _part_apiStateFlow.value = PartApiState.FailureInsertPart(e)
            }
            .collect { response->
                _part_apiStateFlow.value = PartApiState.SuccessInsertPart(response)
            }
    }

    fun updatePart(machineName: String, partId: Int, partName: String, partDetails: String, partImages: ArrayList<ByteArray>) = viewModelScope.launch {
        partRepository.updatePart(machineName,partId,partName,partDetails,partImages)
            .onStart {
                _part_apiStateFlow.value = PartApiState.LoadingUpdatePart
            }
            .catch { e->
                _part_apiStateFlow.value = PartApiState.FailureUpdatePart(e)
            }
            .collect { response->
                _part_apiStateFlow.value = PartApiState.SuccessUpdatePart(response)
            }
    }

    fun deletePart(machineName: String, partId: Int) = viewModelScope.launch {
        partRepository.deletePart(machineName,partId)
            .onStart {
                _part_apiStateFlow.value = PartApiState.LoadingDeletePart
            }
            .catch { e->
                _part_apiStateFlow.value = PartApiState.FailureDeletePart(e)
            }
            .collect { response->
                _part_apiStateFlow.value = PartApiState.SuccessDeletePart(response)
            }
    }

}