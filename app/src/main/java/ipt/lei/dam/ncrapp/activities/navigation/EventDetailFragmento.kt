package ipt.lei.dam.ncrapp.activities.navigation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.EventResponse

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventDetailFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventDetailFragmento : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_event_detail_fragmento, container, false)

        val eventImage: ImageView = view.findViewById(R.id.eventDetailImage)
        val eventName: TextView =  view.findViewById(R.id.eventDetailName)
        val eventDescription: TextView =  view.findViewById(R.id.eventDetailDescription)
        val eventLocation: TextView =  view.findViewById(R.id.eventDetailLocation)
        val eventDate: TextView =  view.findViewById(R.id.eventDetailDate)
        val eventTransport: TextView =  view.findViewById(R.id.eventDetailTransport)

        val name = arguments?.getString("eventName")
        val description = arguments?.getString("eventDescription")
        val location = arguments?.getString("eventLocation")
        val date = arguments?.getString("eventDate")
        val transport = arguments?.getBoolean("eventTransport")
        val image = arguments?.getString("eventImage")


        eventName.text = name
        eventDescription.text = description
        eventLocation.text = location
        eventDate.text = date
        eventTransport.text = "Transporte: " + if (transport == true) "Sim" else "Não"

        eventImage.setImageResource(R.drawable.baseline_event_note_24) // Um placeholder ou imagem padrão

        if (!image.isNullOrBlank()){
            val base64Image: String = image.split(",").get(1)
            val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            eventImage.setImageBitmap(decodedByte)
        }



        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventDetailFragmento.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(event: EventResponse): EventDetailFragmento {
            val args = Bundle()
            args.putInt("eventId", event.eventId)
            args.putString("eventName", event.name)
            args.putString("eventDescription", event.description)
            args.putString("eventDate", event.date.toString())
            args.putString("eventLocation", event.location)
            args.putBoolean("eventTransport", event.transport)
            args.putString("eventCreatedAt", event.createAt.toString())
            args.putString("eventUpdatedAt", event.updatedAt.toString())
            args.putString("eventImage", event.image)

            val fragment = EventDetailFragmento()
            fragment.arguments = args
            return fragment
            }
    }
}