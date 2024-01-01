package ipt.lei.dam.ncrapp.fragments.events

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
import ipt.lei.dam.ncrapp.adapters.EventsAdapter
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.models.events.GetEventsRequest
import ipt.lei.dam.ncrapp.models.events.SubscribeEventRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date

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
        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        /**
         * ClickListeners
         */
        if(!clientType.equals("ADMINISTRADOR")){
            fab.visibility = View.GONE;
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
                    updateRecyclerView(myListEvents!!)
                },
                onError = { errorMessage ->

                }
            )
        }

        /**
         * Carregar eventos para a UI
         */
        //Se os eventos já foram descarregados da API
        if(null != myListEvents && !myListEvents!!.isEmpty()){
            //Mas existem ordem de voltar a atualizar (adiciona, editado ou removido algum evento)
            //Volta a fazer um pedido de getEvents
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
                //Se nao e necessário atualizar, simplesmente constroi o recycler view com os eventos já guardados em local
                updateRecyclerView(myListEvents!!)
            }
        //Se nao existem eventos local -> getEvents
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

    /**
     *
     * Função de obter todos os eventos
     *
     */
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

    /**
     *
     * Função para atualizar recyclerView com a lista de eventos
     * Recebe um parâmetro:
     *  eventList -> lista de todos os eventos
     *
     */
    private fun updateRecyclerView(eventList: List<EventResponse>) {
        setLoadingVisibility(true)

        // Definiçao do adapter com a lista de eventos
        adapter = EventsAdapter(requireContext(), eventList).apply {
            // Definiçao do clickListener de abrir detalhes
            onItemClickListener = { event ->
                val bundle = Bundle().apply {
                    putParcelable("myEvent", event)
                }
                findNavController().navigate(R.id.navigation_events_details, bundle)
            }

            // Definiçao do clickListener de subscrever
            onItemClickSubscribeListener = { event, position ->
                var eventDate = event.date.toString().substring(0,10)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                var date : Date =  dateFormat.parse(eventDate);
                val today = Date()
                if(!today.after(date)) {
                    if (event.subscribed == true) {
                        cancelarInscricao(event, position)
                    } else {
                        inscreverEvento(event, position)
                    }
                }else{
                    Toast.makeText(requireContext(), "Este evento já expirou", Toast.LENGTH_SHORT).show()
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
    fun inscreverEvento(event : EventResponse?, pos : Int){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "");

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.subscribeEvent(SubscribeEventRequest(event!!.id, clientEmail)).execute()
            },
            onSuccess = { EventResponseList ->
                Toast.makeText(requireContext(), "Adesão ao evento realizada com sucesso!", Toast.LENGTH_SHORT).show()
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
    fun cancelarInscricao(event : EventResponse?, pos: Int){
        setLoadingVisibility(true)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientEmail = sharedPref.getString("clientEmail", "");

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.cancelarInscricao(SubscribeEventRequest(event!!.id, clientEmail)).execute()
            },
            onSuccess = { EventResponseList ->
                Toast.makeText(requireContext(), "Cancelamento realizado com sucesso.", Toast.LENGTH_SHORT).show()
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