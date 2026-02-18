package it.polito.group9.paymentservice.dtos

import java.math.BigDecimal

data class RefundRequestDTO(val amount: BigDecimal? = null) // null => full refund
