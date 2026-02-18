package it.polito.group9.paymentservice.dtos

import java.util.*

data class CaptureOrderResponseDTO(
    val paymentOrderId: UUID,
    val paypalOrderId: String,
    val status: String
)
