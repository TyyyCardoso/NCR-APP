package ipt.lei.dam.ncrapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.authentication.LoginActivity
import ipt.lei.dam.ncrapp.databinding.ActivityMainBinding


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbarLoginContainerText: TextView
    private lateinit var toolbarLoginImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Construção da toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_custom)
        val toolbarBackButton: ImageView = findViewById(R.id.back_button)
        val toolbarLoginContainer: LinearLayout = findViewById(R.id.entrar_container)
        toolbarLoginContainerText = findViewById(R.id.containerText)
        toolbarLoginImage = findViewById(R.id.containerImage)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        toolbarBackButton.visibility = GONE;

        //Inicialização do objeto de cache
        val sharedPref = getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        //Obter tipo de cliente da cache
        var clientType = sharedPref.getString("clientType", "student");

        //Obter views de navegação (Side and bottom navigation)
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

        //Alterar o design caso o client esteja logado
        if(!clientType.equals("student")){
            toolbarLoginContainerText.text = "Sair"
            toolbarLoginImage.setImageResource(R.drawable.baseline_logout_24)
            val menu = navigationDrawerView.menu
            val logoutItem = menu.findItem(R.id.navigation_logout)
            logoutItem.isVisible = true

            val sharedPreferencesBiometric = getSharedPreferences("BiometricLogin", Context.MODE_PRIVATE)
            val isBiometric = sharedPreferencesBiometric.getBoolean("isBiometric", false)
            val isToRequestBiometric = sharedPreferencesBiometric.getBoolean("isToRequestBiometric", true)

            if(!isBiometric && isToRequestBiometric){
                AlertDialog.Builder(this)
                    .setTitle("Alerta")
                    .setMessage("Quer ativar o login por impressão digital?")
                    .setNeutralButton("Mais tarde") { dialog, which ->
                        val editor = sharedPreferencesBiometric.edit()
                        editor.putBoolean("isToRequestBiometric", false)
                        editor.apply()
                        AlertDialog.Builder(this)
                            .setTitle("Informação")
                            .setMessage("Quando pretender usar impressão digital diriga-se às definições para ativar.")
                            .setPositiveButton("Ok") { dialog, which ->
                            }
                            .show()
                    }
                    .setPositiveButton("Ativar") { dialog, which ->
                        val sharedPreferencesUserInfo = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
                        val editor = sharedPreferencesBiometric.edit()
                        editor.putBoolean("isUsingBiometric", true)
                        editor.putString("biometricEmail", sharedPreferencesUserInfo.getString("clientEmail", ""))
                        editor.putBoolean("isToRequestBiometric", false)
                        editor.apply()
                    }
                    .show()
            }

        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    navigationDrawerView.setCheckedItem(R.id.navigation_profile)
                    clientType = sharedPref.getString("clientType", "student");
                    if(clientType.equals("student")){
                        AlertDialog.Builder(this)
                            .setTitle("Aviso")
                            .setMessage("Tem que fazer login para poder aceder ao seu perfil.")
                            .setNeutralButton("Mais tarde") { dialog, which ->
                            }
                            .setPositiveButton(" Fazer Login") { dialog, which ->
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                finish()
                            }
                            .show()
                        false
                    }else{
                        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_profile)
                        true
                    }

                }
                R.id.navigation_sabias -> {
                    navigationDrawerView.setCheckedItem(R.id.navigation_sabias)
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_sabias)
                    true
                }
                R.id.navigation_events -> {
                    navigationDrawerView.setCheckedItem(R.id.navigation_events)
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
                    true
                }
                else -> true
            }

        }

        toolbarLoginContainer.setOnClickListener {
            clientType = sharedPref.getString("clientType", "student");
            if(clientType.equals("student")){
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }else{
                // To clear all SharedPreferences data
                val sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                toolbarLoginContainerText.text = "Entrar"
                toolbarLoginImage.setImageResource(R.drawable.baseline_person_24) // Use o nome do resource drawab
                Toast.makeText(this@MainActivity, "Logout efetuado com sucesso.", Toast.LENGTH_SHORT).show()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
            }

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val sharedPref = getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        when(item.itemId) {
            R.id.navigation_profile -> {
                var clientType = sharedPref.getString("clientType", "student");
                if(clientType.equals("student")){
                    AlertDialog.Builder(this)
                        .setTitle("Aviso")
                        .setMessage("Tem que fazer login para poder aceder ao seu perfil.")
                        .setNeutralButton("Mais tarde") { dialog, which ->
                        }
                        .setPositiveButton("Fazer Login") { dialog, which ->
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                        .show()
                    false
                }else{
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_profile)
                    true
                }
            }
            R.id.navigation_sabias -> {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_sabias)
                true
            }
            R.id.navigation_events -> {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
                true
            }
            R.id.navigation_staff -> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_staff)
                true
            }
            R.id.navigation_schedule -> {
                Toast.makeText(this@MainActivity, "Brevemente...", Toast.LENGTH_SHORT).show()
                /*false
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_schedule)*/

            }
            R.id.navigation_settings-> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_settings)
                true
            }
            R.id.navigation_info -> {
                clearBottomNavigationSelection()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_info)
                true
            }
            R.id.navigation_logout -> {
                val sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                toolbarLoginContainerText.text = "Entrar"
                toolbarLoginImage.setImageResource(R.drawable.baseline_person_24) // Use o nome do resource drawab
                Toast.makeText(this@MainActivity, "Logout efetuado com sucesso.", Toast.LENGTH_SHORT).show()
                item.isVisible = false
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_events)
                true
            }
            else -> true
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

}