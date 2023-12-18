package ipt.lei.dam.ncrapp.activities.navigation

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.io.ByteArrayOutputStream
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
    private var eventSelectedImage: String = ""
    private lateinit var etEventName: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var etEventLocation: EditText
    private lateinit var btnEditEventSubmit: Button
    private lateinit var btnPickDateTime: Button
    private lateinit var eventImageSelectButton: Button
    private lateinit var eventImagemCaptureButton: Button
    private lateinit var eventImageEditLayout: LinearLayout
    private lateinit var selectedDateTime: String
    private lateinit var checkboxEditEventTransport: CheckBox
    private val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatShow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    //Componentes de DELETE
    private lateinit var btnRemoveEvent: Button

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
        eventImageEditLayout =  view.findViewById(R.id.eventImageEditLayout)
        eventImageSelectButton = view.findViewById(R.id.btnEditEventImageSelect)
        eventImagemCaptureButton =  view.findViewById(R.id.btnEditEventImageCapture)
        checkboxEditEventTransport =  view.findViewById(R.id.checkboxEditEventTransport)

        // Componentes DELETE
        btnRemoveEvent =  view.findViewById(R.id.btnDeleteEvent)

        //Obter evento do bundle
        event = arguments?.getParcelable<EventResponse>("myEvent")!!

        println("" + event.id + " - " + event.name + " - " + event.createdAt)

        //Definir valores VIEW
        eventName.text = event?.name
        eventDescription.text = event?.description
        eventLocation.text = event?.location
        eventDate.text = convertDateTime(event.date.toString())

        if (event?.transport == true) {
            eventTransport.text = "Sim"
            checkboxEditEventTransport.isChecked = true
        } else {
            eventTransport.text = "Não"
            checkboxEditEventTransport.isChecked = false
        }
        eventImage.setImageResource(R.drawable.default_event_img) // Um placeholder ou imagem padrão

        selectedDateTime = event?.date.toString()
        eventSelectedImage = event?.image.toString()

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

        eventImageSelectButton.setOnClickListener {
            pickImageFromGallery()
        }

        eventImagemCaptureButton.setOnClickListener {
            captureImageFromCamera()
        }

        btnRemoveEvent.setOnClickListener {
            deleteEvent()
        }
        return view
    }

    private fun deleteEvent(){
        var doEventRequest = false
        doEventRequest = true
        if (doEventRequest) {
            setLoadingVisibility(true)
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.deleteEvent(event.id!!).execute()
                },
                onSuccess = { isEditted ->
                    setLoadingVisibility(false)

                    val navController = findNavController()
                    navController.navigate(R.id.navigation_events)

                    if (toast != null) {
                        toast!!.setText("Evento removido com sucesso")
                    } else {
                        toast = Toast.makeText(requireActivity(), "Evento removido com sucesso", Toast.LENGTH_SHORT)
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
    private fun saveEvent(){
        if(editMode){
            //Atualizar objeto event
            event.name = etEventName.text.toString()
            event.description = etEventDescription.text.toString()
            event.location = etEventLocation.text.toString()
            event.transport = checkboxEditEventTransport.isChecked

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
                image = eventSelectedImage
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

            eventImageEditLayout.visibility = View.VISIBLE

            btnPickDateTime.visibility = View.VISIBLE
            checkboxEditEventTransport.visibility = View.VISIBLE
            btnRemoveEvent.visibility = View.VISIBLE
        } else {
            btnEditEventSubmit.visibility = View.INVISIBLE

            eventName.visibility = View.VISIBLE
            etEventName.visibility = View.GONE

            eventDescription.visibility = View.VISIBLE
            etEventDescription.visibility = View.GONE

            eventLocation.visibility = View.VISIBLE
            etEventLocation.visibility = View.GONE

            btnPickDateTime.visibility = View.GONE

            eventImageEditLayout.visibility = View.GONE
            checkboxEditEventTransport.visibility = View.GONE
            btnRemoveEvent.visibility = View.GONE
        }
    }

    fun convertDateTime(dateTimeStr: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        val date = inputFormat.parse(dateTimeStr)
        return outputFormat.format(date)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun captureImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
    }

    // Dentro do seu Fragmento
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            updateImageView(imageUri)
        } else if(requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            updateImageViewWithBitmap(imageBitmap)
        }
    }

    private fun updateImageView(uri: Uri?) {
        println("########!!!!!!!Atualizando imagem")
        uri?.let {
            val imageView = view?.findViewById<ImageView>(R.id.eventDetailImage)
            imageView?.setImageURI(uri)
            if (imageView != null) {
                //imageView.setImageBitmap(uriToBitmap(uri))
                var myBitmap : Bitmap? = uriToBitmap(uri)
                var myBase64 : String? = myBitmap?.let { it1 -> bitmapToBase64(it1) }
                if (myBase64 != null) {
                    eventSelectedImage = "data:image/png;base64," + myBase64
                }
            }
        }
    }

    private fun updateImageViewWithBitmap(bitmap: Bitmap) {
        val imageView = view?.findViewById<ImageView>(R.id.eventDetailImage)
        if (imageView != null) {
            imageView.setImageBitmap(bitmap)
            var myBase64 : String? = bitmap?.let { it1 -> bitmapToBase64(it1) }
            if (myBase64 != null) {
                eventSelectedImage = "data:image/png;base64," + myBase64
            }
        }
    }

    fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream = context?.contentResolver?.openInputStream(uri)
        return inputStream?.use { BitmapFactory.decodeStream(it) }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    companion object {
        private const val REQUEST_CODE_CAPTURE_IMAGE = 1
        private const val REQUEST_CODE_PICK_IMAGE = 2
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