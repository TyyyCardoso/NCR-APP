package ipt.lei.dam.ncrapp.activities.navigation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.models.EventRequest
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class EventDetailFragmento :  BasicFragment() {
    private var editMode : Boolean = false

    private lateinit var event : EventResponse

    // Componentes VIEW
    private lateinit var eventImage: ImageView
    private lateinit var eventName: TextView
    private lateinit var eventDescription: TextView
    private lateinit var eventLocation: TextView
    private lateinit var eventDate: TextView
    private lateinit var eventTransport: TextView

    // Componentes de EDIT
    private lateinit var etEventName: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var etEventLocation: EditText
    private lateinit var btnEditEventSubmit: Button
    private lateinit var btnPickDateTime: Button
    private lateinit var selectedDateTime: String
    private val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatShow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

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

        setupLoadingAnimation(view)

        val fabEditEvent = view.findViewById<FloatingActionButton>(R.id.fabEditEvent)
        fabEditEvent.setOnClickListener {
            toggleEditMode()
        }

        // Componentes VIEW
        eventImage = view.findViewById(R.id.eventDetailImage)
        eventName =  view.findViewById(R.id.eventDetailName)
        eventDescription =  view.findViewById(R.id.eventDetailDescription)
        eventLocation =  view.findViewById(R.id.eventDetailLocation)
        eventDate =  view.findViewById(R.id.eventDetailDate)
        eventTransport =  view.findViewById(R.id.eventDetailTransport)

        // Componentes EDIT
        etEventName =  view.findViewById(R.id.etEditEventName)
        etEventDescription =  view.findViewById(R.id.etEditEventDesc)
        etEventLocation =  view.findViewById(R.id.etEditEventLocation)
        btnEditEventSubmit =  view.findViewById(R.id.btnEditEventSubmit)
        btnPickDateTime =  view.findViewById(R.id.btnPickDateTime)

        //Obter evento do bundle
        event = arguments?.getParcelable<EventResponse>("myEvent")!!

        println("" + event.id + " - " + event.name + " - " + event.createdAt)

        //Definir valores VIEW
        eventName.text = event?.name
        eventDescription.text = event?.description
        eventLocation.text = event?.location
        eventDate.text = event?.date.toString()
        eventTransport.text = "Transporte: " + if (event?.transport == true) "Sim" else "Não"
        eventImage.setImageResource(R.drawable.default_event_img) // Um placeholder ou imagem padrão

        selectedDateTime = event?.date.toString()

        if (!event?.image.isNullOrBlank()){
            val base64Image: String = event?.image!!.split(",").get(1)
            val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            eventImage.setImageBitmap(decodedByte)
        }

        val navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.navigation_events)
        }


        //EDIT MODE FUNCOES
        btnPickDateTime.setOnClickListener {
            pickDateTime()
        }

        btnEditEventSubmit.setOnClickListener {
            saveEvent()
        }
        return view
    }

    private fun saveEvent(){
        if(editMode){
            //Atualizar objeto event
            event.name = etEventName.text.toString()
            event.description = etEventDescription.text.toString()
            event.location = etEventLocation.text.toString()

            //Atualizar VIEW
            eventName.text = event.name
            eventDescription.text = event.description
            eventLocation.text = event.location

            //Atualizar em BD
            saveEventBD()

            //Voltar ao VIEW
            toggleEditMode()

        }
    }

    private fun saveEventBD(){
        if(validateFields()){
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            val eventRequest  = EventRequest(
                id = event.id!!,
                name = event.name!!,
                description = event.description!!,
                date = selectedDateTime,
                location = event.location!!,
                transport = event.transport!!,
                createdAt = event.createdAt.toString(),
                updatedAt = now.format(formatter),
                image = event.image!!
            )

            var doEventRequest = false
            doEventRequest = true
            if (doEventRequest) {
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.editEvent(eventRequest).execute()
                    },
                    onSuccess = { isEditted ->
                        setLoadingVisibility(false)

                        val navController = findNavController()
                        navController.navigate(R.id.navigation_events)

                        if (toast != null) {
                            toast!!.setText("Evento editado com sucesso")
                        } else {
                            toast = Toast.makeText(requireActivity(), "Evento editado com sucesso", Toast.LENGTH_SHORT)
                        }
                        toast!!.show()

                    },
                    onError = { errorMessage ->
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }


        }
    }

    private fun validateFields(): Boolean{
        if (etEventName.text.toString().trim().isEmpty()) {
            etEventName.error = "Introduza um nome"
            return false
        }
        if (etEventDescription.text.toString().trim().isEmpty()) {
            etEventDescription.error = "Introduza uma descrição"
            return false
        }
        if (etEventLocation.text.toString().trim().isEmpty()) {
            etEventLocation.error = "Introduza uma localização"
            return false
        }
        return true
    }

    private fun pickDateTime() {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                selectedDateTime = format.format(calendar.time)
                eventDate.text = formatShow.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
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

            btnPickDateTime.visibility = View.VISIBLE
        } else {
            btnEditEventSubmit.visibility = View.INVISIBLE

            eventName.visibility = View.VISIBLE
            etEventName.visibility = View.GONE

            eventDescription.visibility = View.VISIBLE
            etEventDescription.visibility = View.GONE

            eventLocation.visibility = View.VISIBLE
            etEventLocation.visibility = View.GONE

            btnPickDateTime.visibility = View.GONE

        }
    }


    companion object {
        @JvmStatic
        fun newInstance(event: EventResponse): EventDetailFragmento {
            val args = Bundle()

            args.putParcelable("myEvent", event)

            val fragment = EventDetailFragmento()
            fragment.arguments = args
            return fragment
            }
    }
}