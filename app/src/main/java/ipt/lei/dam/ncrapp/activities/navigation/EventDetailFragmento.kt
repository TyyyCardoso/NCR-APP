package ipt.lei.dam.ncrapp.activities.navigation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.EventResponse
import org.w3c.dom.Text


class EventDetailFragmento : Fragment() {
    private var editMode : Boolean = false

    private lateinit var eventImage: ImageView
    private lateinit var eventName: TextView
    private lateinit var eventDescription: TextView
    private lateinit var eventLocation: TextView
    private lateinit var eventDate: TextView
    private lateinit var eventTransport: TextView

    private lateinit var etEventName: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var etEventLocation: EditText
    private lateinit var btnEditEventSubmit: Button

            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_detail_fragmento, container, false)

        val fabEditEvent = view.findViewById<FloatingActionButton>(R.id.fabEditEvent)
        fabEditEvent.setOnClickListener {
            toggleEditMode()
        }

        eventImage = view.findViewById(R.id.eventDetailImage)
        eventName =  view.findViewById(R.id.eventDetailName)
        eventDescription =  view.findViewById(R.id.eventDetailDescription)
        eventLocation =  view.findViewById(R.id.eventDetailLocation)
        eventDate =  view.findViewById(R.id.eventDetailDate)
        eventTransport =  view.findViewById(R.id.eventDetailTransport)

        etEventName =  view.findViewById(R.id.etEditEventName)
        etEventDescription =  view.findViewById(R.id.etEditEventDesc)
        etEventLocation =  view.findViewById(R.id.etEditEventLocation)
        btnEditEventSubmit =  view.findViewById(R.id.btnEditEventSubmit)

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

    private fun toggleEditMode() {
        editMode = !editMode

        if (editMode) {
            btnEditEventSubmit.visibility = View.VISIBLE
            eventName.visibility = View.GONE
            etEventName.visibility = View.VISIBLE
            etEventName.text = Editable.Factory.getInstance().newEditable(eventName.text)

            eventDescription.visibility = View.GONE
            etEventDescription.visibility = View.VISIBLE
            etEventDescription.text = Editable.Factory.getInstance().newEditable(eventDescription.text)

            eventLocation.visibility = View.GONE
            etEventLocation.visibility = View.VISIBLE
            etEventLocation.text = Editable.Factory.getInstance().newEditable(eventLocation.text)
        } else {
            btnEditEventSubmit.visibility = View.INVISIBLE

            eventName.visibility = View.VISIBLE
            etEventName.visibility = View.GONE

            eventDescription.visibility = View.VISIBLE
            etEventDescription.visibility = View.GONE

            eventLocation.visibility = View.VISIBLE
            etEventLocation.visibility = View.GONE

        }
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