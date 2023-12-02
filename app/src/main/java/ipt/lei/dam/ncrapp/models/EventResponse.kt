package ipt.lei.dam.ncrapp.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return "EventResponse(eventId=$eventId, name='$name', description='$description', date='${date.format(formatter)}', location='$location', transport=$transport, createAt='${createAt.format(formatter)}', updatedAt='${updatedAt.format(formatter)}', image='$image')"
    }
}
