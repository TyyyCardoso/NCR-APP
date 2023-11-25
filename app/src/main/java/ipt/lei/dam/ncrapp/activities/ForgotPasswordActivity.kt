package ipt.lei.dam.ncrapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncr.activities.LoginActivity
import ipt.lei.dam.ncrapp.R

class ForgotPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)


        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val backToLogin = findViewById<Button>(R.id.btnBackToLogin);

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
    }
}