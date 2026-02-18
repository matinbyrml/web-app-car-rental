package it.polito.group9.paymentservice.controller

import it.polito.group9.paymentservice.dtos.*
import it.polito.group9.paymentservice.services.PaymentService
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class PaymentController(private val paymentService: PaymentService) {

  private val logger = LoggerFactory.getLogger(PaymentController::class.java)

  /* ---------- create order ---------- */
  @PreAuthorize("hasRole('reservation_service')")
  @PostMapping
  fun createOrder(@RequestBody req: CreateOrderRequestDTO): ResponseEntity<CreateOrderResponseDTO> {
    val response = paymentService.createOrder(req.clientId, req.reservationId, req.amount)
    return ResponseEntity.ok(response)
  }

  /* ---------- PayPal return ---------- */
  /** PayPal sends ?token=<paypalOrderId> */
  @GetMapping("/return")
  fun handleReturn(@RequestParam token: String, response: HttpServletResponse) {
    logger.info("Handling return {}", token)

    val capture = paymentService.captureOrder(token) // updates DB + emits outbox
    logger.info("Captured order {}", capture)

    /* 🔁 302 redirect to the React app */
    val target = "http://localhost:4173/reservations?paid=${capture.status.lowercase()}"
    response.sendRedirect(target) // 302 Location: <target>
  }

  /* ---------- PayPal cancel ---------- */
  @GetMapping("/cancel")
  fun handleCancel(@RequestParam token: String, response: HttpServletResponse) {
    paymentService.cancelOrder(token)
    response.sendRedirect("http://localhost:4173/reservations?paid=ko")
  }

  /* ---------- lookup by internal id ---------- */
  @GetMapping("/{paymentOrderId}")
  fun getOrder(@PathVariable paymentOrderId: UUID): ResponseEntity<OrderDetailsResponseDTO> =
      ResponseEntity.ok(paymentService.getOrder(paymentOrderId))

  /* ---------- get approval URL by reservation ---------- */
  @PreAuthorize("hasRole('CUSTOMER')")
  @GetMapping("/approval-url/{reservationId}")
  fun getApprovalUrl(@PathVariable reservationId: Long): ResponseEntity<Map<String, String?>> {
    val approvalUrl = paymentService.getApprovalUrlForReservation(reservationId)
    return ResponseEntity.ok(mapOf("approvalUrl" to approvalUrl))
  }

  @PostMapping("/refund/reservation/{reservationId}")
  fun refundByReservation(
      @PathVariable reservationId: Long,
      @RequestBody body: RefundRequestDTO
  ): ResponseEntity<RefundResponseDTO> =
      ResponseEntity.ok(paymentService.refundByReservation(reservationId, body.amount))
}
