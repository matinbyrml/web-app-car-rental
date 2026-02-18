package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import it.polito.group9.reservationservice.dtos.ReservationDTO
import it.polito.group9.reservationservice.dtos.ReturnInspectionRequestDTO
import it.polito.group9.reservationservice.entities.AvailabilityStatus
import it.polito.group9.reservationservice.entities.CleanlinessStatus
import it.polito.group9.reservationservice.entities.Reservation
import it.polito.group9.reservationservice.entities.ReservationNote
import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.entities.Vehicle
import it.polito.group9.reservationservice.exceptions.ReservationNotFoundException
import it.polito.group9.reservationservice.exceptions.VehicleNotAvailableException
import it.polito.group9.reservationservice.exceptions.VehicleNotFoundException
import it.polito.group9.reservationservice.repositories.ReservationNoteRepository
import it.polito.group9.reservationservice.repositories.ReservationRepository
import it.polito.group9.reservationservice.repositories.VehicleRepository
import it.polito.group9.reservationservice.services.MaintenanceService
import it.polito.group9.reservationservice.services.OutboxService
import it.polito.group9.reservationservice.services.ReservationService
import jakarta.persistence.criteria.Predicate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of the `ReservationService` interface. Provides business logic for managing
 * reservations in the system.
 *
 * @property reservationRepository The repository used to interact with the reservation database.
 */
@Service
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val vehicleRepository: VehicleRepository,
    private val outboxService: OutboxService,
    private val reservationNoteRepository: ReservationNoteRepository,
    private val maintenanceService: MaintenanceService,
) : ReservationService {
  private val logger = LoggerFactory.getLogger(ReservationServiceImpl::class.java)

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
  override fun getAllReservations(
      startDate: String?,
      endDate: String?,
      userId: Long?,
      vehicleId: Long?,
      page: Int,
      size: Int
  ): Page<ReservationDTO> {
    // 1) Build your spec as before
    val spec =
        Specification<Reservation> { root, _, cb ->
          val preds = mutableListOf<Predicate>()
          startDate?.let { preds += cb.greaterThanOrEqualTo(root.get<String>("startDate"), it) }
          endDate?.let { preds += cb.lessThanOrEqualTo(root.get<String>("endDate"), it) }
          userId?.let { preds += cb.equal(root.get<Long>("userId"), it) }
          vehicleId?.let { preds += cb.equal(root.get<Vehicle>("vehicle").get<Long>("id"), it) }
          cb.and(*preds.toTypedArray())
        }

    // 2) Fetch ALL matching rows into a List
    val all: List<Reservation> = reservationRepository.findAll(spec)

    // 3) Map to DTOs
    val dtos: List<ReservationDTO> = all.map { it.toDTO() }

    // 4) Manually slice out the sub-list for this page
    val pageable = PageRequest.of(page, size)
    val startIdx = pageable.offset.toInt().coerceAtMost(dtos.size)
    val endIdx = (startIdx + pageable.pageSize).coerceAtMost(dtos.size)
    val pageContent = if (startIdx < endIdx) dtos.subList(startIdx, endIdx) else emptyList()

    // 5) Create a PageImpl carrying content + metadata
    return PageImpl(pageContent, pageable, dtos.size.toLong())
  }

  /**
   * Retrieves a reservation by its unique identifier.
   *
   * @param id The unique identifier of the reservation.
   * @return The `ReservationDTO` object representing the reservation.
   * @throws ReservationNotFoundException if no reservation is found with the given ID.
   */
  @Transactional
  override fun getReservationById(id: Long): ReservationDTO =
      reservationRepository
          .findById(id)
          .orElseThrow { ReservationNotFoundException("Reservation with id $id not found") }
          .toDTO()

  /**
   * Creates a new reservation in the system.
   *
   * @param dto The `ReservationDTO` containing the reservation details.
   * @param vehicle The `Vehicle` entity associated with the reservation.
   * @return The created `ReservationDTO` object.
   */
  @Transactional
  override fun createReservation(dto: ReservationDTO, vehicle: Vehicle): ReservationDTO {
    logger.info("Creating reservation: $dto" + " for vehicle: ${vehicle.id}")
    vehicleRepository.findAvailableVehicle(vehicle.id!!, dto.startDate, dto.endDate)
        ?: throw VehicleNotAvailableException("Vehicle with id ${vehicle.id} is not available")

    val savedReservation = reservationRepository.save(dto.toEntity().copy(vehicle = vehicle))
    val amount = calculateAmount(savedReservation)
    outboxService.publishReservationEvent(savedReservation, ReservationStatus.PENDING, amount)

    return savedReservation.toDTO()
  }

  /**
   * Updates an existing reservation in the system.
   *
   * @param id The unique identifier of the reservation to update.
   * @param dto The `ReservationDTO` containing the updated reservation details.
   * @param vehicle The `Vehicle` entity associated with the reservation.
   * @return The updated `ReservationDTO` object.
   * @throws ReservationNotFoundException if no reservation is found with the given ID.
   */
  @Transactional
  override fun updateReservation(id: Long, dto: ReservationDTO): ReservationDTO {
    logger.info("Updating reservation: $dto")
    val reservation =
        reservationRepository.findById(id).orElseThrow {
          ReservationNotFoundException("Reservation with id $id not found")
        }

    val vehicle: Vehicle =
        vehicleRepository.findById(dto.vehicleId).orElseThrow {
          VehicleNotFoundException("Vehicle with id ${dto.vehicleId} not found")
        }

    val savedReservation =
        reservationRepository.save(
            reservation.copy(
                startDate = dto.startDate,
                endDate = dto.endDate,
                vehicle = vehicle,
                userId = dto.userId))

    return savedReservation.toDTO()
  }

  /**
   * Updates the status of an existing reservation.
   *
   * @param id The unique identifier of the reservation to update.
   * @param status The new status to set for the reservation.
   * @return The updated `ReservationDTO` object after the status change.
   * @throws ReservationNotFoundException if no reservation is found with the given ID.
   */
  @Transactional
  override fun updateReservationByStatus(id: Long, status: ReservationStatus): ReservationDTO {
    logger.info("Updating reservation with id: $id to status: $status")
    val reservation =
        reservationRepository.findById(id).orElseThrow {
          ReservationNotFoundException("Reservation with id $id not found")
        }

    val savedReservation = reservationRepository.save(reservation.copy(status = status))
    // Se passiamo a PICKED_UP, crea una ReservationNote iniziale
    if (status == ReservationStatus.PICKED_UP) {
      val veh =
          savedReservation.vehicle
              ?: vehicleRepository.findById(savedReservation.vehicle!!.id!!).orElse(null)
      if (veh != null) {
        reservationNoteRepository.save(
            ReservationNote(
                reservation = savedReservation,
                vehicle = veh,
                startAt = OffsetDateTime.now(),
                kmAtPickup = veh.km,
            ))
        // segna veicolo come RENTED
        veh.availability = AvailabilityStatus.RENTED
        vehicleRepository.save(veh)
      }
    }
    outboxService.publishReservationEvent(savedReservation, status)

    return savedReservation.toDTO()
  }

  /**
   * Cancels a reservation with intelligent refund handling. If the reservation is PAID, PICKED_UP,
   * or RETURNED, it will be marked as CANCELLED_PENDING_REFUND and a refund request will be sent to
   * PaymentService. If the reservation is PENDING, it will be simply marked as CANCELLED.
   */
  @Transactional
  override fun cancelReservationWithRefund(id: Long): ReservationDTO {
    logger.info("Cancelling reservation with id: $id")
    val reservation =
        reservationRepository.findById(id).orElseThrow {
          ReservationNotFoundException("Reservation with id $id not found")
        }

    val targetStatus =
        when (reservation.status) {
          ReservationStatus.PAID,
          ReservationStatus.PICKED_UP,
          ReservationStatus.RETURNED -> {
            // Prenotazioni pagate richiedono rimborso
            logger.info("Reservation $id was paid - will request refund")
            ReservationStatus.CANCELLED_PENDING_REFUND
          }
          ReservationStatus.PENDING -> {
            // Prenotazioni non pagate possono essere cancellate direttamente
            logger.info("Reservation $id was not paid - cancelling directly")
            ReservationStatus.CANCELLED
          }
          ReservationStatus.CANCELLED,
          ReservationStatus.CANCELLED_PENDING_REFUND -> {
            // Già cancellata
            logger.info("Reservation $id is already cancelled")
            reservation.status!!
          }
          null -> {
            // Handle null case - should not happen but required for exhaustive when
            logger.warn("Reservation $id has null status, defaulting to CANCELLED")
            ReservationStatus.CANCELLED
          }
        }

    val savedReservation = reservationRepository.save(reservation.copy(status = targetStatus))
    outboxService.publishReservationEvent(savedReservation, targetStatus)

    return savedReservation.toDTO()
  }

  /**
   * Finalizza il processo di restituzione di una prenotazione, aggiornando la nota di ispezione e
   * lo stato del veicolo.
   *
   * @param id L'ID della prenotazione da aggiornare.
   * @param req I dettagli della richiesta di ispezione al ritorno.
   * @return L'oggetto `ReservationDTO` aggiornato dopo la restituzione.
   * @throws ReservationNotFoundException se non viene trovata alcuna prenotazione con l'ID fornito.
   * @throws IllegalStateException se la prenotazione non è nello stato appropriato per essere
   *   restituita.
   */
  @Transactional
  override fun finishReturn(id: Long, req: ReturnInspectionRequestDTO): ReservationDTO {
    val reservation =
        reservationRepository.findById(id).orElseThrow {
          ReservationNotFoundException("Reservation with id $id not found")
        }
    if (reservation.status != ReservationStatus.PICKED_UP) {
      throw IllegalStateException("Reservation $id must be PICKED_UP to finish return")
    }
    val vehicle =
        reservation.vehicle ?: throw IllegalStateException("Reservation $id has no vehicle")
    // chiudi/aggiorna l'ultima nota
    val note =
        reservationNoteRepository.findFirstByReservationIdOrderByIdDesc(id)
            ?: ReservationNote(
                reservation = reservation,
                vehicle = vehicle,
                startAt = OffsetDateTime.now(),
                kmAtPickup = vehicle.km,
            )
    note.endAt = OffsetDateTime.now()
    note.kmAtReturn = req.kmAtReturn
    vehicle.km = req.kmAtReturn
    note.cleanliness = req.cleanliness
    note.needsMaintenance = req.needsMaintenance
    note.damages = (req.damages?.toMutableList() ?: mutableListOf())
    reservationNoteRepository.save(note)

    // Penalità e richiesta maintenance
    val dirtyOrDamaged =
        req.cleanliness == CleanlinessStatus.DIRTY ||
            !req.damages.isNullOrEmpty() ||
            req.needsMaintenance
    if (dirtyOrDamaged) {
      // TODO: Penalizzare lo score dell'utente (integrare con UserService se disponibile)
      // userService.decreaseScore(reservation.userId)
      // TODO: Richiedi una maintenance programmata (integrare con MaintenanceService)
      // maintenanceService.requestMaintenance(vehicle)
    }

    vehicle.pendingCleaning = req.cleanliness != CleanlinessStatus.CLEAN
    vehicle.pendingRepair = !req.damages.isNullOrEmpty() || req.needsMaintenance == true
    vehicle.availability =
        if (vehicle.pendingRepair || vehicle.pendingCleaning) AvailabilityStatus.MAINTENANCE
        else AvailabilityStatus.AVAILABLE
    vehicleRepository.save(vehicle)

    if (vehicle.pendingRepair || vehicle.pendingCleaning) {
      // Calcola la data della prossima reservation futura per il veicolo
      val now = java.time.LocalDate.now()
      val nextReservation =
          reservationRepository.findFirstByVehicleIdAndStartDateAfterAndStatusInOrderByStartDateAsc(
              vehicle.id!!,
              now.toString(),
              listOf(ReservationStatus.PENDING, ReservationStatus.PAID))
      val upcomingServiceNeeds =
          if (nextReservation != null) {
            // startDate è in formato String ISO, lo converto a LocalDate
            val nextStart = java.time.LocalDate.parse(nextReservation.startDate.split("T")[0])
            java.sql.Date.valueOf(nextStart.minusDays(1))
          } else {
            java.sql.Date.valueOf(now.plusDays(1))
          }
      val maintenanceDTO =
          MaintenanceRecordDTO(
              id = null,
              pastDefects = req.damages?.joinToString(", ") ?: "",
              completedMaintenance = "",
              upcomingServiceNeeds = upcomingServiceNeeds,
              date = java.util.Date(),
              vehicleId = vehicle.id!!,
              maintenanceTypes = listOf<String>(),
              requiresFurtherMaintenance = vehicle.pendingRepair,
              requiresFurtherCleaning = vehicle.pendingCleaning)
      maintenanceService.createMaintenanceRecord(maintenanceDTO, vehicle)
    }

    reservation.status = ReservationStatus.RETURNED
    reservationRepository.save(reservation)
    outboxService.publishReservationEvent(reservation, ReservationStatus.RETURNED)

    return reservation.toDTO()
  }

  private fun calculateAmount(reservation: Reservation): Long {
    val days =
        ChronoUnit.DAYS.between(
            OffsetDateTime.parse(reservation.startDate + 'Z'),
            OffsetDateTime.parse(reservation.endDate + 'Z'),
        )
    val pricePerDay = reservation.vehicle?.vehicleModel?.price?.toLong() ?: 0L

    return days * pricePerDay
  }
}
