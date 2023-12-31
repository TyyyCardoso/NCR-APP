package ipt.lei.dam.ncrapp.fragments.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.alexvasilkov.gestures.views.GestureImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.events.EventEditRequest
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EventDetailFragmento :  BasicFragment() {
    private var editMode : Boolean = false

    private lateinit var event : EventResponse

    // Componentes VIEW
    private lateinit var linearLayoutEventDetails : LinearLayout
    private lateinit var eventImage: ImageView
    private lateinit var eventName: TextView
    private lateinit var eventDescription: TextView
    private lateinit var eventLocation: TextView
    private lateinit var eventDate: TextView
    private lateinit var eventTransport: TextView

    // Componentes de EDIT
    private var eventSelectedImage: String = ""
    private var eventSelectedImageUri: Uri? = null
    private var wasImageEdited : Boolean = false
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

    //Componentes de DELETE
    private lateinit var btnRemoveEvent: Button

    // DateTime
    private val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatShow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    //Images
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri

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

        // Componentes VIEW
        linearLayoutEventDetails = view.findViewById(R.id.linearLayoutEventDetails)
        eventImage = view.findViewById(R.id.eventDetailImage)
        eventName =  view.findViewById(R.id.eventDetailName)
        eventDescription =  view.findViewById(R.id.eventDetailDescription)
        eventLocation =  view.findViewById(R.id.eventDetailLocation)
        eventDate =  view.findViewById(R.id.eventDetailDate)
        eventTransport =  view.findViewById(R.id.eventDetailTransport)

        val gestureImageView = view.findViewById<GestureImageView>(R.id.gestureImageView)
        val fabGestureImageView = view.findViewById<FloatingActionButton>(R.id.closeGestureImageViewFAB)

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

        setupLoadingAnimation(view)

        val fabEditEvent = view.findViewById<FloatingActionButton>(R.id.fabEditEvent)

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE
        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        if(!clientType.equals("ADMINISTRADOR")){
            fabEditEvent.visibility = View.GONE;
            //fabEditEvent.setOnClickListener {
            //    toggleEditMode()
            //}
        }else{
            fabEditEvent.setOnClickListener {
                toggleEditMode()
            }
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_events)
        }

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->

                Picasso.get()
                    .load(selectedImageUri)
                    .fit()
                    .centerInside()
                    //.placeholder(R.drawable.default_event_img)
                    .error(R.drawable.default_event_img)
                    .into(eventImage)

                eventSelectedImageUri = selectedImageUri
                wasImageEdited = true
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                wasImageEdited = true
                eventSelectedImageUri = currentPhotoUri
                Picasso.get()
                    .load(eventSelectedImageUri)
                    .fit()
                    .centerInside()
                    //.placeholder(R.drawable.default_event_img)
                    .error(R.drawable.default_event_img)
                    .into(eventImage)

            }
        }

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

        selectedDateTime = event?.date.toString()
        eventSelectedImage = event?.image.toString()

        var urlImage = ""

        if (!event?.image.isNullOrBlank()){
            urlImage = "" + RetrofitClient.BASE_URL + "event/images/" + event.image
            println("Event: " + event.name + "getting image from: " + urlImage)
            Picasso.get()
                .load(urlImage)
                .resize(400, 200) // Substitua "width" e "height" pelos valores desejados
                .centerCrop()
                //.placeholder(R.drawable.default_event_img)
                .error(R.drawable.default_event_img)
                .into(eventImage)
        }

        eventImage.setOnClickListener {
            if (linearLayoutEventDetails.visibility == View.VISIBLE) {
                linearLayoutEventDetails.visibility = View.GONE
                fabEditEvent.visibility = View.GONE

                Picasso.get()
                    .load(urlImage)
                    .fit() // Substitua "width" e "height" pelos valores desejados
                    .centerInside()
                    //.placeholder(R.drawable.default_event_img)
                    .error(R.drawable.default_event_img)
                    .into(gestureImageView)

                gestureImageView.visibility = View.VISIBLE
                fabGestureImageView.visibility = View.VISIBLE
            }
        }

        fabGestureImageView.setOnClickListener {
            linearLayoutEventDetails.visibility = View.VISIBLE
            gestureImageView.visibility = View.GONE
            fabGestureImageView.visibility = View.GONE
            if(clientType.equals("ADMINISTRADOR")){
                fabEditEvent.visibility = View.VISIBLE
            }
        }


        //EDIT MODE FUNCOES
        btnPickDateTime.setOnClickListener {
            pickDateTime()
        }

        btnEditEventSubmit.setOnClickListener {
            saveEvent()
        }

        eventImageSelectButton.setOnClickListener {
            getContent.launch("image/*")
        }

        eventImagemCaptureButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    // Permissão já concedida, pode abrir a câmera
                    openCamera()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                    // Fornecer uma explicação adicional ao usuário
                    Toast.makeText(context, "A câmera é necessária para capturar fotos", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Solicitar permissão
                    requestCameraPermission()
                }
            }
        }

        btnRemoveEvent.setOnClickListener {
            //deleteEvent()
            AlertDialog.Builder(requireContext())
                .setTitle("Aviso")
                .setMessage("Tem a certeza que quer apagar o evento?")
                .setNeutralButton("Não") { dialog, which ->
                }
                .setPositiveButton("Apagar") { dialog, which ->
                    deleteEvent()
                }
                .show()
        }
        return view
    }

    private fun deleteEvent(){
        var doEventRequest = false
        doEventRequest = true
        if (doEventRequest) {
            btnRemoveEvent.visibility = View.GONE
            setLoadingVisibility(true)
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.deleteEvent(event.id!!).execute()
                },
                onSuccess = { isEditted ->
                    setLoadingVisibility(false)

                    EventsFragmento.setMyNeedRefresh(true)

                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_events, true)
                        .build()


                    findNavController().navigate(R.id.navigation_events, null, navOptions)

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
                    btnRemoveEvent.visibility = View.VISIBLE
                    setLoadingVisibility(false)
                }
            )
        }
    }
    private fun saveEvent(){
        if(editMode){
            //Atualizar em BD
            saveEventBD()

            //Voltar ao VIEW
            toggleEditMode()

        }
    }


    private fun saveEventBD(){
        if(validateFields()){
            //Atualizar objeto event
            val eventName = etEventName.text.toString()
            val eventDesc = etEventDescription.text.toString()
            val eventLocal = etEventLocation.text.toString()
            val eventTransport = checkboxEditEventTransport.isChecked

            val eventToEdit = EventEditRequest(
                id = event.id!!,
                name = eventName,
                description = eventDesc,
                date = selectedDateTime,
                location = eventLocal,
                transport = eventTransport,
                createdAt = event.createdAt.toString(),
                image = eventSelectedImageUri,
                imageFileName = event.image!!
            )

            val idPart = RequestBody.create(MultipartBody.FORM, eventToEdit.id.toString())
            val namePart = RequestBody.create(MultipartBody.FORM, eventToEdit.name)
            val descriptionPart = RequestBody.create(MultipartBody.FORM, eventToEdit.description)
            val datePart = RequestBody.create(MultipartBody.FORM, eventToEdit.date)
            val locationPart = RequestBody.create(MultipartBody.FORM, eventToEdit.location)
            val transportPart = RequestBody.create(MultipartBody.FORM, eventToEdit.transport.toString())
            val createdAtPart = RequestBody.create(MultipartBody.FORM, eventToEdit.createdAt)
            val imageFileNamePart = RequestBody.create(MultipartBody.FORM, eventToEdit.imageFileName)

            var imagePart: MultipartBody.Part? = null

            if(wasImageEdited && eventToEdit.image != null) {
                val imageFile = compressImage(eventToEdit.image)
                val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
            } else {
                val emptyRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), ByteArray(0))
                imagePart = MultipartBody.Part.createFormData("image", "", emptyRequestBody)
            }
                val call = RetrofitClient.apiService.editEvent(idPart, namePart, descriptionPart, datePart, locationPart, transportPart, createdAtPart, imagePart, imageFileNamePart)
                btnEditEventSubmit.visibility = View.GONE
                setLoadingVisibility(true)

                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        val responseCodeNum = response.code()
                        val responseCode = when (responseCodeNum) {
                            400 -> "Solicitação inválida"
                            401 -> "Não autorizado"
                            404 -> "Recurso não encontrado"
                            413 -> "Tamanho Máximo de IMG: 1MB"
                            500 -> "Interno do servidor"
                            else -> "Desconhecido: $responseCodeNum"
                        }
                        if (response.isSuccessful) {
                            setLoadingVisibility(false)

                            EventsFragmento.setMyNeedRefresh(true)

                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.navigation_events, true)
                                .build()


                            findNavController().navigate(R.id.navigation_events, null, navOptions)

                            if (toast != null) {
                                toast!!.setText("Evento editado com sucesso")
                            } else {
                                toast = Toast.makeText(requireActivity(), "Evento editado com sucesso", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()
                        } else {
                            btnEditEventSubmit.visibility = View.VISIBLE
                            setLoadingVisibility(false)
                            if (toast != null) {
                                toast!!.setText("ERRO: $responseCode")
                            } else {
                                toast = Toast.makeText(requireActivity(), "ERRO: $responseCode", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        // Tratamento de falha
                    }
                })


        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val filePath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()
        return filePath
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
            eventTransport.visibility = View.GONE
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
            eventTransport.visibility = View.VISIBLE
            btnRemoveEvent.visibility = View.GONE
        }
    }

    fun convertDateTime(dateTimeStr: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        val date = inputFormat.parse(dateTimeStr)
        return outputFormat.format(date)
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CODE_CAPTURE_IMAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_CAPTURE_IMAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permissão concedida, pode abrir a câmera
                    openCamera()
                } else {
                    // Permissão negada, lide com a situação
                    Toast.makeText(context, "Permissão de câmera necessária", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openCamera() {
        currentPhotoUri = getOutputMediaFileUri()
        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun getOutputMediaFileUri(): Uri {
        val contentResolver = requireActivity().applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "my_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
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