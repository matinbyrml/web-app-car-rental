package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.dtos.ReservationDTO
import it.polito.group9.reservationservice.dtos.ReturnInspectionRequestDTO
import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page

/**
 * Service interface for managing reservations in the system. Provides methods for CRUD operations
 * and retrieving reservations with optional filters.
 */
interface ReservationService {

  /**
   * Retrieves a paginated list of reservations based on optional filters.
   *
   * @param startDate Optional filter for the start date of reservations (inclusive).
   * @param endDate Optional filter for the end date of reservations (inclusive).
   * @param userId Optional filter for the user ID associated with the reservations.
   * @param vehicleId Optional filter for the vehicle ID associated with the reservations.
   * @param page The page number to retrieve (0-based index).
   * @param size The number of records per page.
   * @return A paginated list of `ReservationDTO` objects matching the filters.
   */
  fun getAllReservations(
      startDate: String?,
      endDate: String?,
      userId: Long?,
      vehicleId: Long?,
      page: Int,
      size: Int,
  ): Page<ReservationDTO>

  /**
   * Retrieves a reservation by its unique identifier.
   *
   * @param id The unique identifier of the reservation.
   * @return The `ReservationDTO` object representing the reservation.
   */
  fun getReservationById(id: Long): ReservationDTO

  /**
   * Creates a new reservation in the system.
   *
   * @param dto The `ReservationDTO` containing the reservation details.
   * @param vehicle The `Vehicle` entity associated with the reservation.
   * @return The created `ReservationDTO` object.
   */
  fun createReservation(dto: ReservationDTO, vehicle: Vehicle): ReservationDTO

  /**
   * Updates an existing reservation in the system.
   *
   * @param id The unique identifier of the reservation to update.
   * @param dto The `ReservationDTO` containing the updated reservation details.
   * @return The updated `ReservationDTO` object.
   */
  fun updateReservation(id: Long, dto: ReservationDTO): ReservationDTO

  /**
   * Updates the status of an existing reservation.
   *
   * @param id The unique identifier of the reservation to update.
   * @param status The new status to set for the reservation.
   * @return The updated `ReservationDTO` object after the status change.
   * @throws ReservationNotFoundException if no reservation is found with the given ID.
   */
  fun updateReservationByStatus(id: Long, status: ReservationStatus): ReservationDTO

  /**
   * Cancels a reservation with intelligent refund handling. Automatically determines if a refund is
   * needed based on the current status:
   * - PAID/PICKED_UP/RETURNED: Sets status to CANCELLED_PENDING_REFUND and triggers refund
   * - PENDING: Sets status to CANCELLED (no refund needed)
   *
   * @param id The unique identifier of the reservation to cancel.
   * @return The updated ReservationDTO object after cancellation.
   * @throws ReservationNotFoundException if no reservation is found with the given ID.
   */
  fun cancelReservationWithRefund(id: Long): ReservationDTO

  /**
   * Finalizes the return of a vehicle with inspection details.
   *
   * @param id The unique identifier of the reservation.
   * @param req The `ReturnInspectionRequestDTO` containing inspection and return details.
   * @return The updated `ReservationDTO` object after finalizing the return.
   * @throws ReservationNotFoundException if no reservation is found with the given ID.
   */
  fun finishReturn(id: Long, req: ReturnInspectionRequestDTO): ReservationDTO
}
