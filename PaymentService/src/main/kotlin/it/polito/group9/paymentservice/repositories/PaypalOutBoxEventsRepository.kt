package it.polito.group9.paymentservice.repositories

import it.polito.group9.paymentservice.model.PaypalOutboxEvent
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface PaypalOutBoxEventsRepository : JpaRepository<PaypalOutboxEvent, UUID> {}
