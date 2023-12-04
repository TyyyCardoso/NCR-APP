package ipt.lei.dam.ncrapp.activities.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.models.EventRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EventAddFragmento : BasicFragment() {
    private var eventSelectedImage: String = ""

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
        val eventNameEditText = view.findViewById<EditText>(R.id.etNewEventName)
        val eventDescEditText = view.findViewById<EditText>(R.id.etNewEventDesc)
        val eventLocalEditText = view.findViewById<EditText>(R.id.etNewEventLocal)
        val eventDatePicker = view.findViewById<DatePicker>(R.id.etNewEventDate)
        val eventTransportCheckbox = view.findViewById<CheckBox>(R.id.checkboxNewEventTransport)
        val eventSubmitButton = view.findViewById<Button>(R.id.btnNewEventSubmit)
        val EventImageSelectButton = view.findViewById<Button>(R.id.btnNewEventImageSelect)

        setupLoadingAnimation(view)

        EventImageSelectButton.setOnClickListener {
            pickImageFromGallery()
        }

        eventSubmitButton.setOnClickListener {
            val eventName = eventNameEditText.text.toString()
            val eventDesc = eventDescEditText.text.toString()
            val eventLocal = eventLocalEditText.text.toString()

            // Convertendo DatePicker para LocalDateTime
            val eventDate = LocalDateTime.of(
                eventDatePicker.year,
                eventDatePicker.month + 1,
                eventDatePicker.dayOfMonth,
                0, 0
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val formattedDate = eventDate.format(formatter)

            val eventTransport = eventTransportCheckbox.isChecked

            // Obtendo a data e hora atual
            val now = LocalDateTime.now()

            // Criando o objeto EventResponse
            val eventRequest = EventRequest(
                name = eventName,
                description = eventDesc,
                date = formattedDate,
                location = eventLocal,
                transport = eventTransport,
                createAt = now.format(formatter),
                updatedAt = now.format(formatter),
                image = eventSelectedImage // Ou o caminho da imagem, se você estiver permitindo o upload de imagens
            )

            println(eventRequest.toString())

            // Agora você pode usar eventResponse conforme necessário
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
                        val bundle = Bundle().apply {
                            putString("eventName", eventRequest.name ?: "Nome não disponível")
                            putString("eventDescription", eventRequest.description ?: "Descrição não disponível")
                            putString("eventDate", eventRequest.date?.toString() ?: "Data não disponível")
                            putString("eventLocation", eventRequest.location ?: "Localização não disponível")
                            putBoolean("eventTransport", eventRequest.transport ?: false) // false como valor padrão
                            putString("eventCreatedAt", eventRequest.createAt?.toString() ?: "Data de criação não disponível")
                            putString("eventUpdatedAt", eventRequest.updatedAt?.toString() ?: "Data de atualização não disponível")
                            putString("eventImage", eventRequest.image ?: "")
                        }
                        val navController = findNavController()
                        navController.navigate(R.id.navigation_events_details, bundle)

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
        return view
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    // Dentro do seu Fragmento
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            // Faça algo com o Uri da imagem, como mostrar em um ImageView
            updateImageView(imageUri)
        }
    }

    private fun updateImageView(uri: Uri?) {
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
        private const val REQUEST_CODE_PICK_IMAGE = 2
    }
}