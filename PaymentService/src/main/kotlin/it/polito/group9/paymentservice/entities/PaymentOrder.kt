package it.polito.group9.paymentservice.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "payment_order")
data class PaymentOrder(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "reservation_id", nullable = false) val reservationId: Long,
    @Column(nullable = false, precision = 12, scale = 2) val amount: BigDecimal,
    @Column(nullable = false, length = 3) val currency: String = "EUR",
    @Column(nullable = false, length = 20)
    var status: String, // consider using enum + @Enumerated if you want
    @Column(name = "paypal_order_id", length = 100) var paypalOrderId: String? = null,
    @Column(name = "approval_url", length = 500) var approvalUrl: String? = null,
    @Column(name = "tms_created", nullable = false)
    val created: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "tms_updated", nullable = false)
    var updated: OffsetDateTime = OffsetDateTime.now()
)
