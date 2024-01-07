package ipt.lei.dam.ncrapp.fragments.schedule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.network.RetrofitClient

class ScheduleAddFragment : BasicFragment() {

    var uri: Uri? = null

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

        val btnSelectFile = requireActivity().findViewById<ImageView>(R.id.btnSelectFile)

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"  // Set the desired file type (e.g., "application/pdf" for PDFs)
            startActivityForResult(intent, 3)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            uri = data?.data // Get the file's URI

        }
    }

    fun uploadDocument(){
        /*makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.uploadDoc(uri).execute()
            },
            onSuccess = { uploadDocumentResponse ->
                toast = Toast.makeText(requireContext(), "Documento upload com sucesso" ,Toast.LENGTH_SHORT)
            },
            onError = { errorMessage ->
                if (toast != null) {
                    toast!!.setText(errorMessage)
                } else {
                    toast = Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                }
                toast!!.show()
            }
        )*/
    }

    companion object {
    }
}