package ipt.lei.dam.ncrapp.fragments.schedule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.schedule.ScheduleAddResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException

class ScheduleAddFragment : BasicFragment() {

    var uri: Uri? = null
    final lateinit var bntSelectFile : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_add, container, false)
        setupLoadingAnimation(view)

        bntSelectFile = view.findViewById<Button>(R.id.btnSelectFile)

        val docNameTextView = view.findViewById<TextView>(R.id.nomeDoFicheiro)
        val btnNewDidYouKnowSubmit = view.findViewById<Button>(R.id.btnNewDidYouKnowSubmit)

        bntSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"  // Set the desired file type (e.g., "application/pdf" for PDFs)
            startActivityForResult(intent, 3)
        }



        btnNewDidYouKnowSubmit.setOnClickListener {
            val schedule = ScheduleAddResponse(
                docName = docNameTextView.text.toString(),
                docDescription = "",
                docType = "1",
                pdf = uri
            )

            //Contruir parts com toda a info do event
            val docName = RequestBody.create(MultipartBody.FORM, schedule.docName)
            val docDescription = RequestBody.create(MultipartBody.FORM, schedule.docDescription)
            val docType = RequestBody.create(MultipartBody.FORM, schedule.docType)
            var pdf : MultipartBody.Part? = null

            val inputStream = requireContext().contentResolver.openInputStream(schedule.pdf!!) ?: throw FileNotFoundException()

            // Create a temporary file
            val tempFile = File.createTempFile("prefix_", ".pdf", requireContext().cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }

            //Se foi introduzida uma imagem
            val fileRequestBody = RequestBody.create("application/pdf".toMediaTypeOrNull(), tempFile)
            pdf = MultipartBody.Part.createFormData("pdf", tempFile.name, fileRequestBody)

            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.uploadDoc(docName, docDescription, docType, pdf).execute()
                },
                onSuccess = { uploadDocumentResponse ->
                    toast = Toast.makeText(requireContext(), "Documento upload com sucesso" ,Toast.LENGTH_SHORT)
                    toast!!.show()
                    findNavController().navigate(R.id.navigation_schedule)

                },
                onError = { errorMessage ->
                    if (toast != null) {
                        toast!!.setText(errorMessage)
                    } else {
                        toast = Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                }
            )
        }


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            uri = data?.data // Get the file's UR
            bntSelectFile.text = "Selecionar outro ficheiro"
        }
    }

    companion object {
    }
}