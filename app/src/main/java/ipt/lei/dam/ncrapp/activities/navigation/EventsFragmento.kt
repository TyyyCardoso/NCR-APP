package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.activities.EventsAdapter
import ipt.lei.dam.ncrapp.network.RetrofitClient

class EventsFragmento : BasicFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        setupLoadingAnimation(view)

        val navController = findNavController()

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_event)
        fab.setOnClickListener {
            navController.navigate(R.id.navigation_events_add)
        }

        var doEventRequest = false
            doEventRequest = true
            if (doEventRequest) {
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.getEvents().execute()
                    },
                    onSuccess = { EventResponseList ->
                        EventResponseList.forEach { event ->
                            println("" + event.id + " - " + event.name + " - " + event.createdAt)

                        }
                        setLoadingVisibility(false)
                        recyclerView.visibility = View.VISIBLE
                        adapter = EventsAdapter(EventResponseList).apply {
                            onItemClickListener = { event ->

                                val bundle = Bundle().apply {
                                    putParcelable("myEvent", event)
                                }
                                navController.navigate(R.id.navigation_events_details, bundle)
                            }
                        }
                        recyclerView.adapter = adapter
                    },
                    onError = { errorMessage ->
                        println(errorMessage)
                        setLoadingVisibility(false)
                    }
                )
            }

        return view
    }
}