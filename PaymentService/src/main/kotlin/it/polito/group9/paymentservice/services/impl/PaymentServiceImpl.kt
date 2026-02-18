package it.polito.group9.paymentservice.services.impl

import com.paypal.core.PayPalHttpClient
import com.paypal.orders.*
import com.paypal.orders.OrdersGetRequest
import com.paypal.payments.CapturesRefundRequest
import com.paypal.payments.Money
import com.paypal.payments.Refund
import com.paypal.payments.RefundRequest as PpRefundRequest
import it.polito.group9.paymentservice.config.PayPalProperties
import it.polito.group9.paymentservice.dtos.*
import it.polito.group9.paymentservice.kafka.PaymentStatus
import it.polito.group9.paymentservice.model.PaymentOrder
import it.polito.group9.paymentservice.repositories.*
import it.polito.group9.paymentservice.services.OutboxService
import it.polito.group9.paymentservice.services.PaymentService
import jakarta.transaction.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentServiceImpl(
    private val payPalClient: PayPalHttpClient,
    private val props: PayPalProperties,
    private val orderRepo: PaymentInfoRepository,
    private val outboxService: OutboxService,
) : PaymentService {
  private val logger = LoggerFactory.getLogger(PaymentService::class.java)

  // ---------------- Create order ----------------
  @Transactional
  override fun createOrder(
      clientId: Long,
      reservationId: Long,
      amount: BigDecimal
  ): CreateOrderResponseDTO {
    // 1. Fetch BillingInfo for client
    //        val billing: BillingInfo = billingRepo.findFirstByCustomerId(clientId)
    //            ?: throw IllegalArgumentException("No billing info found for customer $clientId")

    // 2. Persist PaymentOrder with status CREATED
    val paymentOrder =
        orderRepo.save(
            PaymentOrder(
                reservationId = reservationId,
                amount = amount,
                currency = props.currency,
                status = "CREATED"))

    // 3. Create PayPal order
    val payPalOrder = createPayPalOrder(amount)

    // 4. Update PaymentOrder with PayPal id and approval URL
    val approvalUrl = payPalOrder.links().first { it.rel() == "approve" }.href()
    paymentOrder.paypalOrderId = payPalOrder.id()
    paymentOrder.approvalUrl = approvalUrl
    orderRepo.save(paymentOrder)

    // 5. Publish Outbox event
    outboxService.publishPaymentEvent(paymentOrder, PaymentStatus.CREATED)
    logger.info("Payment order created")
    // 6. Return DTO
    logger.info("Approval url: $approvalUrl")
    return CreateOrderResponseDTO(paymentOrder.id, payPalOrder.id(), approvalUrl)
  }

  // ---------------- Capture order (PayPal return URL) ----------------
  @Transactional
  override fun captureOrder(paypalOrderId: String): CaptureOrderResponseDTO {
    logger.info("${paypalOrderId} capture order ${paypalOrderId}")
    val paymentOrder =
        orderRepo.findByPaypalOrderId(paypalOrderId)
            ?: throw IllegalArgumentException("Unknown PayPal order $paypalOrderId")

    val captureRequest = OrdersCaptureRequest(paypalOrderId)
    val captureResponse = payPalClient.execute(captureRequest)
    val captured = captureResponse.result()
    logger.info("${captured}")
    // Update status
    paymentOrder.status = captured.status()
    paymentOrder.updated = OffsetDateTime.now()
    orderRepo.save(paymentOrder)

    // Outbox - mappa lo status PayPal all'enum PaymentStatus
    val paymentStatus =
        when (captured.status()) {
          "COMPLETED" -> PaymentStatus.COMPLETED
          "CANCELLED" -> PaymentStatus.CANCELLED
          "FAILED" -> PaymentStatus.FAILED
          else -> PaymentStatus.FAILED // fallback per stati non riconosciuti
        }
    outboxService.publishPaymentEvent(paymentOrder, paymentStatus)
    logger.info("Order captured ${captured}")
    return CaptureOrderResponseDTO(paymentOrder.id, paypalOrderId, captured.status())
  }

  // ---------------- Cancel order (from cancel URL) ----------------
  @Transactional
  override fun cancelOrder(paypalOrderId: String) {
    val paymentOrder = orderRepo.findByPaypalOrderId(paypalOrderId) ?: return // nothing to do
    paymentOrder.status = "CANCELLED"
    paymentOrder.updated = OffsetDateTime.now()
    orderRepo.save(paymentOrder)
    outboxService.publishPaymentEvent(paymentOrder, PaymentStatus.CANCELLED)
  }

  // ---------------- Get order by internal id ----------------
  override fun getOrder(paymentOrderId: UUID): OrderDetailsResponseDTO {
    val po = orderRepo.findById(paymentOrderId).orElseThrow()
    return OrderDetailsResponseDTO(po.id, po.paypalOrderId ?: "", po.status)
  }

  // ---------------- Get approval URL by reservation ID ----------------
  override fun getApprovalUrlForReservation(reservationId: Long): String? {
    val paymentOrder = orderRepo.findByReservationId(reservationId)
    return paymentOrder?.approvalUrl
  }

  // ---------------- Helpers ----------------
  private fun createPayPalOrder(amount: BigDecimal): Order {
    val amt = AmountWithBreakdown().currencyCode(props.currency).value(amount.toPlainString())
    val purchaseUnit = PurchaseUnitRequest().amountWithBreakdown(amt)
    val appCtx = ApplicationContext().returnUrl(props.returnUrl).cancelUrl(props.cancelUrl)
    val orderRequest =
        OrderRequest()
            .checkoutPaymentIntent("CAPTURE")
            .purchaseUnits(listOf(purchaseUnit))
            .applicationContext(appCtx)
    val response = payPalClient.execute(OrdersCreateRequest().requestBody(orderRequest))
    return response.result()
  }

  @Transactional
  override fun refundByReservation(reservationId: Long, amount: BigDecimal?): RefundResponseDTO {
    val po =
        orderRepo.findByReservationId(reservationId)
            ?: throw IllegalArgumentException("No payment order for reservation $reservationId")

    val paypalOrderId =
        po.paypalOrderId
            ?: throw IllegalStateException("Reservation $reservationId has no PayPal order id")

    val captureId =
        getLatestCaptureId(paypalOrderId)
            ?: throw IllegalStateException(
                "No capture found on PayPal order $paypalOrderId (is it captured?)")

    val req = CapturesRefundRequest(captureId)
    if (amount != null) {
      req.requestBody(
          PpRefundRequest().amount(Money().currencyCode(po.currency).value(amount.toPlainString())))
    }

    val resp = payPalClient.execute(req)
    val refund: Refund = resp.result()

    // Mappa lo status del rimborso PayPal agli stati interni e all'enum PaymentStatus
    val (internalStatus, paymentStatus) =
        when (refund.status()) {
          "COMPLETED" -> {
            if (amount == null || amount.compareTo(po.amount) == 0) {
              "REFUNDED" to PaymentStatus.REFUNDED
            } else {
              "PARTIALLY_REFUNDED" to PaymentStatus.PARTIALLY_REFUNDED
            }
          }
          "PENDING" -> "REFUND_PENDING" to PaymentStatus.REFUND_PENDING
          else -> po.status to PaymentStatus.FAILED // keep current if unknown, assume failed
        }

    po.status = internalStatus
    po.updated = OffsetDateTime.now()
    orderRepo.save(po)

    // Pubblica evento tipizzato usando OutboxService
    outboxService.publishPaymentEvent(po, paymentStatus)

    val refundedAmount = refund.amount()?.value()?.let { BigDecimal(it) }

    return RefundResponseDTO(
        paymentOrderId = po.id,
        reservationId = reservationId,
        paypalOrderId = paypalOrderId,
        paypalRefundId = refund.id(),
        status = refund.status(),
        refundedAmount = refundedAmount)
  }

  /** Pull the latest capture id from a PayPal order */
  private fun getLatestCaptureId(paypalOrderId: String): String? {
    val getResp = payPalClient.execute(OrdersGetRequest(paypalOrderId))
    val order = getResp.result()

    val captures =
        order.purchaseUnits()?.flatMap { pu -> pu.payments()?.captures() ?: emptyList() }
            ?: emptyList()

    return captures.lastOrNull()?.id()
  }
}
