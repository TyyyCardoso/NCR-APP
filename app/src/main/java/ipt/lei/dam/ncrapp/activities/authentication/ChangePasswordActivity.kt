package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.password.ChangePasswordRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class ChangePasswordActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)


        val userID = intent.getIntExtra("userID", 0)

        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val passwordConfirmEditText = findViewById<EditText>(R.id.etConfirmPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val backToLogin = findViewById<Button>(R.id.btnBackToLogin)

        backToLogin.setOnClickListener {
            val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnResetPassword.setOnClickListener {
            var changePassword = true;

            val passwordText = passwordEditText.text.toString()
            val passwordConfirmText = passwordConfirmEditText.text.toString()

            if(passwordText.isNullOrEmpty()) {
                passwordEditText.error = getString(R.string.changePasswordBoxNotFilledError)
                changePassword = false;
            }

            if(passwordText.length<8){
                passwordEditText.error = getString(R.string.changePasswordBoxMinimumLengthError)
                changePassword = false;
            }

            if(passwordConfirmText.isNullOrEmpty()) {
                passwordConfirmEditText.error = getString(R.string.changePasswordConfirmBoxNotFilledError)
                changePassword = false;
            }

            if(!passwordText.equals(passwordConfirmText)){
                passwordConfirmEditText.error = getString(R.string.changePasswordNotEqualError)
                changePassword = false;
            }

            if(changePassword){
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.changePassword(ChangePasswordRequest(passwordText, userID)).execute()
                    },
                    onSuccess = { changePasswordResponse ->
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