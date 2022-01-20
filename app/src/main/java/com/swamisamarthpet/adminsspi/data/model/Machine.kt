package com.swamisamarthpet.adminsspi.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Machine(
    val machineId: Int,
    val machineName: String,
    val machineImages: String,
    val youtubeVideoLink: String,
    val machineDetails: String,
    val machinePdf: String
)
