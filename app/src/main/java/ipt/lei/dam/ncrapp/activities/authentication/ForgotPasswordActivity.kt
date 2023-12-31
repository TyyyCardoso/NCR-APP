package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.password.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class ForgotPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)


        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val backToLogin = findViewById<Button>(R.id.btnBackToLogin);
        val enviarEmail = findViewById<Button>(R.id.btnResetPassword);

        val userEmailFromLogin = intent.getStringExtra("userInsertedEmail")
        if(userEmailFromLogin!=null)
            emailEditText.setText(userEmailFromLogin.toString())

        backToLogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
            if((email.isNotEmpty()) && super.isEmailValid(email))
                intent.putExtra("userInsertedEmail", email)
            startActivity(intent)
            finish()
        }

        enviarEmail.setOnClickListener {
            var doRecoverRequest = true;

            val email = emailEditText.text.toString()

            if(email.isEmpty()){
                emailEditText.error = getString(R.string.loginEmailBoxNotFilledError)
                doRecoverRequest = false;
            }
            if(!super.isEmailValid(email) && doRecoverRequest){
                emailEditText.error = getString(R.string.loginEmailBoxInvalid)
                doRecoverRequest = false;
            }

            if (doRecoverRequest) {
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.recoverPassword(RecoverPasswordRequest(email)).execute()
                    },
                    onSuccess = { recoverPasswordResponse ->
                        val intent = Intent(this@ForgotPasswordActivity, InsertOTPActivity::class.java)
                        if((email.isNotEmpty()) && super.isEmailValid(email))
                            intent.putExtra("userInsertedEmail", email)
                        intent.putExtra("type", "1")
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
}