package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Switch
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment


class InfoFragmento : BasicFragment() {

    private lateinit var switchToggle: Switch


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_info_fragmento, container, false)
        setupLoadingAnimation(view)

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.GONE


        val toggleRadioGroup = view.findViewById<RadioGroup>(R.id.toggle)
        val nucleoInfoLayout = view.findViewById<LinearLayout>(R.id.nucleoInfo)
        val professorInfoLayout = view.findViewById<LinearLayout>(R.id.professorInfo)

        toggleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.ncr -> {
                    nucleoInfoLayout.visibility = View.VISIBLE
                    professorInfoLayout.visibility = View.GONE
                }
                R.id.prof -> {
                    nucleoInfoLayout.visibility = View.GONE
                    professorInfoLayout.visibility = View.VISIBLE
                }
            }
        }

        return view
    }

    private fun getSavedSwitchState(): Boolean {
        // Placeholder function to get the saved state
        // In a real application, you would retrieve this value from shared preferences or a database
        return true // or false
    }

    companion object {
    }
}