package ipt.lei.dam.ncrapp.activities.navigation

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
import com.squareup.picasso.Picasso
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.activities.navigation.EventsFragmento.Companion.setMyNeedRefresh
import ipt.lei.dam.ncrapp.models.EventAddRequest
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


class EventAddFragmento : BasicFragment() {
    private var eventSelectedImage: String = ""
    private var eventSelectedImageUri: Uri? = null
    private lateinit var eventNameEditText: EditText
    private lateinit var eventDescEditText: EditText
    private lateinit var eventLocalEditText: EditText
    private lateinit var eventImage : ImageView

    private lateinit var btnPickDateTime: Button
    private lateinit var selectedDateTime: String
    private lateinit var tvSelectedDateTime: TextView
    private val calendar = Calendar.getInstance()

    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatShow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())


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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_add_fragmento, container, false)
        eventNameEditText = view.findViewById(R.id.etNewEventName)
        eventDescEditText = view.findViewById(R.id.etNewEventDesc)
        eventLocalEditText = view.findViewById(R.id.etNewEventLocal)
        eventImage = view.findViewById(R.id.imageviewNewEventImageView)
        val eventTransportCheckbox = view.findViewById<CheckBox>(R.id.checkboxNewEventTransport)
        val eventSubmitButton = view.findViewById<Button>(R.id.btnNewEventSubmit)
        val eventImageSelectButton = view.findViewById<Button>(R.id.btnNewEventImageSelect)
        val eventImagemCaptureButton =  view.findViewById<Button>(R.id.btnNewEventImageCapture)

        btnPickDateTime = view.findViewById(R.id.btnPickDateTime)
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime)
        tvSelectedDateTime.text = formatShow.format(calendar.time)
        selectedDateTime = format.format(calendar.time)

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->

                Picasso.get()
                    .load(selectedImageUri)
                    .fit()
                    .centerInside()
                    //.placeholder(R.drawable.default_event_img)
                    .error(R.drawable.default_event_img)
                    .into(eventImage)


                // Armazena o arquivo no eventRequest.image
                eventSelectedImageUri = selectedImageUri
            }
        }


        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                eventSelectedImageUri = currentPhotoUri
                Picasso.get()
                    .load(eventSelectedImageUri)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.default_event_img)
                    .error(R.drawable.default_event_img)
                    .into(eventImage)
            }
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_events)
        }

        setupLoadingAnimation(view)

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

        btnPickDateTime.setOnClickListener {
            pickDateTime()
        }

        eventSubmitButton.setOnClickListener {
            if(validateFields()){
                val eventName = eventNameEditText.text.toString()
                val eventDesc = eventDescEditText.text.toString()
                val eventLocal = eventLocalEditText.text.toString()

                val eventTransport = eventTransportCheckbox.isChecked

                // Criando o objeto EventResponse
                val event = EventAddRequest(
                    name = eventName,
                    description = eventDesc,
                    date = selectedDateTime,
                    location = eventLocal,
                    transport = eventTransport,
                    image = eventSelectedImageUri
                )

                val namePart = RequestBody.create(MultipartBody.FORM, event.name)
                val descriptionPart = RequestBody.create(MultipartBody.FORM, event.description)
                val datePart = RequestBody.create(MultipartBody.FORM, event.date)
                val locationPart = RequestBody.create(MultipartBody.FORM, event.location)
                val transportPart = RequestBody.create(MultipartBody.FORM, event.transport.toString())

                println("Fields validating, checking image...")

                var imagePart: MultipartBody.Part? = null

                if(event.image != null){
                    println("Image inserted")
                    val imageFile = File(getRealPathFromUri(event.image))
                    val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                    imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

                } else {
                    println("Using default image")
                    val emptyRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), ByteArray(0))
                    imagePart = MultipartBody.Part.createFormData("image", "", emptyRequestBody)

                }

                val call = RetrofitClient.apiService.addEvent(namePart, descriptionPart, datePart, locationPart, transportPart, imagePart)
                eventSubmitButton.visibility = View.GONE
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

                            setMyNeedRefresh(true)

                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.navigation_events, true)
                                .build()


                            findNavController().navigate(R.id.navigation_events, null, navOptions)

                            if (toast != null) {
                                toast!!.setText("Evento criado com sucesso.")
                            } else {
                                toast = Toast.makeText(requireActivity(), "Evento criado com sucesso.", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()
                        } else {
                            setLoadingVisibility(false)
                            eventSubmitButton.visibility = View.VISIBLE
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
        return view
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

    private fun pickDateTime() {
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                selectedDateTime = format.format(calendar.time)
                tvSelectedDateTime.text = formatShow.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

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
        private const val REQUEST_CODE_PICK_IMAGE = 2
    }
}