package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.EventsAdapter
import ipt.lei.dam.ncrapp.network.RetrofitClient
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
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
    private lateinit var loadingImage: ImageView
    private lateinit var rotationAnimation: Animation


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter

    private fun setupLoadingAnimation(view: View) {
        loadingImage = view.findViewById(R.id.loading_image)
        rotationAnimation = AnimationUtils.loadAnimation(this.context, R.anim.rotate_loading)
    }

    protected fun setLoadingVisibility(visible: Boolean) {
        if (visible) {
            loadingImage.visibility = View.VISIBLE
            loadingImage.startAnimation(rotationAnimation)
        } else {
            loadingImage.visibility = View.GONE
            loadingImage.clearAnimation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    fun <T> makeRequestWithRetries(
        requestCall: () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        maxAttempts: Int = 3
    ) {
        setLoadingVisibility(true)
        Thread {
            var attemptCount = 0
            var successful = false

            while (attemptCount < maxAttempts && !successful) {
                try {
                    val response = requestCall()
                    if (response.isSuccessful) {
                        activity?.runOnUiThread {
                            onSuccess(response.body()!!)
                            setLoadingVisibility(false)
                        }
                        successful = true
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = JSONObject(errorBody).getString("message")
                        activity?.runOnUiThread {
                            onError(errorMessage)
                            setLoadingVisibility(false)
                        }
                        break // Não tentar novamente em caso de erro de resposta HTTP
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    attemptCount++
                    if (attemptCount >= maxAttempts) {
                        activity?.runOnUiThread {
                            onError("Erro ao tentar conectar. Por favor, tente novamente.")
                            setLoadingVisibility(false)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        onError("Erro inesperado. Por favor, tente novamente.")
                        setLoadingVisibility(false)
                    }
                    break
                }
            }
        }.start()
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
                            println(event.name)
                        }
                        setLoadingVisibility(false)
                        recyclerView.visibility = View.VISIBLE
                        adapter = EventsAdapter(EventResponseList).apply {
                            onItemClickListener = { event ->
                                //val intent = Intent(context, EventsFragmento::class.java)
                                //intent.putExtra("event_id", event.eventId)
                                //startActivity(intent)
                                //val detailsFragment = EventDetailFragmento.newInstance(event)
                                //fragmentManager?.beginTransaction()
                                //    ?.replace(R.id.nav_host_fragment_activity_main, detailsFragment)
                                //    ?.addToBackStack(null)
                                //    ?.commit()

                                val bundle = Bundle().apply {
                                    putInt("eventId", event.eventId ?: 0) // Substituir 0 pelo valor padrão desejado para ID
                                    putString("eventName", event.name ?: "Nome não disponível")
                                    putString("eventDescription", event.description ?: "Descrição não disponível")
                                    putString("eventDate", event.date?.toString() ?: "Data não disponível")
                                    putString("eventLocation", event.location ?: "Localização não disponível")
                                    putBoolean("eventTransport", event.transport ?: false) // false como valor padrão
                                    putString("eventCreatedAt", event.createAt?.toString() ?: "Data de criação não disponível")
                                    putString("eventUpdatedAt", event.updatedAt?.toString() ?: "Data de atualização não disponível")
                                    putString("eventImage", event.image ?: "")
                                    // Adicione outros campos do evento
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