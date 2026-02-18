package it.polito.group9.paymentservice.dtos

import java.math.BigDecimal

data class CreateOrderRequestDTO(
    val clientId: Long,
    val reservationId: Long,
    val amount: BigDecimal
)
