package com.swamisamarthpet.adminsspi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val categoryId:Int,
    val categoryName:String,
    val categoryImage:String
)
