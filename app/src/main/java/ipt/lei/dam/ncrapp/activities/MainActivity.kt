package ipt.lei.dam.ncrapp.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.authentication.LoginActivity
import ipt.lei.dam.ncrapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Referencia ao BottomNavigationView
        val navView: BottomNavigationView = binding.navView




        //Referencia ao Fragment principal que fica por cima do BottomNavigationView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //Referencia aos destinos (id de menu item que é == ao id usado em mobile_navegation)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_events, R.id.navigation_sabias, R.id.navigation_profile
            )
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val sharedPref = getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        var clientType = sharedPref.getString("clientType", "student");

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    clientType = sharedPref.getString("clientType", "student");
                    if(clientType.equals("student")){
                        AlertDialog.Builder(this)
                            .setTitle("Aviso")
                            .setMessage("Tem que fazer login para poder aceder ao seu perfil.")
                            .setPositiveButton("OK") { dialog, which ->
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
                else -> true
            }

        }

        val toolbar: Toolbar = findViewById(R.id.toolbar_custom)
        val toolbarBackButton: ImageView = findViewById(R.id.back_button)
        val toolbarLoginContainer: LinearLayout = findViewById(R.id.entrar_container)
        val toolbarLoginContainerText: TextView = findViewById(R.id.containerText)
        val toolbarLoginImage: ImageView = findViewById(R.id.containerImage)

        setSupportActionBar(toolbar)
        // Get the ActionBar here and set the title to null
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)  // Hide the default title
        toolbarBackButton.visibility = GONE;

        if(!clientType.equals("student")){
            toolbarLoginContainerText.text = "Sair"
            toolbarLoginImage.setImageResource(R.drawable.baseline_logout_24) // Use o nome do resource drawable
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
}