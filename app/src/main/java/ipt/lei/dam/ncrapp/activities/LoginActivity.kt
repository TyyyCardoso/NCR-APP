package ipt.lei.dam.ncr.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import ipt.lei.dam.ncr.MainActivity
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.activities.CreateAccountActivity
import ipt.lei.dam.ncrapp.activities.ForgotPasswordActivity
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import org.json.JSONObject
import java.io.IOException

class LoginActivity : BaseActivity() {

    //Criado toast singleton para evitar multiplas ativações
    private var toast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Obter componentes do ecrã
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val forgotPasswordLabel = findViewById<TextView>(R.id.forgot_password)
        val registerLabel = findViewById<TextView>(R.id.register_prompt)

        val userEmailFromLogin = intent.getStringExtra("userInsertedEmail")
        if(userEmailFromLogin!=null)
            emailEditText.setText(userEmailFromLogin.toString())

        forgotPasswordLabel.setOnClickListener {
            val email = emailEditText.text.toString()
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            if((email.isNotEmpty()) && super.isEmailValid(email))
                intent.putExtra("userInsertedEmail", email)
            startActivity(intent)
            finish()
        }

        registerLabel.setOnClickListener {
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            //Variável de controlo
            var doLoginRequest = true;

            //Textos
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if(email.isEmpty()){
                emailEditText.error = getString(R.string.loginEmailBoxNotFilledError)
                doLoginRequest = false;
            }
            if(!super.isEmailValid(email) && doLoginRequest){
                emailEditText.error = getString(R.string.loginEmailBoxInvalid)
                doLoginRequest = false;
            }
            if(password.isEmpty()){
                passwordEditText.error = getString(R.string.loginPasswordBoxHintNotFilledError)
                doLoginRequest = false;
            }

            if (doLoginRequest) {
                setLoadingVisibility(true)
                Thread {
                    var attemptCount = 0
                    val maxAttempts = 3
                    val retryDelayMillis = 2000L // Delay of 2 seconds between attempts

                    while (attemptCount < maxAttempts) {
                        try {
                            val call = RetrofitClient.apiService.login(LoginRequest(email, password))
                            val response = call.execute()

                            runOnUiThread {
                                if (response.isSuccessful) {
                                    val loginResponse = response.body()
                                    RetrofitClient.setAuthToken(loginResponse?.token ?: "")
                                    println(loginResponse?.token ?: "")
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                    setLoadingVisibility(false)
                                    return@runOnUiThread
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    val jsonObject = JSONObject(errorBody)
                                    val errorMessage = jsonObject.getString("message")

                                    if (toast != null) {
                                        toast!!.setText(errorMessage)
                                    } else {
                                        toast = Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT)
                                    }
                                    toast!!.show()
                                    setLoadingVisibility(false)
                                }
                            }
                            break // Break out of the loop if successful
                        } catch (e: IOException) {
                            e.printStackTrace()
                            attemptCount++
                            if (attemptCount >= maxAttempts) {
                                runOnUiThread {
                                    setLoadingVisibility(false)
                                    toast = Toast.makeText(this@LoginActivity, "Erro ao tentar conectar. Por favor, Tente novamente.", Toast.LENGTH_SHORT)
                                    toast!!.show()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                setLoadingVisibility(false)
                                toast = Toast.makeText(this@LoginActivity, "Erro ao tentar conectar. Por favor, Tente novamente.", Toast.LENGTH_SHORT)
                                toast!!.show()
                            }
                            break // Break on non-IOException errors
                        }

                        if (attemptCount < maxAttempts) {
                            Thread.sleep(retryDelayMillis) // Delay before next attempt
                        }
                    }
                }.start()
            }
        }
    }

}
