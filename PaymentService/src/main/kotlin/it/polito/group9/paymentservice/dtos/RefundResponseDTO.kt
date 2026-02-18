package it.polito.group9.paymentservice.dtos

import java.math.BigDecimal
import java.util.UUID

data class RefundResponseDTO(
    val paymentOrderId: UUID,
    val reservationId: Long,
    val paypalOrderId: String,
    val paypalRefundId: String,
    val status: String?,
    val refundedAmount: BigDecimal?
)
