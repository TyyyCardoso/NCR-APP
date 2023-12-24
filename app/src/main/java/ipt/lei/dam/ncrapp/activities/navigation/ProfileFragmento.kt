package ipt.lei.dam.ncrapp.activities.navigation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [settingsFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragmento : BasicFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val fab: FloatingActionButton = view.findViewById(R.id.fabEditProfile)

        val profileImage = view.findViewById<ImageView>(R.id.imageProfile)
        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail =  view.findViewById<TextView>(R.id.profileEmail)
        val profileTipoCliente =  view.findViewById<TextView>(R.id.profileTipoCliente)
        val profileValidated =  view.findViewById<TextView>(R.id.profileValidado)
        val profileDataRegisto =  view.findViewById<TextView>(R.id.profileDataRegisto)

        val editProfileImageButtons = view.findViewById<LinearLayout>(R.id.eventImageEditLayout)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientName = sharedPref.getString("clientName", "Erro a obter o seu nome.");
        val clientEmail = sharedPref.getString("clientEmail", "Erro a obter o seu email.");
        val clientType = sharedPref.getString("clientType", "member");
        val clientValidated = sharedPref.getBoolean("clientValidated", true);
        val clientDataRegisto = sharedPref.getString("clientRegistrationDate", "Erro ao obter a sua data de registo");
        val clientImage = sharedPref.getString("clientImage", "");

        profileName.text = clientName
        profileEmail.text = clientEmail
        profileTipoCliente.text = clientType.toString().uppercase()
        profileValidated.text = if (clientValidated) "Sim" else "Não"
        profileDataRegisto.text = clientDataRegisto
        if(clientImage.toString().isNotEmpty()){
            // Decodificar base64 para um array de bytes
            val decodedString: ByteArray = Base64.decode(clientImage, Base64.DEFAULT)

            // Converter array de bytes em Bitmap
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            // Definir o Bitmap no ImageView
            profileImage.setImageBitmap(decodedByte)
        }

        fab.setOnClickListener {
            println("Teste")
            editProfileImageButtons.visibility = View.VISIBLE

        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragmento().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}