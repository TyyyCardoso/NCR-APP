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
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowAddRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient


class DidYouKnowAddFragmento : BasicFragment() {
    //Text Fields
    private lateinit var newDidYouKnowTitle: TextView
    private lateinit var newDidYouKnowDescription: TextView
    private lateinit var newDidYouKnowReferences: TextView

    //Others
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
        val view = inflater.inflate(R.layout.did_you_know_add_fragmento, container, false)

        /**
         * REFERENCES TO UI
         */
        //Text Fields
        newDidYouKnowTitle = view.findViewById(R.id.etNewDidYouKnowTitle)
        newDidYouKnowDescription = view.findViewById(R.id.etNewDidYouKnowDescription)
        newDidYouKnowReferences = view.findViewById(R.id.etNewDidYouKnowReferences)

        //Others
        newDidYouKnowSubmitBtn = view.findViewById(R.id.btnNewDidYouKnowSubmit)
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE

        setupLoadingAnimation(view)

        /**
         * ClickListeners
         */
        backButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.navigation_sabias)
        }

        newDidYouKnowSubmitBtn.setOnClickListener {
            saveDidYouKnow()
        }

        return view
    }

    /**
     * Função para guardar sabias que
     */
    private fun saveDidYouKnow(){
        if(validateFields()){
            setLoadingVisibility(true)
            newDidYouKnowSubmitBtn.visibility = View.GONE

            val didYouKnowTitle = newDidYouKnowTitle.text.toString()
            val didYouKnowDesc = newDidYouKnowDescription.text.toString()
            val didYouKnowRef = newDidYouKnowReferences.text.toString()

            // Criando o objeto
            val newDidYouKnow = DidYouKnowAddRequest(
                title = didYouKnowTitle,
                text = didYouKnowDesc,
                references = didYouKnowRef
            )

            //Iniciar chamada à API
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.addDidYouKnow(newDidYouKnow).execute()
                },
                onSuccess = { isAdded ->
                    //Atualizar loading
                    setLoadingVisibility(false)

                    //Informar via Toast
                    if (toast != null) {
                        toast!!.setText("Sabias Que criado com sucesso")
                    } else {
                        toast = Toast.makeText(requireActivity(), "Sabias Que criado com sucesso", Toast.LENGTH_SHORT)
                    }
                    toast!!.show()

                    //Informar que deve atualizar recyclerView
                    sabiasQueFragmento.setMyNeedRefresh(true)

                    //Redirecionar para eventos
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_sabias, true)
                        .build()
                    findNavController().navigate(R.id.navigation_sabias, null, navOptions)
                },
                onError = { errorMessage ->
                    if (toast != null) {
                        toast!!.setText(errorMessage)
                    } else {
                        toast = Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                    setLoadingVisibility(false)
                    newDidYouKnowSubmitBtn.visibility = View.VISIBLE
                }
            )

        }
    }

    /**
     *
     * Função para validar campos
     *
     */
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
}