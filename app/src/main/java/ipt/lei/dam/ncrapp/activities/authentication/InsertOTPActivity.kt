package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.RecoverPasswordRequest
import ipt.lei.dam.ncrapp.models.ValidateOTPRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class InsertOTPActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_otp)


        val otpEditText = findViewById<EditText>(R.id.etOTP)
        val btnValidateOTP = findViewById<Button>(R.id.btnValidateOTP);
        val backToRecoverPassword = findViewById<Button>(R.id.btnBackToRecoverPassword);

        val userEmailFromLogin = intent.getStringExtra("userInsertedEmail")

        backToRecoverPassword.setOnClickListener {
            val intent = Intent(this@InsertOTPActivity, ForgotPasswordActivity::class.java)
            intent.putExtra("userInsertedEmail", userEmailFromLogin)
            startActivity(intent)
            finish()
        }

        btnValidateOTP.setOnClickListener {

            var validateOTP = true;
            val otpInserted = otpEditText.text.toString()

            if(otpInserted.isNullOrEmpty() || otpInserted.length!=7){
                otpEditText.error = getString(R.string.loginEmailBoxNotFilledError)
                validateOTP = false;
            }


            if(validateOTP){
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.validateOTP(ValidateOTPRequest(otpInserted, userEmailFromLogin)).execute()
                    },
                    onSuccess = { validateOTPResponse ->
                        val intent = Intent(this@InsertOTPActivity, ChangePasswordActivity::class.java)
                        intent.putExtra("userID", validateOTPResponse.userID)
                        startActivity(intent)
                        finish()
                        setLoadingVisibility(false)
                    },
                    onError = { errorMessage ->
                        // Tratamento de erro
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(this@InsertOTPActivity, errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }

        }
    }
}