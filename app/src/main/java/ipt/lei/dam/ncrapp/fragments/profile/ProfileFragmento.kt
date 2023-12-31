package ipt.lei.dam.ncrapp.fragments.profile

import android.Manifest
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass.
 * Use the [settingsFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragmento : BasicFragment() {
    private var profileSelectedImageUri: Uri? = null
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

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
        val clientType = sharedPref.getString("clientType", "Membro");
        val clientValidated = sharedPref.getBoolean("clientValidated", true);
        val clientDataRegisto = sharedPref.getString("clientRegistrationDate", "Erro ao obter a sua data de registo");
        val clientImage = sharedPref.getString("clientImage", "");
        val clientAbout = sharedPref.getString("clientAbout", "Diz algo sobre ti...");

        var inputFormat = SimpleDateFormat("yyyy-MM-dd");
        var outputFormat  = SimpleDateFormat("dd-MM-yyyy");

        var date : Date = inputFormat.parse(clientDataRegisto);
        var formattedDate = outputFormat.format(date);

        profileName.text = clientName
        profileEmail.text = clientEmail
        profileTipoCliente.text = clientType.toString().uppercase()
        //profileValidated.text = if (clientValidated) "Sim" else "Não"
        profileDataRegisto.text = formattedDate
        profileAbout.text = clientAbout



        val url = "" + RetrofitClient.BASE_URL + "event/images/" + clientImage

        Picasso.get()
            .load(url)
            .fit()
            .centerInside()
            //.placeholder(R.drawable.default_event_img)
            .error(R.drawable.baseline_account_circle_24)
            .into(profileImage)

        var base64String = "";

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->

                Picasso.get()
                    .load(selectedImageUri)
                    .fit()
                    .centerInside()
                    //.placeholder(R.drawable.default_event_img)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(profileImage)


                // Armazena o arquivo no eventRequest.image
                profileSelectedImageUri = selectedImageUri
            }

        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                profileSelectedImageUri = currentPhotoUri
                Picasso.get()
                    .load(profileSelectedImageUri)
                    .fit()
                    .centerInside()
                    //.placeholder(R.drawable.default_event_img)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(profileImage)
            }
        }

        var editMode = false;

        fab.setOnClickListener {

            editMode = !editMode

            if(editMode){
                editProfileImageButtons.visibility = View.VISIBLE
                profileName.visibility = View.GONE
                EditProfileName.visibility = View.VISIBLE
                EditProfileName.text = Editable.Factory.getInstance().newEditable(profileName.text)

                profileAbout.visibility = View.GONE
                EditProfileAboutMe.visibility = View.VISIBLE
                if(!profileAbout.text.equals("Diz algo sobre ti...")){
                    EditProfileAboutMe.text = Editable.Factory.getInstance().newEditable(profileAbout.text)
                }


                fab.setImageResource(R.drawable.baseline_check_24)
                editProfileImageChooseButton.setOnClickListener {
                    getContent.launch("image/*")
                }
                editProfileImageTakePhotoButton.setOnClickListener {
                    when {
                        ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // Permissão já concedida, pode abrir a câmera
                            openCamera()
                        }
                        shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                            // Fornecer uma explicação adicional ao usuário
                            Toast.makeText(
                                context,
                                "A câmera é necessária para capturar fotos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            // Solicitar permissão
                            requestCameraPermission()
                        }
                    }
                }
            }else{
                if (!EditProfileName.text.toString().trim().isEmpty()) {
                    val newName = EditProfileName.text.toString()
                    val newImage = profileSelectedImageUri
                    var newAbout = EditProfileAboutMe.text.toString()

                    clientName = sharedPref.getString("clientName", "");
                    val editor = sharedPref.edit()

                    if(!newName.equals("")){
                        if(!newName.equals(clientName) && !newName.equals("")){
                            profileName.text = newName
                            editor.putString("clientName", newName)
                        }

                        if(!newAbout.equals("")){
                            profileAbout.text = newAbout
                            editor.putString("clientAbout", newAbout)
                        } else {
                            newAbout = "Algo sobre mim..."
                            profileAbout.text = newAbout
                            editor.putString("clientAbout", newAbout)
                        }

                        updateProfile(newName, newImage, newAbout)

                        editor.apply()
                    }

                    editProfileImageButtons.visibility = View.GONE
                    profileName.visibility = View.VISIBLE
                    profileAbout.visibility = View.VISIBLE
                    EditProfileName.visibility = View.GONE
                    EditProfileAboutMe.visibility = View.GONE
                    fab.setImageResource(R.drawable.baseline_edit_24)
                } else {
                    EditProfileName.error = "Introduza um nome"
                    if (toast != null) {
                        toast!!.setText("Preencher campos corretamente!")
                    } else {
                        toast = Toast.makeText(
                            requireActivity(),
                            "Preencher campos corretamente!",
                            Toast.LENGTH_SHORT
                        )
                    }
                    toast!!.show()
                }
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

    fun updateProfile(newName : String, newImage : Uri?, newAbout : String ){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "");

        val namePart = RequestBody.create(MultipartBody.FORM, newName)
        val aboutPart = RequestBody.create(MultipartBody.FORM, newAbout)
        val emailPart = RequestBody.create(MultipartBody.FORM, clientEmail!!)

        var imagePart: MultipartBody.Part? = null

        if(newImage != null){
            println("Image inserted")
            val imageFile = compressImage(newImage)
            val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        } else {
            println("Using default image")
            val emptyRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), ByteArray(0))
            imagePart = MultipartBody.Part.createFormData("image", "", emptyRequestBody)

        }

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.editProfile(namePart, aboutPart, emailPart, imagePart).execute()
            },
            onSuccess = { responseBody ->

                if(responseBody.message.isNotBlank()){
                    val editor = sharedPref.edit()
                    editor.putString("clientImage", responseBody.message)
                    editor.apply()
                    println("" + responseBody.code + " - " + responseBody.message)
                }

                if (toast != null) {
                    toast!!.setText("Perfil editado com sucesso")
                } else {
                    toast = Toast.makeText(
                        requireActivity(),
                        "Perfil editado com sucesso",
                        Toast.LENGTH_SHORT
                    )
                }
                toast!!.show()
            },
            onError = { errorMessage ->
                //println(errorMessage)
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

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val filePath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()
        return filePath
    }

}