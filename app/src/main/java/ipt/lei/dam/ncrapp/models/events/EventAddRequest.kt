package ipt.lei.dam.ncrapp.models.events

import android.net.Uri

data class EventAddRequest(
    val name: String,
    val description: String,
    val initDate: String,
    val endDate: String,
    val location: String,
    val transport: Boolean,
    val image: Uri?
)