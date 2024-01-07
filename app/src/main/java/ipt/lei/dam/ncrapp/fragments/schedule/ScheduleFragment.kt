package ipt.lei.dam.ncrapp.fragments.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.adapters.DidYouKnowAdapter
import ipt.lei.dam.ncrapp.adapters.EventsAdapter
import ipt.lei.dam.ncrapp.adapters.ScheduleAdapter
import ipt.lei.dam.ncrapp.adapters.StaffSliderAdapter
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.fragments.didyouknow.sabiasQueFragmento
import ipt.lei.dam.ncrapp.fragments.events.EventsFragmento
import ipt.lei.dam.ncrapp.fragments.staff.StaffFragment
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.models.schedule.ScheduleResponse
import ipt.lei.dam.ncrapp.models.staff.StaffMemberResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date

class ScheduleFragment : BasicFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter

    companion object {
        var scheduleList: List<ScheduleResponse> = emptyList()
        var needRefresh: Boolean = false
        fun setMyNeedRefresh(state : Boolean){
            needRefresh = state
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        setupLoadingAnimation(view)

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_button)
        backButton.visibility = View.GONE

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ScheduleAdapter(requireContext(), scheduleList).apply {}
        recyclerView.adapter = adapter
        recyclerView.visibility = View.VISIBLE
        getSchedulesList()
        // Definiçao do adapter com a lista






        return view
    }

    private fun getSchedulesList() {
        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.getSchedules().execute()
            },
            onSuccess = { schedulesListResponse ->
                scheduleList = schedulesListResponse
                adapter.updateData(scheduleList)

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


}