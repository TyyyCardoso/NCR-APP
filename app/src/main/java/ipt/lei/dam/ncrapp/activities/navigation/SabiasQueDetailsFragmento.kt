package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.models.DidYouKnowResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class SabiasQueDetailsFragmento : BasicFragment() {
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
        val view =  inflater.inflate(R.layout.fragment_sabias_que_details_fragmento, container, false)

        setupLoadingAnimation(view)

        val fabEditDidYouKnow = view.findViewById<FloatingActionButton>(R.id.fabEditDidYouKnow)
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.VISIBLE
        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        if(!clientType.equals("admin")){
            fabEditDidYouKnow.visibility = View.GONE;
        }else{
            fabEditDidYouKnow.setOnClickListener {
                toggleEditMode()
            }
        }

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

        //Componentes DELETE
        btnDidYouKnowDelete = view.findViewById(R.id.btnDeleteDidYouKnow)

        didYouKnowTitle.text = didYouKnow?.title
        didYouKnowDescription.text = didYouKnow?.text
        didYouKnowReferences.text = didYouKnow?.references

        return view
    }

    private fun toggleEditMode() {
        editMode = !editMode
        if (editMode) {

        } else {

        }
    }

    companion object {

        @JvmStatic
        fun newInstance(didYouKnow: DidYouKnowResponse) : SabiasQueDetailsFragmento {
            val args = Bundle()

            args.putParcelable("myDidYouKow", didYouKnow)

            val fragment = SabiasQueDetailsFragmento()
            fragment.arguments = args
            return fragment
        }

    }
}