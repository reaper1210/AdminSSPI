package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.Machine

sealed class MachineApiState{
    object EmptyGetAllMachines:MachineApiState()
    class FailureGetAllMachines(val msg:Throwable) : MachineApiState()
    class SuccessGetAllMachines(val data: List<HashMap<String,String>>) : MachineApiState()
    object LoadingGetAllMachines : MachineApiState()
    class SuccessGetMachineById(val data: Machine): MachineApiState()
    object LoadingGetMachineById : MachineApiState()
    object EmptyGetMachineById:MachineApiState()
    class FailureGetMachineById(val msg:Throwable) : MachineApiState()
}