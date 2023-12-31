package ipt.lei.dam.ncrapp.fragments.didyouknow

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.fragments.didyouknow.sabiasQueFragmento.Companion.setMyNeedRefresh
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DidYouKnowAddFragmento : BasicFragment() {
    private lateinit var newDidYouKnowTitle: TextView
    private lateinit var newDidYouKnowDescription: TextView
    private lateinit var newDidYouKnowReferences: TextView
    private lateinit var newDidYouKnowSubmitBtn: Button

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
        val view = inflater.inflate(R.layout.did_you_know_add_fragmento, container, false)

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE

        setupLoadingAnimation(view)

        backButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.navigation_sabias)
        }



        newDidYouKnowTitle = view.findViewById(R.id.etNewDidYouKnowTitle)
        newDidYouKnowDescription = view.findViewById(R.id.etNewDidYouKnowDescription)
        newDidYouKnowReferences = view.findViewById(R.id.etNewDidYouKnowReferences)
        newDidYouKnowSubmitBtn = view.findViewById(R.id.btnNewDidYouKnowSubmit)

        newDidYouKnowSubmitBtn.setOnClickListener {
            if(validateFields()){
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

                val didYouKnowTitle = newDidYouKnowTitle.text.toString()
                val didYouKnowDesc = newDidYouKnowReferences.text.toString()
                val didYouKnowRef = newDidYouKnowReferences.text.toString()

                val newDidYouKnow = DidYouKnowRequest(
                    id = -1,
                    title = didYouKnowTitle,
                    text = didYouKnowDesc,
                    references = didYouKnowRef,
                    createdAt = now.format(formatter),
                    updatedAt = now.format(formatter),
                )

                var doEventRequest = false

                doEventRequest = true
                if (doEventRequest) {
                    setLoadingVisibility(true)
                    makeRequestWithRetries(
                        requestCall = {
                            RetrofitClient.apiService.addDidYouKnow(newDidYouKnow).execute()
                        },
                        onSuccess = { isAdded ->
                            setLoadingVisibility(false)

                            setMyNeedRefresh(true)

                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.navigation_sabias, true)
                                .build()


                            findNavController().navigate(R.id.navigation_sabias, null, navOptions)

                            if (toast != null) {
                                toast!!.setText("Sabias Que criado com sucesso")
                            } else {
                                toast = Toast.makeText(requireActivity(), "Sabias Que criado com sucesso", Toast.LENGTH_SHORT)
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

    private fun validateFields():Boolean{
        if (newDidYouKnowTitle.text.toString().trim().isEmpty()) {
            newDidYouKnowTitle.error = "Introduza um titulo"
            return false
        }
        if (newDidYouKnowDescription.text.toString().trim().isEmpty()) {
            newDidYouKnowDescription.error = "Introduza uma descrição"
            return false
        }
        if (newDidYouKnowReferences.text.toString().trim().isEmpty()) {
            newDidYouKnowReferences.error = "Introduza uma referência"
            return false
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DidYouKnowAddFragmento().apply {
                arguments = Bundle().apply {

                }
            }
    }
}