package ipt.lei.dam.ncrapp.fragments.didyouknow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.SharedViewModel
import ipt.lei.dam.ncrapp.fragments.BasicFragment
import ipt.lei.dam.ncrapp.adapters.DidYouKnowAdapter
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient


class sabiasQueFragmento : BasicFragment() {
    private lateinit var selectedSortOption: String
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var isInitialized = false
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSortOption()
        isInitialized = true
    }

    private fun observeSortOption() {
        sharedViewModel.sortOption.observe(viewLifecycleOwner) { sortOption ->
            if (isInitialized) {
                selectedSortOption = sortOption
                updateRecyclerView()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.did_you_know_fragmento, container, false)

        selectedSortOption = getString(R.string.recent)
        /**
         * REFERENCES TO UI
         */
        recyclerView = view.findViewById(R.id.recyclerViewDidYouKnow)
        recyclerView.layoutManager = LinearLayoutManager(context)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutDidYouKnow)
        setupLoadingAnimation(view)
        val fabAddDidYouKnow: FloatingActionButton = view.findViewById(R.id.fab_add_didyouknow)

        try {
            val backButton : ImageView = requireActivity().findViewById(R.id.back_button)

            backButton.visibility = View.INVISIBLE
        } catch (e: Exception) { }

        /**
         * Obter info do user
         */
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString(getString(R.string.clientType), getString(R.string.member))

        /**
         * ClickListeners
         */
        if(!clientType.equals(getString(R.string.admin))){
            fabAddDidYouKnow.visibility = View.GONE
        }else{
            fabAddDidYouKnow.visibility = View.VISIBLE
            fabAddDidYouKnow.setOnClickListener {
                findNavController().navigate(R.id.navigation_didyouknow_add)
            }
        }

        //Swipe down refresh
        swipeRefreshLayout.setOnRefreshListener {
            setLoadingVisibility(true)
            getDidYouKnowFromApi (
                onDidYouKnowLoaded = { didYouKnowList ->
                    myListDidYouKnow = didYouKnowList
                    updateRecyclerView()
                },
                onError = {

                }
            )
        }

        /**
         * Carregar eventos para a UI
         */
        //Se os sabias que já foram descarregados da API
        if(null != myListDidYouKnow && myListDidYouKnow!!.isNotEmpty()){
            //Mas existem ordem de voltar a atualizar (adiciona, editado ou removido algum sabias que)
            //Volta a fazer um pedido de getSabiasQue
            if(needRefresh){
                getDidYouKnowFromApi (
                    onDidYouKnowLoaded = { didYouKnowList ->
                        myListDidYouKnow = didYouKnowList
                        updateRecyclerView()
                        setMyNeedRefresh(false)
                    },
                    onError = {
                    }
                )
            } else {
                //Se nao e necessário atualizar, simplesmente constroi o recycler view com os sabias que guardados em local
                updateRecyclerView()
            }
        //Se nao existem sabias que local -> getSabiasQue
        } else {
            getDidYouKnowFromApi (
                onDidYouKnowLoaded = { didYouKnowList ->
                    myListDidYouKnow = didYouKnowList
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
     * Função de obter todos os sabias que
     *
     */
    private fun getDidYouKnowFromApi(onDidYouKnowLoaded: (List<DidYouKnowResponse>) -> Unit, onError: (String) -> Unit) {
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

    /**
     *
     * Função para atualizar recyclerView com a lista de sabias que
     * Recebe um parâmetro:
     *  didYouKnowList -> lista de todos os sabias que
     *
     */
    private fun updateRecyclerView(){
        setLoadingVisibility(true)

        if(!myListDidYouKnow.isNullOrEmpty()) {
            myListDidYouKnow = if(selectedSortOption == getString(R.string.recent)) {
                myListDidYouKnow!!.sortedByDescending { it.createdAt }
            } else if (selectedSortOption == getString(R.string.old)){
                myListDidYouKnow!!.sortedBy { it.createdAt }
            } else {
                myListDidYouKnow // Mantém a lista como está se não se encaixar nas condições anteriores
            }

            // Definiçao do adapter com a lista
            adapter = DidYouKnowAdapter(myListDidYouKnow!!).apply {
                // Definiçao do clickListener de abrir detalhes
                onItemClickListener = { didYouKnow ->

                    val bundle = Bundle().apply {
                        putParcelable(getString(R.string.myDidYouKow), didYouKnow)
                    }
                    findNavController().navigate(R.id.navigation_didyouknow_details, bundle)
                }
            }
            recyclerView.adapter = adapter

            swipeRefreshLayout.isRefreshing = false
            setLoadingVisibility(false)
            recyclerView.visibility = View.VISIBLE
        }

    }
}