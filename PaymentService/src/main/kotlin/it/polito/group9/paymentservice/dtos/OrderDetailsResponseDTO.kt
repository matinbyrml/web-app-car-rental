package it.polito.group9.paymentservice.dtos

import java.util.*

data class OrderDetailsResponseDTO(
    val paymentOrderId: UUID,
    val paypalOrderId: String,
    val status: String
)
