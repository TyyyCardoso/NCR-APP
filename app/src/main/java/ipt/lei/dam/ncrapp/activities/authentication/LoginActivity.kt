package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.activities.MainActivity
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class LoginActivity : BaseActivity() {

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
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
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
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.login(LoginRequest(email, password)).execute()
                    },
                    onSuccess = { loginResponse ->
                        // Aqui, loginResponse é diretamente o corpo da resposta e não o objeto Response
                        RetrofitClient.setAuthToken(loginResponse.token ?: "")
                        println(loginResponse.token ?: "")
                        if(loginResponse.isValidated){
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                            setLoadingVisibility(false)
                        }else{
                            val intent = Intent(this@LoginActivity, InsertOTPActivity::class.java)
                            intent.putExtra("userInsertedEmail", loginResponse.email)
                            intent.putExtra("type", "2")
                            startActivity(intent)
                            finish()
                            setLoadingVisibility(false)
                        }

                    },
                    onError = { errorMessage ->
                        // Tratamento de erro
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }
        }
    }

}
