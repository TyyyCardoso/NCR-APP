package ipt.lei.dam.ncrapp.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.SharedViewModel
import ipt.lei.dam.ncrapp.activities.authentication.LoginActivity
import ipt.lei.dam.ncrapp.databinding.ActivityMainBinding


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        var selectedSortOption: String = "recente"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbarLoginContainerText: TextView
    private lateinit var toolbarLoginImage: ImageView
    private lateinit var sortByImage: ImageView
    private lateinit var userInfo : SharedPreferences
    private lateinit var biometricInfo : SharedPreferences

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ////////////////////////////////////////////////////
        //
        //  Construção da toolbar
        //
        ////////////////////////////////////////////////////
        val toolbar: Toolbar = findViewById(R.id.toolbar_custom)
        val toolbarBackButton: ImageView = findViewById(R.id.back_button)
        val toolbarLoginContainer: LinearLayout = findViewById(R.id.entrar_container)
        toolbarLoginContainerText = findViewById(R.id.containerText)
        toolbarLoginImage = findViewById(R.id.containerImage)
        sortByImage = findViewById(R.id.sort_button)


        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        toolbarBackButton.visibility = GONE

        ////////////////////////////////////////////////////
        //
        //  Tratar views de navegação
        //
        ////////////////////////////////////////////////////
        bottomNavigationView = binding.navView
        drawerLayout = binding.drawerLayout

        //Referencia ao Fragment principal que fica por cima do BottomNavigationView para dar set no controlador
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        bottomNavigationView.setupWithNavController(navController)

        //Obtenção da referência da navegação do drawer menu
        val navigationDrawerView = findViewById<NavigationView>(R.id.nav_side_view)
        navigationDrawerView.setNavigationItemSelectedListener(this)
        //Criado o toggle para o drawer
        val toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        //View selecionada por defeito
        navigationDrawerView.setCheckedItem(R.id.navigation_events)

        ////////////////////////////////////////////////////
        //
        //  Usado o sharedPreferences para controlar diversos aspectos do UI, por exemplo, se o utilizador está logado
        //
        ////////////////////////////////////////////////////

        userInfo = getSharedPreferences(getString(R.string.userInfo), MODE_PRIVATE)
        biometricInfo = getSharedPreferences(getString(R.string.biometricLogin), MODE_PRIVATE)
        var clientType = userInfo.getString(getString(R.string.clientType), getString(R.string.estudante))

        //Alterar o design caso o cliente com o login feito
        if(!clientType.equals(getString(R.string.estudante))){
            //Alterar toolbar para ter o "Sair"
            toolbarLoginContainerText.text = getString(R.string.sair)
            toolbarLoginImage.setImageResource(R.drawable.baseline_logout_24)

            //Mostrar no drawer menu, a opção de "Sair"
            val menu = navigationDrawerView.menu
            val logoutItem = menu.findItem(R.id.navigation_logout)
            logoutItem.isVisible = true

            val isBiometric = biometricInfo.getBoolean(getString(R.string.isUsingBiometric), false)
            val isToRequestBiometric = biometricInfo.getBoolean(getString(R.string.isToRequestBiometric), true)

            if(!isBiometric && isToRequestBiometric){
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialogAlertTitle))
                    .setMessage(getString(R.string.dialogAlertMessage1))
                    .setNeutralButton(getString(R.string.dialogAlertNeutralButton)) { _, _ ->

                        val editor = biometricInfo.edit()
                        editor.putBoolean(getString(R.string.isToRequestBiometric), false)
                        editor.apply()

                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.dialogInfoTitle))
                            .setMessage(getString(R.string.dialogInfoMessage1))
                            .setPositiveButton(getString(R.string.dialogPositiveButton)) { _, _ ->
                            }
                            .show()
                    }
                    .setPositiveButton(getString(R.string.dialogAlertPositiveButton1)) { _, _ ->
                        val editor = biometricInfo.edit()
                        editor.putBoolean(getString(R.string.isUsingBiometric), true)
                        editor.putString(getString(R.string.biometricEmail), userInfo.getString(getString(R.string.clientEmail), ""))
                        editor.putBoolean(getString(R.string.isToRequestBiometric), false)
                        editor.apply()
                    }
                    .show()
            }

        }

        ////////////////////////////////////////////////////
        //
        //  Implementado um listener no bottom navigation para poder navegar e controlar sessão do utilizador
        //
        ////////////////////////////////////////////////////

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    clientType = userInfo.getString(getString(R.string.clientType), getString(R.string.estudante))
                    if(clientType.equals(getString(R.string.estudante))){
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.dialogAlertTitle))
                            .setMessage(getString(R.string.dialogAlertMessage2))
                            .setNeutralButton(getString(R.string.dialogAlertNeutralButton)) { _, _ ->
                            }
                            .setPositiveButton(getString(R.string.dialogAlertPositiveButton2)) { _, _ ->
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                finish()
                            }
                            .show()
                        false
                    }else{
                        //Selecionar o mesmo menu no drawer navigation
                        navigationDrawerView.setCheckedItem(R.id.navigation_profile)
                        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_profile)
                        true
                    }

                }
                R.id.navigation_sabias -> {
                    //Selecionar o mesmo menu no drawer navigation
                    navigationDrawerView.setCheckedItem(R.id.navigation_sabias)
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_sabias)
                    true
                }
                R.id.navigation_events -> {
                    //Selecionar o mesmo menu no drawer navigation
                    navigationDrawerView.setCheckedItem(R.id.navigation_events)
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
                    true
                }
                else -> true
            }

        }

        ////////////////////////////////////////////////////
        //
        //  Implementado um listener na toolbar de forma ser possivel efetuar um logout ou um login quando necessário
        //
        ////////////////////////////////////////////////////

        toolbarLoginContainer.setOnClickListener {
            clientType = userInfo.getString(getString(R.string.clientType), getString(R.string.estudante))
            if(clientType.equals(getString(R.string.estudante))){
                //Se cliente não está logado, é direcionado para a página de login
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }else{
                //Cliente está a fazer logout, é feita a limpeza do UserInfo que até agora dita se o cliente está logadou ou não em diversos sitios
                cleanLogoutSessionAndUpdateUI()
            }

        }

        sortByImage.setOnClickListener {
            val sortOptions = arrayOf("recente", "antigo")
            var checkedItem = sortOptions.indexOf(selectedSortOption)

            if (checkedItem == -1) checkedItem = 0

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.sort_title)
                .setSingleChoiceItems(sortOptions, checkedItem) { _, which ->
                    selectedSortOption = sortOptions[which]
                }
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    onSortOptionSelected(selectedSortOption)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    fun onSortOptionSelected(sortOption: String) {
        sharedViewModel.setSortOption(sortOption)
    }

    /**
     *
     * Método de listener de cliques no menu drawer de navegação
     *
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_profile -> {
                val clientType = userInfo.getString(getString(R.string.clientType), getString(R.string.estudante))
                if(clientType.equals(getString(R.string.estudante))){
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialogAlertTitle))
                        .setMessage(getString(R.string.dialogAlertMessage2))
                        .setNeutralButton(getString(R.string.dialogAlertNeutralButton)) { _, _ ->
                        }
                        .setPositiveButton(getString(R.string.dialogAlertPositiveButton2)) { _, _ ->
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                        .show()
                }else{
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_profile)
                }
            }
            R.id.navigation_sabias -> {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_sabias)
            }
            R.id.navigation_events -> {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
            }
            R.id.navigation_staff -> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_staff)
            }
            R.id.navigation_schedule -> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_schedule)
            }
            R.id.navigation_settings-> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_settings)
            }
            R.id.navigation_info -> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_info)
            }
            R.id.navigation_logout -> {
                cleanLogoutSessionAndUpdateUI()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun clearBottomNavigationSelection() {
        val size = bottomNavigationView.menu.size()
        for (i in 0 until size) {
            bottomNavigationView.menu.getItem(i).isChecked = false
        }
    }

    private fun cleanLogoutSessionAndUpdateUI(){
        userInfo.edit().clear().apply()
        toolbarLoginContainerText.text = getString(R.string.loginButton)
        toolbarLoginImage.setImageResource(R.drawable.baseline_person_24)
        Toast.makeText(this@MainActivity, getString(R.string.toastMessageSuccessLogout), Toast.LENGTH_SHORT).show()
        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
    }

}