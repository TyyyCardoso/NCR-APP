package ipt.lei.dam.ncrapp.fragments.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.threetenabp.AndroidThreeTen
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.SharedViewModel
import ipt.lei.dam.ncrapp.adapters.EventsAdapter
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.models.events.GetEventsRequest
import ipt.lei.dam.ncrapp.models.events.SubscribeEventRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date

class EventsFragmento : BasicFragment() {
    private lateinit var selectedSortOption: String
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

    }

    companion object {
        var myListEvents: List<EventResponse>? = null
        var needRefresh: Boolean = false
        fun setMyNeedRefresh(state : Boolean){
            needRefresh = state
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSortOption()
    }

    private fun observeSortOption() {
        sharedViewModel.sortOption.observe(viewLifecycleOwner) { sortOption ->
            selectedSortOption = sortOption
            updateRecyclerView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        selectedSortOption = getString(R.string.recent)
        /**
         * REFERENCES TO UI
         */
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_event)
        setupLoadingAnimation(view)

        try {
            val backButton : ImageView = requireActivity().findViewById(R.id.back_button)

            backButton.visibility = View.INVISIBLE
        } catch (e: Exception) { }

        /**
         * Obter info do user
         */
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString(getString(R.string.clientType), getString(R.string.member))
        val editor = sharedPref.edit()
        editor.putString(getString(R.string.orderBy), selectedSortOption)
        editor.apply()

        /**
         * ClickListeners
         */
        if(!clientType.equals(getString(R.string.admin))){
            fab.visibility = View.GONE
        }else{
            fab.setOnClickListener {
                findNavController().navigate(R.id.navigation_events_add)
            }
        }

        //Swipe down refresh
        swipeRefreshLayout.setOnRefreshListener {
            getEventsFromApi(
                onEventsLoaded = { eventList ->
                    myListEvents = eventList
                    updateRecyclerView()
                },
                onError = {

                }
            )
        }

        /**
         * Carregar eventos para a UI
         */
        //Se os eventos já foram descarregados da API
        if(null != myListEvents && myListEvents!!.isNotEmpty()){
            //Mas existem ordem de voltar a atualizar (adiciona, editado ou removido algum evento)
            //Volta a fazer um pedido de getEvents
            if(needRefresh){
                getEventsFromApi(
                    onEventsLoaded = { eventList ->
                        myListEvents = eventList
                        updateRecyclerView()
                        setMyNeedRefresh(false)
                    },
                    onError = {
                    }
                )
            } else {
                //Se nao e necessário atualizar, simplesmente constroi o recycler view com os eventos já guardados em local
                updateRecyclerView()
            }
        //Se nao existem eventos local -> getEvents
        } else {
            getEventsFromApi(
                onEventsLoaded = { eventList ->

                    myListEvents = eventList
                    updateRecyclerView()
                },
                onError = {

                }
            )

        }

        return view

    }

    /**
     *
     * Função de obter todos os eventos
     *
     */
    private fun getEventsFromApi(onEventsLoaded: (List<EventResponse>) -> Unit, onError: (String) -> Unit) {
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString(getString(R.string.clientEmail), "")

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

    /**
     *
     * Função para atualizar recyclerView com a lista de eventos
     * Recebe um parâmetro:
     *  eventList -> lista de todos os eventos
     *
     */
    private fun updateRecyclerView() {
        setLoadingVisibility(true)

        if(selectedSortOption == getString(R.string.recent)){
            myListEvents = myListEvents!!.sortedByDescending { it.initDate }
        } else if (selectedSortOption == getString(R.string.old)){
            myListEvents = myListEvents!!.sortedBy { it.initDate }
        }

        // Definiçao do adapter com a lista de eventos
        adapter = EventsAdapter(requireContext(), myListEvents!!).apply {
            // Definiçao do clickListener de abrir detalhes
            onItemClickListener = { event ->
                val bundle = Bundle().apply {
                    putParcelable(getString(R.string.myEvent), event)
                }
                findNavController().navigate(R.id.navigation_events_details, bundle)
            }

            // Definiçao do clickListener de subscrever
            onItemClickSubscribeListener = { event, position ->
                val eventInitDate = event.initDate.toString().substring(0,10)
                val eventEndDate = event.endDate.toString().substring(0,10)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val initDate : Date =  dateFormat.parse(eventInitDate)
                val endDate : Date =  dateFormat.parse(eventEndDate)
                val today = Date()

                if(endDate.equals(null)){
                    if(initDate.before(today)) {
                        if (event.subscribed == true) {
                            cancelarInscricao(event, position)
                        } else {
                            inscreverEvento(event, position)
                        }
                    }else{
                        Toast.makeText(requireContext(), getString(R.string.eventNotAvailable), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    if(today.before(initDate) || (today.after(initDate) && today.before(endDate))){
                        if (event.subscribed == true) {
                            cancelarInscricao(event, position)
                        } else {
                            inscreverEvento(event, position)
                        }
                    }else{
                        Toast.makeText(requireContext(), getString(R.string.eventNotAvailable), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        recyclerView.adapter = adapter

        swipeRefreshLayout.isRefreshing = false
        setLoadingVisibility(false)
        recyclerView.visibility = View.VISIBLE
    }

    /**
     *
     * Função de criar inscrição de um utilizador a um evento
     * Recebe um parâmetro:
     *  event -> Evento a ser inscrito
     *
     */
    private fun inscreverEvento(event : EventResponse?, pos : Int){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString(getString(R.string.clientEmail), "")

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.subscribeEvent(SubscribeEventRequest(event!!.id, clientEmail)).execute()
            },
            onSuccess = {
                Toast.makeText(requireContext(), getString(R.string.eventSusbcribed), Toast.LENGTH_SHORT).show()
                event!!.subscribed = true
                adapter.notifyItemChanged(pos)
            },
            onError = { errorMessage ->
                println(errorMessage)
                setLoadingVisibility(false)
            }
        )
    }

    /**
     *
     * Função de cancelar inscrição de um utilizador a um evento
     * Recebe um parâmetro:
     *  event -> Evento a retirar inscrição
     *
     */
    private fun cancelarInscricao(event : EventResponse?, pos: Int){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString(getString(R.string.clientEmail), "")

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.cancelarInscricao(SubscribeEventRequest(event!!.id, clientEmail)).execute()
            },
            onSuccess = {
                Toast.makeText(requireContext(), getString(R.string.eventSusbcriptionCancel), Toast.LENGTH_SHORT).show()
                event!!.subscribed = false
                adapter.notifyItemChanged(pos)
            },
            onError = { errorMessage ->
                println(errorMessage)
                setLoadingVisibility(false)
            }
        )
    }
}