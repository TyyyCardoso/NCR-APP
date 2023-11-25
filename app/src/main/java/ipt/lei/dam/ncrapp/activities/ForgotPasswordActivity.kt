package ipt.lei.dam.ncrapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncr.activities.LoginActivity
import ipt.lei.dam.ncrapp.R

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backToLogin = findViewById<Button>(R.id.btnBackToLogin);

        backToLogin.setOnClickListener {
            startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
            finish()
        }
    }
}