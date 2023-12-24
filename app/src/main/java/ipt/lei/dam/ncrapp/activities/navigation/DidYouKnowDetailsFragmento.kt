package ipt.lei.dam.ncrapp.activities.navigation

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
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.models.DidYouKnowRequest
import ipt.lei.dam.ncrapp.models.DidYouKnowResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class DidYouKnowDetailsFragmento : BasicFragment() {
    private var editMode : Boolean = false

    private lateinit var didYouKnow: DidYouKnowResponse

    //Componentes VIEW
    private lateinit var didYouKnowTitle: TextView
    private lateinit var didYouKnowDescription: TextView
    private lateinit var didYouKnowReferences: TextView

    //Componentes EDIT
    private lateinit var etDidYouKnowTitle: EditText
    private lateinit var etDidYouKnowDescription: EditText
    private lateinit var etDidYouKnowReferences: EditText
    private lateinit var btnDidYouKnowSubmit: Button
    private val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatShow = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())


    //Componentes DELETE
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
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.did_you_know_details_fragmento, container, false)

        setupLoadingAnimation(view)

        val fabEditDidYouKnow = view.findViewById<FloatingActionButton>(R.id.fabEditDidYouKnow)
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE
        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");


        backButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.navigation_sabias)
        }



        didYouKnow = arguments?.getParcelable<DidYouKnowResponse>("myDidYouKow")!!

        //Componentes VIEW
        didYouKnowTitle = view.findViewById(R.id.DidYouKnowDetailTitle)
        didYouKnowDescription = view.findViewById(R.id.DidYouKnowDetailDescription)
        didYouKnowReferences = view.findViewById(R.id.DidYouKnowDetailReferences)

        //Componentes EDIT
        etDidYouKnowTitle = view.findViewById(R.id.etEditDidYouKnowTitle)
        etDidYouKnowDescription = view.findViewById(R.id.etEditDidYouKnowDescription)
        etDidYouKnowReferences = view.findViewById(R.id.etEditDidYouKnowReferences)
        btnDidYouKnowSubmit = view.findViewById(R.id.btnEditDidYouKnowSubmit)

        //Componentes DELETE
        btnDidYouKnowDelete = view.findViewById(R.id.btnDeleteDidYouKnow)

        didYouKnowTitle.text = didYouKnow?.title
        didYouKnowDescription.text = didYouKnow?.text
        didYouKnowReferences.text = didYouKnow?.references

        if(!clientType.equals("admin")){
            fabEditDidYouKnow.visibility = View.GONE;
        }else{
            fabEditDidYouKnow.visibility = View.VISIBLE;
            fabEditDidYouKnow.setOnClickListener {
                toggleEditMode()
            }
        }

        return view
    }

    private fun toggleEditMode() {
        editMode = !editMode
        if (editMode) {
            didYouKnowTitle.visibility = View.GONE
            didYouKnowDescription.visibility = View.GONE
            didYouKnowReferences.visibility = View.GONE

            etDidYouKnowTitle.visibility = View.VISIBLE
            etDidYouKnowTitle.text = Editable.Factory.getInstance().newEditable(didYouKnow.text)

            etDidYouKnowDescription.visibility = View.VISIBLE
            etDidYouKnowDescription.text = Editable.Factory.getInstance().newEditable(didYouKnow.text)

            etDidYouKnowReferences.visibility = View.VISIBLE
            etDidYouKnowReferences.text = Editable.Factory.getInstance().newEditable(didYouKnow.references)

            btnDidYouKnowSubmit.visibility = View.VISIBLE
            btnDidYouKnowDelete.visibility = View.VISIBLE

            btnDidYouKnowSubmit.setOnClickListener {
                saveDidYouKnow()
            }

            btnDidYouKnowDelete.setOnClickListener {
                deleteDidYouKnow()
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

    private fun saveDidYouKnow(){
        if(editMode){
            saveDidYouKnowBD()
            toggleEditMode()
        }

    }

    private fun saveDidYouKnowBD(){
        if(validateFields()){
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            didYouKnow.title = etDidYouKnowTitle.text.toString()
            didYouKnow.text = etDidYouKnowDescription.text.toString()
            didYouKnow.references = etDidYouKnowReferences.text.toString()

            val didYouKnowRequest = DidYouKnowRequest(
                id = didYouKnow.id!!,
                title = didYouKnow.title!!,
                text = didYouKnow.text!!,
                references = didYouKnow.references!!,
                createdAt = didYouKnow.createdAt.toString(),
                updatedAt = now.format(formatter),

            )

            var doEventRequest = false
            doEventRequest = true
            if (doEventRequest) {
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.editDidYouKnow(didYouKnowRequest).execute()
                    },
                    onSuccess = { isEditted ->
                        setLoadingVisibility(false)

                        val navController = findNavController()
                        navController.navigate(R.id.navigation_sabias)

                        if (toast != null) {
                            toast!!.setText("Sabias Que editado com sucesso")
                        } else {
                            toast = Toast.makeText(requireActivity(), "Sabias Que editado com sucesso", Toast.LENGTH_SHORT)
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

    private fun deleteDidYouKnow(){
        var doEventRequest = false
        doEventRequest = true
        if (doEventRequest) {
            setLoadingVisibility(true)
            makeRequestWithRetries(
                requestCall = {
                    println("Deleting did you know with id=" + didYouKnow.id)
                    RetrofitClient.apiService.deleteDidYouKnow(didYouKnow.id!!).execute()
                },
                onSuccess = { isEditted ->
                    setLoadingVisibility(false)

                    val navController = findNavController()
                    navController.navigate(R.id.navigation_sabias)

                    if (toast != null) {
                        toast!!.setText("Sabias Que removido com sucesso")
                    } else {
                        toast = Toast.makeText(requireActivity(), "Sabias Que com sucesso", Toast.LENGTH_SHORT)
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

    companion object {

        @JvmStatic
        fun newInstance(didYouKnow: DidYouKnowResponse) : DidYouKnowDetailsFragmento {
            val args = Bundle()

            args.putParcelable("myDidYouKow", didYouKnow)

            val fragment = DidYouKnowDetailsFragmento()
            fragment.arguments = args
            return fragment
        }

    }
}