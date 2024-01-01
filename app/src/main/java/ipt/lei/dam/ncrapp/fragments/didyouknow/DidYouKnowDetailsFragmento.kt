package ipt.lei.dam.ncrapp.fragments.didyouknow

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.fragments.didyouknow.sabiasQueFragmento.Companion.setMyNeedRefresh
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowAddRequest
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowEditRequest
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class DidYouKnowDetailsFragmento : BasicFragment() {
    /**
     * Componentes VIEW
     */
    //Text Fields
    private lateinit var didYouKnowTitle: TextView
    private lateinit var didYouKnowDescription: TextView
    private lateinit var didYouKnowReferences: TextView

    //Others
    private lateinit var didYouKnow: DidYouKnowResponse

    /**
     * Componentes EDIT
     */
    //Text Fields
    private lateinit var etDidYouKnowTitle: EditText
    private lateinit var etDidYouKnowDescription: EditText
    private lateinit var etDidYouKnowReferences: EditText

    //Others
    private var editMode : Boolean = false
    private lateinit var btnDidYouKnowSubmit: Button

    /**
     * Componentes DELETE
     */
    private lateinit var btnDidYouKnowDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.did_you_know_details_fragmento, container, false)

        setupLoadingAnimation(view)

        /**
         * REFERENCES TO UI VIEW
         */
        //Text Fiels
        didYouKnowTitle = view.findViewById(R.id.DidYouKnowDetailTitle)
        didYouKnowDescription = view.findViewById(R.id.DidYouKnowDetailDescription)
        didYouKnowReferences = view.findViewById(R.id.DidYouKnowDetailReferences)

        //Others
        val fabEditDidYouKnow = view.findViewById<FloatingActionButton>(R.id.fabEditDidYouKnow)
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE

        /**
         * REFERENCES TO UI EDIT
         */
        //Text Fiels
        etDidYouKnowTitle = view.findViewById(R.id.etEditDidYouKnowTitle)
        etDidYouKnowDescription = view.findViewById(R.id.etEditDidYouKnowDescription)
        etDidYouKnowReferences = view.findViewById(R.id.etEditDidYouKnowReferences)

        //Others
        btnDidYouKnowSubmit = view.findViewById(R.id.btnEditDidYouKnowSubmit)

        /**
         * REFERENCES TO UI DELETE
         */
        btnDidYouKnowDelete = view.findViewById(R.id.btnDeleteDidYouKnow)

        /**
         * ClickListeners base
         */
        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        if(!clientType.equals("ADMINISTRADOR")){
            fabEditDidYouKnow.visibility = View.GONE;
        }else{
            fabEditDidYouKnow.visibility = View.VISIBLE;
            fabEditDidYouKnow.setOnClickListener {
                toggleEditMode()
            }
        }

        backButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.navigation_sabias)
        }

        //obter do bundle
        didYouKnow = arguments?.getParcelable<DidYouKnowResponse>("myDidYouKow")!!

        /**
         * Carregar eventos para a UI
         */
        didYouKnowTitle.text = didYouKnow?.title
        didYouKnowDescription.text = didYouKnow?.text
        didYouKnowReferences.text = didYouKnow?.references


        return view
    }

    /**
     *
     * Função trocar entre VIEWMODE ou EDITMODE
     *
     */
    private fun toggleEditMode() {
        editMode = !editMode
        if (editMode) {
            didYouKnowTitle.visibility = View.GONE
            didYouKnowDescription.visibility = View.GONE
            didYouKnowReferences.visibility = View.GONE

            etDidYouKnowTitle.visibility = View.VISIBLE
            etDidYouKnowTitle.text = Editable.Factory.getInstance().newEditable(didYouKnow.title)

            etDidYouKnowDescription.visibility = View.VISIBLE
            etDidYouKnowDescription.text = Editable.Factory.getInstance().newEditable(didYouKnow.text)

            etDidYouKnowReferences.visibility = View.VISIBLE
            etDidYouKnowReferences.text = Editable.Factory.getInstance().newEditable(didYouKnow.references)

            btnDidYouKnowSubmit.visibility = View.VISIBLE
            btnDidYouKnowDelete.visibility = View.VISIBLE

            btnDidYouKnowSubmit.setOnClickListener {
                if(editMode) {
                    saveDidYouKnowBD()
                }
            }

            btnDidYouKnowDelete.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Aviso")
                    .setMessage("Tem a certeza que quer apagar este Sabias que?")
                    .setNeutralButton("Não") { dialog, which ->
                    }
                    .setPositiveButton("Apagar") { dialog, which ->
                        deleteDidYouKnow()
                    }
                    .show()
            }


        } else {
            didYouKnowTitle.visibility = View.VISIBLE
            didYouKnowDescription.visibility = View.VISIBLE
            didYouKnowReferences.visibility = View.VISIBLE

            etDidYouKnowTitle.visibility = View.GONE
            etDidYouKnowDescription.visibility = View.GONE
            etDidYouKnowReferences.visibility = View.GONE

            btnDidYouKnowSubmit.visibility = View.GONE
            btnDidYouKnowDelete.visibility = View.GONE
        }
    }

    private fun saveDidYouKnowBD(){
        if(validateFields()){
            btnDidYouKnowSubmit.visibility = View.GONE
            setLoadingVisibility(true)

            didYouKnow.title = etDidYouKnowTitle.text.toString()
            didYouKnow.text = etDidYouKnowDescription.text.toString()
            didYouKnow.references = etDidYouKnowReferences.text.toString()

            //Construir objeto
            val didYouKnowRequest = DidYouKnowEditRequest(
                id = didYouKnow.id!!,
                title = didYouKnow.title!!,
                text = didYouKnow.text!!,
                references = didYouKnow.references!!,
                createdAt = didYouKnow.createdAt.toString()
            )

            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.editDidYouKnow(didYouKnowRequest).execute()
                },
                onSuccess = { isEditted ->
                    //Atualizar loading
                    setLoadingVisibility(false)

                    //Informar via Toast
                    if (toast != null) {
                        toast!!.setText("Sabias Que editado com sucesso")
                    } else {
                        toast = Toast.makeText(requireActivity(), "Sabias Que editado com sucesso", Toast.LENGTH_SHORT)
                    }
                    toast!!.show()

                    //Informar que deve atualizar recyclerView
                    setMyNeedRefresh(true)

                    //Redirecionar
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
                    btnDidYouKnowSubmit.visibility = View.GONE
                }
            )


        }
    }

    private fun deleteDidYouKnow(){
        btnDidYouKnowDelete.visibility = View.GONE
        setLoadingVisibility(true)

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.deleteDidYouKnow(didYouKnow.id!!).execute()
            },
            onSuccess = { isEditted ->
                //Atualizar loading
                setLoadingVisibility(false)

                //Informar via toast
                if (toast != null) {
                    toast!!.setText("Sabias Que removido com sucesso")
                } else {
                    toast = Toast.makeText(requireActivity(), "Sabias Que removido com sucesso", Toast.LENGTH_SHORT)
                }
                toast!!.show()

                //Informar que deve atualizar recyclerView
                setMyNeedRefresh(true)

                //Redirecionar
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
                btnDidYouKnowDelete.visibility = View.VISIBLE
            }
        )
    }

    /**
     *
     * Função para validar campos
     *
     */
    private fun validateFields():Boolean{
        if (etDidYouKnowTitle.text.toString().trim().isEmpty()) {
            etDidYouKnowTitle.error = "Introduza um titulo"
            return false
        }
        if (etDidYouKnowDescription.text.toString().trim().isEmpty()) {
            etDidYouKnowDescription.error = "Introduza uma descrição"
            return false
        }
        if (etDidYouKnowReferences.text.toString().trim().isEmpty()) {
            etDidYouKnowReferences.error = "Introduza uma referência"
            return false
        }
        return true
    }
}