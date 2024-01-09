package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.activities.MainActivity
import ipt.lei.dam.ncrapp.models.login.BiometricLoginRequest
import ipt.lei.dam.ncrapp.models.login.LoginRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.util.concurrent.Executor


class LoginActivity : BaseActivity() {

    private lateinit var prompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor
    private lateinit var userInfo : SharedPreferences
    private lateinit var biometricInfo : SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        executor = ContextCompat.getMainExecutor(this)

        userInfo = getSharedPreferences(getString(R.string.userInfo), MODE_PRIVATE)
        /**
         * Iniciada configuração biométrica se tiver disponivel
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val biometricManager = BiometricManager.from(this)
            configureBiometricLogin(biometricManager)

            biometricInfo = getSharedPreferences(getString(R.string.biometricLogin), MODE_PRIVATE)
            val isBiometricLogin = biometricInfo.getBoolean(getString(R.string.isUsingBiometric), false)
            if(isBiometricLogin){
                prompt.authenticate(promptInfo)
            }
        }


        /**
         * Obter componentes do ecrã
         */
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val loginButton = findViewById<Button>(R.id.login_button)
        val forgotPasswordLabel = findViewById<TextView>(R.id.forgot_password)
        val registerLabel = findViewById<TextView>(R.id.register_prompt)
        val backButton = findViewById<ImageView>(R.id.backButtonLogin)
        val keepLogin = findViewById<CheckBox>(R.id.checkbox_keep_login)

        //Obter o email do utilizador se este tiver vindo de uma operação de restaurar password
        val userEmailFromLogin = intent.getStringExtra(getString(R.string.userInsertedEmail))
        if(userEmailFromLogin!=null)
            emailEditText.setText(userEmailFromLogin.toString())

        //Listener para navegar para o ecrã de recuperar password
        forgotPasswordLabel.setOnClickListener {
            val email = emailEditText.text.toString()
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            if((email.isNotEmpty()) && super.isEmailValid(email))
                intent.putExtra(getString(R.string.userInsertedEmail), email)
            startActivity(intent)
            finish()
        }

        //Listener para navegar para o ecrã de registar
        registerLabel.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        }

        //Listener para navegar para o ecrã principal
        backButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        //É adicionada uma validação de o texto foi alterada na box da password de forma ao cliente poder clicar no icone de "Ver password"
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                passwordInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            }
            override fun afterTextChanged(editable: Editable) {}
        })

        /**
         * Listener para executar o login.
         * Primeiro são feitas as validações dos campos inseridos pelo cliente
         *
         */
        loginButton.setOnClickListener {
            //Variável de controlo
            var doLoginRequest = true

            //Textos
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if(email.isEmpty()){
                emailEditText.error = getString(R.string.loginEmailBoxNotFilledError)
                doLoginRequest = false
            }
            if(!super.isEmailValid(email) && doLoginRequest){
                emailEditText.error = getString(R.string.loginEmailBoxInvalid)
                doLoginRequest = false
            }
            if(password.isEmpty()){
                passwordInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                passwordEditText.error = getString(R.string.loginPasswordBoxHintNotFilledError)
                doLoginRequest = false
            }

            if (doLoginRequest) {
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.login(LoginRequest(email, password)).execute()
                    },
                    onSuccess = { loginResponse ->
                        // Aqui, loginResponse é diretamente o corpo da resposta e não o objeto Response
                        RetrofitClient.setAuthToken(loginResponse.token)
                        println(loginResponse.token)
                        if(loginResponse.isValidated){
                            //Se o login for bem sucedido, informação é guardada no local do telemovel
                            val editor = userInfo.edit()
                            editor.putString(getString(R.string.clientName), loginResponse.name)
                            editor.putString(getString(R.string.clientEmail), loginResponse.email)
                            editor.putBoolean(getString(R.string.clientValidated), true)
                            editor.putString(getString(R.string.clientType), loginResponse.type)
                            editor.putString(getString(R.string.clientRegistrationDate), loginResponse.registrationDate)
                            editor.putString(getString(R.string.clientImage), loginResponse.image)
                            editor.putString(getString(R.string.clientAbout), loginResponse.about)
                            editor.putBoolean(getString(R.string.keepLogin), keepLogin.isChecked)
                            editor.apply()

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val biometricEmail = biometricInfo.getString(getString(R.string.biometricEmail), "")
                                //valida se o cliente tem biometrico ativado
                                if(!loginResponse.email.equals(biometricEmail)){
                                    val biometricEditor = biometricInfo.edit()
                                    biometricEditor.putBoolean(getString(R.string.isUsingBiometric), false)
                                    biometricEditor.putString(getString(R.string.biometricEmail), "")
                                    biometricEditor.apply()
                                }
                            }


                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                            setLoadingVisibility(false)
                        }else{
                            val intent = Intent(this@LoginActivity, InsertOTPActivity::class.java)
                            intent.putExtra(getString(R.string.userInsertedEmail), loginResponse.email)
                            intent.putExtra(getString(R.string.type), "2")
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

    private fun configureBiometricLogin(biometricManager : BiometricManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    Log.d(getString(R.string.appTag), "Aplicação consegue usar biométricos para autenticar")
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Log.e(getString(R.string.appTag), "Dispositivo não suporta biométricos")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Log.e(getString(R.string.appTag), "Erro na obtenção de funcionalidades biométricas")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                    }
                    startActivityForResult(enrollIntent, 2)
                }
            }

            prompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext,
                            getString(R.string.leituraImpressaoDigitalErro), Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        biometricLogin()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, getString(R.string.falhaAuth),
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.app_name))
                .setSubtitle(getString(R.string.biometricPromptMessage))
                .setNegativeButtonText(getString(R.string.useCredential))
                .build()
        }

    }

    private fun biometricLogin(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setLoadingVisibility(true)

            val email = biometricInfo.getString(getString(R.string.biometricEmail), "")

            makeRequestWithRetries(
                requestCall = {
                    RetrofitClient.apiService.biometricLogin(BiometricLoginRequest(email)).execute()
                },
                onSuccess = { loginResponse ->
                    // Aqui, loginResponse é diretamente o corpo da resposta e não o objeto Response
                    RetrofitClient.setAuthToken(loginResponse.token)
                    println(loginResponse.token)
                    if (loginResponse.isValidated) {
                        val editor = userInfo.edit()
                        editor.putString(getString(R.string.clientName), loginResponse.name)
                        editor.putString(getString(R.string.clientEmail), loginResponse.email)
                        editor.putBoolean(getString(R.string.clientValidated), true)
                        editor.putString(getString(R.string.clientType), loginResponse.type)
                        editor.putString(
                            getString(R.string.clientRegistrationDate),
                            loginResponse.registrationDate
                        )
                        editor.putString(getString(R.string.clientImage), loginResponse.image)
                        editor.putString(getString(R.string.clientAbout), loginResponse.about)
                        editor.apply()



                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                        setLoadingVisibility(false)
                    } else {
                        val intent = Intent(this@LoginActivity, InsertOTPActivity::class.java)
                        intent.putExtra(getString(R.string.userInsertedEmail), loginResponse.email)
                        intent.putExtra(getString(R.string.type), "2")
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
