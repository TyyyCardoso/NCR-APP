package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_sign_up)

        val etSignUpName = findViewById<EditText>(R.id.etSignUpName);
        val etSignUpEmail = findViewById<EditText>(R.id.etSignUpEmail);
        val etSignUpPassword = findViewById<EditText>(R.id.etSignUpPassword);
        val etSignUpPasswordConfirm = findViewById<EditText>(R.id.etSignUpPasswordConfirm);

        val btnSignUp = findViewById<Button>(R.id.btnSignUp);
        val btnBackToLogin = findViewById<TextView>(R.id.btnBackToLogin);

        btnBackToLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                etSignUpPassword.error = getString(R.string.generalNotFilledField)
                doSignUp = false;
            }

            if(etSignUpPasswordConfirmText.isNullOrEmpty()){
                etSignUpPassword.error = getString(R.string.generalNotFilledField)
                doSignUp = false;
            }

            if(etSignUpNameText.length<2 && doSignUp){
                etSignUpName.error = getString(R.string.signUpNameBoxMinimumLength)
                doSignUp = false;
            }

            if(isEmailValid(etSignUpEmailText) && doSignUp){
                etSignUpEmail.error = getString(R.string.signUpEmailBoxInvalid)
                doSignUp = false;
            }

            if(etSignUpPasswordText.length<8 && doSignUp){
                etSignUpPassword.error = getString(R.string.signUpPasswordBoxMinimumLengthError)
                doSignUp = false;
            }

            if(!etSignUpPasswordText.equals(etSignUpPasswordConfirmText) && doSignUp){
                etSignUpPasswordConfirm.error = getString(R.string.signUpNotEqualError)
                doSignUp = false;
            }

            if(doSignUp){


            }
        }
    }
}