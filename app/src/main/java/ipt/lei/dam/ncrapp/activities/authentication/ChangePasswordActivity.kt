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
import ipt.lei.dam.ncrapp.models.password.ChangePasswordRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class ChangePasswordActivity : BaseActivity(){

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        //Obter o id do utilizador da atividade anterior
        val userID = intent.getIntExtra(getString(R.string.userID), 0)

        //Carregar design em variáveis
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val passwordConfirmEditText = findViewById<EditText>(R.id.etConfirmPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val backToLogin = findViewById<Button>(R.id.btnBackToLogin)

        //Listener para o botão de voltar para trás
        backToLogin.setOnClickListener {
            //Volta para o ecrã de login
            val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Listenar para o botão de resetar a password
        btnResetPassword.setOnClickListener {
            var changePassword = true

            val passwordText = passwordEditText.text.toString()
            val passwordConfirmText = passwordConfirmEditText.text.toString()

            //Faz as validação necessárias para ver se é possível efetuar o reset
            if(passwordText.isEmpty()) {
                passwordEditText.error = getString(R.string.changePasswordBoxNotFilledError)
                changePassword = false
            }

            if(passwordText.length<8){
                passwordEditText.error = getString(R.string.changePasswordBoxMinimumLengthError)
                changePassword = false
            }

            if(passwordConfirmText.isEmpty()) {
                passwordConfirmEditText.error = getString(R.string.changePasswordConfirmBoxNotFilledError)
                changePassword = false
            }

            if(passwordText != passwordConfirmText){
                passwordConfirmEditText.error = getString(R.string.changePasswordNotEqualError)
                changePassword = false
            }

            if(changePassword){
                setLoadingVisibility(true)
                //Método importado do BaseActivity
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.changePassword(ChangePasswordRequest(passwordText, userID)).execute()
                    },
                    onSuccess = { _ ->
                        val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        setLoadingVisibility(false)
                    },
                    onError = { errorMessage ->
                        // Tratamento de erro
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(this@ChangePasswordActivity, errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }





        }


    }
}