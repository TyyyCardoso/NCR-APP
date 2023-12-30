package ipt.lei.dam.ncrapp.activities.navigation

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.models.SubscribeEventRequest
import ipt.lei.dam.ncrapp.models.UpdateProfileRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.io.ByteArrayOutputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [settingsFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragmento : BasicFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setupLoadingAnimation(view)
        val fab: FloatingActionButton = view.findViewById(R.id.fabEditProfile)

        val profileImage = view.findViewById<ImageView>(R.id.imageProfile)
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail =  view.findViewById<TextView>(R.id.profileEmail)
        val profileTipoCliente =  view.findViewById<TextView>(R.id.profileTipoCliente)
        val profileValidated =  view.findViewById<TextView>(R.id.profileValidado)
        val profileDataRegisto =  view.findViewById<TextView>(R.id.profileDataRegisto)
        val profileAbout = view.findViewById<TextView>(R.id.profileAboutMe)

        val editProfileImageButtons = view.findViewById<LinearLayout>(R.id.eventImageEditLayout)
        val editProfileImageChooseButton = view.findViewById<Button>(R.id.btnEditEventImageSelect)
        val editProfileImageTakePhotoButton = view.findViewById<Button>(R.id.btnEditEventImageCapture)
        val EditProfileName = view.findViewById<EditText>(R.id.etEditProfileName)
        val EditProfileAboutMe = view.findViewById<EditText>(R.id.editProfileAboutMe)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        var clientName = sharedPref.getString("clientName", "Erro a obter o seu nome.");
        val clientEmail = sharedPref.getString("clientEmail", "Erro a obter o seu email.");
        val clientType = sharedPref.getString("clientType", "member");
        val clientValidated = sharedPref.getBoolean("clientValidated", true);
        val clientDataRegisto = sharedPref.getString("clientRegistrationDate", "Erro ao obter a sua data de registo");
        val clientImage = sharedPref.getString("clientImage", "");
        var clientAbout = sharedPref.getString("clientAbout", "Say something about you...");

        profileName.text = clientName
        profileEmail.text = clientEmail
        profileTipoCliente.text = clientType.toString().uppercase()
        profileValidated.text = if (clientValidated) "Sim" else "Não"
        profileDataRegisto.text = clientDataRegisto
        if(clientImage.toString().isNotEmpty()){
            // Decodificar base64 para um array de bytes
            val decodedString: ByteArray = Base64.decode(clientImage, Base64.DEFAULT)

            // Converter array de bytes em Bitmap
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            // Definir o Bitmap no ImageView
            profileImage.setImageBitmap(decodedByte)
        }
        profileAbout.text = clientAbout

        var base64String = "";

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {

                // Abre um InputStream para a URI da imagem
                val inputStream = requireActivity().contentResolver.openInputStream(uri)
                // Converte o InputStream em Bitmap
                val bitmap = BitmapFactory.decodeStream(inputStream)
                profileImage.setImageBitmap(bitmap)
                inputStream?.close()

                // Prepara o OutputStream para a conversão
                val outputStream = ByteArrayOutputStream()
                // Comprime o Bitmap em JPEG (ou PNG) no OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                // Converte o OutputStream em um array de bytes
                val imageBytes = outputStream.toByteArray()

                // Codifica os bytes da imagem em Base64 e obtém a String resultante
                base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                profileImage.setImageURI(currentPhotoUri)

                // Abre um InputStream para a URI da imagem
                val inputStream = requireActivity().contentResolver.openInputStream(currentPhotoUri)
                // Converte o InputStream em Bitmap
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Prepara o OutputStream para a conversão
                val outputStream = ByteArrayOutputStream()
                // Comprime o Bitmap em JPEG (ou PNG) no OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                // Converte o OutputStream em um array de bytes
                val imageBytes = outputStream.toByteArray()

                // Codifica os bytes da imagem em Base64 e obtém a String resultante
                base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            }
        }

        var editMode = false;

        fab.setOnClickListener {

            editMode = !editMode

            if(editMode){
                editProfileImageButtons.visibility = View.VISIBLE

                profileName.visibility = View.GONE
                profileAbout.visibility = View.GONE

                EditProfileName.visibility = View.VISIBLE
                EditProfileAboutMe.visibility = View.VISIBLE

                EditProfileName.setText(profileName.text)
                EditProfileAboutMe.setText(profileAbout.text)


                fab.setImageResource(R.drawable.baseline_check_24)
                editProfileImageChooseButton.setOnClickListener {
                    getContent.launch("image/*")
                }
                editProfileImageTakePhotoButton.setOnClickListener {
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
            }else{

                val newName = EditProfileName.text.toString()
                val newImage = base64String
                val newAbout = EditProfileAboutMe.text.toString()

                clientName = sharedPref.getString("clientName", "");
                clientAbout = sharedPref.getString("clientAbout", "");
                val editor = sharedPref.edit()

                if(!newName.equals(clientName) && !newName.equals("") || !newImage.equals("") || !newAbout.equals(clientAbout) && !newAbout.equals("")){
                    updateProfile(newName, newImage, newAbout)

                    if(!newName.equals(clientName) && !newName.equals("")){
                        profileName.text = newName
                        editor.putString("clientName", newName)
                    }

                    if(!newImage.equals("")){
                        editor.putString("clientImage", newImage)
                    }

                    if(!newAbout.equals(clientAbout) && !newAbout.equals("")){
                        profileAbout.text = newAbout
                        editor.putString("clientAbout", newAbout)
                    }

                    editor.apply()

                }

                editProfileImageButtons.visibility = View.GONE
                profileName.visibility = View.VISIBLE
                profileAbout.visibility = View.VISIBLE
                EditProfileName.visibility = View.GONE
                EditProfileAboutMe.visibility = View.GONE
                fab.setImageResource(R.drawable.baseline_edit_24)


            }


        }

        // Inflate the layout for this fragment
        return view
    }

    private fun getOutputMediaFileUri(): Uri {
        val contentResolver = requireActivity().applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "my_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private fun openCamera() {
        currentPhotoUri = getOutputMediaFileUri()
        takePictureLauncher.launch(currentPhotoUri)
    }

    fun updateProfile(newName : String?, newImage : String?, newAbout : String? ){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "");

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.editProfile(UpdateProfileRequest(newName, newImage, clientEmail, newAbout)).execute()
            },
            onSuccess = { editProfileResponse ->
                Toast.makeText(requireContext(), "Perfil editado com sucesso.", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMessage ->
                println(errorMessage)
                setLoadingVisibility(false)
            }
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragmento().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}