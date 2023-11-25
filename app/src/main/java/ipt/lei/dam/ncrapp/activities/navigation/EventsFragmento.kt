package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.EventsAdapter
import ipt.lei.dam.ncrapp.models.EventResponse

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventsFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventsFragmento : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter

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
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Substitua isso pela sua lista de eventos
        val eventsList = listOf(
            EventResponse("Lisboa Games Week", "Lisboa Games Week Descrição", "https://www.actigamer.pt/cnt/uploads/I22H011lisboagamesweek.jpg"),
            EventResponse("Web Summit", "Web Summit Descrição", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTTclv-4FHV1CKx5fUBNGQf0K8sU6prmdRvXKJB6Jti8TP8VcYhmMzJZYHGyjXc8S5Fo-c&usqp=CAU"),
            EventResponse("Web Summit2", "Web Summit Descrição", "https://www.actigamer.pt/cnt/uploads/I22H011lisboagamesweek.jpg"),
            EventResponse("Web Summit3", "Web Summit Descrição", "https://www.actigamer.pt/cnt/uploads/I22H011lisboagamesweek.jpg")

            // ...
        )

        adapter = EventsAdapter(eventsList)
        recyclerView.adapter = adapter

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventsFragmento.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventsFragmento().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}