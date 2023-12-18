package ipt.lei.dam.ncrapp.models

data class EventRequest(
    val id: Int,
    val name: String,
    val description: String,
    val date: String,
    val location: String,
    val transport: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val image: String
)