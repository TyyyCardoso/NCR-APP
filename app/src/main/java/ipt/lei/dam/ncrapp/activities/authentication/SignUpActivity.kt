package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity
import ipt.lei.dam.ncrapp.models.signup.SignUpRequest
import ipt.lei.dam.ncrapp.network.RetrofitClient

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etSignUpName = findViewById<EditText>(R.id.etSignUpName)
        val etSignUpEmail = findViewById<EditText>(R.id.etSignUpEmail)
        val etSignUpPassword = findViewById<EditText>(R.id.etSignUpPassword)
        val etSignUpPasswordConfirm = findViewById<EditText>(R.id.etSignUpPasswordConfirm)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayoutSignup)
        val passwordConfirmInputLayout = findViewById<TextInputLayout>(R.id.confirmPasswordInputLayout)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        btnBackToLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        etSignUpPassword.addTextChangedListener(object : TextWatcher {
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

        etSignUpPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // This method is called before the text is changed.
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                passwordConfirmInputLayout.isPasswordVisibilityToggleEnabled = true
            }

            override fun afterTextChanged(editable: Editable) {
                // This method is called after the text has been changed.
            }
        })


        btnSignUp.setOnClickListener {
            var doSignUp = true;

            val etSignUpNameText = etSignUpName.text.toString()
            val etSignUpEmailText = etSignUpEmail.text.toString()
            val etSignUpPasswordText = etSignUpPassword.text.toString()
            val etSignUpPasswordConfirmText = etSignUpPasswordConfirm.text.toString()

            if(etSignUpNameText.isNullOrEmpty()){
                etSignUpName.error = getString(R.string.generalNotFilledField)
                doSignUp = false;
            }

            if(etSignUpEmailText.isNullOrEmpty()){
                etSignUpEmail.error = getString(R.string.generalNotFilledField)
                doSignUp = false;
            }

            if(etSignUpPasswordText.isNullOrEmpty()){
                passwordInputLayout.isPasswordVisibilityToggleEnabled = false
                etSignUpPassword.error = getString(R.string.generalNotFilledField)
                doSignUp = false;
            }

            if(etSignUpPasswordConfirmText.isNullOrEmpty()){
                passwordConfirmInputLayout.isPasswordVisibilityToggleEnabled = false
                etSignUpPasswordConfirm.error = getString(R.string.generalNotFilledField)
                doSignUp = false;
            }

            if(etSignUpNameText.length<2 && doSignUp){
                etSignUpName.error = getString(R.string.signUpNameBoxMinimumLength)
                doSignUp = false;
            }

            if(!isEmailValid(etSignUpEmailText) && doSignUp){
                etSignUpEmail.error = getString(R.string.signUpEmailBoxInvalid)
                doSignUp = false;
            }

            if(etSignUpPasswordText.length<8 ){
                passwordInputLayout.isPasswordVisibilityToggleEnabled = false
                etSignUpPassword.error = getString(R.string.signUpPasswordBoxMinimumLengthError)
                doSignUp = false;
            }

            if(!etSignUpPasswordText.equals(etSignUpPasswordConfirmText)){
                passwordConfirmInputLayout.isPasswordVisibilityToggleEnabled = false
                etSignUpPasswordConfirm.error = getString(R.string.signUpNotEqualError)
                doSignUp = false;
            }

            if(doSignUp){
                setLoadingVisibility(true)
                makeRequestWithRetries(
                    requestCall = {
                        RetrofitClient.apiService.signUp(SignUpRequest(etSignUpNameText, etSignUpEmailText, etSignUpPasswordText)).execute()
                    },
                    onSuccess = { signUpResponse ->
                        val intent = Intent(this@SignUpActivity, InsertOTPActivity::class.java)
                        intent.putExtra("userInsertedEmail", signUpResponse.email)
                        intent.putExtra("type", "2")
                        toast = Toast.makeText(this@SignUpActivity, "Conta criada com sucesso.", Toast.LENGTH_SHORT)
                        toast!!.show()
                        startActivity(intent)
                        finish()
                        setLoadingVisibility(false)
                    },
                    onError = { errorMessage ->
                        // Tratamento de erro
                        if (toast != null) {
                            toast!!.setText(errorMessage)
                        } else {
                            toast = Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_SHORT)
                        }
                        toast!!.show()
                        setLoadingVisibility(false)
                    }
                )
            }
        }
    }
}