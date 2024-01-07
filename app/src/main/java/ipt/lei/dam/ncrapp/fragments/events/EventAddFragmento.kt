package ipt.lei.dam.ncrapp.fragments.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.events.EventAddRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.Calendar


class EventAddFragmento : BasicFragment() {
    //Text Fields
    private var eventSelectedImageUri: Uri? = null
    private lateinit var eventNameEditText: EditText
    private lateinit var eventDescEditText: EditText
    private lateinit var eventLocalEditText: EditText

    //DateTime Fields
    private lateinit var btnPickInitDateTime: Button
    private lateinit var btnPickEndDateTime: Button
    private lateinit var selectedInitDateTime: String
    private lateinit var selectedEndDateTime: String
    private lateinit var tvSelectedInitDateTime: TextView
    private lateinit var tvSelectedEndDateTime: TextView
    private val calendar = Calendar.getInstance()

    //Others
    private lateinit var eventTransportCheckbox: CheckBox
    private lateinit var eventSubmitButton: Button

    //Image
    private lateinit var eventImage : ImageView
    private lateinit var eventImageSelectButton: Button
    private lateinit var eventImagemCaptureButton: Button

    //Image AUX
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_add_fragmento, container, false)

        /**
         * REFERENCES TO UI
         */
        //Text Fields
        eventNameEditText = view.findViewById(R.id.etNewEventName)
        eventDescEditText = view.findViewById(R.id.etNewEventDesc)
        eventLocalEditText = view.findViewById(R.id.etNewEventLocal)

        //DateTime Fields
        btnPickInitDateTime = view.findViewById(R.id.btnPickInitDateTime)
        btnPickEndDateTime = view.findViewById(R.id.btnPickEndDateTime)
        tvSelectedInitDateTime = view.findViewById(R.id.tvSelectedInitDateTime)
        tvSelectedEndDateTime = view.findViewById(R.id.tvSelectedEndDateTime)
        tvSelectedInitDateTime.text = formatShow.format(calendar.time)
        tvSelectedEndDateTime.text = formatShow.format(calendar.time)
        selectedInitDateTime = format.format(calendar.time)
        selectedEndDateTime = format.format(calendar.time)

        //Others
        eventTransportCheckbox = view.findViewById(R.id.checkboxNewEventTransport)
        eventSubmitButton = view.findViewById(R.id.btnNewEventSubmit)
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE

        //Image
        eventImage = view.findViewById(R.id.imageviewNewEventImageView)
        eventImageSelectButton = view.findViewById(R.id.btnNewEventImageSelect)
        eventImagemCaptureButton =  view.findViewById(R.id.btnNewEventImageCapture)
        setupLoadingAnimation(view)

        //Image AUX
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->

                Glide.with(requireContext())
                    .load(selectedImageUri)
                    .fitCenter()
                    .centerInside()
                    .error(R.drawable.default_event_img)
                    .into(eventImage)

                // Armazena o arquivo no eventRequest.image
                eventSelectedImageUri = selectedImageUri
            }
        }


        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                eventSelectedImageUri = currentPhotoUri

                Glide.with(requireContext())
                    .load(eventSelectedImageUri)
                    .fitCenter()
                    .centerInside()
                    .placeholder(R.drawable.default_event_img)
                    .error(R.drawable.default_event_img)
                    .into(eventImage)
            }
        }

        /**
         * ClickListeners
         */
        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_events)
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

        btnPickInitDateTime.setOnClickListener {
            pickDateTime(1)
        }

        btnPickEndDateTime.setOnClickListener {
            pickDateTime(2)
        }

        eventSubmitButton.setOnClickListener {
            if(validateFields()){
                saveEvent()
            }
        }
        return view
    }

    /**
     *
     * Funcao de guardar evento
     *
     */
    private fun saveEvent(){
        setLoadingVisibility(true)
        eventSubmitButton.visibility = View.GONE

        val eventName = eventNameEditText.text.toString()
        val eventDesc = eventDescEditText.text.toString()
        val eventLocal = eventLocalEditText.text.toString()

        val eventTransport = eventTransportCheckbox.isChecked

        // Criando o objeto EventResponse
        val event = EventAddRequest(
            name = eventName,
            description = eventDesc,
            initDate = selectedInitDateTime,
            endDate = selectedEndDateTime,
            location = eventLocal,
            transport = eventTransport,
            image = eventSelectedImageUri
        )

        //Contruir parts com toda a info do event
        val namePart = RequestBody.create(MultipartBody.FORM, event.name)
        val descriptionPart = RequestBody.create(MultipartBody.FORM, event.description)
        val initDatePart = RequestBody.create(MultipartBody.FORM, event.initDate)
        val endDatePart = RequestBody.create(MultipartBody.FORM, event.endDate)
        val locationPart = RequestBody.create(MultipartBody.FORM, event.location)
        val transportPart = RequestBody.create(MultipartBody.FORM, event.transport.toString())

        //Construir part de imagem
        var imagePart: MultipartBody.Part? = null

        //Se foi introduzida uma imagem
        if(event.image != null){
            val imageFile = compressImage(event.image)
            val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
        } else {
            //Senao, usar imagem default
            val emptyRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), ByteArray(0))
            imagePart = MultipartBody.Part.createFormData("image", "", emptyRequestBody)

        }

        //Iniciar chamada à API
        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.addEvent(namePart, descriptionPart, initDatePart, endDatePart, locationPart, transportPart, imagePart).execute()
            },
            onSuccess = { responseBody ->
                //Atualizar loading
                setLoadingVisibility(false)

                //Informar via Toast
                if (toast != null) {
                    toast!!.setText("Evento criado com sucesso")
                } else {
                    toast = Toast.makeText(
                        requireActivity(),
                        "Evento criado com sucesso",
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
                eventSubmitButton.visibility = View.VISIBLE
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

    /**
     *
     * Funcao para abrir popup de escolha de data e hora
     *
     */
    private fun pickDateTime(type: Int) {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                if(type==1){
                    selectedInitDateTime = format.format(calendar.time)
                    tvSelectedEndDateTime.text = formatShow.format(calendar.time)
                }else{
                    selectedInitDateTime = format.format(calendar.time)
                    tvSelectedEndDateTime.text = formatShow.format(calendar.time)
                }

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     *
     * Função para validar campos
     *
     */
    private fun validateFields(): Boolean {
        if (eventNameEditText.text.toString().trim().isEmpty()) {
            eventNameEditText.error = "Introduza um nome"
            return false
        }
        if (eventDescEditText.text.toString().trim().isEmpty()) {
            eventDescEditText.error = "Introduza uma descrição"
            return false
        }
        if (eventLocalEditText.text.toString().trim().isEmpty()) {
            eventLocalEditText.error = "Introduza uma localização"
            return false
        }
        return true
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE_CAPTURE_IMAGE)
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
    }
}