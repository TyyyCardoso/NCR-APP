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
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.events.EventEditRequest
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar


class EventDetailFragmento :  BasicFragment() {
    /**
     * Componentes VIEW
     */
    //Text Fields
    private lateinit var eventName: TextView
    private lateinit var eventDescription: TextView
    private lateinit var eventLocation: TextView
    private lateinit var eventTransport: TextView

    //DateTimeFields
    private lateinit var eventInitDate: TextView
    private lateinit var eventEndDate: TextView

    //Others
    private lateinit var linearLayoutEventDetails : LinearLayout
    private lateinit var event : EventResponse

    //Image
    private lateinit var eventImage: ImageView
    private lateinit var eventImageBig: GestureImageView
    private lateinit var eventImageBigCloseBtn: FloatingActionButton

    /**
     * Componentes EDIT
     */
    //Text Fields
    private lateinit var etEventName: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var etEventLocation: EditText


    //DateTimeFields
    private lateinit var selectedInitDateTime: String
    private lateinit var selectedEndDateTime: String
    private lateinit var eventDetailEndDateTitle: TextView
    private lateinit var btnPickInitDateTime: Button
    private lateinit var btnPickEndDateTime: Button
    private val calendar = Calendar.getInstance()

    //Others
    private var editMode : Boolean = false
    private lateinit var checkboxEditEventTransport: CheckBox
    private lateinit var btnEditEventSubmit: Button

    //Image
    private var eventSelectedImage: String = ""
    private var eventSelectedImageUri: Uri? = null
    private var wasImageEdited : Boolean = false
    private lateinit var eventImageSelectButton: Button
    private lateinit var eventImagemCaptureButton: Button
    private lateinit var eventImageEditLayout: LinearLayout
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri

    //paths
    private var midPath = "event/images/"

    /**
     * Componentes DELETE
     */
    private lateinit var btnRemoveEvent: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_detail_fragmento, container, false)
        setupLoadingAnimation(view)
        /**
         * REFERENCES TO UI VIEW
         */
        //Text Fiels
        eventName =  view.findViewById(R.id.eventDetailName)
        eventDescription =  view.findViewById(R.id.eventDetailDescription)
        eventLocation =  view.findViewById(R.id.eventDetailLocation)
        eventTransport =  view.findViewById(R.id.eventDetailTransport) //TextView
        eventInitDate =  view.findViewById(R.id.eventDetailInitDate) //TextView
        eventEndDate =  view.findViewById(R.id.eventDetailEndDate) //TextView
        eventDetailEndDateTitle =  view.findViewById(R.id.eventDetailEndDateTitle) //TextView

        //Others
        linearLayoutEventDetails = view.findViewById(R.id.linearLayoutEventDetails)
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE

        //Image
        eventImage = view.findViewById(R.id.eventDetailImage)
        eventImageBig = view.findViewById(R.id.gestureImageView)
        eventImageBigCloseBtn = view.findViewById(R.id.closeGestureImageViewFAB)

        /**
         * REFERENCES TO UI EDIT
         */
        //Text Fiels
        etEventName =  view.findViewById(R.id.etEditEventName)
        etEventDescription =  view.findViewById(R.id.etEditEventDesc)
        etEventLocation =  view.findViewById(R.id.etEditEventLocation)

        //DateTime Fields
        btnPickInitDateTime =  view.findViewById(R.id.btnPickInitDateTime)
        btnPickEndDateTime =  view.findViewById(R.id.btnPickEndDateTime)

        //Others
        btnEditEventSubmit =  view.findViewById(R.id.btnEditEventSubmit)
        checkboxEditEventTransport =  view.findViewById(R.id.checkboxEditEventTransport)
        val fabEditEvent = view.findViewById<FloatingActionButton>(R.id.fabEditEvent)

        //Image
        eventImageEditLayout =  view.findViewById(R.id.eventImageEditLayout)
        eventImageSelectButton = view.findViewById(R.id.btnEditEventImageSelect)
        eventImagemCaptureButton =  view.findViewById(R.id.btnEditEventImageCapture)

        //Image AUX
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->

                Glide.with(this)
                    .load(selectedImageUri)
                    .fitCenter()
                    .centerInside()
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

                Glide.with(this)
                    .load(eventSelectedImageUri)
                    .fitCenter()
                    .centerInside()
                    .error(R.drawable.default_event_img)
                    .into(eventImage)

            }
        }

        /**
         * REFERENCES TO UI DELETE
         */
        btnRemoveEvent =  view.findViewById(R.id.btnDeleteEvent)

        /**
         * ClickListeners base
         */
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString(getString(R.string.clientType), getString(R.string.member))

        if(!clientType.equals(getString(R.string.admin))){
            fabEditEvent.visibility = View.GONE
        }else{
            fabEditEvent.setOnClickListener {
                toggleEditMode()
            }
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_events)
        }

        /**
         * Carregar eventos para a UI
         */
        //Obter evento do bundle
        event = arguments?.getParcelable(getString(R.string.myEvent))!!

        //Definir valores VIEW
        eventName.text = event.name
        eventDescription.text = event.description
        eventLocation.text = event.location
        eventInitDate.text = convertDateTime(event.initDate.toString())
        if(event.endDate!=null){
            eventEndDate.text = convertDateTime(event.endDate.toString())
        }else{
            eventEndDate.visibility = View.GONE
            eventDetailEndDateTitle.visibility = View.GONE
            btnPickEndDateTime.visibility = View.GONE
        }

        if (event.transport == true) {
            eventTransport.text = getString(R.string.addEventTextEventTranspBox)
            checkboxEditEventTransport.isChecked = true
        } else {
            eventTransport.text = getString(R.string.dialogNegativeButton)
            checkboxEditEventTransport.isChecked = false
        }

        selectedInitDateTime = event.initDate.toString()
        selectedEndDateTime = event.endDate.toString()
        eventSelectedImage = event.image.toString()

        var urlImage = ""

        if (!event.image.isNullOrBlank()){
            urlImage = RetrofitClient.BASE_URL + midPath + event.image

            Glide.with(this)
                .load(urlImage)
                .override(400, 200)
                .fitCenter()
                .centerInside()
                .error(R.drawable.default_event_img)
                .into(eventImage)

        }

        //ClickListener de abrir visualização de Imagem BIG
        eventImage.setOnClickListener {
            if (linearLayoutEventDetails.visibility == View.VISIBLE) {
                linearLayoutEventDetails.visibility = View.GONE
                fabEditEvent.visibility = View.GONE

                Glide.with(this)
                    .load(urlImage)
                    .fitCenter()
                    .centerInside()
                    .error(R.drawable.default_event_img)
                    .into(eventImageBig)

                eventImageBig.visibility = View.VISIBLE
                eventImageBigCloseBtn.visibility = View.VISIBLE
            }
        }

        //ClickListener de fechar visualização de Imagem BIG
        eventImageBigCloseBtn.setOnClickListener {
            linearLayoutEventDetails.visibility = View.VISIBLE
            eventImageBig.visibility = View.GONE
            eventImageBigCloseBtn.visibility = View.GONE
            if(clientType.equals(getString(R.string.admin))){
                fabEditEvent.visibility = View.VISIBLE
            }
        }

        /**
         * ClickListeners para EDITMODE
         */
        btnPickInitDateTime.setOnClickListener {
            pickDateTime(1)
        }

        btnPickEndDateTime.setOnClickListener {
            pickDateTime(2)
        }


        btnEditEventSubmit.setOnClickListener {
            if(editMode) {
                //Atualizar em BD
                saveEventBD()
            }
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
                    Toast.makeText(context, getString(R.string.cameraNeeded), Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Solicitar permissão
                    requestCameraPermission()
                }
            }
        }

        btnRemoveEvent.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialogAlertTitle))
                .setMessage(getString(R.string.dialogAlertMessage4))
                .setNeutralButton(getString(R.string.dialogNegativeButton)) { _, _ ->
                }
                .setPositiveButton(R.string.addEventTextEventTranspBox) { _, _ ->
                    deleteEvent()
                }
                .show()
        }
        return view
    }

    /**
     *
     * Função de apagar evento
     *
     */
    private fun deleteEvent(){
        btnRemoveEvent.visibility = View.GONE
        setLoadingVisibility(true)
        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.deleteEvent(event.id!!).execute()
            },
            onSuccess = {
                //Atualizar loading
                setLoadingVisibility(false)

                //Informar via toast
                if (toast != null) {
                    toast!!.setText(getString(R.string.eventRemoved))
                } else {
                    toast = Toast.makeText(requireActivity(), getString(R.string.eventRemoved), Toast.LENGTH_SHORT)
                }
                toast!!.show()

                //Informar que deve atualizar recyclerView
                EventsFragmento.setMyNeedRefresh(true)

                //Redirecionar para eventos
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_events, true)
                    .build()
                findNavController().navigate(R.id.navigation_events, null, navOptions)
            },
            onError = { errorMessage ->
                setLoadingVisibility(false)
                btnRemoveEvent.visibility = View.VISIBLE
                if (toast != null) {
                    toast!!.setText(errorMessage)
                } else {
                    toast = Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT)
                }
                toast!!.show()


            }
        )
    }

    private fun saveEventBD(){
        if(validateFields()){
            btnEditEventSubmit.visibility = View.GONE


            //Atualizar objeto event
            val eventName = etEventName.text.toString()
            val eventDesc = etEventDescription.text.toString()
            val eventLocal = etEventLocation.text.toString()
            val eventTransport = checkboxEditEventTransport.isChecked

            val eventToEdit = EventEditRequest(
                id = event.id!!,
                name = eventName,
                description = eventDesc,
                initDate = selectedInitDateTime,
                endDate = selectedEndDateTime,
                location = eventLocal,
                transport = eventTransport,
                createdAt = event.createdAt.toString(),
                image = eventSelectedImageUri,
                imageFileName = event.image!!
            )

            //Contruir parts com toda a info do event
            val idPart = eventToEdit.id.toString().toRequestBody(MultipartBody.FORM)
            val namePart = eventToEdit.name.toRequestBody(MultipartBody.FORM)
            val descriptionPart = eventToEdit.description.toRequestBody(MultipartBody.FORM)
            val initDatePart = eventToEdit.initDate.toRequestBody(MultipartBody.FORM)
            val endDatePart = eventToEdit.endDate.toRequestBody(MultipartBody.FORM)
            val locationPart = eventToEdit.location.toRequestBody(MultipartBody.FORM)
            val transportPart = eventToEdit.transport.toString().toRequestBody(MultipartBody.FORM)
            val createdAtPart = eventToEdit.createdAt.toRequestBody(MultipartBody.FORM)
            val imageFileNamePart = eventToEdit.imageFileName.toRequestBody(MultipartBody.FORM)

            //Construir part de imagem
            var imagePart: MultipartBody.Part? = null

            //Se foi introduzida/editada uma imagem
            if(wasImageEdited && eventToEdit.image != null) {
                val imageFile = compressImage(eventToEdit.image)
                val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
            } else {
                //Senao, usar imagem default/manter imagem
                val emptyRequestBody =
                    ByteArray(0).toRequestBody("image/*".toMediaTypeOrNull(), 0, 0)
                imagePart = MultipartBody.Part.createFormData("image", "", emptyRequestBody)
            }

            //Iniciar chamada à API
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.editEvent(idPart, namePart, descriptionPart, initDatePart, endDatePart, locationPart, transportPart, createdAtPart, imagePart, imageFileNamePart).execute()
                },
                onSuccess = {
                    //Atualizar loading
                    setLoadingVisibility(false)

                    //Informar via Toast
                    if (toast != null) {
                        toast!!.setText(getString(R.string.eventEdited))
                    } else {
                        toast = Toast.makeText(
                            requireActivity(),
                            getString(R.string.eventEdited),
                            Toast.LENGTH_SHORT
                        )
                    }
                    toast!!.show()

                    //Informar que deve atualizar recyclerView
                    EventsFragmento.setMyNeedRefresh(true)

                    //Redirecionar para eventos
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_events, true)
                        .build()
                    findNavController().navigate(R.id.navigation_events, null, navOptions)
                },
                onError = { errorMessage ->
                    setLoadingVisibility(false)
                    btnEditEventSubmit.visibility = View.VISIBLE

                    if (toast != null) {
                        toast!!.setText("ERRO: $errorMessage")
                    } else {
                        toast =
                            Toast.makeText(requireActivity(), "ERRO: $errorMessage", Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                }
            )
        }
    }

    /**
     *
     * Função para validar campos
     *
     */
    private fun validateFields(): Boolean{
        if (etEventName.text.toString().trim().isEmpty()) {
            etEventName.error = getString(R.string.addEventTitleError)
            return false
        }
        if (etEventDescription.text.toString().trim().isEmpty()) {
            etEventDescription.error = getString(R.string.didYouKnowDescriptionError)
            return false
        }
        if (etEventLocation.text.toString().trim().isEmpty()) {
            etEventLocation.error = getString(R.string.addEventLocationError)
            return false
        }
        return true
    }

    /**
     *
     * Funcao para abrir popup de escolha de data e hora
     *
     */
    private fun pickDateTime(type : Int) {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                if(type==1){
                    selectedInitDateTime = format.format(calendar.time)
                    eventInitDate.text = formatShow.format(calendar.time)
                }else{
                    selectedEndDateTime = format.format(calendar.time)
                    eventEndDate.text = formatShow.format(calendar.time)
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     *
     * Função trocar entre VIEWMODE ou EDITMODE
     *
     */
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

            btnPickInitDateTime.visibility = View.VISIBLE
            val eventEndDate = eventEndDate.text

            if(eventEndDate.equals(null) || eventEndDate.isEmpty()){
                btnPickEndDateTime.visibility = View.GONE
            }else{
                btnPickEndDateTime.visibility = View.VISIBLE
            }

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

            btnPickInitDateTime.visibility = View.GONE
            btnPickEndDateTime.visibility = View.GONE

            eventImageEditLayout.visibility = View.GONE
            checkboxEditEventTransport.visibility = View.GONE
            eventTransport.visibility = View.VISIBLE
            btnRemoveEvent.visibility = View.GONE
        }
    }

    private fun convertDateTime(dateTimeStr: String): String {
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
                    Toast.makeText(context, getString(R.string.cameraNeeded), Toast.LENGTH_LONG).show()
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
    }
}