package ipt.lei.dam.ncrapp.models


import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime


data class EventResponse(
    val id: Int?,
    var name: String?,
    var description: String?,
    var date: LocalDateTime?,  // Supondo que você tenha uma maneira de lidar com LocalDateTime
    var location: String?,
    var transport: Boolean?,
    var createdAt: LocalDateTime?,
    var updatedAt: LocalDateTime?,
    var image: String?,
    var subscribed: Boolean?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        TODO("date"),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        TODO("createAt"),
        TODO("updatedAt"),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(location)
        parcel.writeValue(transport)
        parcel.writeString(image)
        parcel.writeValue(subscribed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventResponse> {
        override fun createFromParcel(parcel: Parcel): EventResponse {
            return EventResponse(parcel)
        }

        override fun newArray(size: Int): Array<EventResponse?> {
            return arrayOfNulls(size)
        }
    }
}
