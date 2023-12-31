package ipt.lei.dam.ncrapp.activities.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.authentication.LoginActivity

class SettingsFragmento : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_fragmento, container, false)

        val checkboxEditEventTransport = view.findViewById<CheckBox>(R.id.checkboxEditEventTransport)

        val sharedPreferencesBiometric = requireContext().getSharedPreferences("BiometricLogin", Context.MODE_PRIVATE)
        val sharedPreferences = requireContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val isBiometricLogin = sharedPreferencesBiometric.getBoolean("isUsingBiometric", false)
        val clientType = sharedPreferences.getString("clientType", "student");

        if(!clientType.equals("student")) {
            checkboxEditEventTransport.isChecked = isBiometricLogin
        }else{3
            checkboxEditEventTransport.isChecked = false
        }

        checkboxEditEventTransport.setOnCheckedChangeListener { buttonView, isChecked ->

            if(!clientType.equals("student")) {
                val editor = sharedPreferencesBiometric.edit()

                if (isChecked) {
                    editor.putBoolean("isUsingBiometric", true)
                    editor.putString(
                        "biometricEmail",
                        sharedPreferences.getString("clientEmail", "")
                    )
                } else {
                    editor.putBoolean("isUsingBiometric", false)
                    editor.putString("biometricEmail", "")
                }
                editor.apply()
            }else{
                AlertDialog.Builder(requireContext())
                    .setTitle("Aviso")
                    .setMessage("Tem que fazer login para poder aceder ao seu perfil.")
                    .setNeutralButton("Mais tarde") { dialog, which ->
                    }
                    .setPositiveButton(" Fazer Login") { dialog, which ->
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    }
                    .show()
            }


        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
    }
}