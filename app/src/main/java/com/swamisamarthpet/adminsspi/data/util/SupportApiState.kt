package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.SupportMessage
import com.swamisamarthpet.adminsspi.data.model.User

sealed class SupportApiState {
    object EmptyGetAllUsers:SupportApiState()
    object EmptyGetAllMessages:SupportApiState()
    class FailureGetAllUsers(val msg:Throwable): SupportApiState()
    class FailureGetAllMessages(val msg:Throwable): SupportApiState()
    class SuccessGetAllUsers(val data: List<User>): SupportApiState()
    class SuccessGetAllMessages(val data: List<SupportMessage>): SupportApiState()
    object LoadingGetAllUsers : SupportApiState()
    object LoadingGetAllMessages : SupportApiState()
}