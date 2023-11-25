package ipt.lei.dam.ncrapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ipt.lei.dam.ncr.activities.LoginActivity
import ipt.lei.dam.ncrapp.R

class CreateAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val backToLogin = findViewById<TextView>(R.id.btnBackToLogin);

        backToLogin.setOnClickListener {
            startActivity(Intent(this@CreateAccountActivity, LoginActivity::class.java))
            finish()
        }

    }
}