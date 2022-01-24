package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.Category

sealed class CategoryApiState {
    object EmptyGetAllMachines:CategoryApiState()
    class FailureGetAllMachines(val msg:Throwable) : CategoryApiState()
    class SuccessGetAllMachines(val data: List<Category>) : CategoryApiState()
    object LoadingGetAllMachines: CategoryApiState()
    object EmptyDeleteCategory:CategoryApiState()
    class FailureDeleteCategory(val msg:Throwable) : CategoryApiState()
    class SuccessDeleteCategory(val data: Int) : CategoryApiState()
    object LoadingDeleteCategory: CategoryApiState()
    object EmptyInsertCategory:CategoryApiState()
    class FailureInsertCategory(val msg:Throwable) : CategoryApiState()
    class SuccessInsertCategory(val data: Int) : CategoryApiState()
    object LoadingInsertCategory: CategoryApiState()
    object EmptyUpdateCategory:CategoryApiState()
    class FailureUpdateCategory(val msg:Throwable) : CategoryApiState()
    class SuccessUpdateCategory(val data: Int) : CategoryApiState()
    object LoadingUpdateCategory: CategoryApiState()
}