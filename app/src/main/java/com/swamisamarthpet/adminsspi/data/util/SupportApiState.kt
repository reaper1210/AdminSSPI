package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.User

sealed class SupportApiState {
    object Empty:SupportApiState()
    class Failure(val msg:Throwable) : SupportApiState()
    class SuccessGetAllUsers(val data: List<User>) : SupportApiState()
    object Loading : SupportApiState()
}