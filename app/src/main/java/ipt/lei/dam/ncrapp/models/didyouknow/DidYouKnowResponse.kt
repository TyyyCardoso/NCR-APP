package ipt.lei.dam.ncrapp.models.didyouknow

import android.os.Parcel
import android.os.Parcelable
import org.threeten.bp.LocalDateTime

/**
 *
 * Classe modelo para pedido à API de resposta de "sabiasque"
 *
 * id: identificador do sabias que
 * title: Titulo do sabias que
 * text: Texto do sabias que
 * references: Referências do sabias que
 * createdAt: data e hora que o sabias que foi criado
 * updatedAt: data e hora que o sabias que foi atualizado
 */

data class DidYouKnowResponse(
    val id: Int?,
    var title: String?,
    var text: String?,
    var references: String?,
    var createdAt: LocalDateTime?,
    var updatedAt: LocalDateTime?
)  : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        TODO("createdAt"),
        TODO("updatedAt")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeString(references)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DidYouKnowResponse> {
        override fun createFromParcel(parcel: Parcel): DidYouKnowResponse {
            return DidYouKnowResponse(parcel)
        }

        override fun newArray(size: Int): Array<DidYouKnowResponse?> {
            return arrayOfNulls(size)
        }
    }
}
