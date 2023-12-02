package ipt.lei.dam.ncrapp.activities.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.EventsAdapter
import ipt.lei.dam.ncrapp.models.EventRequest
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import org.json.JSONObject
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventAddFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventAddFragmento : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var loadingImage: ImageView
    private lateinit var rotationAnimation: Animation
    private var eventSelectedImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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
                        print("yes")
                        setLoadingVisibility(false)

                    },
                    onError = { errorMessage ->
                        println(errorMessage)
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
                    eventSelectedImage = myBase64
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

    private fun setupLoadingAnimation(view: View) {
        loadingImage = view.findViewById(R.id.loading_image)
        rotationAnimation = AnimationUtils.loadAnimation(this.context, R.anim.rotate_loading)
    }

    protected fun setLoadingVisibility(visible: Boolean) {
        if (visible) {
            loadingImage.visibility = View.VISIBLE
            loadingImage.startAnimation(rotationAnimation)
        } else {
            loadingImage.visibility = View.GONE
            loadingImage.clearAnimation()
        }
    }

    fun <T> makeRequestWithRetries(
        requestCall: () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        maxAttempts: Int = 3
    ) {
        setLoadingVisibility(true)
        Thread {
            var attemptCount = 0
            var successful = false

            while (attemptCount < maxAttempts && !successful) {
                try {
                    val response = requestCall()
                    if (response.isSuccessful) {
                        activity?.runOnUiThread {
                            onSuccess(response.body()!!)
                            setLoadingVisibility(false)
                        }
                        successful = true
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = JSONObject(errorBody).getString("message")
                        activity?.runOnUiThread {
                            onError(errorMessage)
                            setLoadingVisibility(false)
                        }
                        break // Não tentar novamente em caso de erro de resposta HTTP
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    attemptCount++
                    if (attemptCount >= maxAttempts) {
                        activity?.runOnUiThread {
                            onError("Erro ao tentar conectar. Por favor, tente novamente.")
                            setLoadingVisibility(false)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        onError("Erro inesperado. Por favor, tente novamente.")
                        setLoadingVisibility(false)
                    }
                    break
                }
            }
        }.start()
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 2
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventAddFragmento.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventAddFragmento().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}