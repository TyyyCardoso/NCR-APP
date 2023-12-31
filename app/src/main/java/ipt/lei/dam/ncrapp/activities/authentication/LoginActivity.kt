package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.activities.MainActivity
import ipt.lei.dam.ncrapp.models.BiometricLoginRequest
import ipt.lei.dam.ncrapp.models.LoginRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.util.concurrent.Executor


class LoginActivity : BaseActivity() {

    private lateinit var prompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                startActivityForResult(enrollIntent, 2)
            }
        }

        executor = ContextCompat.getMainExecutor(this)
        prompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Ocorreu um erro na leitura da sua impressão digital", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    biometricLogin()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autenticação falhou",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Nucleo de Conservação e Restauro")
            .setSubtitle("Entrar com a sua impressão digital:")
            .setNegativeButtonText("Usar credenciais")
            .build()

        val sharedPref = getSharedPreferences("BiometricLogin", MODE_PRIVATE)
        var isBiometricLogin = sharedPref.getBoolean("isUsingBiometric", false)

        if(isBiometricLogin){
            prompt.authenticate(promptInfo)
        }


        //Obter componentes do ecrã
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val loginButton = findViewById<Button>(R.id.login_button)
        val forgotPasswordLabel = findViewById<TextView>(R.id.forgot_password)
        val registerLabel = findViewById<TextView>(R.id.register_prompt)
        val backButton = findViewById<ImageView>(R.id.backButtonLogin)

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

        backButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // This method is called before the text is changed.
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                passwordInputLayout.isPasswordVisibilityToggleEnabled = true
            }

            override fun afterTextChanged(editable: Editable) {
                // This method is called after the text has been changed.
            }
        })

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
                passwordInputLayout.isPasswordVisibilityToggleEnabled = false
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
                            val sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putString("clientName", loginResponse.name)
                            editor.putString("clientEmail", loginResponse.email)
                            editor.putBoolean("clientValidated", loginResponse.isValidated)
                            editor.putString("clientType", loginResponse.type)
                            editor.putString("clientRegistrationDate", loginResponse.registrationDate)
                            editor.putString("clientImage", loginResponse.image)
                            editor.putString("clientAbout", loginResponse.about)
                            editor.apply()

                            val sharedPrefBiometric = getSharedPreferences("BiometricLogin", MODE_PRIVATE)
                            val biometricEmail = sharedPrefBiometric.getString("biometricEmail", "")

                            if(!loginResponse.email.equals(biometricEmail)){
                                 val editor = sharedPrefBiometric.edit()
                                 editor.putBoolean("isUsingBiometric", false)
                                 editor.putString("biometricEmail", "")
                                 editor.apply()
                             }


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

    private fun biometricLogin(){
        setLoadingVisibility(true)

        val sharedPref = getSharedPreferences("BiometricLogin", MODE_PRIVATE)
        val email = sharedPref.getString("biometricEmail", "")

        makeRequestWithRetries(
            requestCall = {
                RetrofitClient.apiService.biometricLogin(BiometricLoginRequest(email)).execute()
            },
            onSuccess = { loginResponse ->
                // Aqui, loginResponse é diretamente o corpo da resposta e não o objeto Response
                RetrofitClient.setAuthToken(loginResponse.token ?: "")
                println(loginResponse.token ?: "")
                if(loginResponse.isValidated){
                    val sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("clientName", loginResponse.name)
                    editor.putString("clientEmail", loginResponse.email)
                    editor.putBoolean("clientValidated", loginResponse.isValidated)
                    editor.putString("clientType", loginResponse.type)
                    editor.putString("clientRegistrationDate", loginResponse.registrationDate)
                    editor.putString("clientImage", loginResponse.image)
                    editor.putString("clientAbout", loginResponse.about)
                    editor.apply()



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
