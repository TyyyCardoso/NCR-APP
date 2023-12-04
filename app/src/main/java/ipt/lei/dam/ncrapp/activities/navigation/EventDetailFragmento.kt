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
import androidx.activity.addCallback
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.EventResponse


class EventDetailFragmento : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

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

        eventImage.setImageResource(R.drawable.default_event_img) // Um placeholder ou imagem padrão

        if (!image.isNullOrBlank()){
            val base64Image: String = image.split(",").get(1)
            val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            eventImage.setImageBitmap(decodedByte)
        }

        val navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.navigation_events)
        }

        return view
    }


    companion object {
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