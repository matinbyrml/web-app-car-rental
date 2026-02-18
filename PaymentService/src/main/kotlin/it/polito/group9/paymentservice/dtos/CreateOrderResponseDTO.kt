package it.polito.group9.paymentservice.dtos

import java.util.*

data class CreateOrderResponseDTO(
    val paymentOrderId: UUID,
    val paypalOrderId: String,
    val approvalUrl: String
)
