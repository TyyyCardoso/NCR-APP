package ipt.lei.dam.ncrapp.activities.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.models.EventRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class EventAddFragmento : BasicFragment() {
    private var eventSelectedImage: String = ""
    private lateinit var eventNameEditText: EditText
    private lateinit var eventDescEditText: EditText
    private lateinit var eventLocalEditText: EditText

    private lateinit var btnPickDateTime: Button
    private lateinit var selectedDateTime: String
    private lateinit var tvSelectedDateTime: TextView
    private val calendar = Calendar.getInstance()

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
        eventNameEditText = view.findViewById<EditText>(R.id.etNewEventName)
        eventDescEditText = view.findViewById<EditText>(R.id.etNewEventDesc)
        eventLocalEditText = view.findViewById<EditText>(R.id.etNewEventLocal)
        val eventTransportCheckbox = view.findViewById<CheckBox>(R.id.checkboxNewEventTransport)
        val eventSubmitButton = view.findViewById<Button>(R.id.btnNewEventSubmit)
        val eventImageSelectButton = view.findViewById<Button>(R.id.btnNewEventImageSelect)
        val eventImagemCaptureButton =  view.findViewById<Button>(R.id.btnNewEventImageCapture)

        btnPickDateTime = view.findViewById(R.id.btnPickDateTime)
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime)
        tvSelectedDateTime.text = formatShow.format(calendar.time)
        selectedDateTime = format.format(calendar.time)

        setupLoadingAnimation(view)

        eventImageSelectButton.setOnClickListener {
            pickImageFromGallery()
        }

        eventImagemCaptureButton.setOnClickListener {
            captureImageFromCamera()
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

                // Obtendo a data e hora atual
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

                // Criando o objeto EventResponse
                val eventRequest = EventRequest(
                    id = -1,
                    name = eventName,
                    description = eventDesc,
                    date = selectedDateTime,
                    location = eventLocal,
                    transport = eventTransport,
                    createdAt = now.format(formatter),
                    updatedAt = now.format(formatter),
                    image = eventSelectedImage
                )

                var doEventRequest = false

                doEventRequest = true
                if (doEventRequest) {
                    setLoadingVisibility(true)
                    makeRequestWithRetries(
                        requestCall = {
                            RetrofitClient.apiService.addEvent(eventRequest).execute()
                        },
                        onSuccess = { isAdded ->
                            setLoadingVisibility(false)

                            val navController = findNavController()
                            navController.navigate(R.id.navigation_events)

                            if (toast != null) {
                                toast!!.setText("Evento criado com sucesso")
                            } else {
                                toast = Toast.makeText(requireActivity(), "Evento criado com sucesso", Toast.LENGTH_SHORT)
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
        return view
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
            val imageView = view?.findViewById<ImageView>(R.id.imageviewNewEventImageView)
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
        val imageView = view?.findViewById<ImageView>(R.id.imageviewNewEventImageView)
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
    }
}