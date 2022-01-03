package com.swamisamarthpet.adminsspi.data.util

import com.swamisamarthpet.adminsspi.data.model.SupportMessage
import com.swamisamarthpet.adminsspi.data.model.User

sealed class SupportApiState {
    object EmptyGetAllUsers:SupportApiState()
    object EmptyGetAllMessages:SupportApiState()
    object EmptySendMessage:SupportApiState()
    object EmptyUpdateLastMessageTime: SupportApiState()
    object EmptyUpdateUnreadMessages: SupportApiState()
    class FailureGetAllUsers(val msg:Throwable): SupportApiState()
    class FailureGetAllMessages(val msg:Throwable): SupportApiState()
    class FailureSendMessage(val msg:Throwable): SupportApiState()
    class FailureUpdateLastMessageTime(val msg:Throwable): SupportApiState()
    class FailureUpdateUnreadMessages(val msg:Throwable): SupportApiState()
    class SuccessGetAllUsers(val data: List<User>): SupportApiState()
    class SuccessGetAllMessages(val data: List<SupportMessage>): SupportApiState()
    class SuccessSendMessage(val data: SupportMessage): SupportApiState()
    class SuccessUpdateLastMessageTime(val data:Int): SupportApiState()
    class SuccessUpdateUnreadMessages(val data:Int): SupportApiState()
    object LoadingGetAllUsers : SupportApiState()
    object LoadingGetAllMessages : SupportApiState()
    object LoadingSendMessage : SupportApiState()
    object LoadingUpdateLastMessageTime: SupportApiState()
    object LoadingUpdateUnreadMessages: SupportApiState()
}