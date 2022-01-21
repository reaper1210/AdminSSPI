package com.swamisamarthpet.adminsspi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Part(
    val partId: Int,
    val partName: String,
    val partImages: String,
    val partDetails: String
)
