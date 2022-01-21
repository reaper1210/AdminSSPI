package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.Part

sealed class PartApiState {
    object Empty:PartApiState()
    class FailureGetAllParts(val msg:Throwable) : PartApiState()
    class FailureGetPartById(val msg:Throwable) : PartApiState()
    class SuccessGetAllParts(val data: List<HashMap<String,String>>) : PartApiState()
    class SuccessGetPartById(val data: Part): PartApiState()
    object LoadingGetAllParts: PartApiState()
    object LoadingGetPartById: PartApiState()

}