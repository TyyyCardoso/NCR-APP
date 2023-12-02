package ipt.lei.dam.ncrapp.models

import java.time.LocalDateTime
import java.util.Date

data class EventResponse(
    val eventId: Int,
    val name: String,
    val description: String,
    val date: LocalDateTime,
    val location: String,
    val transport: Boolean,
    val createAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val image: String
)
