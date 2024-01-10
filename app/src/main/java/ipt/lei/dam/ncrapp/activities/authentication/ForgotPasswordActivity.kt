package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.password.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class ForgotPasswordActivity : BaseActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //Carregar views de design para variáveis
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val backToLogin = findViewById<Button>(R.id.btnBackToLogin)
        val enviarEmail = findViewById<Button>(R.id.btnResetPassword)

        //Se existir uma intent de outra atividade, carrega o email dentro da caixa de texto
        val userEmailFromLogin = intent.getStringExtra(getString(R.string.userInsertedEmail))
        if(userEmailFromLogin!=null)
            emailEditText.setText(userEmailFromLogin.toString())

        //Implementação de um listener para voltar para trás ao clicar no botão de voltar
        backToLogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
            if((email.isNotEmpty()) && super.isEmailValid(email))
                intent.putExtra(getString(R.string.userInsertedEmail), email)
            startActivity(intent)
            finish()
        }

        //Implementação de um listener para enviar o email
        enviarEmail.setOnClickListener {
            var doRecoverRequest = true

            val email = emailEditText.text.toString()

            //Validações necessárias
            if(email.isEmpty()){
                emailEditText.error = getString(R.string.loginEmailBoxNotFilledError)
                doRecoverRequest = false
            }
            if(!super.isEmailValid(email) && doRecoverRequest){
                emailEditText.error = getString(R.string.loginEmailBoxInvalid)
                doRecoverRequest = false
            }

            if (doRecoverRequest) {
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.recoverPassword(RecoverPasswordRequest(email)).execute()
                    },
                    onSuccess = { _ ->
                        val intent = Intent(this@ForgotPasswordActivity, InsertOTPActivity::class.java)
                        if((email.isNotEmpty()) && super.isEmailValid(email))
                            intent.putExtra(getString(R.string.userInsertedEmail), email)
                        intent.putExtra(getString(R.string.type), "1")
                        startActivity(intent)
                        finish()
                        setLoadingVisibility(false)
                    },
                    onError = { errorMessage ->
                        // Tratamento de erro
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(this@ForgotPasswordActivity, errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}