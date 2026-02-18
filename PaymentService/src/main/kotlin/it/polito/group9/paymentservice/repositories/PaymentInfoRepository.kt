package it.polito.group9.paymentservice.repositories

import it.polito.group9.paymentservice.model.PaymentOrder
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentInfoRepository : JpaRepository<PaymentOrder, UUID> {
  fun findByPaypalOrderId(paypalOrderId: String): PaymentOrder?

  fun findByReservationId(reservationId: Long): PaymentOrder?
}
