package it.polito.group9.paymentservice.services

import it.polito.group9.paymentservice.dtos.*
import java.math.BigDecimal
import java.util.*

/**
 * Contract for all payment-related operations.
 *
 * • createOrder – creates a local PaymentOrder, calls PayPal, returns approval URL • captureOrder –
 * captures an already-authorized PayPal order (triggered by return URL) • cancelOrder – marks an
 * order as CANCELLED (triggered by cancel URL) • getOrder – inspects the current status by internal
 * UUID
 */
interface PaymentService {

  /**
   * Create a new payment order for the given client and reservation.
   *
   * @param clientId the customer id (foreign key to UserManagementService)
   * @param reservationId the reservation in ReservationService we’re paying for
   * @param amount total amount to charge
   * @return DTO with internal paymentOrderId, PayPal orderId, approval URL
   */
  fun createOrder(clientId: Long, reservationId: Long, amount: BigDecimal): CreateOrderResponseDTO

  /**
   * Complete the flow after PayPal redirects to the return URL.
   *
   * @param paypalOrderId the PayPal order id (comes in the `token` query param)
   */
  fun captureOrder(paypalOrderId: String): CaptureOrderResponseDTO

  /**
   * Handle PayPal cancel link.
   *
   * @param paypalOrderId the PayPal order id (comes in the `token` query param)
   */
  fun cancelOrder(paypalOrderId: String)

  /** Retrieve order details by our internal UUID. */
  fun getOrder(paymentOrderId: UUID): OrderDetailsResponseDTO

  /**
   * Get approval URL for a reservation.
   *
   * @param reservationId the reservation ID to get the approval URL for
   * @return the PayPal approval URL if available
   */
  fun getApprovalUrlForReservation(reservationId: Long): String?

  fun refundByReservation(reservationId: Long, amount: BigDecimal? = null): RefundResponseDTO
}
