package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.Machine

sealed class MachineApiState {

    object EmptyGetAllMachines : MachineApiState()
    class FailureGetAllMachines(val msg: Throwable) : MachineApiState()
    class SuccessGetAllMachines(val data: List<HashMap<String, String>>) : MachineApiState()
    object LoadingGetAllMachines : MachineApiState()
    class SuccessGetMachineById(val data: Machine) : MachineApiState()
    object LoadingGetMachineById : MachineApiState()
    object EmptyGetMachineById : MachineApiState()
    class FailureGetMachineById(val msg: Throwable) : MachineApiState()
    class SuccessDeleteMachine(val data: Int) : MachineApiState()
    object LoadingDeleteMachine : MachineApiState()
    class FailureDeleteMachine(val msg: Throwable) : MachineApiState()
    object EmptyDeleteMachine : MachineApiState()
    class SuccessInsertMachine(val data: Int) : MachineApiState()
    object LoadingInsertMachine : MachineApiState()
    class FailureInsertMachine(val msg: Throwable) : MachineApiState()
    object EmptyInsertMachine : MachineApiState()
    class SuccessUpdateMachine(val data: Int) : MachineApiState()
    object LoadingUpdateMachine : MachineApiState()
    class FailureUpdateMachine(val msg: Throwable) : MachineApiState()
    object EmptyUpdateMachine : MachineApiState()

}