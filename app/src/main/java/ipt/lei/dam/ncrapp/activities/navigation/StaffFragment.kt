package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.activities.StaffSliderAdapter
import ipt.lei.dam.ncrapp.models.StaffMemberResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import me.relex.circleindicator.CircleIndicator3


class StaffFragment : BasicFragment() {

    private var staffSlider: ViewPager2? = null
    private var profileContainer: LinearLayout? = null
    private var adapter: StaffSliderAdapter? = null
    private var indicator: CircleIndicator3? = null

    companion object {
        var staffMembers: List<StaffMemberResponse> = emptyList()
        var needRefresh: Boolean = false
        fun setMyNeedRefresh(state : Boolean){
            needRefresh = state
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_staff, container, false)
        setupLoadingAnimation(view)

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.GONE

        staffSlider = view.findViewById(R.id.staffSlider)
        profileContainer = view.findViewById(R.id.profileContainer)
        indicator = view.findViewById(R.id.indicator)

        indicator?.animatePageSelected(2)
        indicator?.visibility = View.GONE
        staffSlider?.visibility = View.GONE
        profileContainer?.visibility = View.GONE

        getAdministrators()

        if (staffSlider != null) {
            adapter = StaffSliderAdapter(requireContext(), staffMembers)
            staffSlider!!.adapter = adapter
        }

        staffSlider?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val selectedMember = staffMembers[position]
                displayProfile(selectedMember)
            }
        })


        return view
    }

    private fun displayProfile(member: StaffMemberResponse) {
        // Getting references to the TextViews
        //val emailTextView = view?.findViewById<TextView>(R.id.profile_email)

        val sobreUsername = view?.findViewById<TextView>(R.id.sobreUsername)
        val descricaoTextView = view?.findViewById<TextView>(R.id.profile_descricao)
        val telefoneTextView = view?.findViewById<TextView>(R.id.profile_telefone)
        val statusTextView = view?.findViewById<TextView>(R.id.profile_estado)
        val dataEntradaTextView = view?.findViewById<TextView>(R.id.profile_dataEntrada)

        sobreUsername?.text = "Sobre "
        sobreUsername?.text = sobreUsername?.text.toString() + member.name?.split(" ")?.get(0) + "..."
        //emailTextView?.text = member.email
        descricaoTextView?.text = member.descricao
        telefoneTextView?.text = "+"+member.codpais + " " + member.telefone
        statusTextView?.text = member.status
        dataEntradaTextView?.text = member.dataEntrada
    }


    private fun getAdministrators() {

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.getStaff().execute()
            },
            onSuccess = { adminsList ->
                staffMembers = adminsList
                updateAdapter()
            },
            onError = { errorMessage ->
                if (toast != null) {
                    toast!!.setText(errorMessage)
                } else {
                    toast = Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                }
                toast!!.show()
            }
        )
    }



    private fun updateAdapter() {
        if (adapter == null) {
            adapter = StaffSliderAdapter(requireContext(), staffMembers)
            staffSlider?.adapter = adapter
        } else {
            adapter?.updateData(staffMembers)
            indicator?.setViewPager(staffSlider)
            indicator?.visibility = View.VISIBLE
            staffSlider?.visibility = View.VISIBLE
            profileContainer?.visibility = View.VISIBLE
        }
    }


}