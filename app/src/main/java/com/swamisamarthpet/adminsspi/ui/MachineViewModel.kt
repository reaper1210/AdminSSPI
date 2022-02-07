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

    fun deleteMachine(machineId: Int, categoryName: String) = viewModelScope.launch{
        machineRepository.deleteMachine(machineId,categoryName)
            .onStart {
                _Machine_apiStateFlow.value = MachineApiState.LoadingDeleteMachine
            }
            .catch { e->
                _Machine_apiStateFlow.value = MachineApiState.FailureDeleteMachine(e)
            }
            .collect{ response->
                _Machine_apiStateFlow.value = MachineApiState.SuccessDeleteMachine(response)
            }
    }

    fun updateMachine(categoryName:String, machineId:Int,machineName:String, machineDetails:String,
                      machinePDF:ByteArray, machineImages:ArrayList<ByteArray>, youtubeVideoLink:String) = viewModelScope.launch{
        machineRepository.updateMachine(categoryName, machineId, machineName, machineDetails, machinePDF, machineImages, youtubeVideoLink)
            .onStart {
                _Machine_apiStateFlow.value = MachineApiState.LoadingUpdateMachine
            }
            .catch { e->
                _Machine_apiStateFlow.value = MachineApiState.FailureUpdateMachine(e)
            }
            .collect{ response->
                _Machine_apiStateFlow.value = MachineApiState.SuccessUpdateMachine(response)
            }
    }

    fun insertMachine(categoryName:String, machineName:String, machineDetails:String,
                      machinePDF:ByteArray, machineImages:ArrayList<ByteArray>, youtubeVideoLink:String) = viewModelScope.launch{
        machineRepository.insertMachine(categoryName, machineName, machineDetails, machinePDF, machineImages, youtubeVideoLink)
            .onStart {
                _Machine_apiStateFlow.value = MachineApiState.LoadingInsertMachine
            }
            .catch { e->
                _Machine_apiStateFlow.value = MachineApiState.FailureInsertMachine(e)
            }
            .collect{ response->
                _Machine_apiStateFlow.value = MachineApiState.SuccessInsertMachine(response)
            }
    }

}