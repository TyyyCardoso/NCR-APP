package ipt.lei.dam.ncrapp.fragments.profile

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ProfileFragmento : BasicFragment() {
    private var profileSelectedImageUri: Uri? = null
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var currentPhotoUri: Uri
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
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
        val profileDataRegisto =  view.findViewById<TextView>(R.id.profileDataRegisto)
        val profileAbout = view.findViewById<TextView>(R.id.profileAboutMe)

        val editProfileImageButtons = view.findViewById<LinearLayout>(R.id.eventImageEditLayout)
        val editProfileImageChooseButton = view.findViewById<Button>(R.id.btnEditEventImageSelect)
        val editProfileImageTakePhotoButton = view.findViewById<Button>(R.id.btnEditEventImageCapture)
        val editProfileName = view.findViewById<EditText>(R.id.etEditProfileName)
        val editProfileAboutMe = view.findViewById<EditText>(R.id.editProfileAboutMe)

        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        var clientName = sharedPref.getString(getString(R.string.clientName), getString(R.string.profileDefaultName))
        val clientEmail = sharedPref.getString(getString(R.string.clientEmail), getString(R.string.profileDefaultEmail))
        val clientType = sharedPref.getString(getString(R.string.clientType), getString(R.string.member))
        val clientDataRegisto = sharedPref.getString(getString(R.string.clientRegistrationDate), getString(R.string.profileDefaultRegist))
        val clientImage = sharedPref.getString(getString(R.string.clientImage), "")
        val clientAbout = sharedPref.getString(getString(R.string.clientAbout), getString(R.string.profileDefaultAbout))

        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat  = SimpleDateFormat("dd-MM-yyyy")

        val date : Date = inputFormat.parse(clientDataRegisto)
        val formattedDate = outputFormat.format(date)

        profileName.text = clientName
        profileEmail.text = clientEmail
        profileTipoCliente.text = clientType.toString().uppercase()
        //profileValidated.text = if (clientValidated) "Sim" else "Não"
        profileDataRegisto.text = formattedDate
        profileAbout.text = clientAbout



        val url = "" + RetrofitClient.BASE_URL + "event/images/" + clientImage

        Glide.with(this)
            .load(url)
            .fitCenter()
            .centerInside()
            .error(R.drawable.baseline_account_circle_24)
            .into(profileImage)

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->

                Glide.with(this)
                    .load(selectedImageUri)
                    .fitCenter()
                    .centerInside()
                    .error(R.drawable.baseline_account_circle_24)
                    .into(profileImage)

                // Armazena o arquivo no eventRequest.image
                profileSelectedImageUri = selectedImageUri
            }

        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                profileSelectedImageUri = currentPhotoUri

                Glide.with(this)
                    .load(profileSelectedImageUri)
                    .fitCenter()
                    .centerInside()
                    .error(R.drawable.baseline_account_circle_24)
                    .into(profileImage)
            }
        }

        var editMode = false

        fab.setOnClickListener {

            editMode = !editMode

            if(editMode){
                editProfileImageButtons.visibility = View.VISIBLE
                profileName.visibility = View.GONE
                editProfileName.visibility = View.VISIBLE
                editProfileName.text = Editable.Factory.getInstance().newEditable(profileName.text)

                profileAbout.visibility = View.GONE
                editProfileAboutMe.visibility = View.VISIBLE
                if(!profileAbout.text.equals(getString(R.string.profileDefaultAbout))){
                    editProfileAboutMe.text = Editable.Factory.getInstance().newEditable(profileAbout.text)
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
                        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                            // Fornecer uma explicação adicional ao usuário
                            Toast.makeText(
                                context,
                                getString(R.string.cameraNeeded),
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
                if (editProfileName.text.toString().trim().isNotEmpty()) {
                    val newName = editProfileName.text.toString()
                    val newImage = profileSelectedImageUri
                    var newAbout = editProfileAboutMe.text.toString()

                    clientName = sharedPref.getString(getString(R.string.clientName), "")
                    val editor = sharedPref.edit()

                    if(newName != ""){
                        if(newName != clientName && newName != ""){
                            profileName.text = newName
                            editor.putString(getString(R.string.clientName), newName)
                        }

                        if(newAbout != ""){
                            profileAbout.text = newAbout
                            editor.putString(getString(R.string.clientAbout), newAbout)
                        } else {
                            newAbout = getString(R.string.profileDefaultAbout2)
                            profileAbout.text = newAbout
                            editor.putString(getString(R.string.clientAbout), newAbout)
                        }

                        updateProfile(newName, newImage, newAbout)

                        editor.apply()
                    }

                    editProfileImageButtons.visibility = View.GONE
                    profileName.visibility = View.VISIBLE
                    profileAbout.visibility = View.VISIBLE
                    editProfileName.visibility = View.GONE
                    editProfileAboutMe.visibility = View.GONE
                    fab.setImageResource(R.drawable.baseline_edit_24)
                } else {
                    editProfileName.error = getString(R.string.addEventTitleError)
                    if (toast != null) {
                        toast!!.setText(getString(R.string.fillFieldsCorrectly))
                    } else {
                        toast = Toast.makeText(
                            requireActivity(),
                            getString(R.string.fillFieldsCorrectly),
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


    private fun getOutputMediaFileUri(): Uri? {
        val context = requireActivity().applicationContext

        // Criando um arquivo de imagem no armazenamento interno do aplicativo
        val fileName = "my_image_${System.currentTimeMillis()}.jpg"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return try {
            val imageFile = File(storageDir, fileName)
            val imageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)

            imageUri
        } catch (e: IOException) {
            null
        }
    }

    private fun openCamera() {
        if(getOutputMediaFileUri() != null){
            currentPhotoUri = getOutputMediaFileUri()!!
            takePictureLauncher.launch(currentPhotoUri)
        }
    }

    private fun updateProfile(newName : String, newImage : Uri?, newAbout : String ){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString(getString(R.string.clientEmail), "")

        val namePart = newName.toRequestBody(MultipartBody.FORM)
        val aboutPart = newAbout.toRequestBody(MultipartBody.FORM)
        val emailPart = clientEmail!!.toRequestBody(MultipartBody.FORM)

        val imagePart: MultipartBody.Part?

        if(newImage != null){
            println("Image inserted")
            val imageFile = compressImage(newImage)
            val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        } else {
            println("Using default image")
            val emptyRequestBody =
                ByteArray(0).toRequestBody("image/*".toMediaTypeOrNull(), 0, 0)
            imagePart = MultipartBody.Part.createFormData("image", "", emptyRequestBody)

        }

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.editProfile(namePart, aboutPart, emailPart, imagePart).execute()
            },
            onSuccess = { responseBody ->

                if(responseBody.message.isNotBlank()){
                    val editor = sharedPref.edit()
                    editor.putString(getString(R.string.clientImage), responseBody.message)
                    editor.apply()
                    println("" + responseBody.code + " - " + responseBody.message)
                }

                if (toast != null) {
                    toast!!.setText(getString(R.string.profileEditadoSucesso))
                } else {
                    toast = Toast.makeText(
                        requireActivity(),
                        getString(R.string.profileEditadoSucesso),
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

}