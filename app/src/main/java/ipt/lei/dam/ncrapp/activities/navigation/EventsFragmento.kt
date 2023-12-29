package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.activities.EventsAdapter
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.models.GetEventsRequest
import ipt.lei.dam.ncrapp.models.SubscribeEventRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class EventsFragmento : BasicFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    companion object {
        var myListEvents: List<EventResponse>? = null
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
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        println("LOADING EVENTS FRAGMENTO.................")
        
        setupLoadingAnimation(view)

        val navController = findNavController()

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_event)

        try {
            val backButton : ImageView = requireActivity().findViewById(R.id.back_button)

            backButton.visibility = View.INVISIBLE
        } catch (e: Exception) { }



        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        if(!clientType.equals("admin")){
            //fab.visibility = GONE;
            fab.setOnClickListener {
                navController.navigate(R.id.navigation_events_add)
            }
        }else{
            fab.setOnClickListener {
                navController.navigate(R.id.navigation_events_add)
            }
        }

        // Configure o listener de atualização
        swipeRefreshLayout.setOnRefreshListener {
            getEventsFromApi(
                onEventsLoaded = { eventList ->
                    myListEvents = eventList
                    updateRecyclerView(myListEvents!!)
                },
                onError = { errorMessage ->

                }
            )
        }

        if(null != myListEvents && !myListEvents!!.isEmpty()){
            if(needRefresh){
                getEventsFromApi(
                    onEventsLoaded = { eventList ->
                        myListEvents = eventList
                        updateRecyclerView(myListEvents!!)
                        setMyNeedRefresh(false)
                    },
                    onError = { errorMessage ->
                    }
                )
            } else {
                updateRecyclerView(myListEvents!!)
            }

        } else {
            getEventsFromApi(
                onEventsLoaded = { eventList ->

                    myListEvents = eventList
                    updateRecyclerView(myListEvents!!)
                },
                onError = { errorMessage ->

                }
            )

        }

        return view

    }

    fun getEventsFromApi(onEventsLoaded: (List<EventResponse>) -> Unit, onError: (String) -> Unit) {
        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "")

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.getEvents(GetEventsRequest(clientEmail)).execute()
            },
            onSuccess = { eventList ->
                onEventsLoaded(eventList)
            },
            onError = { errorMessage ->
                onError(errorMessage)
            }
        )
    }

    private fun updateRecyclerView(eventList: List<EventResponse>) {
        // Esta função é chamada quando a lista de eventos está pronta para uso
        eventList.forEach { event ->
            println("${event.id} - ${event.name} - ${event.createdAt}")
        }
        setLoadingVisibility(false)
        recyclerView.visibility = View.VISIBLE

        // Atualize o RecyclerView com os novos dados
        adapter = EventsAdapter(requireContext(), eventList).apply {
            onItemClickListener = { event ->
                val bundle = Bundle().apply {
                    putParcelable("myEvent", event)
                }
                findNavController().navigate(R.id.navigation_events_details, bundle)
            }
            onItemClickSubscribeListener = { event, position ->
                if (event.subscribed == true) {
                    cancelarInscricao(event.id)
                    event.subscribed = false
                    adapter.notifyItemChanged(position)
                } else {
                    inscreverEvento(event.id)
                    event.subscribed = true
                    adapter.notifyItemChanged(position)
                }
            }
        }
        println("SETTING ADAPTER")
        recyclerView.adapter = adapter

        // Termine a animação de atualização
        swipeRefreshLayout.isRefreshing = false
    }

    fun inscreverEvento(id : Int?){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "");

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.subscribeEvent(SubscribeEventRequest(id, clientEmail)).execute()
            },
            onSuccess = { EventResponseList ->
                Toast.makeText(requireContext(), "Adesão ao evento realizada com sucesso!", Toast.LENGTH_SHORT).show()

            },
            onError = { errorMessage ->
                println(errorMessage)
                setLoadingVisibility(false)
            }
        )
    }

    fun cancelarInscricao(id : Int?){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "");

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.cancelarInscricao(SubscribeEventRequest(id, clientEmail)).execute()
            },
            onSuccess = { EventResponseList ->
                Toast.makeText(requireContext(), "Cancelamento realizado com sucesso.", Toast.LENGTH_SHORT).show()

            },
            onError = { errorMessage ->
                println(errorMessage)
                setLoadingVisibility(false)
            }
        )
    }
}