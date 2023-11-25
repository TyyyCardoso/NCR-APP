package ipt.lei.dam.ncr.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncr.MainActivity
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.util.logging.Logger

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            Thread {
                try {
                    val call = RetrofitClient.apiService.login(LoginRequest(email, password))
                    val response = call.execute() // Execute the call synchronously on this thread

                    // Switch back to the main thread for UI updates
                    runOnUiThread {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            RetrofitClient.setAuthToken(loginResponse?.token ?: "")
                            println(loginResponse?.token ?: "")
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            // Handle login error
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle exceptions, switch back to the main thread for UI updates
                    runOnUiThread {

                    }
                }
            }.start() // Start the thread
        }
    }
}
