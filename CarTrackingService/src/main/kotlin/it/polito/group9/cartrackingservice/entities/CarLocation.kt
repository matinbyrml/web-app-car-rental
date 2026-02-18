package it.polito.group9.cartrackingservice.entities

import jakarta.persistence.*

@Entity
@Table(name = "car_location")
data class CarLocation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(nullable = false) val carId: Long,
    @Column(nullable = false) val createdAt: String,
    @Column(nullable = false) val latitude: Double,
    @Column(nullable = false) val longitude: Double,
)
