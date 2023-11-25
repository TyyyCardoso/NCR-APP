package ipt.lei.dam.ncrapp.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.activities.BaseActivity

class InsertOTPActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_otp)


        val otpEditText = findViewById<EditText>(R.id.etOTP)
        val backToRecoverPassword = findViewById<Button>(R.id.btnBackToRecoverPassword);

        val userEmailFromLogin = intent.getStringExtra("userInsertedEmail")

        backToRecoverPassword.setOnClickListener {
            val intent = Intent(this@InsertOTPActivity, ForgotPasswordActivity::class.java)
            intent.putExtra("userInsertedEmail", userEmailFromLogin)
            startActivity(intent)
            finish()
        }
    }
}