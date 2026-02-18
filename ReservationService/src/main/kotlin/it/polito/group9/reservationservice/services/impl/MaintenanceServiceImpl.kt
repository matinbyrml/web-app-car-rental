package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import it.polito.group9.reservationservice.entities.AvailabilityStatus
import it.polito.group9.reservationservice.entities.MaintenanceRecord
import it.polito.group9.reservationservice.entities.MaintenanceStatus
import it.polito.group9.reservationservice.entities.Vehicle
import it.polito.group9.reservationservice.exceptions.MaintenanceRecordNotFoundException
import it.polito.group9.reservationservice.repositories.MaintenanceRepository
import it.polito.group9.reservationservice.repositories.VehicleRepository
import it.polito.group9.reservationservice.services.MaintenanceService
import jakarta.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

/**
 * Implementazione dell'interfaccia MaintenanceService.
 *
 * Questo servizio fornisce metodi per gestire i record di manutenzione, inclusi filtri, recupero,
 * aggiornamento e cancellazione dei record di manutenzione.
 */
@Service
class MaintenanceServiceImpl(
    private val maintenanceRepository: MaintenanceRepository,
    private val vehicleRepository: VehicleRepository
) : MaintenanceService {

  private val log = LoggerFactory.getLogger(MaintenanceServiceImpl::class.java)

  /** Recupera una lista paginata di record di manutenzione filtrati da vari criteri. */
  override fun getMaintenancesWithFilters(
      vehicleId: Long,
      startDate: String?,
      endDate: String?,
      startUpcomingServiceNeeds: String?,
      endUpcomingServiceNeeds: String?,
      page: Int,
      size: Int
  ): Page<MaintenanceRecordDTO> {
    val spec =
        Specification<MaintenanceRecord> { root, _, cb ->
          val predicates = mutableListOf<Predicate>()

          // Filtro per vehicle ID
          predicates.add(cb.equal(root.get<Vehicle>("vehicle").get<Long>("id"), vehicleId))

          // Filtri opzionali per intervalli di date
          startDate?.let { predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), it)) }
          endDate?.let { predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), it)) }
          startUpcomingServiceNeeds?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("upcomingServiceNeeds"), it))
          }
          endUpcomingServiceNeeds?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("upcomingServiceNeeds"), it))
          }

          cb.and(*predicates.toTypedArray())
        }

    log.debug("Filter Specification: {}", spec)
    val maintenanceRecordsPage = maintenanceRepository.findAll(spec, PageRequest.of(page, size))
    return maintenanceRecordsPage.map { it.toDTO() }
  }

  /** Recupera un record di manutenzione tramite il suo ID. */
  override fun getMaintenanceById(id: Long): MaintenanceRecordDTO {
    val maintenanceRecord =
        maintenanceRepository.findById(id).orElseThrow {
          MaintenanceRecordNotFoundException("MaintenanceRecord with id $id not found")
        }
    return maintenanceRecord.toDTO()
  }

  /** Aggiorna un record di manutenzione esistente con nuovi dati. */
  override fun updateMaintenanceRecord(
      dto: MaintenanceRecordDTO,
      vehicle: Vehicle
  ): MaintenanceRecordDTO {
    log.info("Updating maintenance record {}", dto)
    maintenanceRepository.findById(dto.id!!).orElseThrow {
      MaintenanceRecordNotFoundException("MaintenanceRecord with id ${dto.id} not found")
    }
    val saved = maintenanceRepository.save(dto.toEntity().copy(vehicle = vehicle))

    // Se la maintenance è COMPLETED o non richiede più cleaning/repair, sblocca sempre il veicolo
    if (saved.status == MaintenanceStatus.COMPLETED ||
        (!saved.requiresFurtherMaintenance || !saved.requiresFurtherCleaning)) {
      vehicle.pendingRepair = saved.requiresFurtherMaintenance
      vehicle.pendingCleaning = saved.requiresFurtherCleaning
      if (!vehicle.pendingRepair && !vehicle.pendingCleaning) {
        vehicle.availability = AvailabilityStatus.AVAILABLE
      }
      vehicleRepository.save(vehicle)
    }
    return saved.toDTO()
  }

  /** Cancella un record di manutenzione tramite il suo ID. */
  override fun deleteMaintenanceRecord(id: Long) {
    log.info("Deleting maintenance with id {}", id)
    val existing =
        maintenanceRepository.findById(id).orElseThrow {
          MaintenanceRecordNotFoundException("MaintenanceRecord with id $id not found")
        }
    maintenanceRepository.delete(existing)
  }

  /** Crea un nuovo record di manutenzione e lo associa al veicolo fornito. */
  override fun createMaintenanceRecord(
      dto: MaintenanceRecordDTO,
      vehicle: Vehicle
  ): MaintenanceRecordDTO {
    log.info("Creating new maintenance record {}", dto)
    val maintenanceRecord = dto.toEntity().copy(vehicle = vehicle)
    val saved = maintenanceRepository.save(maintenanceRecord)

    // Aggiorna lo stato del veicolo se non serve altra manutenzione o pulizia
    var updated = false
    if (!dto.requiresFurtherMaintenance) {
      vehicle.pendingRepair = false
      updated = true
    }
    if (!dto.requiresFurtherCleaning) {
      vehicle.pendingCleaning = false
      updated = true
    }
    if (updated) {
      vehicleRepository.save(vehicle)
    }

    return saved.toDTO()
  }
}
