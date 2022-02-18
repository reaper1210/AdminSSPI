package com.swamisamarthpet.adminsspi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PopularProduct(
    val productId: Int,
    val productName: String,
    val productImages: String,
    val productDetails: String,
    val productPdf: String,
    val productType: String,
    val productPopularity: Int,
    val productYoutubeVideo: String
)
