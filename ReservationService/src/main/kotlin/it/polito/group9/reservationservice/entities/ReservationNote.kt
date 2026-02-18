package it.polito.group9.reservationservice.entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "reservation_note")
data class ReservationNote(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    val reservation: Reservation,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    val vehicle: Vehicle,
    @Column(name = "start_at", nullable = false) val startAt: OffsetDateTime,
    @Column(name = "end_at") var endAt: OffsetDateTime? = null,
    @Column(name = "km_at_pickup", nullable = false) val kmAtPickup: Int,
    @Column(name = "km_at_return") var kmAtReturn: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "cleanliness")
    var cleanliness: CleanlinessStatus? = null,
    @ElementCollection
    @CollectionTable(
        name = "reservation_note_notes", joinColumns = [JoinColumn(name = "reservation_note_id")])
    @Column(name = "note", columnDefinition = "TEXT")
    var notes: MutableList<String> = mutableListOf(),
    @Column(name = "needs_maintenance") var needsMaintenance: Boolean? = null,
    @ElementCollection
    @CollectionTable(
        name = "reservation_note_damages", joinColumns = [JoinColumn(name = "reservation_note_id")])
    @Column(name = "damage", columnDefinition = "TEXT")
    var damages: MutableList<String> = mutableListOf()
)
