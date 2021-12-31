package com.swamisamarthpet.adminsspi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId:String,
    val userName:String,
    val phoneNumber:String
)
