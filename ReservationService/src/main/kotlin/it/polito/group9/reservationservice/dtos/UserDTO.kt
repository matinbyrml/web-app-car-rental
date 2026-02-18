package it.polito.group9.reservationservice.dtos

import java.sql.Date

data class UserDTO(
    val id: Long,
    val username: String,
    val name: String,
    val surname: String,
    val ssn: String,
    val email: String,
    val password: String,
    val phone: String?,
    val address: String?,
    val dateOfBirth: Date?,
    val role: String,
    val createdDate: Date,
)
