package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.Part

sealed class PartApiState {

    object Empty:PartApiState()

    class FailureGetAllParts(val msg:Throwable) : PartApiState()
    class SuccessGetAllParts(val data: List<HashMap<String,String>>) : PartApiState()
    object LoadingGetAllParts: PartApiState()

    class FailureGetPartById(val msg:Throwable) : PartApiState()
    class SuccessGetPartById(val data: Part): PartApiState()
    object LoadingGetPartById: PartApiState()

    class FailureInsertPart(val msg:Throwable) : PartApiState()
    class SuccessInsertPart(val data: Int) : PartApiState()
    object LoadingInsertPart: PartApiState()

    class FailureUpdatePart(val msg:Throwable) : PartApiState()
    class SuccessUpdatePart(val data: Int): PartApiState()
    object LoadingUpdatePart: PartApiState()

    class FailureDeletePart(val msg:Throwable) : PartApiState()
    class SuccessDeletePart(val data: Int) : PartApiState()
    object LoadingDeletePart: PartApiState()

    class FailureMarkPartAsPopular(val msg:Throwable) : PartApiState()
    class SuccessMarkPartAsPopular(val data: Int) : PartApiState()
    object LoadingMarkPartAsPopular: PartApiState()

}