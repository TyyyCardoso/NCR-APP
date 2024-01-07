package ipt.lei.dam.ncrapp.models.events

import android.net.Uri

data class EventEditRequest(
    val id: Int,
    val name: String,
    val description: String,
    val initDate: String,
    val endDate: String,
    val location: String,
    val transport: Boolean,
    val createdAt: String,
    val image: Uri?,
    val imageFileName: String
)
