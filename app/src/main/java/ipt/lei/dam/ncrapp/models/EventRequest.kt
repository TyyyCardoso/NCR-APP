package ipt.lei.dam.ncrapp.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

data class EventRequest(
    val name: String,
    val description: String,
    val date: String,
    val location: String,
    val transport: Boolean,
    val createAt: String,
    val updatedAt: String,
    val image: String
)