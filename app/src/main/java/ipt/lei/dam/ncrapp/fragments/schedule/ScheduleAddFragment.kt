package ipt.lei.dam.ncrapp.fragments.schedule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.schedule.ScheduleAddResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileNotFoundException

class ScheduleAddFragment : BasicFragment() {

    private var uri: Uri? = null
    private lateinit var bntSelectFile : Button
    private val fileType = "application/pdf"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_add, container, false)
        setupLoadingAnimation(view)

        bntSelectFile = view.findViewById(R.id.btnSelectFile)

        val docNameTextView = view.findViewById<TextView>(R.id.nomeDoFicheiro)
        val btnNewDidYouKnowSubmit = view.findViewById<Button>(R.id.btnNewDidYouKnowSubmit)

        bntSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = fileType  // Set the desired file type (e.g., "application/pdf" for PDFs)
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
            val docName = schedule.docName.toRequestBody(MultipartBody.FORM)
            val docDescription = schedule.docDescription.toRequestBody(MultipartBody.FORM)
            val docType = schedule.docType.toRequestBody(MultipartBody.FORM)
            val pdf : MultipartBody.Part?

            val inputStream = requireContext().contentResolver.openInputStream(schedule.pdf!!) ?: throw FileNotFoundException()

            // Create a temporary file
            val tempFile = File.createTempFile("prefix_", ".pdf", requireContext().cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }

            //Se foi introduzida uma imagem
            val fileRequestBody = tempFile.asRequestBody(fileType.toMediaTypeOrNull())
            pdf = MultipartBody.Part.createFormData("pdf", tempFile.name, fileRequestBody)

            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.uploadDoc(docName, docDescription, docType, pdf).execute()
                },
                onSuccess = {
                    toast = Toast.makeText(requireContext(), getString(R.string.documentSubmited) ,Toast.LENGTH_SHORT)
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
            bntSelectFile.alpha = 0.5f
            bntSelectFile.text = getString(R.string.selectOtherFile)
        }
    }

}