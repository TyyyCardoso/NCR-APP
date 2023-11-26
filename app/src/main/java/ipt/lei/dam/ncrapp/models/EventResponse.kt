package ipt.lei.dam.ncrapp.models

import java.util.Date

data class EventResponse(
    val eventId: Int,
    val name: String,
    val description: String,
    val date: Date,
    val location: String,
    val transport: Boolean,
    val createAt: Date,
    val updatedAt: Date,
    val image: String
)
