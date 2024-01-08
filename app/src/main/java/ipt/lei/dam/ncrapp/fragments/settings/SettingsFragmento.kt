package ipt.lei.dam.ncrapp.fragments.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.authentication.LoginActivity

class SettingsFragmento : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_fragmento, container, false)

        val checkboxEditEventTransport = view.findViewById<CheckBox>(R.id.checkboxEditEventTransport)

        val sharedPreferencesBiometric = requireContext().getSharedPreferences(getString(R.string.biometricLogin), Context.MODE_PRIVATE)
        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.userInfo), Context.MODE_PRIVATE)
        val isBiometricLogin = sharedPreferencesBiometric.getBoolean(getString(R.string.isUsingBiometric), false)
        val clientType = sharedPreferences.getString(getString(R.string.clientType), getString(R.string.estudante))

        if(!clientType.equals(getString(R.string.estudante))) {
            checkboxEditEventTransport.isChecked = isBiometricLogin
        }else{
            checkboxEditEventTransport.isChecked = false
        }

        checkboxEditEventTransport.setOnCheckedChangeListener { _, isChecked ->

            if(!clientType.equals(getString(R.string.estudante))) {
                val editor = sharedPreferencesBiometric.edit()

                if (isChecked) {
                    editor.putBoolean(getString(R.string.isUsingBiometric), true)
                    editor.putString(
                        getString(R.string.biometricEmail),
                        sharedPreferences.getString(getString(R.string.clientEmail), "")
                    )
                } else {
                    editor.putBoolean(getString(R.string.isUsingBiometric), false)
                    editor.putString(getString(R.string.biometricEmail), "")
                }
                editor.apply()
            }else{
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.dialogAlertTitle))
                    .setMessage(getString(R.string.dialogAlertMessage2))
                    .setNeutralButton(getString(R.string.dialogAlertNeutralButton)) { _, _ ->
                    }
                    .setPositiveButton(getString(R.string.dialogAlertPositiveButton2)) { _, _ ->
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    }
                    .show()

                checkboxEditEventTransport.isChecked = false
            }


        }

        // Inflate the layout for this fragment
        return view
    }

}