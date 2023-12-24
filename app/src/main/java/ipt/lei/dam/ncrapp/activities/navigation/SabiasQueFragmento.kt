package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.activities.DidYouKnowAdapter
import ipt.lei.dam.ncrapp.activities.EventsAdapter
import ipt.lei.dam.ncrapp.models.GetEventsRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient


class sabiasQueFragmento : BasicFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DidYouKnowAdapter

            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_sabias_que, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewDidYouKnow)
        recyclerView.layoutManager = LinearLayoutManager(context)

        setupLoadingAnimation(view)

        val navController = findNavController()

        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_didyouknow)

        var doEventRequest = false
        doEventRequest = true
        if (doEventRequest) {
            setLoadingVisibility(true)
            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.getDidYouKnow().execute()
                },
                onSuccess = { getDidYouKnowList ->
                    getDidYouKnowList.forEach { didYouKnow ->
                        println("" + didYouKnow.id + " - " + didYouKnow.title + "")

                    }
                    setLoadingVisibility(false)
                    recyclerView.visibility = View.VISIBLE
                    adapter = DidYouKnowAdapter(requireContext(), getDidYouKnowList).apply {
                        onItemClickListener = { didYouKnow ->

                            val bundle = Bundle().apply {
                                putParcelable("myDidYouKow", didYouKnow)
                            }
                            navController.navigate(R.id.navigation_didyouknow_details, bundle)
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

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            sabiasQueFragmento().apply {
                arguments = Bundle().apply {

                }
            }
    }
}