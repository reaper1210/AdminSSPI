package com.swamisamarthpet.adminsspi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swamisamarthpet.adminsspi.data.repository.MachineRepository
import com.swamisamarthpet.adminsspi.data.util.MachineApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MachineViewModel
@Inject constructor(private val machineRepository: MachineRepository): ViewModel(){

    private val _Machine_apiStateFlow: MutableStateFlow<MachineApiState> = MutableStateFlow(MachineApiState.EmptyGetAllMachines)
    val machineApiStateFlow: StateFlow<MachineApiState> = _Machine_apiStateFlow

    fun getAllMachines(categoryName:String) = viewModelScope.launch {
        machineRepository.getAllMachines(categoryName)
            .onStart {
                _Machine_apiStateFlow.value = MachineApiState.LoadingGetAllMachines
            }
            .catch { e ->
                _Machine_apiStateFlow.value = MachineApiState.FailureGetAllMachines(e)
            }
            .collect {response->
                _Machine_apiStateFlow.value = MachineApiState.SuccessGetAllMachines(response)
            }

    }

    fun getMachineById(machineId: Int, categoryName: String) = viewModelScope.launch{
        machineRepository.getMachineById(machineId,categoryName)
            .onStart {
                _Machine_apiStateFlow.value = MachineApiState.LoadingGetMachineById
            }
            .catch { e->
                _Machine_apiStateFlow.value = MachineApiState.FailureGetMachineById(e)
            }
            .collect{ response->
                _Machine_apiStateFlow.value = MachineApiState.SuccessGetMachineById(response)
            }
    }

}