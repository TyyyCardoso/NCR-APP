package ipt.lei.dam.ncrapp.models

import android.net.Uri

data class EventAddRequest(
    val name: String,
    val description: String,
    val date: String,
    val location: String,
    val transport: Boolean,
    val image: Uri?
)