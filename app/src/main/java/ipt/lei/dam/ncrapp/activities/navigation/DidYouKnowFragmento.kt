package ipt.lei.dam.ncrapp.activities.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BasicFragment
import ipt.lei.dam.ncrapp.activities.DidYouKnowAdapter
import ipt.lei.dam.ncrapp.models.DidYouKnowResponse
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.models.GetEventsRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient


class sabiasQueFragmento : BasicFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DidYouKnowAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        var myListDidYouKnow: List<DidYouKnowResponse>? = null
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
        val view =  inflater.inflate(R.layout.did_you_know_fragmento, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewDidYouKnow)
        recyclerView.layoutManager = LinearLayoutManager(context)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutDidYouKnow)

        setupLoadingAnimation(view)

        val navController = findNavController()

        val fabAddDidYouKnow: FloatingActionButton = view.findViewById(R.id.fab_add_didyouknow)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        if(!clientType.equals("ADMINISTRADOR")){
            fabAddDidYouKnow.visibility = View.GONE;
        }else{
            fabAddDidYouKnow.visibility = View.VISIBLE;
            fabAddDidYouKnow.setOnClickListener {
                navController.navigate(R.id.navigation_didyouknow_add)
            }
        }

        try {
            val backButton : ImageView = requireActivity().findViewById(R.id.back_button)

            backButton.visibility = View.INVISIBLE
        } catch (e: Exception) { }

        swipeRefreshLayout.setOnRefreshListener {
            setLoadingVisibility(true)
            getDidYouKnowFromApi (
                onDidYouKnowLoaded = { didYouKnowList ->
                    myListDidYouKnow = didYouKnowList
                    updateRecyclerView(myListDidYouKnow!!)
                },
                onError = { errorMessage ->

                }
            )
        }

        if(null != myListDidYouKnow && !myListDidYouKnow!!.isEmpty()){
            if(needRefresh){
                getDidYouKnowFromApi (
                    onDidYouKnowLoaded = { didYouKnowList ->
                        myListDidYouKnow = didYouKnowList
                        updateRecyclerView(myListDidYouKnow!!)
                        setMyNeedRefresh(false)
                    },
                    onError = { errorMessage ->
                    }
                )
            } else {
                updateRecyclerView(myListDidYouKnow!!)
            }
        } else {
            getDidYouKnowFromApi (
                onDidYouKnowLoaded = { didYouKnowList ->
                    myListDidYouKnow = didYouKnowList
                    updateRecyclerView(myListDidYouKnow!!)
                },
                onError = { errorMessage ->

                }
            )
        }

        return view
    }

    fun getDidYouKnowFromApi(onDidYouKnowLoaded: (List<DidYouKnowResponse>) -> Unit, onError: (String) -> Unit) {
        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.getDidYouKnow().execute()
            },
            onSuccess = { didYouKnowList ->
                onDidYouKnowLoaded(didYouKnowList)
            },
            onError = { errorMessage ->
                onError(errorMessage)
            }
        )
    }

    private fun updateRecyclerView(didYouKnowList: List<DidYouKnowResponse>){
        didYouKnowList.forEach { didYouKnow ->
            println("" + didYouKnow.id + " - " + didYouKnow.title + "")

        }
        setLoadingVisibility(false)
        recyclerView.visibility = View.VISIBLE
        adapter = DidYouKnowAdapter(requireContext(), didYouKnowList).apply {
            onItemClickListener = { didYouKnow ->

                val bundle = Bundle().apply {
                    putParcelable("myDidYouKow", didYouKnow)
                }
                findNavController().navigate(R.id.navigation_didyouknow_details, bundle)
            }
        }
        recyclerView.adapter = adapter

        swipeRefreshLayout.isRefreshing = false
        setLoadingVisibility(false)
    }
}